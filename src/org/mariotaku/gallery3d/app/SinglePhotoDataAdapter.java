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
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.ui.BitmapScreenNail;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.ScreenNail;
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

public class SinglePhotoDataAdapter extends TileImageViewAdapter implements GalleryActivity.Model {

	private static final String TAG = "SinglePhotoDataAdapter";
	private static final int SIZE_BACKUP = 1024;
	private static final int MSG_UPDATE_IMAGE = 1;

	private final MediaItem mItem;
	private final boolean mHasFullImage;
	private Future<?> mTask;
	private final Handler mHandler;

	private final PhotoView mPhotoView;
	private final ThreadPool mThreadPool;
	private int mLoadingState = LOADING_INIT;
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
			mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_IMAGE, new ImageBundle(decoder, bitmap)));
		}
	};

	private final FutureListener<Bitmap> mThumbListener = new FutureListener<Bitmap>() {
		@Override
		public void onFutureDone(final Future<Bitmap> future) {
			mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_IMAGE, future));
		}
	};

	public SinglePhotoDataAdapter(final GalleryActivity activity, final PhotoView view, final MediaItem item) {
		mItem = Utils.checkNotNull(item);
		mHasFullImage = (item.getSupportedOperations() & MediaItem.SUPPORT_FULL_IMAGE) != 0;
		mPhotoView = Utils.checkNotNull(view);
		mHandler = new SynchronizedHandler(activity.getGLRoot()) {
			@Override
			@SuppressWarnings("unchecked")
			public void handleMessage(final Message message) {
				Utils.assertTrue(message.what == MSG_UPDATE_IMAGE);
				if (mHasFullImage) {
					onDecodeLargeComplete((ImageBundle) message.obj);
				} else {
					onDecodeThumbComplete((Future<Bitmap>) message.obj);
				}
			}
		};
		mThreadPool = activity.getThreadPool();
	}

	@Override
	public int getImageRotation(final int offset) {
		return offset == 0 ? mItem.getFullImageRotation() : 0;
	}

	@Override
	public int getLoadingState(final int offset) {
		return mLoadingState;
	}

	@Override
	public MediaItem getMediaItem(final int offset) {
		return offset == 0 ? mItem : null;
	}

	@Override
	public ScreenNail getScreenNail(final int offset) {
		return offset == 0 ? getScreenNail() : null;
	}

	@Override
	public boolean isEmpty() {
		return false;
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
			if (mHasFullImage) {
				mTask = mThreadPool.submit(mItem.requestLargeImage(), mLargeListener);
			} else {
				mTask = mThreadPool.submit(mItem.requestImage(MediaItem.TYPE_THUMBNAIL), mThumbListener);
			}
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

	private void onDecodeThumbComplete(final Future<Bitmap> future) {
		try {
			final Bitmap backup = future.get();
			if (backup == null) {
				mLoadingState = LOADING_FAIL;
				return;
			} else {
				mLoadingState = LOADING_COMPLETE;
			}
			setScreenNail(backup, backup.getWidth(), backup.getHeight());
			mPhotoView.notifyImageChange(0);
		} catch (final Throwable t) {
			Log.w(TAG, "fail to decode thumb", t);
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
