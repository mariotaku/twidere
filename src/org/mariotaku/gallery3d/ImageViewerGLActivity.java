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

package org.mariotaku.gallery3d;

import android.app.ActionBar;
import android.app.ActionBar.OnMenuVisibilityListener;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ShareActionProvider;

import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener;

import org.mariotaku.gallery3d.ui.GLRoot;
import org.mariotaku.gallery3d.ui.GLRootView;
import org.mariotaku.gallery3d.ui.GLView;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.SynchronizedHandler;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.gallery3d.util.ThreadPool;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.TwidereSwipeBackActivity;
import org.mariotaku.twidere.util.SaveImageTask;
import org.mariotaku.twidere.util.Utils;

import java.io.File;

public final class ImageViewerGLActivity extends TwidereSwipeBackActivity implements Constants, PhotoView.Listener,
		GLImageLoader.DownloadListener, LoaderManager.LoaderCallbacks<GLImageLoader.Result>, OnMenuVisibilityListener,
		SwipeListener {

	private final GLView mRootPane = new GLView() {
		@Override
		protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
			mPhotoView.layout(0, 0, right - left, bottom - top);
		}
	};
	protected static final int FLAG_HIDE_ACTION_BAR = 1;
	protected static final int FLAG_HIDE_STATUS_BAR = 2;

	private static final int MSG_HIDE_BARS = 1;
	private static final int MSG_ON_FULL_SCREEN_CHANGED = 4;
	private static final int MSG_UPDATE_ACTION_BAR = 5;
	private static final int MSG_UNFREEZE_GLROOT = 6;
	private static final int MSG_WANT_BARS = 7;
	private static final int MSG_REFRESH_BOTTOM_CONTROLS = 8;
	private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

	private ShareActionProvider mShareActionProvider;

	private ActionBar mActionBar;

	private GLView mContentPane;
	private GLRootView mGLRootView;
	private ProgressBar mProgress;
	private ImageView mImageViewer;

	private PhotoView mPhotoView;

	private PhotoView.ITileImageAdapter mAdapter;
	private Handler mHandler;
	protected int mFlags;

	private boolean mShowBars = true;
	private boolean mActionBarAllowed = true;
	private boolean mImageLoaded;
	private boolean mLoaderInitialized;

	private long mContentLength;
	private ThreadPool mThreadPool;

	private File mImageFile;

	public GLRoot getGLRoot() {
		return mGLRootView;
	}

	public ThreadPool getThreadPool() {
		if (mThreadPool != null) return mThreadPool;
		return mThreadPool = new ThreadPool();
	}

	public void hideProgress() {
		mProgress.setVisibility(View.GONE);
		mProgress.setProgress(0);
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
	public void onContentChanged() {
		super.onContentChanged();
		mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
		mImageViewer = (ImageView) findViewById(R.id.image_viewer);
		mProgress = (ProgressBar) findViewById(R.id.progress);
	}

	@Override
	public Loader<GLImageLoader.Result> onCreateLoader(final int id, final Bundle args) {
		mProgress.setVisibility(View.VISIBLE);
		mProgress.setIndeterminate(true);
		invalidateOptionsMenu();
		final Uri uri = args != null ? (Uri) args.getParcelable(EXTRA_URI) : null;
		return new GLImageLoader(this, this, uri);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_image_viewer, menu);
		// Locate MenuItem with ShareActionProvider
		final MenuItem item = menu.findItem(R.id.share);
		// Fetch and store ShareActionProvider
		mShareActionProvider = (ShareActionProvider) item.getActionProvider();
		return true;
	}

	@Override
	public void onCurrentImageUpdated() {
		mGLRootView.unfreeze();
	}

	@Override
	public void onDownloadError(final Throwable t) {
		mContentLength = 0;
	}

	@Override
	public void onDownloadFinished() {
		mContentLength = 0;
	}

	@Override
	public void onDownloadStart(final long total) {
		mContentLength = total;
		mProgress.setIndeterminate(total <= 0);
		mProgress.setMax(total > 0 ? (int) (total / 1024) : 0);
	}

	@Override
	public void onEdgeTouch(final int edgeFlag) {
		showBars();
	}

	@Override
	public void onLoaderReset(final Loader<GLImageLoader.Result> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<GLImageLoader.Result> loader, final GLImageLoader.Result data) {
		if (data.decoder != null || data.bitmap != null) {
			if (data.decoder != null) {
				mGLRootView.setVisibility(View.VISIBLE);
				mImageViewer.setVisibility(View.GONE);
				mAdapter.setData(data.decoder, data.bitmap, data.orientation);
				mImageViewer.setImageBitmap(null);
			} else if (data.bitmap != null) {
				mGLRootView.setVisibility(View.GONE);
				mImageViewer.setVisibility(View.VISIBLE);
				mImageViewer.setImageBitmap(data.bitmap);
			}
			mImageFile = data.file;
			mImageLoaded = true;
			invalidateOptionsMenu();
		} else {
			mImageFile = null;
			mImageLoaded = false;
			invalidateOptionsMenu();
			if (data != null) {
				Utils.showErrorMessage(this, null, data.exception, true);
			}
		}
		updateShareIntent();
		mProgress.setVisibility(View.GONE);
		mProgress.setProgress(0);
	}

	@Override
	public void onMenuVisibilityChanged(final boolean isVisible) {
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final Intent intent = getIntent();
		final Uri uri = intent.getData();
		final Uri orig = intent.getParcelableExtra(EXTRA_URI_ORIG);
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
			case MENU_ACTIONS: {
				if (mImageLoaded) {
					new SaveImageTask(this, mImageFile).execute();
				} else {
					loadImage();
				}
				break;
			}
			case MENU_OPEN_IN_BROWSER: {
				final Uri uri_preferred = orig != null ? orig : uri;
				if (uri_preferred == null) return false;
				final String scheme = uri_preferred.getScheme();
				if ("http".equals(scheme) || "https".equals(scheme)) {
					final Intent open_intent = new Intent(Intent.ACTION_VIEW, uri_preferred);
					open_intent.addCategory(Intent.CATEGORY_BROWSABLE);
					try {
						startActivity(open_intent);
					} catch (final ActivityNotFoundException e) {
						// Ignore.
					}
				}
				break;
			}
		}
		return true;
	}

	@Override
	public void onPictureCenter() {
		mPhotoView.setWantPictureCenterCallbacks(false);
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final MenuItem itemActions = menu.findItem(MENU_ACTIONS);
		final LoaderManager lm = getSupportLoaderManager();
		itemActions.setVisible(!lm.hasRunningLoaders());
		if (mImageLoaded) {
			itemActions.setTitle(R.string.save);
			itemActions.setIcon(android.R.drawable.ic_menu_save);
		} else {
			itemActions.setTitle(R.string.refresh);
			itemActions.setIcon(R.drawable.ic_menu_refresh);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onProgressUpdate(final long downloaded) {
		if (mContentLength == 0) {
			mProgress.setIndeterminate(true);
			return;
		}
		mProgress.setIndeterminate(false);
		mProgress.setProgress((int) (downloaded / 1024));
	}

	@Override
	public void onScrollOverThreshold() {

	}

	@Override
	public void onScrollStateChange(final int state, final float scrollPercent) {

	}

	@Override
	public void onSingleTapUp(final int x, final int y) {
		toggleBars();
	}

	public void showProgress() {
		mProgress.setVisibility(View.VISIBLE);
		mProgress.setIndeterminate(true);
	}

	@Override
	protected int getThemeResource() {
		return R.style.Theme_Twidere_Viewer;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_viewer_gl);
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.addOnMenuVisibilityListener(this);
		mHandler = new MyHandler(this);
		mPhotoView = new PhotoView(this);
		mPhotoView.setListener(this);
		mRootPane.addComponent(mPhotoView);
		mAdapter = new PhotoViewAdapter(mPhotoView);
		mPhotoView.setModel(mAdapter);
		if (savedInstanceState == null) {
			loadImage();
		}
		setSwipeListener(this);
	}

	@Override
	protected void onDestroy() {
		mActionBar.removeOnMenuVisibilityListener(this);
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
		setIntent(intent);
		loadImage();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mGLRootView.onPause();
		mGLRootView.lockRenderThread();
		try {
			mGLRootView.unfreeze();
			mHandler.removeMessages(MSG_UNFREEZE_GLROOT);

			if (mAdapter != null) {
				mAdapter.recycleScreenNail();
			}
			mPhotoView.pause();
			mHandler.removeMessages(MSG_HIDE_BARS);
			mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
		} finally {
			mGLRootView.unlockRenderThread();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		mGLRootView.lockRenderThread();
		try {
			if (mAdapter == null) {
				finish();
				return;
			}
			mGLRootView.freeze();
			setContentPane(mRootPane);

			mPhotoView.resume();
			if (!mShowBars) {
				hideBars();
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
		if (!mShowBars || isSwiping()) return;
		mShowBars = false;
		mActionBar.hide();
		mHandler.removeMessages(MSG_HIDE_BARS);
	}

	private void loadImage() {
		getSupportLoaderManager().destroyLoader(0);
		final Uri uri = getIntent().getData();
		if (uri == null) {
			finish();
			return;
		}
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_URI, uri);
		if (!mLoaderInitialized) {
			getSupportLoaderManager().initLoader(0, args, this);
			mLoaderInitialized = true;
		} else {
			getSupportLoaderManager().restartLoader(0, args, this);
		}
	}

	private void showBars() {
		if (mShowBars) return;
		mShowBars = true;
		mActionBar.show();
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

	void updateShareIntent() {
		if (mShareActionProvider == null) return;
		final Intent intent = getIntent();
		final Uri uri = intent.getData();
		final Intent share_intent = new Intent(Intent.ACTION_SEND);
		if (mImageFile != null && mImageFile.exists()) {
			share_intent.setType("image/*");
			share_intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mImageFile));
		} else {
			share_intent.setType("text/plain");
			share_intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
		}
		mShareActionProvider.setShareIntent(share_intent);
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
