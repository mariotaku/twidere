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

import org.mariotaku.gallery3d.common.BitmapUtils;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.ui.BitmapScreenNail;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.SynchronizedHandler;
import org.mariotaku.gallery3d.ui.TileImageViewAdapter;
import org.mariotaku.gallery3d.util.Future;
import org.mariotaku.gallery3d.util.FutureListener;
import org.mariotaku.gallery3d.util.ThreadPool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SinglePhotoDataAdapter extends TileImageViewAdapter implements ImageViewerGLActivity.Model {

	private static final String TAG = "SinglePhotoDataAdapter";
	private static final int SIZE_BACKUP = 1024;
	private static final int MSG_IMAGE_LOAD_FINISHED = 2;
	private static final int MSG_IMAGE_LOAD_START = 1;

	private final MediaItem mItem;
	private Future<?> mTask;
	private final Handler mHandler;

	private final PhotoView mPhotoView;
	private final ThreadPool mThreadPool;
	private final ImageViewerGLActivity mActivity;
	private final int mLoadingState = LOADING_INIT;
	private BitmapScreenNail mBitmapScreenNail;

	private final FutureListener<BitmapRegionDecoder> mLargeListener = new FutureListener<BitmapRegionDecoder>() {
		@Override
		public void onFutureDone(final Future<BitmapRegionDecoder> future) {
			final BitmapRegionDecoder decoder = future.get();
			if (decoder == null) return;
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

	public SinglePhotoDataAdapter(final ImageViewerGLActivity activity, final PhotoView view, final MediaItem item) {
		mItem = item;
		mPhotoView = view;
		mActivity = activity;
		mThreadPool = activity.getThreadPool();
		mHandler = new SynchronizedHandler(activity.getGLRoot()) {
			@Override
			public void handleMessage(final Message message) {
				switch (message.what) {
					case MSG_IMAGE_LOAD_START: {
						mActivity.onLoadStart();
						break;
					}
					case MSG_IMAGE_LOAD_FINISHED: {
						onDecodeLargeComplete((ImageBundle) message.obj);
						mActivity.onLoadFinished();
						break;
					}
				}

			}
		};
	}

	@Override
	public int getImageRotation() {
		return mItem.getFullImageRotation();
	}

	@Override
	public int getLoadingState() {
		return mLoadingState;
	}

	@Override
	public MediaItem getMediaItem() {
		return mItem;
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

	private void onDecodeLargeComplete(final ImageBundle bundle) {
		try {
			setScreenNail(bundle.backupImage, bundle.decoder.getWidth(), bundle.decoder.getHeight());
			setRegionDecoder(bundle.decoder);
			mPhotoView.notifyImageChange(0);
		} catch (final Throwable t) {
			Log.w(TAG, "fail to decode large", t);
		}
	}

	private void setScreenNail(final Bitmap bitmap, final int width, final int height) {
		mBitmapScreenNail = new BitmapScreenNail(bitmap);
		setScreenNail(mBitmapScreenNail, width, height);
	}

	private static class ImageBundle {
		public final BitmapRegionDecoder decoder;
		public final Bitmap backupImage;

		public ImageBundle(final BitmapRegionDecoder decoder, final Bitmap backupImage) {
			this.decoder = decoder;
			this.backupImage = backupImage;
		}
	}
}
