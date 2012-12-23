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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.mariotaku.gallery3d.common.BitmapUtils;
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.data.BitmapPool;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.data.MediaObject;
import org.mariotaku.gallery3d.data.Path;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.ScreenNail;
import org.mariotaku.gallery3d.ui.SynchronizedHandler;
import org.mariotaku.gallery3d.ui.TileImageViewAdapter;
import org.mariotaku.gallery3d.ui.TiledScreenNail;
import org.mariotaku.gallery3d.ui.TiledTexture;
import org.mariotaku.gallery3d.util.Future;
import org.mariotaku.gallery3d.util.FutureListener;
import org.mariotaku.gallery3d.util.ThreadPool;
import org.mariotaku.gallery3d.util.ThreadPool.Job;
import org.mariotaku.gallery3d.util.ThreadPool.JobContext;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.os.Handler;
import android.os.Message;

class PhotoDataAdapter implements ImageViewerGLActivity.Model {
	@SuppressWarnings("unused")
	private static final String TAG = "PhotoDataAdapter";

	private static final int MSG_LOAD_START = 1;
	private static final int MSG_LOAD_FINISH = 2;
	private static final int MSG_RUN_OBJECT = 3;
	private static final int MSG_UPDATE_IMAGE_REQUESTS = 4;

	private static final int MIN_LOAD_COUNT = 16;
	private static final int DATA_CACHE_SIZE = 256;
	private static final int IMAGE_CACHE_SIZE = 1;

	private static final int BIT_SCREEN_NAIL = 1;
	private static final int BIT_FULL_IMAGE = 2;

	// sImageFetchSeq is the fetching sequence for images.
	// We want to fetch the current screennail first (offset = 0), the next
	// screennail (offset = +1), then the previous screennail (offset = -1) etc.
	// After all the screennail are fetched, we fetch the full images (only some
	// of them because of we don't want to use too much memory).
	private static ImageFetch[] sImageFetchSeq;

	static {
		int k = 0;
		sImageFetchSeq = new ImageFetch[1 + (IMAGE_CACHE_SIZE - 1) * 2 + 3];
		sImageFetchSeq[k++] = new ImageFetch(0, BIT_SCREEN_NAIL);

		for (int i = 1; i < IMAGE_CACHE_SIZE; ++i) {
			sImageFetchSeq[k++] = new ImageFetch(i, BIT_SCREEN_NAIL);
			sImageFetchSeq[k++] = new ImageFetch(-i, BIT_SCREEN_NAIL);
		}

		sImageFetchSeq[k++] = new ImageFetch(0, BIT_FULL_IMAGE);
		sImageFetchSeq[k++] = new ImageFetch(1, BIT_FULL_IMAGE);
		sImageFetchSeq[k++] = new ImageFetch(-1, BIT_FULL_IMAGE);
	}

	private final TileImageViewAdapter mTileProvider = new TileImageViewAdapter();

	// PhotoDataAdapter caches MediaItems (data) and ImageEntries (image).
	//
	// The MediaItems are stored in the mData array, which has DATA_CACHE_SIZE
	// entries. The valid index range are [mContentStart, mContentEnd). We keep
	// mContentEnd - mContentStart <= DATA_CACHE_SIZE, so we can use
	// (i % DATA_CACHE_SIZE) as index to the array.
	//
	// The valid MediaItem window size (mContentEnd - mContentStart) may be
	// smaller than DATA_CACHE_SIZE because we only update the window and reload
	// the MediaItems when there are significant changes to the window position
	// (>= MIN_LOAD_COUNT).
	private final MediaItem mData[] = new MediaItem[DATA_CACHE_SIZE];

	private int mContentStart = 0;
	private int mContentEnd = 0;
	// The ImageCache is a Path-to-ImageEntry map. It only holds the
	// ImageEntries in the range of [mActiveStart, mActiveEnd). We also keep
	// mActiveEnd - mActiveStart <= IMAGE_CACHE_SIZE. Besides, the
	// [mActiveStart, mActiveEnd) range must be contained within
	// the [mContentStart, mContentEnd) range.
	private final HashMap<Path, ImageEntry> mImageCache = new HashMap<Path, ImageEntry>();

