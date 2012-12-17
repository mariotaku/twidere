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

import org.mariotaku.gallery3d.common.ApiHelper;
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.data.BitmapPool;
import org.mariotaku.gallery3d.data.DataManager;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.data.Path;
import org.mariotaku.gallery3d.ui.GLRoot;
import org.mariotaku.gallery3d.ui.GLRootView;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.gallery3d.util.ThreadPool;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public final class GalleryActivity extends FragmentActivity implements Constants, View.OnClickListener, GalleryContext {

	public void onClick(View v) {
		// TODO: Implement this method
	}
	
	private static final String TAG = "Gallery";

	private GLRootView mGLRootView;
	private View mProgress;
	private View mControlButtons;

	private StateManager mStateManager;

	private OrientationManager mOrientationManager;

	private final TransitionStore mTransitionStore = new TransitionStore();

	//private boolean mDisableToggleStatusBar;

	private AlertDialog mAlertDialog = null;

	private final BroadcastReceiver mMountReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getExternalCacheDir() != null) {
				onStorageReady();
			}
		}
	};

	private final IntentFilter mMountFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);

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

	public OrientationManager getOrientationManager() {
		return mOrientationManager;
	}

	public synchronized StateManager getStateManager() {
		if (mStateManager == null) {
			mStateManager = new StateManager(this);
		}
		return mStateManager;
	}

	@Override
	public ThreadPool getThreadPool() {
		return ((IGalleryApplication) getApplication()).getThreadPool();
	}

	public TransitionStore getTransitionStore() {
		return mTransitionStore;
	}

	public void hideControls() {
		mControlButtons.setVisibility(View.GONE);
	}

	public void hideProgress() {
		mProgress.setVisibility(View.GONE);
	}

	@Override
	public void onBackPressed() {
		// send the back event to the top sub-state
		final GLRoot root = getGLRoot();
		root.lockRenderThread();
		try {
			getStateManager().onBackPressed();
		} finally {
			root.unlockRenderThread();
		}
	}

	@Override
	public void onConfigurationChanged(final Configuration config) {
		super.onConfigurationChanged(config);
		mStateManager.onConfigurationChange(config);
		invalidateOptionsMenu();
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mGLRootView = (GLRootView) findViewById(R.id.gl_root_view);
		mProgress = findViewById(R.id.progress);
		mControlButtons = findViewById(R.id.control_buttons);
	}

	public void showControls() {
		mControlButtons.setVisibility(View.VISIBLE);
	}

	public void showProgress() {
		mProgress.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		mGLRootView.lockRenderThread();
		try {
			getStateManager().notifyActivityResult(requestCode, resultCode, data);
		} finally {
			mGLRootView.unlockRenderThread();
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mOrientationManager = new OrientationManager(this);

		setContentView(R.layout.image_viewer_gl);

		if (savedInstanceState != null) {
			getStateManager().restoreFromState(savedInstanceState);
		} else {
			initializeByIntent();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mGLRootView.lockRenderThread();
		try {
			getStateManager().destroy();
		} finally {
			mGLRootView.unlockRenderThread();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		mOrientationManager.pause();
		mGLRootView.onPause();
		mGLRootView.lockRenderThread();
		try {
			getStateManager().pause();
			getDataManager().pause();
		} finally {
			mGLRootView.unlockRenderThread();
		}
		clearBitmapPool(MediaItem.getThumbPool());

	}

	@Override
	protected void onResume() {
		Utils.assertTrue(getStateManager().getStateCount() > 0);
		super.onResume();
		mGLRootView.lockRenderThread();
		try {
			getStateManager().resume();
			getDataManager().resume();
		} finally {
			mGLRootView.unlockRenderThread();
		}
		mGLRootView.onResume();
		mOrientationManager.resume();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mGLRootView.lockRenderThread();
		try {
			super.onSaveInstanceState(outState);
			getStateManager().saveState(outState);
		} finally {
			mGLRootView.unlockRenderThread();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (getExternalCacheDir() == null) {
			final OnCancelListener onCancel = new OnCancelListener() {
				@Override
				public void onCancel(final DialogInterface dialog) {
					finish();
				}
			};
			final OnClickListener onClick = new OnClickListener() {
				@Override
				public void onClick(final DialogInterface dialog, final int which) {
					dialog.cancel();
				}
			};
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.no_external_storage_title);
			builder.setMessage(R.string.no_external_storage);
			builder.setNegativeButton(android.R.string.cancel, onClick);
			builder.setOnCancelListener(onCancel);
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			mAlertDialog = builder.show();
			registerReceiver(mMountReceiver, mMountFilter);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mAlertDialog != null) {
			unregisterReceiver(mMountReceiver);
			mAlertDialog.dismiss();
			mAlertDialog = null;
		}
	}

	protected void onStorageReady() {
		if (mAlertDialog != null) {
			mAlertDialog.dismiss();
			mAlertDialog = null;
			unregisterReceiver(mMountReceiver);
		}
	}

	private String getContentType(final Intent intent) {
		final String type = intent.getType();
		if (type != null) return GalleryUtils.MIME_TYPE_PANORAMA360.equals(type) ? MediaItem.MIME_TYPE_JPEG : type;

		final Uri uri = intent.getData();
		try {
			return getContentResolver().getType(uri);
		} catch (final Throwable t) {
			Log.w(TAG, "get type fail", t);
			return null;
		}
	}

	private void initializeByIntent() {
		final Intent intent = getIntent();
		final String action = intent.getAction();

		if (INTENT_ACTION_VIEW_IMAGE.equals(action)) {
			startViewAction(intent);
		} else {
			finish();
		}
	}

	private void startViewAction(final Intent intent) {
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
			Toast.makeText(this, R.string.no_such_item, Toast.LENGTH_LONG).show();
			finish();
		} else {
			final Path itemPath = dm.findPathByUri(uri, contentType);
			final Path albumPath = dm.getDefaultSetOf(itemPath);

			data.putString(PhotoPage.KEY_MEDIA_ITEM_PATH, itemPath.toString());

			// TODO: Make the parameter "SingleItemOnly" public so other
			// activities can reference it.
			final boolean singleItemOnly = albumPath == null || intent.getBooleanExtra("SingleItemOnly", false);
			if (!singleItemOnly) {
				data.putString(PhotoPage.KEY_MEDIA_SET_PATH, albumPath.toString());
				// when FLAG_ACTIVITY_NEW_TASK is set, (e.g. when intent is
				// fired
				// from notification), back button should behave the same as up
				// button
				// rather than taking users back to the home screen
				if (intent.getBooleanExtra(PhotoPage.KEY_TREAT_BACK_AS_UP, false)
						|| (intent.getFlags() & Intent.FLAG_ACTIVITY_NEW_TASK) != 0) {
					data.putBoolean(PhotoPage.KEY_TREAT_BACK_AS_UP, true);
				}
			}
			getStateManager().startState(PhotoPage.class, data);
		}
	}

	private static void clearBitmapPool(final BitmapPool pool) {
		if (pool != null) {
			pool.clear();
		}
	}

}
