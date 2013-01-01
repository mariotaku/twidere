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

package org.mariotaku.gallery3d.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.mariotaku.gallery3d.app.IGalleryApplication;
import org.mariotaku.gallery3d.common.ApiHelper;
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.util.ThreadPool.CancelListener;
import org.mariotaku.gallery3d.util.ThreadPool.Job;
import org.mariotaku.gallery3d.util.ThreadPool.JobContext;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

// MediaItem represents an image or a video item.
public class MediaItem {

	public static final long INVALID_DATA_VERSION = -1;

	// These are the bits returned from getSupportedOperations():
	public static final int SUPPORT_FULL_IMAGE = 1 << 6;
	public static final int SUPPORT_ACTION = 1 << 15;

	// These are the bits returned from getMediaType():
	public static final int MEDIA_TYPE_UNKNOWN = 1;
	public static final int MEDIA_TYPE_IMAGE = 2;

	public static final String MEDIA_TYPE_IMAGE_STRING = "image";
	public static final String MEDIA_TYPE_ALL_STRING = "all";

	// These are flags for cache() and return values for getCacheFlag():
	public static final int CACHE_FLAG_NO = 0;

	private static long sVersionSerial = 0;

	protected long mDataVersion;

	protected final Path mPath;

	// NOTE: These type numbers are stored in the image cache, so it should not
	// not be changed without resetting the cache.
	public static final int TYPE_THUMBNAIL = 1;

	public static final String MIME_TYPE_JPEG = "image/jpeg";

	private static int sThumbnailTargetSize = 1024;

	private static final BitmapPool sThumbPool = ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY ? new BitmapPool(4)
			: null;

	private static final String TAG = "UriImage";

	private static final int STATE_INIT = 0;

	private static final int STATE_DOWNLOADING = 1;
	private static final int STATE_DOWNLOADED = 2;

	private static final int STATE_ERROR = -1;

	private final Uri mUri;

	private final String mContentType;

	private DownloadCache.Entry mCacheEntry;

	private ParcelFileDescriptor mFileDescriptor;

	private int mState = STATE_INIT;

	private int mRotation;

	private final IGalleryApplication mApplication;

	public MediaItem(final IGalleryApplication application, final Path path, final Uri uri, final String contentType) {
		path.setObject(this);
		mPath = path;
		mDataVersion = nextVersionNumber();
		mUri = uri;
		mApplication = Utils.checkNotNull(application);
		mContentType = contentType;
	}

	public int getCacheFlag() {
		return CACHE_FLAG_NO;
	}

	public Uri getContentUri() {
		return mUri;
	}

	public long getDataVersion() {
		return mDataVersion;
	}

	public String getFilePath() {
		return "";
	}

	// The rotation of the full-resolution image. By default, it returns the
	// value of
	// getRotation().
	public int getFullImageRotation() {
		return getRotation();
	}

	public int getMediaType() {
		return MEDIA_TYPE_IMAGE;
	}

	public String getMimeType() {
		return mContentType;
	}

	public Path getPath() {
		return mPath;
	}

	public int getRotation() {
		return mRotation;
	}

	public int getSupportedOperations() {
		return SUPPORT_FULL_IMAGE;
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
				Utils.closeSilently(mFileDescriptor);
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
					Utils.closeSilently(mFileDescriptor);
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
			try {
				if (MIME_TYPE_JPEG.equalsIgnoreCase(mContentType)) {
					final InputStream is = mApplication.getContentResolver().openInputStream(mUri);
					mRotation = Exif.getOrientation(is);
					Utils.closeSilently(is);
				}
				mFileDescriptor = mApplication.getContentResolver().openFileDescriptor(mUri, "r");
				if (jc.isCancelled()) return STATE_INIT;
				return STATE_DOWNLOADED;
			} catch (final FileNotFoundException e) {
				Log.w(TAG, "fail to open: " + mUri, e);
				return STATE_ERROR;
			}
		} else {
			try {
				final URL url = new URI(mUri.toString()).toURL();
				mCacheEntry = mApplication.getDownloadCache().download(jc, url);
				if (jc.isCancelled()) return STATE_INIT;
				if (mCacheEntry == null) {
					Log.w(TAG, "download failed " + url);
					return STATE_ERROR;
				}
				if (MIME_TYPE_JPEG.equalsIgnoreCase(mContentType)) {
					final InputStream is = new FileInputStream(mCacheEntry.cacheFile);
					mRotation = Exif.getOrientation(is);
					Utils.closeSilently(is);
				}
				mFileDescriptor = ParcelFileDescriptor.open(mCacheEntry.cacheFile, ParcelFileDescriptor.MODE_READ_ONLY);
				return STATE_DOWNLOADED;
			} catch (final Throwable t) {
				Log.w(TAG, "download error", t);
				return STATE_ERROR;
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

	public static synchronized long nextVersionNumber() {
		return ++MediaItem.sVersionSerial;
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