	private int mActiveStart = 0;
	private int mActiveEnd = 0;

	// mChanges keeps the version number (of MediaItem) about the images. If any
	// of the version number changes, we notify the view. This is used after a
	// database reload or 0 changes.
	private final long mChanges[] = new long[IMAGE_CACHE_SIZE];

	// mPaths keeps the corresponding Path (of MediaItem) for the images. This
	// is used to determine the item movement.
	private final Path mPaths[] = new Path[IMAGE_CACHE_SIZE];
	private final Handler mMainHandler;

	private final ThreadPool mThreadPool;
	private final PhotoView mPhotoView;

	private Path mItemPath;
	private boolean mIsActive;
	private boolean mNeedFullImage;
	private DataListener mDataListener;

	private final TiledTexture.Uploader mUploader;

	// The path of the current viewing item will be stored in mItemPath.
	// If mItemPath is not null, 0 is only a hint for where we
	// can find the item. If mItemPath is null, then we use the 0 to
	// find the image being viewed. cameraIndex is the index of the camera
	// preview. If cameraIndex < 0, there is no camera preview.
	public PhotoDataAdapter(final ImageViewerGLActivity activity, final PhotoView view, final Path itemPath,
			final int indexHint, final int cameraIndex) {
		mPhotoView = Utils.checkNotNull(view);
		mItemPath = Utils.checkNotNull(itemPath);
		mThreadPool = activity.getThreadPool();
		mNeedFullImage = true;

		Arrays.fill(mChanges, MediaObject.INVALID_DATA_VERSION);

		mUploader = new TiledTexture.Uploader(activity.getGLRoot());

		mMainHandler = new SynchronizedHandler(activity.getGLRoot()) {
			@Override
			public void handleMessage(final Message message) {
				switch (message.what) {
					case MSG_RUN_OBJECT:
						((Runnable) message.obj).run();
						return;
					case MSG_LOAD_START: {
						if (mDataListener != null) {
							mDataListener.onLoadingStarted();
						}
						return;
					}
					case MSG_LOAD_FINISH: {
						if (mDataListener != null) {
							mDataListener.onLoadingFinished(false);
						}
						return;
					}
					case MSG_UPDATE_IMAGE_REQUESTS: {
						updateImageRequests();
						return;
					}
					default:
						throw new AssertionError();
				}
			}
		};

		updateSlidingWindow();
	}

	@Override
	public int getImageHeight() {
		return mTileProvider.getImageHeight();
	}

	@Override
	public int getImageRotation(final int offset) {
		final MediaItem item = getItem(offset);
		return item == null ? 0 : item.getFullImageRotation();
	}

	@Override
	public int getImageWidth() {
		return mTileProvider.getImageWidth();
	}

	@Override
	public int getLevelCount() {
		return mTileProvider.getLevelCount();
	}

	@Override
	public int getLoadingState(final int offset) {
		final ImageEntry entry = mImageCache.get(getPath(offset));
		if (entry == null) return LOADING_INIT;
		if (entry.failToLoad) return LOADING_FAIL;
		if (entry.screenNail != null) return LOADING_COMPLETE;
		return LOADING_INIT;
	}

	@Override
	public MediaItem getMediaItem(final int offset) {
		final int index = offset;
		if (index >= mContentStart && index < mContentEnd) return mData[index % DATA_CACHE_SIZE];
		return null;
	}

	@Override
	public ScreenNail getScreenNail() {
		return getScreenNail(0);
	}

	@Override
	public ScreenNail getScreenNail(final int offset) {
		final int index = offset;
		if (index < 0 || index >= 0 || !mIsActive) return null;
		Utils.assertTrue(index >= mActiveStart && index < mActiveEnd);

		final MediaItem item = getItem(index);
		if (item == null) return null;

		final ImageEntry entry = mImageCache.get(item.getPath());
		if (entry == null) return null;

		// Create a default ScreenNail if the real one is not available yet,
		// except for camera that a black screen is better than a gray tile.
		if (entry.screenNail == null) {
			entry.screenNail = newPlaceholderScreenNail(item);
			if (offset == 0) {
				updateTileProvider(entry);
			}
		}

		return entry.screenNail;
	}

