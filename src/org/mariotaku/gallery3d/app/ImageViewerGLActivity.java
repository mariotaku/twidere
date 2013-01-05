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
import org.mariotaku.gallery3d.data.CachedDownloader;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.ui.GLRoot;
import org.mariotaku.gallery3d.ui.GLRootView;
import org.mariotaku.gallery3d.ui.GLView;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.SynchronizedHandler;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.gallery3d.util.ThreadPool;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public final class ImageViewerGLActivity extends FragmentActivity implements Constants, View.OnClickListener,
		PhotoView.Listener {

	private GLRootView mGLRootView;
	private View mProgress;
	private View mControlButtons;

	protected static final int FLAG_HIDE_ACTION_BAR = 1;
	protected static final int FLAG_HIDE_STATUS_BAR = 2;

	protected int mFlags;

	private GLView mContentPane;

	private static final int MSG_HIDE_BARS = 1;
	private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;
	private static final int MSG_UPDATE_ACTION_BAR = 5;
	private static final int MSG_UNFREEZE_GLROOT = 6;
	private static final int MSG_WANT_BARS = 7;
	private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;
	private static final int HIDE_BARS_TIMEOUT = 3500;
	private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

	private PhotoView mPhotoView;
	private PhotoView.Model mModel;
	private Handler mHandler;
	private CachedDownloader mDownloader;

	private boolean mShowBars = true;
	private volatile boolean mActionBarAllowed = true;
	private boolean mIsMenuVisible;
	private final GLView mRootPane = new GLView() {
		@Override
		protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
			mPhotoView.layout(0, 0, right - left, bottom - top);
		}
	};

	private ThreadPool mThreadPool;

	public GLRoot getGLRoot() {
		return mGLRootView;
	}

	public ThreadPool getThreadPool() {
		if (mThreadPool != null) return mThreadPool;
		return mThreadPool = new ThreadPool();
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
		mGLRootView.unfreeze();
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
	}

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
		mDownloader = new CachedDownloader(this);
		setContentView(R.layout.image_viewer_gl);
		mHandler = new MyHandler(this);
		if (savedInstanceState == null) {
			startViewAction(getIntent());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGLRootView.lockRenderThread();
		try {
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
			mGLRootView.unfreeze();
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
			mGLRootView.freeze();
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
		mGLRootView.setContentPane(mContentPane);
	}

	private boolean canShowBars() {
		// No bars if it's not allowed.
		if (!mActionBarAllowed) return false;
		return true;
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
		final Uri uri = intent.getData();
		if (uri == null) {
			finish();
		} else {

			mPhotoView = new PhotoView(this);
			mPhotoView.setListener(this);
			mRootPane.addComponent(mPhotoView);

			// Get default media set by the URI
			final MediaItem mediaItem = new MediaItem(this, mDownloader, uri);
			mModel = new PhotoViewAdapter(this, mPhotoView, mediaItem);
			mPhotoView.setModel(mModel);
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

	private static class MyHandler extends SynchronizedHandler {
		ImageViewerGLActivity activity;

		private MyHandler(final ImageViewerGLActivity activity) {
			super(activity.getGLRoot());
			this.activity = activity;
		}

		@Override
		public void handleMessage(final Message message) {
			switch (message.what) {
				case MSG_HIDE_BARS: {
					activity.hideBars();
					break;
				}
				case MSG_REFRESH_BOTTOM_CONTROLS: {
					break;
				}
				case MSG_ON_FULL_SCREEN_CHANGED: {
					break;
				}
				case MSG_UPDATE_ACTION_BAR: {
					activity.updateBars();
					break;
				}
				case MSG_WANT_BARS: {
					activity.wantBars();
					break;
				}
				case MSG_UNFREEZE_GLROOT: {
					mGLRoot.unfreeze();
					break;
				}
			}
		}
	}

}
