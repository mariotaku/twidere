/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.gallery3d.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mariotaku.gallery3d.util.CachedDownloader.DownloadListener;
import org.mariotaku.gallery3d.util.ThreadPool.CancelListener;
import org.mariotaku.gallery3d.util.ThreadPool.Job;
import org.mariotaku.gallery3d.util.ThreadPool.JobContext;
import org.mariotaku.twidere.util.Exif;
import org.mariotaku.twidere.util.Utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import org.mariotaku.twidere.util.ImageValidator;

// MediaItem represents an image or a video item.
public class MediaItem {

	public static final String MIME_TYPE_JPEG = "image/jpeg";

	private static int sThumbnailTargetSize = 1024;

	private static final BitmapPool sThumbPool = ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY ? new BitmapPool(4)
			: null;

	private static final String TAG = "MediaItem";

	private static final int STATE_INIT = 0;
	private static final int STATE_DOWNLOADING = 1;
	private static final int STATE_DOWNLOADED = 2;
	private static final int STATE_ERROR = -1;

	private final Uri mUri;
	private final ContentResolver mResolver;
	private final CachedDownloader mDownloader;
	private final DownloadListener mListener;

	private String mContentType = "image/*";
	private ParcelFileDescriptor mFileDescriptor;
	private int mState = STATE_INIT;
	private int mRotation;

	private long mContentLength;

	public MediaItem(final Context context, final CachedDownloader downloader, final DownloadListener listener,
			final Uri uri) {
		mUri = uri;
		mResolver = context.getContentResolver();
		mDownloader = downloader;
		mListener = listener;
	}

	public File getCachedFile() {
		if (mUri == null || mUri.getScheme() == null) return null;
		final String scheme = mUri.getScheme();
		final File file;
		if (ContentResolver.SCHEME_FILE.equals(scheme)) {
			file = new File(mUri.getPath());
		} else if (scheme.matches("https?")) {
			file = mDownloader.getCacheFile(mUri.toString());
		} else
			return null;
		if (file.exists()) return file;
		return null;
	}

	public Uri getContentUri() {
		return mUri;
	}

	// The rotation of the full-resolution image. By default, it returns the
	// value of getRotation().
	public int getFullImageRotation() {
		return getRotation();
	}

	public String getMimeType() {
		return mContentType;
	}
	
	public long getContentLength() {
		return mContentLength;
	}

	public int getRotation() {
		return mRotation;
	}

	public Job<Bitmap> requestFallbackImage() {
		return new FallbackDecoderJob();
	}

	public Job<BitmapRegionDecoder> requestLargeImage() {
		return new RegionDecoderJob();
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			if (mFileDescriptor != null) {
				GalleryUtils.closeSilently(mFileDescriptor);
			}
		} finally {
			super.finalize();
		}
	}

	private void openFileOrDownloadTempFile(final JobContext jc) {
		final int state = openOrDownloadInner(jc);
		synchronized (this) {
			mState = state;
			if (mState != STATE_DOWNLOADED) {
				if (mFileDescriptor != null) {
					GalleryUtils.closeSilently(mFileDescriptor);
					mFileDescriptor = null;
				}
			}
			notifyAll();
		}
	}

	private int openOrDownloadInner(final JobContext jc) {
		final String scheme = mUri.getScheme();
		if (ContentResolver.SCHEME_CONTENT.equals(scheme) || ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)
				|| ContentResolver.SCHEME_FILE.equals(scheme)) {
			InputStream is = null;
			try {
				is = mResolver.openInputStream(mUri);
				if (ContentResolver.SCHEME_FILE.equals(scheme)) {
					if (!ImageValidator.checkImageValidity(mUri.getPath())) throw new IOException("Invalid image");
				} else {
					if (!Utils.isValidImage(is)) throw new IOException("Invalid image");
				}
				mContentType = Utils.getImageMimeType(is);
				if (mContentType == null) throw new IOException("Invalid image");
				if (MIME_TYPE_JPEG.equalsIgnoreCase(mContentType)) {
					mRotation = Exif.getOrientation(is);
				}
				mFileDescriptor = mResolver.openFileDescriptor(mUri, "r");
				if (jc.isCancelled()) return STATE_INIT;
				return STATE_DOWNLOADED;
			} catch (final IOException e) {
				Log.w(TAG, "fail to open: " + mUri, e);
				return STATE_ERROR;
			} finally {
				GalleryUtils.closeSilently(is);
			}
		} else {
			InputStream is = null;
			try {
				final String url = mUri.toString();
				// TODO download file
				final File file = mDownloader.downloadOrGetCache(jc, mListener, url);
				if (jc.isCancelled()) return STATE_INIT;
				if (file == null) {
					Log.w(TAG, "download failed " + url);
					return STATE_ERROR;
				} else if (!ImageValidator.checkImageValidity(file)) {
					Log.w(TAG, "invalid image " + file);
					file.delete();
					return STATE_ERROR;
				}
				mContentType = Utils.getImageMimeType(file);
				if (MIME_TYPE_JPEG.equalsIgnoreCase(mContentType)) {
					is = new FileInputStream(file);
					mRotation = Exif.getOrientation(is);
				}
				mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
				return STATE_DOWNLOADED;
			} catch (final Exception e) {
				Log.w(TAG, "download error", e);
				return STATE_ERROR;
			} finally {
				GalleryUtils.closeSilently(is);
			}
		}
	}

	private boolean prepareInputFile(final JobContext jc) {
		jc.setCancelListener(new CancelListener() {
			@Override
			public void onCancel() {
				synchronized (this) {
					notifyAll();
				}
			}
		});

		while (true) {
			synchronized (this) {
				if (jc.isCancelled()) return false;
				if (mState == STATE_INIT) {
					mState = STATE_DOWNLOADING;
					// Then leave the synchronized block and continue.
				} else if (mState == STATE_ERROR)
					return false;
				else if (mState == STATE_DOWNLOADED)
					return true;
				else /* if (mState == STATE_DOWNLOADING) */{
					try {
						wait();
					} catch (final InterruptedException ex) {
						// ignored.
					}
					continue;
				}
			}
			// This is only reached for STATE_INIT->STATE_DOWNLOADING
			openFileOrDownloadTempFile(jc);
		}
	}

	public static BitmapPool getThumbPool() {
		return sThumbPool;
	}

	public static void setThumbnailSizes(final int size, final int microSize) {
		sThumbnailTargetSize = size;
	}

	private class FallbackDecoderJob implements Job<Bitmap> {
		@Override
		public Bitmap run(final JobContext jc) {
			if (!prepareInputFile(jc)) return null;
			final Bitmap bitmap = DecodeUtils.requestDecode(jc, mFileDescriptor.getFileDescriptor(), null,
					sThumbnailTargetSize);
			return bitmap;
		}
	}

	private class RegionDecoderJob implements Job<BitmapRegionDecoder> {
		@Override
		public BitmapRegionDecoder run(final JobContext jc) {
			if (!prepareInputFile(jc)) return null;
			final BitmapRegionDecoder decoder = DecodeUtils.createBitmapRegionDecoder(jc,
					mFileDescriptor.getFileDescriptor(), false);
			return decoder;
		}
	}
}