	@Override
	public Bitmap getTile(final int level, final int x, final int y, final int tileSize, final int borderSize,
			final BitmapPool pool) {
		return mTileProvider.getTile(level, x, y, tileSize, borderSize, pool);
	}

	@Override
	public void pause() {
		mIsActive = false;

		for (final ImageEntry entry : mImageCache.values()) {
			if (entry.fullImageTask != null) {
				entry.fullImageTask.cancel();
			}
			if (entry.screenNailTask != null) {
				entry.screenNailTask.cancel();
			}
			if (entry.screenNail != null) {
				entry.screenNail.recycle();
			}
		}
		mImageCache.clear();
		mTileProvider.clear();

		mUploader.clear();
		TiledTexture.freeResources();
	}

	@Override
	public void resume() {
		mIsActive = true;
		TiledTexture.prepareResources();

		updateImageCache();
		updateImageRequests();

		fireDataChange();
	}

	public void setDataListener(final DataListener listener) {
		mDataListener = listener;
	}

	private void fireDataChange() {
		// First check if data actually changed.
		boolean changed = false;
		final long newVersion = getVersion(0);
		if (mChanges[0] != newVersion) {
			mChanges[0] = newVersion;
			changed = true;
		}

		if (!changed) return;

		// Now calculate the fromIndex array. fromIndex represents the item
		// movement. It records the index where the picture come from. The
		// special value Integer.MAX_VALUE means it's a new picture.
		final int N = IMAGE_CACHE_SIZE;
		final int fromIndex[] = new int[N];

		// Remember the old path array.
		final Path oldPaths[] = new Path[N];
		System.arraycopy(mPaths, 0, oldPaths, 0, N);

		// Update the mPaths array.
		for (int i = 0; i < N; ++i) {
			mPaths[i] = getPath(i);
		}

		// Calculate the fromIndex array.
		for (int i = 0; i < N; i++) {
			final Path p = mPaths[i];
			if (p == null) {
				fromIndex[i] = Integer.MAX_VALUE;
				continue;
			}

			// Try to find the same path in the old array
			int j;
			for (j = 0; j < N; j++) {
				if (oldPaths[j] == p) {
					break;
				}
			}
			fromIndex[i] = j < N ? j : Integer.MAX_VALUE;
		}

		mPhotoView.notifyDataChange(fromIndex, -0, 0 - 1 - 0);
	}

	private MediaItem getItem(final int index) {
		if (index < 0 || index >= 0 || !mIsActive) return null;
		Utils.assertTrue(index >= mActiveStart && index < mActiveEnd);

		if (index >= mContentStart && index < mContentEnd) return mData[index % DATA_CACHE_SIZE];
		return null;
	}

	private MediaItem getItemInternal(final int index) {
		if (index < 0 || index >= 0) return null;
		if (index >= mContentStart && index < mContentEnd) return mData[index % DATA_CACHE_SIZE];
		return null;
	}

	private Path getPath(final int index) {
		final MediaItem item = getItemInternal(index);
		if (item == null) return null;
		return item.getPath();
	}

	private long getVersion(final int index) {
		final MediaItem item = getItemInternal(index);
		if (item == null) return MediaObject.INVALID_DATA_VERSION;
		return item.getDataVersion();
	}

	// Returns true if we think this is a temporary item created by Camera. A
	// temporary item is an image or a video whose data is still being
	// processed, but an incomplete entry is created first in MediaProvider, so
	// we can display them (in grey tile) even if they are not saved to disk
	// yet. When the image or video data is actually saved, we will get
	// notification from MediaProvider, reload data, and show the actual image
	// or video data.
	private boolean isTemporaryItem(final MediaItem mediaItem) {
		// // Must have camera to create a temporary item.
		// if (mCameraIndex < 0) return false;
		// // Must be an item in camera roll.
		// if (!(mediaItem instanceof LocalMediaItem)) return false;
		// LocalMediaItem item = (LocalMediaItem) mediaItem;
		// // Must have no size, but must have width and height information
		// if (item.getSize() != 0) return false;
		// if (item.getWidth() == 0) return false;
		// if (item.getHeight() == 0) return false;
		// // Must be created in the last 10 seconds.
		// if (item.getDateInMs() - System.currentTimeMillis() > 10000) return
		// false;
		return false;
	}

