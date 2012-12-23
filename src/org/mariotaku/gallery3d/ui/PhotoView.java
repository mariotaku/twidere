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

package org.mariotaku.gallery3d.ui;

import org.mariotaku.gallery3d.app.ImageViewerGLActivity;
import org.mariotaku.gallery3d.common.ApiHelper;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.data.MediaObject;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Message;
import android.view.MotionEvent;

public class PhotoView extends GLView {

	public static final int INVALID_SIZE = -1;
	public static final long INVALID_DATA_VERSION = MediaObject.INVALID_DATA_VERSION;

	private static final int MSG_CANCEL_EXTRA_SCALING = 2;

	private static final int MSG_CAPTURE_ANIMATION_DONE = 4;

	private final MyGestureListener mGestureListener;

	private final GestureRecognizer mGestureRecognizer;
	private final PositionController mPositionController;

	private Listener mListener;
	private Model mModel;

	private TileImageView mTileView;
	private EdgeView mEdgeView;
	private SynchronizedHandler mHandler;
	private boolean mCancelExtraScalingPending;
	private boolean mWantPictureCenterCallbacks = false;

	private int mDisplayRotation = 0;

	private int mCompensation = 0;
	// This variable prevents us doing snapback until its values goes to 0. This
	// happens if the user gesture is still in progress or we are in a capture
	// animation.
	private int mHolding;

	private static final int HOLD_TOUCH_DOWN = 1;
	private static final int HOLD_CAPTURE_ANIMATION = 2;

	// This is the index of the last deleted item. This is only used as a hint
	// to hide the undo button when we are too far away from the deleted
	// item. The value Integer.MAX_VALUE means there is no such hint.
	private Context mContext;
	private FullPicture mPicture;

	public PhotoView(final ImageViewerGLActivity activity) {
		mTileView = new TileImageView(activity);
		addComponent(mTileView);
		mContext = activity.getAndroidContext();
		mEdgeView = new EdgeView(mContext);
		addComponent(mEdgeView);
		mHandler = new MyHandler(activity.getGLRoot());

		mGestureListener = new MyGestureListener();
		mGestureRecognizer = new GestureRecognizer(mContext, mGestureListener);

		mPositionController = new PositionController(mContext, new PositionController.Listener() {

			@Override
			public void invalidate() {
				PhotoView.this.invalidate();
			}

			@Override
			public boolean isHoldingDown() {
				return (mHolding & HOLD_TOUCH_DOWN) != 0;
			}

			@Override
			public void onAbsorb(final int velocity, final int direction) {
				mEdgeView.onAbsorb(velocity, direction);
			}

			@Override
			public void onPull(final int offset, final int direction) {
				mEdgeView.onPull(offset, direction);
			}
		});
		mPicture = new FullPicture();
	}

	public Rect getPhotoRect() {
		return mPositionController.getPosition();
	}

	public void notifyImageChange() {
		mListener.onCurrentImageUpdated();
		mPicture.reload();
		setPictureSize();
		invalidate();
	}

