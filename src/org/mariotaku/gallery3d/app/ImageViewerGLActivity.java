/*
 * Copyright (C) 2009 The Android Open Source Project
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

import org.mariotaku.gallery3d.data.BitmapPool;
import org.mariotaku.gallery3d.data.DataManager;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.data.Path;
import org.mariotaku.gallery3d.ui.GLRoot;
import org.mariotaku.gallery3d.ui.GLRootView;
import org.mariotaku.gallery3d.ui.GLView;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.SynchronizedHandler;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.gallery3d.util.ThreadPool;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

public final class ImageViewerGLActivity extends FragmentActivity implements Constants, View.OnClickListener,
		GalleryContext, PhotoView.Listener {

	private static final String TAG = "Gallery";

	private GLRootView mGLRootView;

	private View mProgress;
	private View mControlButtons;

	protected static final int FLAG_HIDE_ACTION_BAR = 1;
	protected static final int FLAG_HIDE_STATUS_BAR = 2;

	protected Bundle mData;
	protected int mFlags;

	private GLView mContentPane;

	private static final int MSG_HIDE_BARS = 1;

	private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;

	private static final int MSG_UPDATE_ACTION_BAR = 5;

	private static final int MSG_UNFREEZE_GLROOT = 6;

	private static final int MSG_WANT_BARS = 7;

	private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;

	private static final int MSG_ON_CAMERA_CENTER = 9;

	private static final int MSG_ON_PICTURE_CENTER = 10;

	private static final int MSG_REFRESH_IMAGE = 11;

	private static final int MSG_UPDATE_PHOTO_UI = 12;

	private static final int MSG_UPDATE_DEFERRED = 14;

	private static final int MSG_UPDATE_SHARE_URI = 15;

	private static final int HIDE_BARS_TIMEOUT = 3500;

	private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

	public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";

	private PhotoView mPhotoView;
	private PhotoView.Model mModel;
	private Handler mHandler;
	private boolean mShowBars = true;
	private volatile boolean mActionBarAllowed = true;

	private boolean mIsMenuVisible;
	private MediaItem mCurrentPhoto = null;

	private final long mDeferUpdateUntil = Long.MAX_VALUE;
	private final GLView mRootPane = new GLView() {
		@Override
		protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
			mPhotoView.layout(0, 0, right - left, bottom - top);
		}
	};

	@Override
	public Context getAndroidContext() {
		return this;
	}

	@Override
	public DataManager getDataManager() {
		return ((IGalleryApplication) getApplication()).getDataManager();
	}

	public GLRoot getGLRoot() {
		return mGLRootView;
	}

	@Override
	public ThreadPool getThreadPool() {
		return ((IGalleryApplication) getApplication()).getThreadPool();
	}

	public void hideControls() {
		mControlButtons.setVisibility(View.GONE);
	}

	public void hideProgress() {
		mProgress.setVisibility(View.GONE);
	}

	@Override
	public void onActionBarAllowed(final boolean allowed) {
		mActionBarAllowed = allowed;
		mHandler.sendEmptyMessage(MSG_UPDATE_ACTION_BAR);
	}

	@Override
	public void onActionBarWanted() {
		mHandler.sendEmptyMessage(MSG_WANT_BARS);
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.close: {
				finish();
				break;
			}
			case R.id.open_in_browser: {
				final Uri uri = getIntent().getData();
				if (uri == null) return;
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
				break;
			}
			case R.id.share: {
				final Uri uri = getIntent().getData();
				if (uri == null) {
					break;
				}
				final Intent intent = new Intent(Intent.ACTION_SEND);
				if ("file".equals(uri.getScheme())) {
					intent.setType("image/*");
					intent.putExtra(Intent.EXTRA_STREAM, uri);
				} else {
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
				}
				startActivity(Intent.createChooser(intent, getString(R.string.share)));
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
		mProgress = findViewById(R.id.progress);
		mControlButtons = findViewById(R.id.control_buttons);
	}

	@Override
	public void onCurrentImageUpdated() {
		getGLRoot().unfreeze();
	}

	public void onLoadFailed() {
		// TODO Auto-generated method stub
	}

	public void onLoadFinished() {
		hideProgress();
	}

	public void onLoadStart() {
		showProgress();
	}

	@Override
	public void onPictureCenter() {
		mPhotoView.setWantPictureCenterCallbacks(false);
		mHandler.removeMessages(MSG_ON_CAMERA_CENTER);
		mHandler.removeMessages(MSG_ON_PICTURE_CENTER);
		mHandler.sendEmptyMessage(MSG_ON_PICTURE_CENTER);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Callbacks from PhotoView
	// //////////////////////////////////////////////////////////////////////////
	@Override
	public void onSingleTapUp(final int x, final int y) {
		if (mModel.getMediaItem() == null) return;
		toggleBars();
	}

	public void showControls() {
		mControlButtons.setVisibility(View.VISIBLE);
	}

	public void showProgress() {
		mProgress.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.image_viewer_gl);

		if (savedInstanceState == null) {
			startViewAction(getIntent());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGLRootView.lockRenderThread();
		try {
			getGLRoot().setOrientationSource(null);

			// Remove all pending messages.
			mHandler.removeCallbacksAndMessages(null);
		} finally {
			mGLRootView.unlockRenderThread();
		}
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		startViewAction(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLRootView.onPause();
		mGLRootView.lockRenderThread();
		try {
			getGLRoot().unfreeze();
			mHandler.removeMessages(MSG_UNFREEZE_GLROOT);

			if (mModel != null) {
				mModel.pause();
			}
			mPhotoView.pause();
			mHandler.removeMessages(MSG_HIDE_BARS);
			mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
		} finally {
			mGLRootView.unlockRenderThread();
		}
		clearBitmapPool(MediaItem.getThumbPool());

	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLRootView.lockRenderThread();
		try {
			if (mModel == null) {
				finish();
				return;
			}
			getGLRoot().freeze();
			setContentPane(mRootPane);

			mModel.resume();
			mPhotoView.resume();
			if (!mShowBars) {
				hideControls();
			}
			mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_GLROOT, UNFREEZE_GLROOT_TIMEOUT);
		} finally {
			mGLRootView.unlockRenderThread();
		}
		mGLRootView.onResume();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mGLRootView.lockRenderThread();
		try {
			super.onSaveInstanceState(outState);
		} finally {
			mGLRootView.unlockRenderThread();
		}
	}

	protected void setContentPane(final GLView content) {
		mContentPane = content;
		mContentPane.setBackgroundColor(GalleryUtils.intColorToFloatARGBArray(Color.BLACK));
		final GLRoot root = getGLRoot();
		root.setContentPane(mContentPane);
	}

	private boolean canShowBars() {
		// No bars if it's not allowed.
		if (!mActionBarAllowed) return false;
		return true;
	}

	private String getContentType(final Intent intent) {
		final String type = intent.getType();
		if (type != null) return type;

		final Uri uri = intent.getData();
		try {
			return getContentResolver().getType(uri);
		} catch (final Throwable t) {
			Log.w(TAG, "get type fail", t);
			return null;
		}
	}

	private void hideBars() {
		if (!mShowBars) return;
		mShowBars = false;
		hideControls();
		mHandler.removeMessages(MSG_HIDE_BARS);
	}

	private void refreshHidingMessage() {
		mHandler.removeMessages(MSG_HIDE_BARS);
		if (!mIsMenuVisible) {
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_BARS, HIDE_BARS_TIMEOUT);
		}
	}

	private void showBars() {
		if (mShowBars) return;
		mShowBars = true;
		showControls();
		refreshHidingMessage();
	}

	private void startViewAction(final Intent intent) {
		if (intent == null) {
			finish();
			return;
		}
		final Bundle data = new Bundle();
		final DataManager dm = getDataManager();
		final Uri uri = intent.getData();
		final String contentType = getContentType(intent);
		if (contentType == null) {
			// Toast.makeText(this, R.string.no_such_item,
			// Toast.LENGTH_LONG).show();
			// finish();
			// return;
		}
		if (uri == null) {
			finish();
		} else {
			final Path itemPath = dm.findPathByUri(uri, contentType);
			data.putString(KEY_MEDIA_ITEM_PATH, itemPath.toString());
			mData = data;

			mPhotoView = new PhotoView(this);
			mPhotoView.setListener(this);
			mRootPane.addComponent(mPhotoView);

			mHandler = new SynchronizedHandler(getGLRoot()) {
				@Override
				public void handleMessage(final Message message) {
					switch (message.what) {
						case MSG_HIDE_BARS: {
							hideBars();
							break;
						}
						case MSG_REFRESH_BOTTOM_CONTROLS: {
							break;
						}
						case MSG_ON_FULL_SCREEN_CHANGED: {
							break;
						}
						case MSG_UPDATE_ACTION_BAR: {
							updateBars();
							break;
						}
						case MSG_WANT_BARS: {
							wantBars();
							break;
						}
						case MSG_UNFREEZE_GLROOT: {
							getGLRoot().unfreeze();
							break;
						}
						case MSG_UPDATE_DEFERRED: {
							final long nextUpdate = mDeferUpdateUntil - SystemClock.uptimeMillis();
							if (nextUpdate <= 0) {
								updateUIForCurrentPhoto();
							} else {
								mHandler.sendEmptyMessageDelayed(MSG_UPDATE_DEFERRED, nextUpdate);
							}
							break;
						}
						case MSG_ON_CAMERA_CENTER: {
							boolean stayedOnCamera = false;
							stayedOnCamera = true;

							if (stayedOnCamera) {
								if (null == null) {
									/*
									 * We got here by swiping from photo 1 to
									 * the placeholder, so make it be the thing
									 * that is in focus when the user presses
									 * back from the camera app
									 */
								} else {
									updateBars();
									updateCurrentPhoto(mModel.getMediaItem());
								}
							}
							break;
						}
						case MSG_ON_PICTURE_CENTER: {
							break;
						}
						case MSG_REFRESH_IMAGE: {
							final MediaItem photo = mCurrentPhoto;
							mCurrentPhoto = null;
							updateCurrentPhoto(photo);
							break;
						}
						case MSG_UPDATE_PHOTO_UI: {
							updateUIForCurrentPhoto();
							break;
						}
						case MSG_UPDATE_SHARE_URI: {
							if (mCurrentPhoto == message.obj) {
								mCurrentPhoto.getContentUri();
								createShareIntent(mCurrentPhoto);
							}
							break;
						}
					}
				}
			};

			// Get default media set by the URI
			final MediaItem mediaItem = getDataManager().getMediaItem(itemPath);
			mModel = new PhotoViewAdapter(this, mPhotoView, mediaItem);
			mPhotoView.setModel(mModel);
			updateCurrentPhoto(mediaItem);
		}
	}

	private void toggleBars() {
		if (mShowBars) {
			hideBars();
		} else {
			if (canShowBars()) {
				showBars();
			}
		}
	}

	private void updateBars() {
		if (!canShowBars()) {
			hideBars();
		}
	}

	private void updateCurrentPhoto(final MediaItem photo) {
		if (mCurrentPhoto == photo) return;
		mCurrentPhoto = photo;
		updateUIForCurrentPhoto();
	}

	private void updateUIForCurrentPhoto() {
		if (mCurrentPhoto == null) return;

	}

	private void wantBars() {
		if (canShowBars()) {
			showBars();
		}
	}

	private static void clearBitmapPool(final BitmapPool pool) {
		if (pool != null) {
			pool.clear();
		}
	}

	// TODO better mimetype
	private static Intent createShareIntent(final MediaItem mediaObject) {
		return new Intent(Intent.ACTION_SEND).setType("image/*")
				.putExtra(Intent.EXTRA_STREAM, mediaObject.getContentUri())
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
	}

}