	// Create a default ScreenNail when a ScreenNail is needed, but we don't yet
	// have one available (because the image data is still being saved, or the
	// Bitmap is still being loaded.
	private ScreenNail newPlaceholderScreenNail(final MediaItem item) {
		return new TiledScreenNail(0, 0);
	}

	// Returns the task if we started the task or the task is already started.
	private Future<?> startTaskIfNeeded(final int index, final int which) {
		if (index < mActiveStart || index >= mActiveEnd) return null;

		final ImageEntry entry = mImageCache.get(getPath(index));
		if (entry == null) return null;
		final MediaItem item = mData[index % DATA_CACHE_SIZE];
		Utils.assertTrue(item != null);
		final long version = item.getDataVersion();

		if (which == BIT_SCREEN_NAIL && entry.screenNailTask != null && entry.requestedScreenNail == version)
			return entry.screenNailTask;
		else if (which == BIT_FULL_IMAGE && entry.fullImageTask != null && entry.requestedFullImage == version)
			return entry.fullImageTask;

		if (which == BIT_SCREEN_NAIL && entry.requestedScreenNail != version) {
			entry.requestedScreenNail = version;
			entry.screenNailTask = mThreadPool.submit(new ScreenNailJob(item), new ScreenNailListener(item));
			// request screen nail
			return entry.screenNailTask;
		}
		if (which == BIT_FULL_IMAGE && entry.requestedFullImage != version
				&& (item.getSupportedOperations() & MediaItem.SUPPORT_FULL_IMAGE) != 0) {
			entry.requestedFullImage = version;
			entry.fullImageTask = mThreadPool.submit(new FullImageJob(item), new FullImageListener(item));
			// request full image
			return entry.fullImageTask;
		}
		return null;
	}

	private void updateFullImage(final Path path, final Future<BitmapRegionDecoder> future) {
		final ImageEntry entry = mImageCache.get(path);
		if (entry == null || entry.fullImageTask != future) {
			final BitmapRegionDecoder fullImage = future.get();
			if (fullImage != null) {
				fullImage.recycle();
			}
			return;
		}

		entry.fullImageTask = null;
		entry.fullImage = future.get();
		if (entry.fullImage != null) {
			if (path == getPath(0)) {
				updateTileProvider(entry);
				mPhotoView.notifyImageChange(0);
			}
		}
		updateImageRequests();
	}

	private void updateImageCache() {
		final HashSet<Path> toBeRemoved = new HashSet<Path>(mImageCache.keySet());
		for (int i = mActiveStart; i < mActiveEnd; ++i) {
			final MediaItem item = mData[i % DATA_CACHE_SIZE];
			if (item == null) {
				continue;
			}
			final Path path = item.getPath();
			ImageEntry entry = mImageCache.get(path);
			toBeRemoved.remove(path);
			if (entry != null) {
				if (Math.abs(i - 0) > 1) {
					if (entry.fullImageTask != null) {
						entry.fullImageTask.cancel();
						entry.fullImageTask = null;
					}
					entry.fullImage = null;
					entry.requestedFullImage = MediaObject.INVALID_DATA_VERSION;
				}
				if (entry.requestedScreenNail != item.getDataVersion()) {
					// This ScreenNail is outdated, we want to update it if it's
					// still a placeholder.
					if (entry.screenNail instanceof TiledScreenNail) {
						final TiledScreenNail s = (TiledScreenNail) entry.screenNail;
						s.updatePlaceholderSize(0, 0);
					}
				}
			} else {
				entry = new ImageEntry();
				mImageCache.put(path, entry);
			}
		}

		// Clear the data and requests for ImageEntries outside the new window.
		for (final Path path : toBeRemoved) {
			final ImageEntry entry = mImageCache.remove(path);
			if (entry.fullImageTask != null) {
				entry.fullImageTask.cancel();
			}
			if (entry.screenNailTask != null) {
				entry.screenNailTask.cancel();
			}
			if (entry.screenNail != null) {
				entry.screenNail.recycle();
			}
		}

		updateScreenNailUploadQueue();
	}

