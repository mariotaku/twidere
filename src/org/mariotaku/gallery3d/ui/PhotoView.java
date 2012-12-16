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

import org.mariotaku.gallery3d.app.GalleryActivity;
import org.mariotaku.gallery3d.common.ApiHelper;
import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.data.MediaItem;
import org.mariotaku.gallery3d.data.MediaObject;
import org.mariotaku.gallery3d.util.RangeArray;
import org.mariotaku.twidere.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;

public class PhotoView extends GLView {
	private static final String TAG = "PhotoView";
	private final int mPlaceholderColor;

	public static final int INVALID_SIZE = -1;
	public static final long INVALID_DATA_VERSION = MediaObject.INVALID_DATA_VERSION;

	private static final int MSG_CANCEL_EXTRA_SCALING = 2;

	private static final int MSG_SWITCH_FOCUS = 3;

	private static final int MSG_CAPTURE_ANIMATION_DONE = 4;

	private static final float DEFAULT_TEXT_SIZE = 20;
	private static float TRANSITION_SCALE_FACTOR = 0.74f;

	// Used to calculate the scaling factor for the card deck effect.
	private final ZInterpolator mScaleInterpolator = new ZInterpolator(0.5f);

	// Used to calculate the alpha factor for the fading animation.
	private final AccelerateInterpolator mAlphaInterpolator = new AccelerateInterpolator(0.9f);

	// We keep this many previous ScreenNails. (also this many next ScreenNails)
	public static final int SCREEN_NAIL_MAX = 3;

	// The picture entries, the valid index is from -SCREEN_NAIL_MAX to
	// SCREEN_NAIL_MAX.
	private final RangeArray<Picture> mPictures = new RangeArray<Picture>(-SCREEN_NAIL_MAX, SCREEN_NAIL_MAX);

	private final Size[] mSizes = new Size[2 * SCREEN_NAIL_MAX + 1];
	private final MyGestureListener mGestureListener;

	private final GestureRecognizer mGestureRecognizer;
	private final PositionController mPositionController;

	private Listener mListener;
	private Model mModel;
	private StringTexture mNoThumbnailText;

	private TileImageView mTileView;
	private EdgeView mEdgeView;
	private SynchronizedHandler mHandler;
	private boolean mCancelExtraScalingPending;
	private boolean mWantPictureCenterCallbacks = false;

	private int mDisplayRotation = 0;

	private int mCompensation = 0;
	private boolean mFullScreenCamera;
	private final Rect mCameraRelativeFrame = new Rect();
	private final Rect mCameraRect = new Rect();
	// [mPrevBound, mNextBound] is the range of index for all pictures in the
	// model, if we assume the index of current focused picture is 0. So if
	// there are some previous pictures, mPrevBound < 0, and if there are some
	// next pictures, mNextBound > 0.
	private int mPrevBound;
	private int mNextBound;
	// This variable prevents us doing snapback until its values goes to 0. This
	// happens if the user gesture is still in progress or we are in a capture
	// animation.
	private int mHolding;

	private static final int HOLD_TOUCH_DOWN = 1;
	private static final int HOLD_CAPTURE_ANIMATION = 2;

	// mTouchBoxIndex is the index of the box that is touched by the down
	// gesture in film mode. The value Integer.MAX_VALUE means no box was
	// touched.
	private int mTouchBoxIndex = Integer.MAX_VALUE;
	// This is the index of the last deleted item. This is only used as a hint
	// to hide the undo button when we are too far away from the deleted
	// item. The value Integer.MAX_VALUE means there is no such hint.
	private Context mContext;

