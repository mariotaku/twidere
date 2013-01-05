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

package org.mariotaku.gallery3d.app;

import org.mariotaku.gallery3d.common.ApiHelper;
import org.mariotaku.gallery3d.common.BitmapUtils;
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.data.BitmapPool;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.ui.BitmapScreenNail;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.ScreenNail;
import org.mariotaku.gallery3d.ui.SynchronizedHandler;
import org.mariotaku.gallery3d.util.Future;
import org.mariotaku.gallery3d.util.FutureListener;
import org.mariotaku.gallery3d.util.ThreadPool;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class PhotoViewAdapter implements PhotoView.Model {
	private static final String TAG = "PhotoViewAdapter";
	protected ScreenNail mScreenNail;
	protected BitmapRegionDecoder mRegionDecoder;
	protected int mImageWidth;
	protected int mImageHeight;
	protected int mLevelCount;

	private static final int SIZE_BACKUP = 1024;
	private static final int MSG_IMAGE_LOAD_FAILED = 3;
	private static final int MSG_IMAGE_LOAD_FINISHED = 2;
	private static final int MSG_IMAGE_LOAD_START = 1;

	private final MediaItem mItem;
	private Future<?> mTask;
	private final Handler mHandler;

	private final PhotoView mPhotoView;
	private final ThreadPool mThreadPool;
	private final ImageViewerGLActivity mActivity;
	private BitmapScreenNail mBitmapScreenNail;

	private final FutureListener<BitmapRegionDecoder> mLargeListener = new FutureListener<BitmapRegionDecoder>() {
		@Override
		public void onFutureDone(final Future<BitmapRegionDecoder> future) {
			final BitmapRegionDecoder decoder = future.get();
			if (decoder == null) {
				mTask = mThreadPool.submit(mItem.requestFallbackImage(), mFallbackListener);
				return;
			}
			final int width = decoder.getWidth();
			final int height = decoder.getHeight();
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = BitmapUtils.computeSampleSize((float) SIZE_BACKUP / Math.max(width, height));
			final Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, width, height), options);
			mHandler.sendMessage(mHandler.obtainMessage(MSG_IMAGE_LOAD_FINISHED, new ImageBundle(decoder, bitmap)));
		}

		@Override
		public void onFutureStart(final Future<BitmapRegionDecoder> future) {
			mHandler.sendMessage(mHandler.obtainMessage(MSG_IMAGE_LOAD_START));
		}
	};

	private final FutureListener<Bitmap> mFallbackListener = new FutureListener<Bitmap>() {
		@Override
		public void onFutureDone(final Future<Bitmap> future) {
			final Bitmap bitmap = future.get();
			if (bitmap == null) {
				mHandler.sendMessage(mHandler.obtainMessage(MSG_IMAGE_LOAD_FAILED));
			}
			mHandler.sendMessage(mHandler.obtainMessage(MSG_IMAGE_LOAD_FINISHED, new ImageBundle(null, bitmap)));
		}

		@Override
		public void onFutureStart(final Future<Bitmap> future) {
			mHandler.sendMessage(mHandler.obtainMessage(MSG_IMAGE_LOAD_START));

		}
	};

	public PhotoViewAdapter(final ImageViewerGLActivity activity, final PhotoView view, final MediaItem item) {
		mItem = item;
		mPhotoView = view;
		mActivity = activity;
		mThreadPool = activity.getThreadPool();
		mHandler = new MyHandler(this);
	}

	@Override
	public int getImageHeight() {
		return mImageHeight;
	}

	@Override
	public int getImageRotation() {
		return mItem.getFullImageRotation();
	}

	@Override
	public int getImageWidth() {
		return mImageWidth;
	}

	@Override
	public int getLevelCount() {
		return mLevelCount;
	}

	@Override
	public MediaItem getMediaItem() {
		return mItem;
	}

	@Override
	public ScreenNail getScreenNail() {
		return mScreenNail;
	}

	// Gets a sub image on a rectangle of the current photo. For example,
	// getTile(1, 50, 50, 100, 3, pool) means to get the region located
	// at (50, 50) with sample level 1 (ie, down sampled by 2^1) and the
	// target tile size (after sampling) 100 with border 3.
	//
	// From this spec, we can infer the actual tile size to be
	// 100 + 3x2 = 106, and the size of the region to be extracted from the
	// photo to be 200 with border 6.
	//
	// As a result, we should decode region (50-6, 50-6, 250+6, 250+6) or
	// (44, 44, 256, 256) from the original photo and down sample it to 106.
	@Override
	public Bitmap getTile(final int level, final int x, final int y, final int tileSize, final int borderSize,
			final BitmapPool pool) {
		if (!ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_REGION_DECODER)
			return getTileWithoutReusingBitmap(level, x, y, tileSize, borderSize);

		final int b = borderSize << level;
		final int t = tileSize << level;

		final Rect wantRegion = new Rect(x - b, y - b, x + t + b, y + t + b);

		boolean needClear;
		BitmapRegionDecoder regionDecoder = null;

		synchronized (this) {
			regionDecoder = mRegionDecoder;
			if (regionDecoder == null) return null;

			// We need to clear a reused bitmap, if wantRegion is not fully
			// within the image.
			needClear = !new Rect(0, 0, mImageWidth, mImageHeight).contains(wantRegion);
		}

		Bitmap bitmap = pool == null ? null : pool.getBitmap();
		if (bitmap != null) {
			if (needClear) {
				bitmap.eraseColor(0);
			}
		} else {
			final int s = tileSize + 2 * borderSize;
			bitmap = Bitmap.createBitmap(s, s, Config.ARGB_8888);
		}

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.ARGB_8888;
		options.inPreferQualityOverSpeed = true;
		options.inSampleSize = 1 << level;
		options.inBitmap = bitmap;

		try {
			// In CropImage, we may call the decodeRegion() concurrently.
			synchronized (regionDecoder) {
				bitmap = regionDecoder.decodeRegion(wantRegion, options);
			}
		} finally {
			if (options.inBitmap != bitmap && options.inBitmap != null) {
				if (pool != null) {
					pool.recycle(options.inBitmap);
				}
				options.inBitmap = null;
			}
		}

		if (bitmap == null) {
			Log.w(TAG, "fail in decoding region");
		}
		return bitmap;
	}

	@Override
	public void pause() {
		final Future<?> task = mTask;
		task.cancel();
		task.waitDone();
		if (task.get() == null) {
			mTask = null;
		}
		if (mBitmapScreenNail != null) {
			mBitmapScreenNail.recycle();
			mBitmapScreenNail = null;
		}
	}

	@Override
	public void resume() {
		if (mTask == null) {
			mTask = mThreadPool.submit(mItem.requestLargeImage(), mLargeListener);
		}
	}

	public synchronized void setRegionDecoder(final BitmapRegionDecoder decoder) {
		mRegionDecoder = decoder;
		if (decoder == null) return;
		mImageWidth = decoder.getWidth();
		mImageHeight = decoder.getHeight();
		mLevelCount = calculateLevelCount();
	}

	private int calculateLevelCount() {
		return Math.max(0, Utils.ceilLog2((float) mImageWidth / mScreenNail.getWidth()));
	}

	private Bitmap getTileWithoutReusingBitmap(final int level, final int x, final int y, final int tileSize,
			final int borderSize) {
		final int b = borderSize << level;
		final int t = tileSize << level;
		final Rect wantRegion = new Rect(x - b, y - b, x + t + b, y + t + b);

		BitmapRegionDecoder regionDecoder;
		Rect overlapRegion;

		synchronized (this) {
			regionDecoder = mRegionDecoder;
			if (regionDecoder == null) return null;
			overlapRegion = new Rect(0, 0, mImageWidth, mImageHeight);
			Utils.assertTrue(overlapRegion.intersect(wantRegion));
		}

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Config.ARGB_8888;
		options.inPreferQualityOverSpeed = true;
		options.inSampleSize = 1 << level;
		Bitmap bitmap = null;

		// In CropImage, we may call the decodeRegion() concurrently.
		synchronized (regionDecoder) {
			bitmap = regionDecoder.decodeRegion(overlapRegion, options);
		}

		if (bitmap == null) {
			Log.w(TAG, "fail in decoding region");
		}

		if (wantRegion.equals(overlapRegion)) return bitmap;

		final int s = tileSize + 2 * borderSize;
		final Bitmap result = Bitmap.createBitmap(s, s, Config.ARGB_8888);
		final Canvas canvas = new Canvas(result);
		canvas.drawBitmap(bitmap, overlapRegion.left - wantRegion.left >> level,
				overlapRegion.top - wantRegion.top >> level, null);
		return result;
	}

	private void onDecodeLargeComplete(final ImageBundle bundle) {
		try {
			if (bundle.decoder != null) {
				setScreenNail(bundle.backupImage, bundle.decoder.getWidth(), bundle.decoder.getHeight());
			} else {
				setScreenNail(bundle.backupImage, bundle.backupImage.getWidth(), bundle.backupImage.getHeight());
			}
			setRegionDecoder(bundle.decoder);
			mPhotoView.notifyImageChange();
		} catch (final Throwable t) {
			Log.w(TAG, "fail to decode large", t);
		}
	}

	private void setScreenNail(final Bitmap bitmap, final int width, final int height) {
		mBitmapScreenNail = new BitmapScreenNail(bitmap);
		setScreenNail(mBitmapScreenNail, width, height);
	}

	// Caller is responsible to recycle the ScreenNail
	private synchronized void setScreenNail(final ScreenNail screenNail, final int width, final int height) {
		Utils.checkNotNull(screenNail);
		mScreenNail = screenNail;
		mImageWidth = width;
		mImageHeight = height;
		mRegionDecoder = null;
		mLevelCount = 0;
	}

	private static class ImageBundle {
		public final BitmapRegionDecoder decoder;
		public final Bitmap backupImage;

		public ImageBundle(final BitmapRegionDecoder decoder, final Bitmap backupImage) {
			this.decoder = decoder;
			this.backupImage = backupImage;
		}
	}

	static class MyHandler extends SynchronizedHandler {

		ImageViewerGLActivity mActivity;
		PhotoViewAdapter mAdapter;

		MyHandler(final PhotoViewAdapter adapter) {
			super(adapter.mActivity.getGLRoot());
			mAdapter = adapter;
			mActivity = adapter.mActivity;
		}

		@Override
		public void handleMessage(final Message message) {
			switch (message.what) {
				case MSG_IMAGE_LOAD_START: {
					mActivity.onLoadStart();
					break;
				}
				case MSG_IMAGE_LOAD_FINISHED: {
					mAdapter.onDecodeLargeComplete((ImageBundle) message.obj);
					mActivity.onLoadFinished();
					break;
				}
				case MSG_IMAGE_LOAD_FAILED: {
					mActivity.onLoadFailed();
					break;
				}
			}
		}
	}

}