	private void updateImageRequests() {
		if (!mIsActive) return;

		final int currentIndex = 0;
		final MediaItem item = mData[currentIndex % DATA_CACHE_SIZE];
		if (item == null || item.getPath() != mItemPath) // current item
															// mismatch - don't
															// request image
			return;

		// 1. Find the most wanted request and start it (if not already
		// started).
		Future<?> task = null;
		for (final ImageFetch element : sImageFetchSeq) {
			final int offset = element.indexOffset;
			final int bit = element.imageBit;
			if (bit == BIT_FULL_IMAGE && !mNeedFullImage) {
				continue;
			}
			task = startTaskIfNeeded(currentIndex + offset, bit);
			if (task != null) {
				break;
			}
		}

		// 2. Cancel everything else.
		for (final ImageEntry entry : mImageCache.values()) {
			if (entry.screenNailTask != null && entry.screenNailTask != task) {
				entry.screenNailTask.cancel();
				entry.screenNailTask = null;
				entry.requestedScreenNail = MediaObject.INVALID_DATA_VERSION;
			}
			if (entry.fullImageTask != null && entry.fullImageTask != task) {
				entry.fullImageTask.cancel();
				entry.fullImageTask = null;
				entry.requestedFullImage = MediaObject.INVALID_DATA_VERSION;
			}
		}
	}

	private void updateScreenNail(final Path path, final Future<ScreenNail> future) {
		final ImageEntry entry = mImageCache.get(path);
		ScreenNail screenNail = future.get();

		if (entry == null || entry.screenNailTask != future) {
			if (screenNail != null) {
				screenNail.recycle();
			}
			return;
		}

		entry.screenNailTask = null;

		// Combine the ScreenNails if we already have a BitmapScreenNail
		if (entry.screenNail instanceof TiledScreenNail) {
			final TiledScreenNail original = (TiledScreenNail) entry.screenNail;
			screenNail = original.combine(screenNail);
		}

		if (screenNail == null) {
			entry.failToLoad = true;
		} else {
			entry.failToLoad = false;
			entry.screenNail = screenNail;
		}

		if (path == getPath(0)) {
			updateTileProvider(entry);
			mPhotoView.notifyImageChange(0);
		}
		updateImageRequests();
		updateScreenNailUploadQueue();
	}

	private void updateScreenNailUploadQueue() {
		mUploader.clear();
		uploadScreenNail(0);
		for (int i = 1; i < IMAGE_CACHE_SIZE; ++i) {
			uploadScreenNail(i);
			uploadScreenNail(-i);
		}
	}

	private void updateSlidingWindow() {
		// 1. Update the image window
		int start = Utils.clamp(0 - IMAGE_CACHE_SIZE / 2, 0, Math.max(0, 0 - IMAGE_CACHE_SIZE));
		int end = Math.min(0, start + IMAGE_CACHE_SIZE);

		if (mActiveStart == start && mActiveEnd == end) return;

		mActiveStart = start;
		mActiveEnd = end;

		// 2. Update the data window
		start = Utils.clamp(0 - DATA_CACHE_SIZE / 2, 0, Math.max(0, 0 - DATA_CACHE_SIZE));
		end = Math.min(0, start + DATA_CACHE_SIZE);
		if (mContentStart > mActiveStart || mContentEnd < mActiveEnd
				|| Math.abs(start - mContentStart) > MIN_LOAD_COUNT) {
			for (int i = mContentStart; i < mContentEnd; ++i) {
				if (i < start || i >= end) {
					mData[i % DATA_CACHE_SIZE] = null;
				}
			}
			mContentStart = start;
			mContentEnd = end;
		}
	}