	public PhotoView(final GalleryActivity activity) {
		mTileView = new TileImageView(activity);
		addComponent(mTileView);
		mContext = activity.getAndroidContext();
		mPlaceholderColor = mContext.getResources().getColor(R.color.photo_placeholder);
		mEdgeView = new EdgeView(mContext);
		addComponent(mEdgeView);
		mNoThumbnailText = StringTexture.newInstance(mContext.getString(R.string.no_thumbnail), DEFAULT_TEXT_SIZE,
				Color.WHITE);

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

			@Override
			public void onRelease() {
				mEdgeView.onRelease();
			}
		});
		for (int i = -SCREEN_NAIL_MAX; i <= SCREEN_NAIL_MAX; i++) {
			if (i == 0) {
				mPictures.put(i, new FullPicture());
			} else {
				mPictures.put(i, new ScreenNailPicture(i));
			}
		}
	}

	public PhotoFallbackEffect buildFallbackEffect(final GLView root, final GLCanvas canvas) {
		final Rect location = new Rect();
		Utils.assertTrue(root.getBoundsOf(this, location));

		final Rect fullRect = bounds();
		final PhotoFallbackEffect effect = new PhotoFallbackEffect();
		for (int i = -SCREEN_NAIL_MAX; i <= SCREEN_NAIL_MAX; ++i) {
			final MediaItem item = mModel.getMediaItem(i);
			if (item == null) {
				continue;
			}
			final ScreenNail sc = mModel.getScreenNail(i);
			if (!(sc instanceof TiledScreenNail) || ((TiledScreenNail) sc).isShowingPlaceholder()) {
				continue;
			}

			// Now, sc is BitmapScreenNail and is not showing placeholder
			final Rect rect = new Rect(getPhotoRect(i));
			if (!Rect.intersects(fullRect, rect)) {
				continue;
			}
			rect.offset(location.left, location.top);

			final int width = sc.getWidth();
			final int height = sc.getHeight();

			final int rotation = mModel.getImageRotation(i);
			RawTexture texture;
			if (rotation % 180 == 0) {
				texture = new RawTexture(width, height, true);
				canvas.beginRenderTarget(texture);
				canvas.translate(width / 2f, height / 2f);
			} else {
				texture = new RawTexture(height, width, true);
				canvas.beginRenderTarget(texture);
				canvas.translate(height / 2f, width / 2f);
			}

			canvas.rotate(rotation, 0, 0, 1);
			canvas.translate(-width / 2f, -height / 2f);
			sc.draw(canvas, 0, 0, width, height);
			canvas.endRenderTarget();
			effect.addEntry(item.getPath(), rect, texture);
		}
		return effect;
	}

	public Rect getPhotoRect(final int index) {
		return mPositionController.getPosition(index);
	}

	public void notifyDataChange(final int[] fromIndex, final int prevBound, final int nextBound) {
		mPrevBound = prevBound;
		mNextBound = nextBound;

		// Update mTouchBoxIndex
		if (mTouchBoxIndex != Integer.MAX_VALUE) {
			final int k = mTouchBoxIndex;
			mTouchBoxIndex = Integer.MAX_VALUE;
			for (int i = 0; i < 2 * SCREEN_NAIL_MAX + 1; i++) {
				if (fromIndex[i] == k) {
					mTouchBoxIndex = i - SCREEN_NAIL_MAX;
					break;
				}
			}
		}

		// Update the ScreenNails.
		for (int i = -SCREEN_NAIL_MAX; i <= SCREEN_NAIL_MAX; i++) {
			final Picture p = mPictures.get(i);
			p.reload();
			mSizes[i + SCREEN_NAIL_MAX] = p.getSize();
		}

		// Move the boxes
		mPositionController.moveBox(fromIndex, mPrevBound < 0, mNextBound > 0, false, mSizes);

		for (int i = -SCREEN_NAIL_MAX; i <= SCREEN_NAIL_MAX; i++) {
			setPictureSize(i);
		}

		invalidate();
	}

	public void notifyImageChange(final int index) {
		if (index == 0) {
			mListener.onCurrentImageUpdated();
		}
		mPictures.get(index).reload();
		setPictureSize(index);
		invalidate();
	}

	public void pause() {
		mPositionController.skipAnimation();
		mTileView.freeTextures();
		for (int i = -SCREEN_NAIL_MAX; i <= SCREEN_NAIL_MAX; i++) {
			mPictures.get(i).setScreenNail(null);
		}
	}

	// move to the camera preview and show controls after resume
	public void resetToFirstPicture() {
	}

	// //////////////////////////////////////////////////////////////////////////
	// Data/Image change notifications
	// //////////////////////////////////////////////////////////////////////////

	public void resume() {
		mTileView.prepareTextures();
		mPositionController.skipToFinalPosition();
	}

	public void setCameraRelativeFrame(final Rect frame) {
		mCameraRelativeFrame.set(frame);
		updateCameraRect();
		// Originally we do
		// mPositionController.setConstrainedFrame(mCameraRect);
		// here, but it is moved to a parameter of the setImageSize() call, so
		// it can be updated atomically with the CameraScreenNail's size change.
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

	public void stopScrolling() {
		mPositionController.stopScrolling();
	}

	public boolean switchWithCaptureAnimation(final int offset) {
		final GLRoot root = getGLRoot();
		if (root == null) return false;
		root.lockRenderThread();
		try {
			return switchWithCaptureAnimationLocked(offset);
		} finally {
			root.unlockRenderThread();
		}
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

		updateCameraRect();
		mPositionController.setConstrainedFrame(mCameraRect);
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
		// Check if the camera preview occupies the full screen.
		// boolean full = mPictures.get(0).isCamera()
		// && mPositionController.isCenter()
		// && mPositionController.isAtMinimalScale();
		final boolean full = false;
		if (full != mFullScreenCamera) {
			mFullScreenCamera = full;
			mListener.onFullScreenChanged(full);
		}

		// Determine how many photos we need to draw in addition to the center
		// one.
		int neighbors;
		if (mFullScreenCamera) {
			neighbors = 0;
		} else {
			// In page mode, we draw only one previous/next photo. But if we are
			// doing capture animation, we want to draw all photos.
			final boolean inPageMode = mPositionController.getFilmRatio() == 0f;
			final boolean inCaptureAnimation = (mHolding & HOLD_CAPTURE_ANIMATION) != 0;
			if (inPageMode && !inCaptureAnimation) {
				neighbors = 1;
			} else {
				neighbors = SCREEN_NAIL_MAX;
			}
		}

		// Draw photos from back to front
		for (int i = neighbors; i >= -neighbors; i--) {
			final Rect r = mPositionController.getPosition(i);
			mPictures.get(i).draw(canvas, r);
		}

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

	// Draw the "no thumbnail" message
	private void drawLoadingFailMessage(final GLCanvas canvas) {
		final StringTexture m = mNoThumbnailText;
		m.draw(canvas, -m.getWidth() / 2, -m.getHeight() / 2);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Gestures Handling
	// //////////////////////////////////////////////////////////////////////////

	// Draw a gray placeholder in the specified rectangle.
	private void drawPlaceHolder(final GLCanvas canvas, final Rect r) {
		canvas.fillRect(r.left, r.top, r.width(), r.height(), mPlaceholderColor);
	}

	// Returns the alpha factor in film mode if a picture is not in the center.
	// The 0.03 lower bound is to make the item always visible a bit.
	private float getOffsetAlpha(float offset) {
		offset /= 0.5f;
		final float alpha = offset > 0 ? 1 - offset : 1 + offset;
		return Utils.clamp(alpha, 0.03f, 1f);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Framework events
	// //////////////////////////////////////////////////////////////////////////

	// Maps a scrolling progress value to the alpha factor in the fading
	// animation.
	private float getScrollAlpha(final float scrollProgress) {
		return scrollProgress < 0 ? mAlphaInterpolator.getInterpolation(1 - Math.abs(scrollProgress)) : 1.0f;
	}

	// Maps a scrolling progress value to the scaling factor in the fading
	// animation.
	private float getScrollScale(final float scrollProgress) {
		final float interpolatedProgress = mScaleInterpolator.getInterpolation(Math.abs(scrollProgress));
		final float scale = 1 - interpolatedProgress + interpolatedProgress * TRANSITION_SCALE_FACTOR;
		return scale;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Rendering
	// //////////////////////////////////////////////////////////////////////////

	private void setPictureSize(final int index) {
		final Picture p = mPictures.get(index);
		mPositionController.setImageSize(index, p.getSize(), null);
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

	// Runs in main thread.
	private void switchFocus() {
	}

	private boolean switchWithCaptureAnimationLocked(final int offset) {
		if (mHolding != 0) return true;
		if (offset == 1) {
			if (mNextBound <= 0) return false;
			// Temporary disable action bar until the capture animation is done.
			mListener.onActionBarAllowed(false);
			mPositionController.startCaptureAnimationSlide(-1);
		} else if (offset == -1) {
			if (mPrevBound >= 0) return false;

			// If we are too far away from the first image (so that we don't
			// have all the ScreenNails in-between), we go directly without
			// animation.
			if (mModel.getCurrentIndex() > SCREEN_NAIL_MAX) {
				mPositionController.skipToFinalPosition();
				return true;
			}

			mPositionController.startCaptureAnimationSlide(1);
		} else
			return false;
		mHolding |= HOLD_CAPTURE_ANIMATION;
		final Message m = mHandler.obtainMessage(MSG_CAPTURE_ANIMATION_DONE, offset, 0);
		mHandler.sendMessageDelayed(m, PositionController.CAPTURE_ANIMATION_TIME);
		return true;
	}

	// Update the camera rectangle due to layout change or camera relative frame
	// change.
	private void updateCameraRect() {
		// Get the width and height in framework orientation because the given
		// mCameraRelativeFrame is in that coordinates.
		int w = getWidth();
		int h = getHeight();
		if (mCompensation % 180 != 0) {
			final int tmp = w;
			w = h;
			h = tmp;
		}
		final int l = mCameraRelativeFrame.left;
		final int t = mCameraRelativeFrame.top;
		final int r = mCameraRelativeFrame.right;
		final int b = mCameraRelativeFrame.bottom;

		// Now convert it to the coordinates we are using.
		switch (mCompensation) {
			case 0:
				mCameraRect.set(l, t, r, b);
				break;
			case 90:
				mCameraRect.set(h - b, l, h - t, r);
				break;
			case 180:
				mCameraRect.set(w - r, h - b, w - l, h - t);
				break;
			case 270:
				mCameraRect.set(t, w - r, b, w - l);
				break;
		}

		Log.d(TAG, "compensation = " + mCompensation + ", CameraRelativeFrame = " + mCameraRelativeFrame
				+ ", mCameraRect = " + mCameraRect);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Opening Animation
	// //////////////////////////////////////////////////////////////////////////

	// Returns the scrolling progress value for an object moving out of a
	// view. The progress value measures how much the object has moving out of
	// the view. The object currently displays in [left, right), and the view is
	// at [0, viewWidth].
	//
	// The returned value is negative when the object is moving right, and
	// positive when the object is moving left. The value goes to -1 or 1 when
	// the object just moves out of the view completely. The value is 0 if the
	// object currently fills the view.
	private static float calculateMoveOutProgress(final int left, final int right, final int viewWidth) {
		// w = object width
		// viewWidth = view width
		final int w = right - left;

		// If the object width is smaller than the view width,
		// |....view....|
		// |<-->| progress = -1 when left = viewWidth
		// |<-->| progress = 0 when left = viewWidth / 2 - w / 2
		// |<-->| progress = 1 when left = -w
		if (w < viewWidth) {
			final int zx = viewWidth / 2 - w / 2;
			if (left > zx)
				return -(left - zx) / (float) (viewWidth - zx); // progress =
																// (0, -1]
			else
				return (left - zx) / (float) (-w - zx); // progress = [0, 1]
		}

		// If the object width is larger than the view width,
		// |..view..|
		// |<--------->| progress = -1 when left = viewWidth
		// |<--------->| progress = 0 between left = 0
		// |<--------->| and right = viewWidth
		// |<--------->| progress = 1 when right = 0
		if (left > 0) return -left / (float) viewWidth;

		if (right < viewWidth) return (viewWidth - right) / (float) viewWidth;

		return 0;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Capture Animation
	// //////////////////////////////////////////////////////////////////////////

	private static int getRotated(final int degree, final int original, final int theother) {
		return degree % 180 == 0 ? original : theother;
	}

	// Returns an interpolated value for the page/film transition.
	// When ratio = 0, the result is from.
	// When ratio = 1, the result is to.
	private static float interpolate(final float ratio, final float from, final float to) {
		return from + (to - from) * ratio * ratio;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Card deck effect calculation
	// //////////////////////////////////////////////////////////////////////////

	public interface Listener {
		public void onActionBarAllowed(boolean allowed);

		public void onActionBarWanted();

		public void onCurrentImageUpdated();

		public void onFullScreenChanged(boolean full);

		public void onPictureCenter(boolean isCamera);

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

		public int getCurrentIndex();

		// Returns the rotation for the specified picture.
		public int getImageRotation(int offset);

		// Returns the size for the specified picture. If the size information
		// is
		// not avaiable, width = height = 0.
		public void getImageSize(int offset, Size size);

		public int getLoadingState(int offset);

		// Returns the media item for the specified picture.
		public MediaItem getMediaItem(int offset);

		// This amends the getScreenNail() method of TileImageView.Model to get
		// ScreenNail at previous (negative offset) or next (positive offset)
		// positions. Returns null if the specified ScreenNail is unavailable.
		public ScreenNail getScreenNail(int offset);

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
		private boolean mIgnoreScalingGesture;
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
			mTouchBoxIndex = Integer.MAX_VALUE;
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
			if (mIgnoreScalingGesture) return true;
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
			mIgnoreScalingGesture = false;
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
			if (mIgnoreScalingGesture) return;
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

	private class ScreenNailPicture implements Picture {
		private final int mIndex;
		private int mRotation;
		private ScreenNail mScreenNail;
		private int mLoadingState = Model.LOADING_INIT;
		private final Size mSize = new Size();

		public ScreenNailPicture(final int index) {
			mIndex = index;
		}

		@Override
		public void draw(final GLCanvas canvas, final Rect r) {
			if (mScreenNail == null) {
				// Draw a placeholder rectange if there should be a picture in
				// this position (but somehow there isn't).
				if (mIndex >= mPrevBound && mIndex <= mNextBound) {
					drawPlaceHolder(canvas, r);
				}
				return;
			}
			final int w = getWidth();
			final int h = getHeight();
			if (r.left >= w || r.right <= 0 || r.top >= h || r.bottom <= 0) {
				mScreenNail.noDraw();
				return;
			}

			final int cx = r.centerX();
			final int cy = r.centerY();
			canvas.save(GLCanvas.SAVE_FLAG_MATRIX | GLCanvas.SAVE_FLAG_ALPHA);
			canvas.translate(cx, cy);
			if (mRotation != 0) {
				canvas.rotate(mRotation, 0, 0, 1);
			}
			final int drawW = getRotated(mRotation, r.width(), r.height());
			final int drawH = getRotated(mRotation, r.height(), r.width());
			mScreenNail.draw(canvas, -drawW / 2, -drawH / 2, drawW, drawH);
			if (isScreenNailAnimating()) {
				invalidate();
			}
			if (mLoadingState == Model.LOADING_FAIL) {
				drawLoadingFailMessage(canvas);
			}
			canvas.restore();
		}

		@Override
		public void forceSize() {
			updateSize();
			mPositionController.forceImageSize(mIndex, mSize);
		}

		@Override
		public Size getSize() {
			return mSize;
		}

		@Override
		public void reload() {
			mLoadingState = mModel.getLoadingState(mIndex);
			setScreenNail(mModel.getScreenNail(mIndex));
			updateSize();
		}

		@Override
		public void setScreenNail(final ScreenNail s) {
			mScreenNail = s;
		}

		private boolean isScreenNailAnimating() {
			return mScreenNail instanceof TiledScreenNail && ((TiledScreenNail) mScreenNail).isAnimating();
		}

		private void updateSize() {
			mRotation = mModel.getImageRotation(mIndex);

			if (mScreenNail != null) {
				mSize.width = mScreenNail.getWidth();
				mSize.height = mScreenNail.getHeight();
			} else {
				// If we don't have ScreenNail available, we can still try to
				// get the size information of it.
				mModel.getImageSize(mIndex, mSize);
			}

			final int w = mSize.width;
			final int h = mSize.height;
			mSize.width = getRotated(mRotation, w, h);
			mSize.height = getRotated(mRotation, h, w);
		}

	}

	// //////////////////////////////////////////////////////////////////////////
	// Simple public utilities
	// //////////////////////////////////////////////////////////////////////////

	// This interpolator emulates the rate at which the perceived scale of an
	// object changes as its distance from a camera increases. When this
	// interpolator is applied to a scale animation on a view, it evokes the
	// sense that the object is shrinking due to moving away from the camera.
	private static class ZInterpolator {
		private final float focalLength;

		public ZInterpolator(final float foc) {
			focalLength = foc;
		}

		public float getInterpolation(final float input) {
			return (1.0f - focalLength / (focalLength + input)) / (1.0f - focalLength / (focalLength + 1.0f));
		}
	}

	class FullPicture implements Picture {
		private int mRotation;
		private int mLoadingState = Model.LOADING_INIT;
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
				mListener.onPictureCenter(false);
			}
		}

		@Override
		public void forceSize() {
			updateSize();
			mPositionController.forceImageSize(0, mSize);
		}

		@Override
		public Size getSize() {
			return mSize;
		}

		@Override
		public void reload() {
			// mImageWidth and mImageHeight will get updated
			mTileView.notifyModelInvalidated();

			mLoadingState = mModel.getLoadingState(0);
			setScreenNail(mModel.getScreenNail(0));
			updateSize();
		}

		@Override
		public void setScreenNail(final ScreenNail s) {
			mTileView.setScreenNail(s);
		}

		private void drawTileView(final GLCanvas canvas, final Rect r) {
			float imageScale = mPositionController.getImageScale();
			final int viewW = getWidth();
			final int viewH = getHeight();
			float cx = r.exactCenterX();
			final float cy = r.exactCenterY();
			float scale = 1f; // the scaling factor due to card effect

			canvas.save(GLCanvas.SAVE_FLAG_MATRIX | GLCanvas.SAVE_FLAG_ALPHA);
			final float filmRatio = mPositionController.getFilmRatio();
			final boolean wantsCardEffect = false;
			final boolean wantsOffsetEffect = false;
			// boolean wantsCardEffect = CARD_EFFECT && !mIsCamera
			// && filmRatio != 1f && !mPictures.get(-1).isCamera()
			// && !mPositionController.inOpeningAnimation();
			// boolean wantsOffsetEffect = OFFSET_EFFECT && mIsDeletable
			// && filmRatio == 1f && r.centerY() != viewH / 2;
			if (wantsCardEffect) {
				// Calculate the move-out progress value.
				final int left = r.left;
				final int right = r.right;
				float progress = calculateMoveOutProgress(left, right, viewW);
				progress = Utils.clamp(progress, -1f, 1f);

				// We only want to apply the fading animation if the scrolling
				// movement is to the right.
				if (progress < 0) {
					scale = getScrollScale(progress);
					float alpha = getScrollAlpha(progress);
					scale = interpolate(filmRatio, scale, 1f);
					alpha = interpolate(filmRatio, alpha, 1f);

					imageScale *= scale;
					canvas.multiplyAlpha(alpha);

					float cxPage; // the cx value in page mode
					if (right - left <= viewW) {
						// If the picture is narrower than the view, keep it at
						// the center of the view.
						cxPage = viewW / 2f;
					} else {
						// If the picture is wider than the view (it's
						// zoomed-in), keep the left edge of the object align
						// the the left edge of the view.
						cxPage = (right - left) * scale / 2f;
					}
					cx = interpolate(filmRatio, cxPage, cx);
				}
			} else if (wantsOffsetEffect) {
				final float offset = (float) (r.centerY() - viewH / 2) / viewH;
				final float alpha = getOffsetAlpha(offset);
				canvas.multiplyAlpha(alpha);
			}

			// Draw the tile view.
			setTileViewPosition(cx, cy, viewW, viewH, imageScale);
			renderChild(canvas, mTileView);

			// Draw the play video icon and the message.
			canvas.translate((int) (cx + 0.5f), (int) (cy + 0.5f));
			if (mLoadingState == Model.LOADING_FAIL) {
				drawLoadingFailMessage(canvas);
			}

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
			mRotation = mModel.getImageRotation(0);

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
				case MSG_SWITCH_FOCUS: {
					switchFocus();
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