	public void pause() {
		mPositionController.skipAnimation();
		mTileView.freeTextures();
		for (int i = -0; i <= 0; i++) {
			mPicture.setScreenNail(null);
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Data/Image change notifications
	// //////////////////////////////////////////////////////////////////////////

	public void resume() {
		mTileView.prepareTextures();
		mPositionController.skipToFinalPosition();
	}

	public void setListener(final Listener listener) {
		mListener = listener;
	}

	public void setModel(final Model model) {
		mModel = model;
		mTileView.setModel(mModel);
	}

	public void setOpenAnimationRect(final Rect rect) {
		mPositionController.setOpenAnimationRect(rect);
	}

	public void setSwipingEnabled(final boolean enabled) {
		mGestureListener.setSwipingEnabled(enabled);
	}

	public void setWantPictureCenterCallbacks(final boolean wanted) {
		mWantPictureCenterCallbacks = wanted;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Pictures
	// //////////////////////////////////////////////////////////////////////////

	@Override
	protected void onLayout(final boolean changeSize, final int left, final int top, final int right, final int bottom) {
		final int w = right - left;
		final int h = bottom - top;
		mTileView.layout(0, 0, w, h);
		mEdgeView.layout(0, 0, w, h);

		final GLRoot root = getGLRoot();
		final int displayRotation = root.getDisplayRotation();
		final int compensation = root.getCompensation();
		if (mDisplayRotation != displayRotation || mCompensation != compensation) {
			mDisplayRotation = displayRotation;
			mCompensation = compensation;

		}

		if (changeSize) {
			mPositionController.setViewSize(getWidth(), getHeight());
		}
	}

	@Override
	protected boolean onTouch(final MotionEvent event) {
		mGestureRecognizer.onTouchEvent(event);
		return true;
	}

	@Override
	protected void render(final GLCanvas canvas) {

		// Draw photos from back to front
		final Rect r = mPositionController.getPosition();
		mPicture.draw(canvas, r);

		renderChild(canvas, mEdgeView);

		mPositionController.advanceAnimation();
		checkFocusSwitching();
	}

	private void captureAnimationDone(final int offset) {
		mHolding &= ~HOLD_CAPTURE_ANIMATION;
		if (offset == 1) {
			// Now the capture animation is done, enable the action bar.
			mListener.onActionBarAllowed(true);
			mListener.onActionBarWanted();
		}
		snapback();
	}

	// Runs in GL thread.
	private void checkFocusSwitching() {
		// if (!mFilmMode) return;
		// if (mHandler.hasMessages(MSG_SWITCH_FOCUS)) return;
		// if (switchPosition() != 0) {
		// mHandler.sendEmptyMessage(MSG_SWITCH_FOCUS);
		// }
	}

	// //////////////////////////////////////////////////////////////////////////
	// Gestures Handling
	// //////////////////////////////////////////////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////
	// Rendering
	// //////////////////////////////////////////////////////////////////////////

	private void setPictureSize() {
		mPositionController.setImageSize(mPicture.getSize(), null);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Film mode focus switching
	// //////////////////////////////////////////////////////////////////////////

	private void snapback() {
		mPositionController.snapback();
	}

	private boolean snapToNeighborImage() {
		// Rect r = mPositionController.getPosition(0);
		// int viewW = getWidth();
		// // Setting the move threshold proportional to the width of the view
		// int moveThreshold = viewW / 5 ;
		// int threshold = moveThreshold + gapToSide(r.width(), viewW);
		//
		// // If we have moved the picture a lot, switching.
		// if (viewW - r.right > threshold) {
		// return slideToNextPicture();
		// } else if (r.left > threshold) {
		// return slideToPrevPicture();
		// }
		//
		return false;
	}

	//
	// private boolean slideToNextPicture() {
	// if (mNextBound <= 0) return false;
	// switchToNextImage();
	// mPositionController.startHorizontalSlide();
	// return true;
	// }
	//
	// private boolean slideToPrevPicture() {
	// if (mPrevBound >= 0) return false;
	// switchToPrevImage();
	// mPositionController.startHorizontalSlide();
	// return true;
	// }

	// //////////////////////////////////////////////////////////////////////////
	// Page mode focus switching
	//
	// We slide image to the next one or the previous one in two cases: 1: If
	// the user did a fling gesture with enough velocity. 2 If the user has
	// moved the picture a lot.
	// //////////////////////////////////////////////////////////////////////////

	private boolean swipeImages(final float velocityX, final float velocityY) {
		// if (mFilmMode) return false;
		//
		// // Avoid swiping images if we're possibly flinging to view the
		// // zoomed in picture vertically.
		// PositionController controller = mPositionController;
		// boolean isMinimal = controller.isAtMinimalScale();
		// int edges = controller.getImageAtEdges();
		// if (!isMinimal && Math.abs(velocityY) > Math.abs(velocityX))
		// if ((edges & PositionController.IMAGE_AT_TOP_EDGE) == 0
		// || (edges & PositionController.IMAGE_AT_BOTTOM_EDGE) == 0)
		// return false;
		//
		// // If we are at the edge of the current photo and the sweeping
		// velocity
		// // exceeds the threshold, slide to the next / previous image.
		// if (velocityX < -SWIPE_THRESHOLD && (isMinimal
		// || (edges & PositionController.IMAGE_AT_RIGHT_EDGE) != 0)) {
		// return slideToNextPicture();
		// } else if (velocityX > SWIPE_THRESHOLD && (isMinimal
		// || (edges & PositionController.IMAGE_AT_LEFT_EDGE) != 0)) {
		// return slideToPrevPicture();
		// }
		//
		return false;
	}

	private static int getRotated(final int degree, final int original, final int theother) {
		return degree % 180 == 0 ? original : theother;
	}

	public interface Listener {
		public void onActionBarAllowed(boolean allowed);

		public void onActionBarWanted();

		public void onCurrentImageUpdated();

		public void onPictureCenter();

		public void onSingleTapUp(int x, int y);
	}

	public interface Model extends TileImageView.Model {
		public static final int LOADING_INIT = 0;

		public static final int LOADING_COMPLETE = 1;

		public static final int LOADING_FAIL = 2;

		// When data change happens, we need to decide which MediaItem to focus
		// on.
		//
		// 1. If focus hint path != null, we try to focus on it if we can find
		// it. This is used for undo a deletion, so we can focus on the
		// undeleted item.
		//
		// 2. Otherwise try to focus on the MediaItem that is currently focused,
		// if we can find it.
		//
		// 3. Otherwise try to focus on the previous MediaItem or the next
		// MediaItem, depending on the value of focus hint direction.
		public static final int FOCUS_HINT_NEXT = 0;

		public static final int FOCUS_HINT_PREVIOUS = 1;

		// Returns the rotation for the specified picture.
		public int getImageRotation();

		public int getLoadingState();

		// Returns the media item for the specified picture.
		public MediaItem getMediaItem();

	}

	public static class Size {
		public int width;
		public int height;
	}

	private class MyGestureListener implements GestureRecognizer.Listener {
		private boolean mIgnoreUpEvent = false;
		// If we can change mode for this scale gesture.
		private boolean mCanChangeMode;
		// If we have changed the film mode in this scaling gesture.
		private boolean mModeChanged;
		// If this scaling gesture should be ignored.
		// If we should ignore all gestures other than onSingleTapUp.
		private boolean mIgnoreSwipingGesture;
		// If a scrolling has happened after a down gesture.
		private boolean mScrolledAfterDown;
		// If the first scrolling move is in X direction. In the film mode, X
		// direction scrolling is normal scrolling. but Y direction scrolling is
		// a delete gesture.
		private boolean mFirstScrollX;
		// The accumulated scaling change from a scaling gesture.
		private float mAccScale;
		// If an onFling happened after the last onDown
		private boolean mHadFling;

		@Override
		public boolean onDoubleTap(final float x, final float y) {
			if (mIgnoreSwipingGesture) return true;
			final PositionController controller = mPositionController;
			final float scale = controller.getImageScale();
			// onDoubleTap happened on the second ACTION_DOWN.
			// We need to ignore the next UP event.
			mIgnoreUpEvent = true;
			if (scale <= .75f || controller.isAtMinimalScale()) {
				controller.zoomIn(x, y, Math.max(1.0f, scale * 1.5f));
			} else {
				controller.resetToFullView();
			}
			return true;
		}

		@Override
		public void onDown(final float x, final float y) {

			mModeChanged = false;

			if (mIgnoreSwipingGesture) return;

			mHolding |= HOLD_TOUCH_DOWN;

			mHadFling = false;
			mScrolledAfterDown = false;
		}

		@Override
		public boolean onFling(final float velocityX, final float velocityY) {
			if (mIgnoreSwipingGesture) return true;
			if (mModeChanged) return true;
			if (swipeImages(velocityX, velocityY)) {
				mIgnoreUpEvent = true;
			} else {
				flingImages(velocityX, velocityY);
			}
			mHadFling = true;
			return true;
		}

		@Override
		public boolean onScale(final float focusX, final float focusY, final float scale) {
			if (mIgnoreSwipingGesture) return true;
			if (mModeChanged) return true;
			if (Float.isNaN(scale) || Float.isInfinite(scale)) return false;

			final int outOfRange = mPositionController.scaleBy(scale, focusX, focusY);

			// We wait for a large enough scale change before changing mode.
			// Otherwise we may mistakenly treat a zoom-in gesture as zoom-out
			// or vice versa.
			mAccScale *= scale;
			final boolean largeEnough = mAccScale < 0.97f || mAccScale > 1.03f;

			// If mode changes, we treat this scaling gesture has ended.
			if (mCanChangeMode && largeEnough) {
				if (outOfRange < 0 || outOfRange > 0) {
					stopExtraScalingIfNeeded();

					// Removing the touch down flag allows snapback to happen
					// for film mode change.
					mHolding &= ~HOLD_TOUCH_DOWN;

					// We need to call onScaleEnd() before setting mModeChanged
					// to true.
					onScaleEnd();
					mModeChanged = true;
					return true;
				}
			}

			if (outOfRange != 0) {
				startExtraScalingIfNeeded();
			} else {
				stopExtraScalingIfNeeded();
			}
			return true;
		}

		@Override
		public boolean onScaleBegin(final float focusX, final float focusY) {
			if (mIgnoreSwipingGesture) return true;
			// We ignore the scaling gesture if it is a camera preview.
			mPositionController.beginScale(focusX, focusY);
			// We can change mode if we are in film mode, or we are in page
			// mode and at minimal scale.
			mCanChangeMode = mPositionController.isAtMinimalScale();
			mAccScale = 1f;
			return true;
		}

		@Override
		public void onScaleEnd() {
			if (mIgnoreSwipingGesture) return;
			if (mModeChanged) return;
			mPositionController.endScale();
		}

		@Override
		public boolean onScroll(final float dx, final float dy, final float totalX, final float totalY) {
			if (mIgnoreSwipingGesture) return true;
			if (!mScrolledAfterDown) {
				mScrolledAfterDown = true;
				mFirstScrollX = Math.abs(dx) > Math.abs(dy);
			}

			final int dxi = (int) (-dx + 0.5f);
			final int dyi = (int) (-dy + 0.5f);
			// if (mFilmMode) {
			// if (mFirstScrollX) {
			// mPositionController.scrollFilmX(dxi);
			// } else {
			// if (mTouchBoxIndex == Integer.MAX_VALUE) return true;
			// int newDeltaY = calculateDeltaY(totalY);
			// int d = newDeltaY - mDeltaY;
			// if (d != 0) {
			// mPositionController.scrollFilmY(mTouchBoxIndex, d);
			// mDeltaY = newDeltaY;
			// }
			// }
			// } else {
			mPositionController.scrollPage(dxi, dyi);
			// }
			return true;
		}

		@Override
		public boolean onSingleTapUp(final float x, final float y) {
			// On crespo running Android 2.3.6 (gingerbread), a pinch out
			// gesture results in the
			// following call sequence: onDown(), onUp() and then
			// onSingleTapUp(). The correct
			// sequence for a single-tap-up gesture should be: onDown(),
			// onSingleTapUp() and onUp().
			// The call sequence for a pinch out gesture in JB is: onDown(),
			// then onUp() and there's
			// no onSingleTapUp(). Base on these observations, the following
			// condition is added to
			// filter out the false alarm where onSingleTapUp() is called within
			// a pinch out
			// gesture. The framework fix went into ICS. Refer to b/4588114.
			if (Build.VERSION.SDK_INT < ApiHelper.VERSION_CODES.ICE_CREAM_SANDWICH) {
				if ((mHolding & HOLD_TOUCH_DOWN) == 0) return true;
			}

			// We do this in addition to onUp() because we want the snapback of
			// setFilmMode to happen.
			mHolding &= ~HOLD_TOUCH_DOWN;

			// if (mFilmMode && !mDownInScrolling) {
			// switchToHitPicture((int) (x + 0.5f), (int) (y + 0.5f));
			//
			// // If this is a lock screen photo, let the listener handle the
			// // event. Tapping on lock screen photo should take the user
			// // directly to the lock screen.
			// MediaItem item = mModel.getMediaItem(0);
			// int supported = 0;
			// if (item != null) supported = item.getSupportedOperations();
			// if ((supported & MediaItem.SUPPORT_ACTION) == 0) {
			// mIgnoreUpEvent = true;
			// return true;
			// }
			// }

			if (mListener != null) {
				// Do the inverse transform of the touch coordinates.
				final Matrix m = getGLRoot().getCompensationMatrix();
				final Matrix inv = new Matrix();
				m.invert(inv);
				final float[] pts = new float[] { x, y };
				inv.mapPoints(pts);
				mListener.onSingleTapUp((int) (pts[0] + 0.5f), (int) (pts[1] + 0.5f));
			}
			return true;
		}

		@Override
		public void onUp() {
			if (mIgnoreSwipingGesture) return;

			mHolding &= ~HOLD_TOUCH_DOWN;
			mEdgeView.onRelease();

			// If we scrolled in Y direction far enough, treat it as a delete
			// gesture.
			// if (mFilmMode && mScrolledAfterDown && !mFirstScrollX
			// && mTouchBoxIndex != Integer.MAX_VALUE) {
			// Rect r = mPositionController.getPosition(mTouchBoxIndex);
			// int h = getHeight();
			// if (Math.abs(r.centerY() - h * 0.5f) > 0.4f * h) {
			// int duration = mPositionController
			// .flingFilmY(mTouchBoxIndex, 0);
			// if (duration >= 0) {
			// mPositionController.setPopFromTop(r.centerY() < h * 0.5f);
			// deleteAfterAnimation(duration);
			// }
			// }
			// }

			if (mIgnoreUpEvent) {
				mIgnoreUpEvent = false;
				return;
			}

			if (!(!mHadFling && mFirstScrollX && snapToNeighborImage())) {
				snapback();
			}
		}

		public void setSwipingEnabled(final boolean enabled) {
			mIgnoreSwipingGesture = !enabled;
		}

		private boolean flingImages(final float velocityX, final float velocityY) {
			final int vx = (int) (velocityX + 0.5f);
			final int vy = (int) (velocityY + 0.5f);
			return mPositionController.flingPage(vx, vy);
		}

		private void startExtraScalingIfNeeded() {
			if (!mCancelExtraScalingPending) {
				mHandler.sendEmptyMessageDelayed(MSG_CANCEL_EXTRA_SCALING, 700);
				mPositionController.setExtraScalingRange(true);
				mCancelExtraScalingPending = true;
			}
		}

		private void stopExtraScalingIfNeeded() {
			if (mCancelExtraScalingPending) {
				mHandler.removeMessages(MSG_CANCEL_EXTRA_SCALING);
				mPositionController.setExtraScalingRange(false);
				mCancelExtraScalingPending = false;
			}
		}
	}

	private interface Picture {
		void draw(GLCanvas canvas, Rect r);

		void forceSize(); // called when mCompensation changes

		Size getSize();

		void reload();

		void setScreenNail(ScreenNail s);
	}

	class FullPicture implements Picture {
		private int mRotation;
		private final Size mSize = new Size();

		@Override
		public void draw(final GLCanvas canvas, final Rect r) {
			drawTileView(canvas, r);

			// We want to have the following transitions:
			// (1) Move camera preview out of its place: switch to film mode
			// (2) Move camera preview into its place: switch to page mode
			// The extra mWasCenter check makes sure (1) does not apply if in
			// page mode, we move _to_ the camera preview from another picture.

			// Holdings except touch-down prevent the transitions.
			if ((mHolding & ~HOLD_TOUCH_DOWN) != 0) return;

			if (mWantPictureCenterCallbacks && mPositionController.isCenter()) {
				mListener.onPictureCenter();
			}
		}

		@Override
		public void forceSize() {
			updateSize();
			mPositionController.forceImageSize(mSize);
		}

		@Override
		public Size getSize() {
			return mSize;
		}

		@Override
		public void reload() {
			// mImageWidth and mImageHeight will get updated
			mTileView.notifyModelInvalidated();
			setScreenNail(mModel.getScreenNail());
			updateSize();
		}

		@Override
		public void setScreenNail(final ScreenNail s) {
			mTileView.setScreenNail(s);
		}

		private void drawTileView(final GLCanvas canvas, final Rect r) {
			final float imageScale = mPositionController.getImageScale();
			final int viewW = getWidth();
			final int viewH = getHeight();
			final float cx = r.exactCenterX();
			final float cy = r.exactCenterY();

			canvas.save(GLCanvas.SAVE_FLAG_MATRIX | GLCanvas.SAVE_FLAG_ALPHA);
			// Draw the tile view.
			setTileViewPosition(cx, cy, viewW, viewH, imageScale);
			renderChild(canvas, mTileView);

			// Draw the play video icon and the message.
			canvas.translate((int) (cx + 0.5f), (int) (cy + 0.5f));

			canvas.restore();
		}

		// Set the position of the tile view
		private void setTileViewPosition(final float cx, final float cy, final int viewW, final int viewH,
				final float scale) {
			// Find out the bitmap coordinates of the center of the view
			final int imageW = mPositionController.getImageWidth();
			final int imageH = mPositionController.getImageHeight();
			final int centerX = (int) (imageW / 2f + (viewW / 2f - cx) / scale + 0.5f);
			final int centerY = (int) (imageH / 2f + (viewH / 2f - cy) / scale + 0.5f);

			final int inverseX = imageW - centerX;
			final int inverseY = imageH - centerY;
			int x, y;
			switch (mRotation) {
				case 0:
					x = centerX;
					y = centerY;
					break;
				case 90:
					x = centerY;
					y = inverseX;
					break;
				case 180:
					x = inverseX;
					y = inverseY;
					break;
				case 270:
					x = inverseY;
					y = centerX;
					break;
				default:
					throw new RuntimeException(String.valueOf(mRotation));
			}
			mTileView.setPosition(x, y, scale, mRotation);
		}

		private void updateSize() {
			mRotation = mModel.getImageRotation();

			final int w = mTileView.mImageWidth;
			final int h = mTileView.mImageHeight;
			mSize.width = getRotated(mRotation, w, h);
			mSize.height = getRotated(mRotation, h, w);
		}
	}

	class MyHandler extends SynchronizedHandler {
		public MyHandler(final GLRoot root) {
			super(root);
		}

		@Override
		public void handleMessage(final Message message) {
			switch (message.what) {
				case MSG_CANCEL_EXTRA_SCALING: {
					mGestureRecognizer.cancelScale();
					mPositionController.setExtraScalingRange(false);
					mCancelExtraScalingPending = false;
					break;
				}
				case MSG_CAPTURE_ANIMATION_DONE: {
					// message.arg1 is the offset parameter passed to
					// switchWithCaptureAnimation().
					captureAnimationDone(message.arg1);
					break;
				}
				default:
					throw new AssertionError(message.what);
			}
		}
	}
}