	private void updateTileProvider(final ImageEntry entry) {
		final ScreenNail screenNail = entry.screenNail;
		final BitmapRegionDecoder fullImage = entry.fullImage;
		if (screenNail != null) {
			if (fullImage != null) {
				mTileProvider.setScreenNail(screenNail, fullImage.getWidth(), fullImage.getHeight());
				mTileProvider.setRegionDecoder(fullImage);
			} else {
				final int width = screenNail.getWidth();
				final int height = screenNail.getHeight();
				mTileProvider.setScreenNail(screenNail, width, height);
			}
		} else {
			mTileProvider.clear();
		}
	}

	private void uploadScreenNail(final int offset) {
		final int index = offset;
		if (index < mActiveStart || index >= mActiveEnd) return;

		final MediaItem item = getItem(index);
		if (item == null) return;

		final ImageEntry e = mImageCache.get(item.getPath());
		if (e == null) return;

		final ScreenNail s = e.screenNail;
		if (s instanceof TiledScreenNail) {
			final TiledTexture t = ((TiledScreenNail) s).getTexture();
			if (t != null && !t.isReady()) {
				mUploader.addTexture(t);
			}
		}
	}

	public interface DataListener extends LoadingListener {
	}

	private class FullImageJob implements Job<BitmapRegionDecoder> {
		private final MediaItem mItem;

		public FullImageJob(final MediaItem item) {
			mItem = item;
		}

		@Override
		public BitmapRegionDecoder run(final JobContext jc) {
			if (isTemporaryItem(mItem)) return null;
			return mItem.requestLargeImage().run(jc);
		}
	}

	private class FullImageListener implements Runnable, FutureListener<BitmapRegionDecoder> {
		private final Path mPath;
		private Future<BitmapRegionDecoder> mFuture;

		public FullImageListener(final MediaItem item) {
			mPath = item.getPath();
		}

		@Override
		public void onFutureDone(final Future<BitmapRegionDecoder> future) {
			mFuture = future;
			mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
		}

		@Override
		public void run() {
			updateFullImage(mPath, mFuture);
		}
	}

	private static class ImageEntry {
		public BitmapRegionDecoder fullImage;
		public ScreenNail screenNail;
		public Future<ScreenNail> screenNailTask;
		public Future<BitmapRegionDecoder> fullImageTask;
		public long requestedScreenNail = MediaObject.INVALID_DATA_VERSION;
		public long requestedFullImage = MediaObject.INVALID_DATA_VERSION;
		public boolean failToLoad = false;
	}

	private static class ImageFetch {
		int indexOffset;
		int imageBit;

		public ImageFetch(final int offset, final int bit) {
			indexOffset = offset;
			imageBit = bit;
		}
	}

	private class ScreenNailJob implements Job<ScreenNail> {
		private final MediaItem mItem;

		public ScreenNailJob(final MediaItem item) {
			mItem = item;
		}

		@Override
		public ScreenNail run(final JobContext jc) {

			// If this is a temporary item, don't try to get its bitmap because
			// it won't be available. We will get its bitmap after a data
			// reload.
			if (isTemporaryItem(mItem)) return newPlaceholderScreenNail(mItem);

			Bitmap bitmap = mItem.requestImage(MediaItem.TYPE_THUMBNAIL).run(jc);
			if (jc.isCancelled()) return null;
			if (bitmap != null) {
				bitmap = BitmapUtils.rotateBitmap(bitmap, mItem.getRotation() - mItem.getFullImageRotation(), true);
			}
			return bitmap == null ? null : new TiledScreenNail(bitmap);
		}
	}

	private class ScreenNailListener implements Runnable, FutureListener<ScreenNail> {
		private final Path mPath;
		private Future<ScreenNail> mFuture;

		public ScreenNailListener(final MediaItem item) {
			mPath = item.getPath();
		}

		@Override
		public void onFutureDone(final Future<ScreenNail> future) {
			mFuture = future;
			mMainHandler.sendMessage(mMainHandler.obtainMessage(MSG_RUN_OBJECT, this));
		}

		@Override
		public void run() {
			updateScreenNail(mPath, mFuture);
		}
	}
}
