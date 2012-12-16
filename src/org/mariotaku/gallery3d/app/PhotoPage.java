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

import org.mariotaku.gallery3d.anim.StateTransitionAnimation;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.data.MediaObject;
import org.mariotaku.gallery3d.data.Path;
import org.mariotaku.gallery3d.ui.GLRoot;
import org.mariotaku.gallery3d.ui.GLView;
import org.mariotaku.gallery3d.ui.PhotoView;
import org.mariotaku.gallery3d.ui.PreparePageFadeoutTexture;
import org.mariotaku.gallery3d.ui.RawTexture;
import org.mariotaku.gallery3d.ui.SynchronizedHandler;
import org.mariotaku.gallery3d.util.GalleryUtils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class PhotoPage implements PhotoView.Listener, OrientationManager.Listener {
	protected static final int FLAG_HIDE_ACTION_BAR = 1;
	protected static final int FLAG_HIDE_STATUS_BAR = 2;
	protected static final int FLAG_SCREEN_ON_WHEN_PLUGGED = 4;
	protected static final int FLAG_SCREEN_ON_ALWAYS = 8;
	protected static final int FLAG_ALLOW_LOCK_WHILE_SCREEN_ON = 16;
	protected static final int FLAG_SHOW_WHEN_LOCKED = 32;

	protected GalleryActivity mActivity;
	protected Bundle mData;
	protected int mFlags;

	protected ResultEntry mReceivedResults;
	protected ResultEntry mResult;

	private boolean mDestroyed = false;

	private boolean mPlugged = false;
	boolean mIsFinishing = false;
	private static final String KEY_TRANSITION_IN = "transition-in";

	private StateTransitionAnimation.Transition mNextTransition = StateTransitionAnimation.Transition.None;

	private StateTransitionAnimation mIntroAnimation;
	private GLView mContentPane;
	protected float[] mBackgroundColor;

	BroadcastReceiver mPowerIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
				final boolean plugged = 0 != intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

				if (plugged != mPlugged) {
					mPlugged = plugged;
					setScreenFlags();
				}
			}
		}
	};

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

	private static final int MSG_UPDATE_PROGRESS = 13;

	private static final int MSG_UPDATE_DEFERRED = 14;

	private static final int MSG_UPDATE_SHARE_URI = 15;

	private static final int HIDE_BARS_TIMEOUT = 3500;

	private static final int UNFREEZE_GLROOT_TIMEOUT = 250;

	public static final String KEY_MEDIA_SET_PATH = "media-set-path";

	public static final String KEY_MEDIA_ITEM_PATH = "media-item-path";
	public static final String KEY_INDEX_HINT = "index-hint";
	public static final String KEY_APP_BRIDGE = "app-bridge";
	public static final String KEY_TREAT_BACK_AS_UP = "treat-back-as-up";
	public static final String KEY_START_IN_FILMSTRIP = "start-in-filmstrip";
	public static final String KEY_RETURN_INDEX_HINT = "return-index-hint";
	public static final String KEY_SHOW_WHEN_LOCKED = "show_when_locked";
	private PhotoView mPhotoView;
	private PhotoPage.Model mModel;
	private boolean mShowDetails;
	private int mCurrentIndex = 0;
	private Handler mHandler;
	private boolean mShowBars = true;

	private volatile boolean mActionBarAllowed = true;
	private boolean mIsMenuVisible;

	private MediaItem mCurrentPhoto = null;
	private boolean mIsActive;
	private String mSetPathString;
	// This is the original mSetPathString before adding the camera preview
	// item.
	private OrientationManager mOrientationManager;
	private boolean mHasCameraScreennailOrPlaceholder = false;
	private final long mDeferUpdateUntil = Long.MAX_VALUE;
	private final GLView mRootPane = new GLView() {
		@Override
		protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
			mPhotoView.layout(0, 0, right - left, bottom - top);
			if (mShowDetails) {
				// mDetailsHelper.layout(left, mActionBar.getHeight(), right,
				// bottom);
			}
		}
	};

	protected PhotoPage() {
	}

	public Bundle getData() {
		return mData;
	}

	public boolean isFinishing() {
		return mIsFinishing;
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

	public void onCreate(final Bundle data, final Bundle restoreState) {
		mBackgroundColor = GalleryUtils.intColorToFloatARGBArray(mActivity.getResources().getColor(
				getBackgroundColorId()));

		mPhotoView = new PhotoView(mActivity);
		mPhotoView.setListener(this);
		mRootPane.addComponent(mPhotoView);
		mOrientationManager = mActivity.getOrientationManager();
		mOrientationManager.addListener(this);
		mActivity.getGLRoot().setOrientationSource(mOrientationManager);

		mHandler = new SynchronizedHandler(mActivity.getGLRoot()) {
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
						mActivity.getGLRoot().unfreeze();
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
								 * We got here by swiping from photo 1 to the
								 * placeholder, so make it be the thing that is
								 * in focus when the user presses back from the
								 * camera app
								 */
							} else {
								updateBars();
								updateCurrentPhoto(mModel.getMediaItem(0));
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
					case MSG_UPDATE_PROGRESS: {
						updateProgressBar();
						break;
					}
					case MSG_UPDATE_SHARE_URI: {
						if (mCurrentPhoto == message.obj) {
							final Uri contentUri = mCurrentPhoto.getContentUri();
							final Intent shareIntent = createShareIntent(mCurrentPhoto);
						}
						break;
					}
					default:
						throw new AssertionError(message.what);
				}
			}
		};

		mSetPathString = data.getString(KEY_MEDIA_SET_PATH);
		final String itemPathString = data.getString(KEY_MEDIA_ITEM_PATH);
		final Path itemPath = itemPathString != null ? Path.fromString(data.getString(KEY_MEDIA_ITEM_PATH)) : null;
		mCurrentIndex = data.getInt(KEY_INDEX_HINT, 0);
		if (mSetPathString != null) {
			if (null != null) {
				mShowBars = false;
				mHasCameraScreennailOrPlaceholder = true;

				if (data.getBoolean(KEY_SHOW_WHEN_LOCKED, false)) {
					// Set the flag to be on top of the lock screen.
					mFlags |= FLAG_SHOW_WHEN_LOCKED;
				}

				// Don't display "empty album" action item for capture intents.
				if (!mSetPathString.equals("/local/all/0")) {
					mSetPathString = "/filter/empty/{" + mSetPathString + "}";
				}

			}

			mSetPathString = "/filter/delete/{" + mSetPathString + "}";
			if (itemPath == null) // Bail out, ActivityState can't load on an
									// empty
									// album
				return;
			final PhotoDataAdapter pda = new PhotoDataAdapter(mActivity, mPhotoView, itemPath, mCurrentIndex,
					null == null ? -1 : 0);
			mModel = pda;
			mPhotoView.setModel(mModel);

			pda.setDataListener(new PhotoDataAdapter.DataListener() {

				@Override
				public void onLoadingFinished(final boolean loadingFailed) {
					// TODO
					mActivity.hideProgress();
					if (!mModel.isEmpty()) {
						final MediaItem photo = mModel.getMediaItem(0);
						if (photo != null) {
							updateCurrentPhoto(photo);
						}
					} else if (mIsActive) {
						// We only want to finish the ActivityState if there is
						// no
						// deletion that the user can undo.
						mActivity.getStateManager().finishState(PhotoPage.this);
					}
				}

				@Override
				public void onLoadingStarted() {
					// TODO
					mActivity.showProgress();
				}

			});
		} else {
			// Get default media set by the URI
			final MediaItem mediaItem = (MediaItem) mActivity.getDataManager().getMediaObject(itemPath);
			mModel = new SinglePhotoDataAdapter(mActivity, mPhotoView, mediaItem);
			mPhotoView.setModel(mModel);
			updateCurrentPhoto(mediaItem);
		}

	}

	@Override
	public void onCurrentImageUpdated() {
		mActivity.getGLRoot().unfreeze();
	}

	@Override
	public void onFullScreenChanged(final boolean full) {
		final Message m = mHandler.obtainMessage(MSG_ON_FULL_SCREEN_CHANGED, full ? 1 : 0, 0);
		m.sendToTarget();
	}

	@Override
	public void onOrientationCompensationChanged() {
		mActivity.getGLRoot().requestLayoutContentPane();
	}

	public void onPause() {
		if (0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
			((Activity) mActivity).unregisterReceiver(mPowerIntentReceiver);
		}
		if (mNextTransition != StateTransitionAnimation.Transition.None) {
			mActivity.getTransitionStore().put(KEY_TRANSITION_IN, mNextTransition);
			PreparePageFadeoutTexture.prepareFadeOutTexture(mActivity, mContentPane);
			mNextTransition = StateTransitionAnimation.Transition.None;
		}
		mIsActive = false;

		mActivity.getGLRoot().unfreeze();
		mHandler.removeMessages(MSG_UNFREEZE_GLROOT);

		// Hide the detail dialog on exit
		if (mShowDetails) {
			hideDetails();
		}
		if (mModel != null) {
			mModel.pause();
		}
		mPhotoView.pause();
		mHandler.removeMessages(MSG_HIDE_BARS);
		mHandler.removeMessages(MSG_REFRESH_BOTTOM_CONTROLS);
	}

	@Override
	public void onPictureCenter(boolean isCamera) {
		isCamera = isCamera || mHasCameraScreennailOrPlaceholder && null == null;
		mPhotoView.setWantPictureCenterCallbacks(false);
		mHandler.removeMessages(MSG_ON_CAMERA_CENTER);
		mHandler.removeMessages(MSG_ON_PICTURE_CENTER);
		mHandler.sendEmptyMessage(isCamera ? MSG_ON_CAMERA_CENTER : MSG_ON_PICTURE_CENTER);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Callbacks from PhotoView
	// //////////////////////////////////////////////////////////////////////////
	@Override
	public void onSingleTapUp(final int x, final int y) {

		final MediaItem item = mModel.getMediaItem(0);
		if (item == null) // item is not ready or it is camera preview, ignore
			return;

		toggleBars();
	}

	protected void clearStateResult() {
	}

	protected float[] getBackgroundColor() {
		return mBackgroundColor;
	}

	protected int getBackgroundColorId() {
		return android.R.color.transparent;
	}

	protected void onBackPressed() {
		mActivity.getStateManager().finishState(this);
	}

	protected void onConfigurationChanged(final Configuration config) {
	}

	protected boolean onCreateActionBar(final Menu menu) {
		updateMenuOperations();
		return true;
	}

	protected void onDestroy() {
		mOrientationManager.removeListener(this);
		mActivity.getGLRoot().setOrientationSource(null);

		// Remove all pending messages.
		mHandler.removeCallbacksAndMessages(null);
		mDestroyed = true;
	}

	protected boolean onItemSelected(final MenuItem item) {
		if (mModel == null) return true;
		refreshHidingMessage();
		return false;
	}

	protected void onResume() {
		final RawTexture fade = mActivity.getTransitionStore().get(PreparePageFadeoutTexture.KEY_FADE_TEXTURE);
		mNextTransition = mActivity.getTransitionStore().get(KEY_TRANSITION_IN,
				StateTransitionAnimation.Transition.None);
		if (mNextTransition != StateTransitionAnimation.Transition.None) {
			mIntroAnimation = new StateTransitionAnimation(mNextTransition, fade);
			mNextTransition = StateTransitionAnimation.Transition.None;
		}

		if (mModel == null) {
			mActivity.getStateManager().finishState(this);
			return;
		}

		mActivity.getGLRoot().freeze();
		mIsActive = true;
		setContentPane(mRootPane);

		mModel.resume();
		mPhotoView.resume();
		if (!mShowBars) {
			mActivity.hideControls();
		}
		mHandler.sendEmptyMessageDelayed(MSG_UNFREEZE_GLROOT, UNFREEZE_GLROOT_TIMEOUT);
	}

	protected void onSaveState(final Bundle outState) {
	}

	protected void onStateResult(final int requestCode, final int resultCode, final Intent data) {
	}

	protected void setContentPane(final GLView content) {
		mContentPane = content;
		if (mIntroAnimation != null) {
			mContentPane.setIntroAnimation(mIntroAnimation);
			mIntroAnimation = null;
		}
		mContentPane.setBackgroundColor(getBackgroundColor());
		final GLRoot root = mActivity.getGLRoot();
		root.setContentPane(mContentPane);
	}

	protected void setStateResult(final int resultCode, final Intent data) {
		if (mResult == null) return;
		mResult.resultCode = resultCode;
		mResult.resultData = data;
	}

	protected void transitionOnNextPause(final Class<? extends PhotoPage> outgoing,
			final Class<? extends PhotoPage> incoming, final StateTransitionAnimation.Transition hint) {
		if (outgoing == PhotoPage.class) {
			mNextTransition = StateTransitionAnimation.Transition.Outgoing;
		} else if (incoming == PhotoPage.class) {
			mNextTransition = StateTransitionAnimation.Transition.PhotoIncoming;
		} else {
			mNextTransition = hint;
		}
	}

	private boolean canShowBars() {
		// No bars if we are showing camera preview.
		if (null != null && mCurrentIndex == 0) return false;

		// No bars if it's not allowed.
		if (!mActionBarAllowed) return false;

		return true;
	}

	private void hideBars() {
		if (!mShowBars) return;
		mShowBars = false;
		mActivity.hideControls();
		mHandler.removeMessages(MSG_HIDE_BARS);
	}

	private void hideDetails() {
		mShowDetails = false;
	}

	private void refreshHidingMessage() {
		mHandler.removeMessages(MSG_HIDE_BARS);
		if (!mIsMenuVisible) {
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_BARS, HIDE_BARS_TIMEOUT);
		}
	}

	private void setScreenFlags() {
		final Window win = mActivity.getWindow();
		final WindowManager.LayoutParams params = win.getAttributes();
		if (0 != (mFlags & FLAG_SCREEN_ON_ALWAYS) || mPlugged && 0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
			params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		} else {
			params.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
		}
		if (0 != (mFlags & FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)) {
			params.flags |= WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
		} else {
			params.flags &= ~WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON;
		}
		if (0 != (mFlags & FLAG_SHOW_WHEN_LOCKED)) {
			params.flags |= WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		} else {
			params.flags &= ~WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		}
		win.setAttributes(params);
	}

	private void showBars() {
		if (mShowBars) return;
		mShowBars = true;
		mActivity.showControls();
		mOrientationManager.unlockOrientation();
		refreshHidingMessage();
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

	// ////////////////////////////////////////////////////////////////////////
	// AppBridge.Server interface
	// ////////////////////////////////////////////////////////////////////////

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

	private void updateMenuOperations() {
	}

	private void updateProgressBar() {
	}

	private void updateUIForCurrentPhoto() {
		if (mCurrentPhoto == null) return;

		// If by swiping or deletion the user ends up on an action item
		// and zoomed in, zoom out so that the context of the action is
		// more clear
		if ((mCurrentPhoto.getSupportedOperations() & MediaObject.SUPPORT_ACTION) != 0) {
			mPhotoView.setWantPictureCenterCallbacks(true);
		}

		updateMenuOperations();
		if (mShowDetails) {
		}
		updateProgressBar();
	}

	private void wantBars() {
		if (canShowBars()) {
			showBars();
		}
	}

	void initialize(final GalleryActivity activity, final Bundle data) {
		mActivity = activity;
		mData = data;
	}

	boolean isDestroyed() {
		return mDestroyed;
	}

	// should only be called by StateManager
	void resume() {
		final GalleryActivity activity = mActivity;

		activity.invalidateOptionsMenu();

		setScreenFlags();

		final ResultEntry entry = mReceivedResults;
		if (entry != null) {
			mReceivedResults = null;
			onStateResult(entry.requestCode, entry.resultCode, entry.resultData);
		}

		if (0 != (mFlags & FLAG_SCREEN_ON_WHEN_PLUGGED)) {
			// we need to know whether the device is plugged in to do this
			// correctly
			final IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			activity.registerReceiver(mPowerIntentReceiver, filter);
		}

		onResume();

		// the transition store should be cleared after resume;
		mActivity.getTransitionStore().clear();
	}

	// TODO better mimetype
	private static Intent createShareIntent(final MediaObject mediaObject) {
		return new Intent(Intent.ACTION_SEND).setType("image/*")
				.putExtra(Intent.EXTRA_STREAM, mediaObject.getContentUri())
				.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
	}

	public static interface Model extends PhotoView.Model {
		public boolean isEmpty();

		public void pause();

		public void resume();

	}

	protected static class ResultEntry {
		public int requestCode;
		public int resultCode = Activity.RESULT_CANCELED;
		public Intent resultData;
	}

}
