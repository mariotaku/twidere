/*
 * Copyright (C) 2011 The Android Open Source Project
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

import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.ui.PhotoView.Size;
import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.gallery3d.util.RangeArray;
import org.mariotaku.gallery3d.util.RangeIntArray;

import android.content.Context;
import android.graphics.Rect;
import android.util.Log;
import android.widget.Scroller;

class PositionController {
	private static final String TAG = "PositionController";

	public static final int IMAGE_AT_LEFT_EDGE = 1;
	public static final int IMAGE_AT_RIGHT_EDGE = 2;
	public static final int IMAGE_AT_TOP_EDGE = 4;
	public static final int IMAGE_AT_BOTTOM_EDGE = 8;

	public static final int CAPTURE_ANIMATION_TIME = 700;
	public static final int SNAPBACK_ANIMATION_TIME = 600;

	// Special values for animation time.
	private static final long NO_ANIMATION = -1;
	private static final long LAST_ANIMATION = -2;

	private static final int ANIM_KIND_NONE = -1;
	private static final int ANIM_KIND_SCROLL = 0;
	private static final int ANIM_KIND_SCALE = 1;
	private static final int ANIM_KIND_SNAPBACK = 2;
	private static final int ANIM_KIND_SLIDE = 3;
	private static final int ANIM_KIND_ZOOM = 4;
	private static final int ANIM_KIND_OPENING = 5;
	private static final int ANIM_KIND_FLING = 6;
	private static final int ANIM_KIND_FLING_X = 7;
	private static final int ANIM_KIND_DELETE = 8;
	private static final int ANIM_KIND_CAPTURE = 9;

	// Animation time in milliseconds. The order must match ANIM_KIND_* above.
	//
	// The values for ANIM_KIND_FLING_X does't matter because we use
	// mFilmScroller.isFinished() to decide when to stop. We set it to 0 so it's
	// faster for Animatable.advanceAnimation() to calculate the progress
	// (always 1).
	private static final int ANIM_TIME[] = { 0, // ANIM_KIND_SCROLL
			0, // ANIM_KIND_SCALE
			SNAPBACK_ANIMATION_TIME, // ANIM_KIND_SNAPBACK
			400, // ANIM_KIND_SLIDE
			300, // ANIM_KIND_ZOOM
			300, // ANIM_KIND_OPENING
			0, // ANIM_KIND_FLING (the duration is calculated dynamically)
			0, // ANIM_KIND_FLING_X (see the comment above)
			0, // ANIM_KIND_DELETE (the duration is calculated dynamically)
			CAPTURE_ANIMATION_TIME, // ANIM_KIND_CAPTURE
	};

	// We try to scale up the image to fill the screen. But in order not to
	// scale too much for small icons, we limit the max up-scaling factor here.
	private static final float SCALE_LIMIT = 4;

	// For user's gestures, we give a temporary extra scaling range which goes
	// above or below the usual scaling limits.
	private static final float SCALE_MIN_EXTRA = 0.7f;
	private static final float SCALE_MAX_EXTRA = 1.4f;

	// Setting this true makes the extra scaling range permanent (until this is
	// set to false again).
	private boolean mExtraScalingRange = false;

	// In addition to the focused box (index == 0). We also keep information
	// about this many boxes on each side.
	private static final int BOX_MAX = PhotoView.SCREEN_NAIL_MAX;
	private static final int[] CENTER_OUT_INDEX = new int[2 * BOX_MAX + 1];

	private static final int IMAGE_GAP = GalleryUtils.dpToPixel(16);
	private static final int HORIZONTAL_SLACK = GalleryUtils.dpToPixel(12);

	private final Listener mListener;
	private volatile Rect mOpenAnimationRect;

	// Use a large enough value, so we won't see the gray shadow in the
	// beginning.
	private int mViewW = 1200;
	private int mViewH = 1200;

	// A scaling gesture is in progress.
	private boolean mInScale;
	// The focus point of the scaling gesture, relative to the center of the
	// picture in bitmap pixels.
	private float mFocusX, mFocusY;

	// whether there is a previous/next picture.
	private boolean mHasPrev, mHasNext;

	// This is used by the fling animation (page mode).
	private final FlingScroller mPageScroller;

	// This is used by the fling animation (film mode).
	private final Scroller mFilmScroller;

	// The bound of the stable region that the focused box can stay, see the
	// comments above calculateStableBound() for details.
	private int mBoundLeft, mBoundRight, mBoundTop, mBoundBottom;

	// Constrained frame is a rectangle that the focused box should fit into if
	// it is constrained. It has two effects:
	//
	// (1) In page mode, if the focused box is constrained, scaling for the
	// focused box is adjusted to fit into the constrained frame, instead of the
	// whole view.
	//
	// (2) In page mode, if the focused box is constrained, the mPlatform's
	// default center (mDefaultX/Y) is moved to the center of the constrained
	// frame, instead of the view center.
	//
	private final Rect mConstrainedFrame = new Rect();

	// Whether the focused box is constrained.
	//
	// Our current program's first call to moveBox() sets constrained = true, so
	// we set the initial value of this variable to true, and we will not see
	// see unwanted transition animation.
	private boolean mConstrained = true;

	//
	// ___________________________________________________________
	// | _____ _____ _____ _____ _____ |
	// | | | | | | | | | | | |
	// | | Box | | Box | | Box*| | Box | | Box | |
	// | |_____|.....|_____|.....|_____|.....|_____|.....|_____| |
	// | Gap Gap Gap Gap |
	// |___________________________________________________________|
	//
	// <-- Platform -->
	//
	// The focused box (Box*) centers at mPlatform's (mCurrentX, mCurrentY)

	private final Platform mPlatform = new Platform();
	private final RangeArray<Box> mBoxes = new RangeArray<Box>(-BOX_MAX, BOX_MAX);
	// The gap at the right of a Box i is at index i. The gap at the left of a
	// Box i is at index i - 1.
	private final RangeArray<Gap> mGaps = new RangeArray<Gap>(-BOX_MAX, BOX_MAX - 1);
	private final FilmRatio mFilmRatio = new FilmRatio();

	// These are only used during moveBox().
	private final RangeArray<Box> mTempBoxes = new RangeArray<Box>(-BOX_MAX, BOX_MAX);
	private final RangeArray<Gap> mTempGaps = new RangeArray<Gap>(-BOX_MAX, BOX_MAX - 1);

	// The output of the PositionController. Available through getPosition().
	private final RangeArray<Rect> mRects = new RangeArray<Rect>(-BOX_MAX, BOX_MAX);

	// The direction of a new picture should appear. New pictures pop from top
	// if this value is true, or from bottom if this value is false.
	boolean mPopFromTop;

	static {
		// Initialize the CENTER_OUT_INDEX array.
		// The array maps 0, 1, 2, 3, 4, ..., 2 * BOX_MAX
		// to 0, 1, -1, 2, -2, ..., BOX_MAX, -BOX_MAX
		for (int i = 0; i < CENTER_OUT_INDEX.length; i++) {
			int j = (i + 1) / 2;
			if ((i & 1) == 0) {
				j = -j;
			}
			CENTER_OUT_INDEX[i] = j;
		}
	}

	public PositionController(final Context context, final Listener listener) {
		mListener = listener;
		mPageScroller = new FlingScroller();
		mFilmScroller = new Scroller(context, null, false);

		// Initialize the areas.
		initPlatform();
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			mBoxes.put(i, new Box());
			initBox(i);
			mRects.put(i, new Rect());
		}
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			mGaps.put(i, new Gap());
			initGap(i);
		}
	}

	public void advanceAnimation() {
		boolean changed = false;
		changed |= mPlatform.advanceAnimation();
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			changed |= mBoxes.get(i).advanceAnimation();
		}
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			changed |= mGaps.get(i).advanceAnimation();
		}
		changed |= mFilmRatio.advanceAnimation();
		if (changed) {
			redraw();
		}
	}

	public void beginScale(float focusX, float focusY) {
		focusX -= mViewW / 2;
		focusY -= mViewH / 2;
		final Box b = mBoxes.get(0);
		final Platform p = mPlatform;
		mInScale = true;
		mFocusX = (int) ((focusX - p.mCurrentX) / b.mCurrentScale + 0.5f);
		mFocusY = (int) ((focusY - b.mCurrentY) / b.mCurrentScale + 0.5f);
	}

	public void endScale() {
		mInScale = false;
		snapAndRedraw();
	}

	public boolean flingPage(int velocityX, int velocityY) {
		final Box b = mBoxes.get(0);
		final Platform p = mPlatform;

		// We only want to do fling when the picture is zoomed-in.
		if (viewWiderThanScaledImage(b.mCurrentScale) && viewTallerThanScaledImage(b.mCurrentScale)) return false;

		// We only allow flinging in the directions where it won't go over the
		// picture.
		final int edges = getImageAtEdges();
		if (velocityX > 0 && (edges & IMAGE_AT_LEFT_EDGE) != 0 || velocityX < 0 && (edges & IMAGE_AT_RIGHT_EDGE) != 0) {
			velocityX = 0;
		}
		if (velocityY > 0 && (edges & IMAGE_AT_TOP_EDGE) != 0 || velocityY < 0 && (edges & IMAGE_AT_BOTTOM_EDGE) != 0) {
			velocityY = 0;
		}

		if (velocityX == 0 && velocityY == 0) return false;

		mPageScroller.fling(p.mCurrentX, b.mCurrentY, velocityX, velocityY, mBoundLeft, mBoundRight, mBoundTop,
				mBoundBottom);
		final int targetX = mPageScroller.getFinalX();
		final int targetY = mPageScroller.getFinalY();
		ANIM_TIME[ANIM_KIND_FLING] = mPageScroller.getDuration();
		return startAnimation(targetX, targetY, b.mCurrentScale, ANIM_KIND_FLING);
	}

	public void forceImageSize(final int index, final Size s) {
		if (s.width == 0 || s.height == 0) return;
		final Box b = mBoxes.get(index);
		b.mImageW = s.width;
		b.mImageH = s.height;
		return;
	}

	public float getFilmRatio() {
		return mFilmRatio.mCurrentRatio;
	}

	public int getImageAtEdges() {
		final Box b = mBoxes.get(0);
		final Platform p = mPlatform;
		calculateStableBound(b.mCurrentScale);
		int edges = 0;
		if (p.mCurrentX <= mBoundLeft) {
			edges |= IMAGE_AT_RIGHT_EDGE;
		}
		if (p.mCurrentX >= mBoundRight) {
			edges |= IMAGE_AT_LEFT_EDGE;
		}
		if (b.mCurrentY <= mBoundTop) {
			edges |= IMAGE_AT_BOTTOM_EDGE;
		}
		if (b.mCurrentY >= mBoundBottom) {
			edges |= IMAGE_AT_TOP_EDGE;
		}
		return edges;
	}

	public int getImageHeight() {
		final Box b = mBoxes.get(0);
		return b.mImageH;
	}

	public float getImageScale() {
		final Box b = mBoxes.get(0);
		return b.mCurrentScale;
	}

	public int getImageWidth() {
		final Box b = mBoxes.get(0);
		return b.mImageW;
	}

	// Returns the position of a box.
	public Rect getPosition(final int index) {
		return mRects.get(index);
	}

	public boolean hasDeletingBox() {
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			if (mBoxes.get(i).mAnimationKind == ANIM_KIND_DELETE) return true;
		}
		return false;
	}

	// Returns the index of the box which contains the given point (x, y)
	// Returns Integer.MAX_VALUE if there is no hit. There may be more than
	// one box contains the given point, and we want to give priority to the
	// one closer to the focused index (0).
	public int hitTest(final int x, final int y) {
		for (int i = 0; i < 2 * BOX_MAX + 1; i++) {
			final int j = CENTER_OUT_INDEX[i];
			final Rect r = mRects.get(j);
			if (r.contains(x, y)) return j;
		}

		return Integer.MAX_VALUE;
	}

	public boolean inOpeningAnimation() {
		return mPlatform.mAnimationKind == ANIM_KIND_OPENING && mPlatform.mAnimationStartTime != NO_ANIMATION
				|| mBoxes.get(0).mAnimationKind == ANIM_KIND_OPENING
				&& mBoxes.get(0).mAnimationStartTime != NO_ANIMATION;
	}

	public boolean isAtMinimalScale() {
		final Box b = mBoxes.get(0);
		return isAlmostEqual(b.mCurrentScale, b.mScaleMin);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Start an animations for the focused box
	// //////////////////////////////////////////////////////////////////////////

	public boolean isCenter() {
		final Box b = mBoxes.get(0);
		return mPlatform.mCurrentX == mPlatform.mDefaultX && b.mCurrentY == 0;
	}

	public boolean isScrolling() {
		return mPlatform.mAnimationStartTime != NO_ANIMATION && mPlatform.mCurrentX != mPlatform.mToX;
	}

	// Move the boxes: it may indicate focus change, box deleted, box appearing,
	// box reordered, etc.
	//
	// Each element in the fromIndex array indicates where each box was in the
	// old array. If the value is Integer.MAX_VALUE (pictured as N below), it
	// means the box is new.
	//
	// For example:
	// N N N N N N N -- all new boxes
	// -3 -2 -1 0 1 2 3 -- nothing changed
	// -2 -1 0 1 2 3 N -- focus goes to the next box
	// N -3 -2 -1 0 1 2 -- focus goes to the previous box
	// -3 -2 -1 1 2 3 N -- the focused box was deleted.
	//
	// hasPrev/hasNext indicates if there are previous/next boxes for the
	// focused box. constrained indicates whether the focused box should be put
	// into the constrained frame.
	public void moveBox(final int fromIndex[], final boolean hasPrev, final boolean hasNext, final boolean constrained,
			final Size[] sizes) {
		// debugMoveBox(fromIndex);
		mHasPrev = hasPrev;
		mHasNext = hasNext;

		final RangeIntArray from = new RangeIntArray(fromIndex, -BOX_MAX, BOX_MAX);

		// 1. Get the absolute X coordinates for the boxes.
		layoutAndSetPosition();
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			final Box b = mBoxes.get(i);
			final Rect r = mRects.get(i);
			b.mAbsoluteX = r.centerX() - mViewW / 2;
		}

		// 2. copy boxes and gaps to temporary storage.
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			mTempBoxes.put(i, mBoxes.get(i));
			mBoxes.put(i, null);
		}
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			mTempGaps.put(i, mGaps.get(i));
			mGaps.put(i, null);
		}

		// 3. move back boxes that are used in the new array.
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			final int j = from.get(i);
			if (j == Integer.MAX_VALUE) {
				continue;
			}
			mBoxes.put(i, mTempBoxes.get(j));
			mTempBoxes.put(j, null);
		}

		// 4. move back gaps if both boxes around it are kept together.
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			final int j = from.get(i);
			if (j == Integer.MAX_VALUE) {
				continue;
			}
			final int k = from.get(i + 1);
			if (k == Integer.MAX_VALUE) {
				continue;
			}
			if (j + 1 == k) {
				mGaps.put(i, mTempGaps.get(j));
				mTempGaps.put(j, null);
			}
		}

		// 5. recycle the boxes that are not used in the new array.
		int k = -BOX_MAX;
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			if (mBoxes.get(i) != null) {
				continue;
			}
			while (mTempBoxes.get(k) == null) {
				k++;
			}
			mBoxes.put(i, mTempBoxes.get(k++));
			initBox(i, sizes[i + BOX_MAX]);
		}

		// 6. Now give the recycled box a reasonable absolute X position.
		//
		// First try to find the first and the last box which the absolute X
		// position is known.
		int first, last;
		for (first = -BOX_MAX; first <= BOX_MAX; first++) {
			if (from.get(first) != Integer.MAX_VALUE) {
				break;
			}
		}
		for (last = BOX_MAX; last >= -BOX_MAX; last--) {
			if (from.get(last) != Integer.MAX_VALUE) {
				break;
			}
		}
		// If there is no box has known X position at all, make the focused one
		// as known.
		if (first > BOX_MAX) {
			mBoxes.get(0).mAbsoluteX = mPlatform.mCurrentX;
			first = last = 0;
		}
		// Now for those boxes between first and last, assign their position to
		// align to the previous box or the next box with known position. For
		// the boxes before first or after last, we will use a new default gap
		// size below.

		// Align to the previous box
		for (int i = Math.max(0, first + 1); i < last; i++) {
			if (from.get(i) != Integer.MAX_VALUE) {
				continue;
			}
			final Box a = mBoxes.get(i - 1);
			final Box b = mBoxes.get(i);
			final int wa = widthOf(a);
			final int wb = widthOf(b);
			b.mAbsoluteX = a.mAbsoluteX + wa - wa / 2 + wb / 2 + getDefaultGapSize(i);
			if (mPopFromTop) {
				b.mCurrentY = -(mViewH / 2 + heightOf(b) / 2);
			} else {
				b.mCurrentY = mViewH / 2 + heightOf(b) / 2;
			}
		}

		// Align to the next box
		for (int i = Math.min(-1, last - 1); i > first; i--) {
			if (from.get(i) != Integer.MAX_VALUE) {
				continue;
			}
			final Box a = mBoxes.get(i + 1);
			final Box b = mBoxes.get(i);
			final int wa = widthOf(a);
			final int wb = widthOf(b);
			b.mAbsoluteX = a.mAbsoluteX - wa / 2 - (wb - wb / 2) - getDefaultGapSize(i);
			if (mPopFromTop) {
				b.mCurrentY = -(mViewH / 2 + heightOf(b) / 2);
			} else {
				b.mCurrentY = mViewH / 2 + heightOf(b) / 2;
			}
		}

		// 7. recycle the gaps that are not used in the new array.
		k = -BOX_MAX;
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			if (mGaps.get(i) != null) {
				continue;
			}
			while (mTempGaps.get(k) == null) {
				k++;
			}
			mGaps.put(i, mTempGaps.get(k++));
			final Box a = mBoxes.get(i);
			final Box b = mBoxes.get(i + 1);
			final int wa = widthOf(a);
			final int wb = widthOf(b);
			if (i >= first && i < last) {
				final int g = b.mAbsoluteX - a.mAbsoluteX - wb / 2 - (wa - wa / 2);
				initGap(i, g);
			} else {
				initGap(i);
			}
		}

		// 8. calculate the new absolute X coordinates for those box before
		// first or after last.
		for (int i = first - 1; i >= -BOX_MAX; i--) {
			final Box a = mBoxes.get(i + 1);
			final Box b = mBoxes.get(i);
			final int wa = widthOf(a);
			final int wb = widthOf(b);
			final Gap g = mGaps.get(i);
			b.mAbsoluteX = a.mAbsoluteX - wa / 2 - (wb - wb / 2) - g.mCurrentGap;
		}

		for (int i = last + 1; i <= BOX_MAX; i++) {
			final Box a = mBoxes.get(i - 1);
			final Box b = mBoxes.get(i);
			final int wa = widthOf(a);
			final int wb = widthOf(b);
			final Gap g = mGaps.get(i - 1);
			b.mAbsoluteX = a.mAbsoluteX + wa - wa / 2 + wb / 2 + g.mCurrentGap;
		}

		// 9. offset the Platform position
		final int dx = mBoxes.get(0).mAbsoluteX - mPlatform.mCurrentX;
		mPlatform.mCurrentX += dx;
		mPlatform.mFromX += dx;
		mPlatform.mToX += dx;
		mPlatform.mFlingOffset += dx;

		if (mConstrained != constrained) {
			mConstrained = constrained;
			mPlatform.updateDefaultXY();
			updateScaleAndGapLimit();
		}

		snapAndRedraw();
	}

	public void resetToFullView() {
		final Box b = mBoxes.get(0);
		startAnimation(mPlatform.mDefaultX, 0, b.mScaleMin, ANIM_KIND_ZOOM);
	}

	// Scales the image by the given factor.
	// Returns an out-of-range indicator:
	// 1 if the intended scale is too large for the stable range.
	// 0 if the intended scale is in the stable range.
	// -1 if the intended scale is too small for the stable range.
	public int scaleBy(float s, float focusX, float focusY) {
		focusX -= mViewW / 2;
		focusY -= mViewH / 2;
		final Box b = mBoxes.get(0);

		// We want to keep the focus point (on the bitmap) the same as when we
		// begin the scale gesture, that is,
		//
		// (focusX' - currentX') / scale' = (focusX - currentX) / scale
		//
		s = b.clampScale(s * getTargetScale(b));
		final int x = (int) (focusX - s * mFocusX + 0.5f);
		final int y = (int) (focusY - s * mFocusY + 0.5f);
		startAnimation(x, y, s, ANIM_KIND_SCALE);
		if (s < b.mScaleMin) return -1;
		if (s > b.mScaleMax) return 1;
		return 0;
	}

	public void scrollFilmX(final int dx) {
		if (!canScroll()) return;

		final Box b = mBoxes.get(0);
		final Platform p = mPlatform;

		// Only allow scrolling when we are not currently in an animation or we
		// are in some animation with can be interrupted.
		if (b.mAnimationStartTime != NO_ANIMATION) {
			switch (b.mAnimationKind) {
				case ANIM_KIND_SCROLL:
				case ANIM_KIND_FLING:
				case ANIM_KIND_FLING_X:
					break;
				default:
					return;
			}
		}

		int x = p.mCurrentX + dx;

		// Horizontal direction: we show the edge effect when the scrolling
		// tries to go left of the first image or go right of the last image.
		x -= mPlatform.mDefaultX;
		if (!mHasPrev && x > 0) {
			mListener.onPull(x, EdgeView.LEFT);
			x = 0;
		} else if (!mHasNext && x < 0) {
			mListener.onPull(-x, EdgeView.RIGHT);
			x = 0;
		}
		x += mPlatform.mDefaultX;
		startAnimation(x, b.mCurrentY, b.mCurrentScale, ANIM_KIND_SCROLL);
	}

	public void scrollFilmY(final int boxIndex, final int dy) {
		if (!canScroll()) return;

		final Box b = mBoxes.get(boxIndex);
		final int y = b.mCurrentY + dy;
		b.doAnimation(y, b.mCurrentScale, ANIM_KIND_SCROLL);
		redraw();
	}

	public void scrollPage(final int dx, final int dy) {
		if (!canScroll()) return;

		final Box b = mBoxes.get(0);
		final Platform p = mPlatform;

		calculateStableBound(b.mCurrentScale);

		int x = p.mCurrentX + dx;
		int y = b.mCurrentY + dy;

		// Vertical direction: If we have space to move in the vertical
		// direction, we show the edge effect when scrolling reaches the edge.
		if (mBoundTop != mBoundBottom) {
			if (y < mBoundTop) {
				mListener.onPull(mBoundTop - y, EdgeView.BOTTOM);
			} else if (y > mBoundBottom) {
				mListener.onPull(y - mBoundBottom, EdgeView.TOP);
			}
		}

		y = Utils.clamp(y, mBoundTop, mBoundBottom);

		// Horizontal direction: we show the edge effect when the scrolling
		// tries to go left of the first image or go right of the last image.
		if (!mHasPrev && x > mBoundRight) {
			final int pixels = x - mBoundRight;
			mListener.onPull(pixels, EdgeView.LEFT);
			x = mBoundRight;
		} else if (!mHasNext && x < mBoundLeft) {
			final int pixels = mBoundLeft - x;
			mListener.onPull(pixels, EdgeView.RIGHT);
			x = mBoundLeft;
		}

		startAnimation(x, y, b.mCurrentScale, ANIM_KIND_SCROLL);
	}

	public void setConstrainedFrame(final Rect cFrame) {
		if (mConstrainedFrame.equals(cFrame)) return;
		mConstrainedFrame.set(cFrame);
		mPlatform.updateDefaultXY();
		updateScaleAndGapLimit();
		snapAndRedraw();
	}

	public void setExtraScalingRange(final boolean enabled) {
		if (mExtraScalingRange == enabled) return;
		mExtraScalingRange = enabled;
		if (!enabled) {
			snapAndRedraw();
		}
	}

	public void setFilmMode(final boolean enabled) {
		mPlatform.updateDefaultXY();
		updateScaleAndGapLimit();
		stopAnimation();
		snapAndRedraw();
	}

	public void setImageSize(final int index, final Size s, final Rect cFrame) {
		if (s.width == 0 || s.height == 0) return;

		boolean needUpdate = false;
		if (cFrame != null && !mConstrainedFrame.equals(cFrame)) {
			mConstrainedFrame.set(cFrame);
			mPlatform.updateDefaultXY();
			needUpdate = true;
		}
		needUpdate |= setBoxSize(index, s.width, s.height, false);

		if (!needUpdate) return;
		updateScaleAndGapLimit();
		snapAndRedraw();
	}

	public void setOpenAnimationRect(final Rect r) {
		mOpenAnimationRect = r;
	}

	public void setPopFromTop(final boolean top) {
		mPopFromTop = top;
	}

	public void setViewSize(final int viewW, final int viewH) {
		if (viewW == mViewW && viewH == mViewH) return;

		final boolean wasMinimal = isAtMinimalScale();

		mViewW = viewW;
		mViewH = viewH;
		initPlatform();

		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			setBoxSize(i, viewW, viewH, true);
		}

		updateScaleAndGapLimit();

		// If the focused box was at minimal scale, we try to make it the
		// minimal scale under the new view size.
		if (wasMinimal) {
			final Box b = mBoxes.get(0);
			b.mCurrentScale = b.mScaleMin;
		}

		// If we have the opening animation, do it. Otherwise go directly to the
		// right position.
		if (!startOpeningAnimationIfNeeded()) {
			skipToFinalPosition();
		}
	}

	public void skipAnimation() {
		if (mPlatform.mAnimationStartTime != NO_ANIMATION) {
			mPlatform.mCurrentX = mPlatform.mToX;
			mPlatform.mCurrentY = mPlatform.mToY;
			mPlatform.mAnimationStartTime = NO_ANIMATION;
		}
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			final Box b = mBoxes.get(i);
			if (b.mAnimationStartTime == NO_ANIMATION) {
				continue;
			}
			b.mCurrentY = b.mToY;
			b.mCurrentScale = b.mToScale;
			b.mAnimationStartTime = NO_ANIMATION;
		}
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			final Gap g = mGaps.get(i);
			if (g.mAnimationStartTime == NO_ANIMATION) {
				continue;
			}
			g.mCurrentGap = g.mToGap;
			g.mAnimationStartTime = NO_ANIMATION;
		}
		redraw();
	}

	public void skipToFinalPosition() {
		stopAnimation();
		snapAndRedraw();
		skipAnimation();
	}

	public void snapback() {
		snapAndRedraw();
	}

	// Slide the focused box to the center of the view with the capture
	// animation. In addition to the sliding, the animation will also scale the
	// the focused box, the specified neighbor box, and the gap between the
	// two. The specified offset should be 1 or -1.
	public void startCaptureAnimationSlide(final int offset) {
		final Box b = mBoxes.get(0);
		final Box n = mBoxes.get(offset); // the neighbor box
		final Gap g = mGaps.get(offset); // the gap between the two boxes

		mPlatform.doAnimation(mPlatform.mDefaultX, mPlatform.mDefaultY, ANIM_KIND_CAPTURE);
		b.doAnimation(0, b.mScaleMin, ANIM_KIND_CAPTURE);
		n.doAnimation(0, n.mScaleMin, ANIM_KIND_CAPTURE);
		g.doAnimation(g.mDefaultSize, ANIM_KIND_CAPTURE);
		redraw();
	}

	// Slide the focused box to the center of the view.
	public void startHorizontalSlide() {
		final Box b = mBoxes.get(0);
		startAnimation(mPlatform.mDefaultX, 0, b.mScaleMin, ANIM_KIND_SLIDE);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Layout
	// //////////////////////////////////////////////////////////////////////////

	// Stop all animations at where they are now.
	public void stopAnimation() {
		mPlatform.mAnimationStartTime = NO_ANIMATION;
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			mBoxes.get(i).mAnimationStartTime = NO_ANIMATION;
		}
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			mGaps.get(i).mAnimationStartTime = NO_ANIMATION;
		}
	}

	public void stopScrolling() {
		if (mPlatform.mAnimationStartTime == NO_ANIMATION) return;
		mPlatform.mFromX = mPlatform.mToX = mPlatform.mCurrentX;
	}

	public void zoomIn(float tapX, float tapY, float targetScale) {
		tapX -= mViewW / 2;
		tapY -= mViewH / 2;
		final Box b = mBoxes.get(0);

		// Convert the tap position to distance to center in bitmap coordinates
		final float tempX = (tapX - mPlatform.mCurrentX) / b.mCurrentScale;
		final float tempY = (tapY - b.mCurrentY) / b.mCurrentScale;

		final int x = (int) (-tempX * targetScale + 0.5f);
		final int y = (int) (-tempY * targetScale + 0.5f);

		calculateStableBound(targetScale);
		final int targetX = Utils.clamp(x, mBoundLeft, mBoundRight);
		final int targetY = Utils.clamp(y, mBoundTop, mBoundBottom);
		targetScale = Utils.clamp(targetScale, b.mScaleMin, b.mScaleMax);

		startAnimation(targetX, targetY, targetScale, ANIM_KIND_ZOOM);
	}

	private void calculateStableBound(final float scale) {
		calculateStableBound(scale, 0);
	}

	// Calculates the stable region of mPlatform.mCurrentX and
	// mBoxes.get(0).mCurrentY, where "stable" means
	//
	// (1) If the dimension of scaled image >= view dimension, we will not
	// see black region outside the image (at that dimension).
	// (2) If the dimension of scaled image < view dimension, we will center
	// the scaled image.
	//
	// We might temporarily go out of this stable during user interaction,
	// but will "snap back" after user stops interaction.
	//
	// The results are stored in mBound{Left/Right/Top/Bottom}.
	//
	// An extra parameter "horizontalSlack" (which has the value of 0 usually)
	// is used to extend the stable region by some pixels on each side
	// horizontally.
	private void calculateStableBound(final float scale, final int horizontalSlack) {
		final Box b = mBoxes.get(0);

		// The width and height of the box in number of view pixels
		final int w = widthOf(b, scale);
		final int h = heightOf(b, scale);

		// When the edge of the view is aligned with the edge of the box
		mBoundLeft = (mViewW + 1) / 2 - (w + 1) / 2 - horizontalSlack;
		mBoundRight = w / 2 - mViewW / 2 + horizontalSlack;
		mBoundTop = (mViewH + 1) / 2 - (h + 1) / 2;
		mBoundBottom = h / 2 - mViewH / 2;

		// If the scaled height is smaller than the view height,
		// force it to be in the center.
		if (viewTallerThanScaledImage(scale)) {
			mBoundTop = mBoundBottom = 0;
		}

		// Same for width
		if (viewWiderThanScaledImage(scale)) {
			mBoundLeft = mBoundRight = mPlatform.mDefaultX;
		}
	}

	// Only allow scrolling when we are not currently in an animation or we
	// are in some animation with can be interrupted.
	private boolean canScroll() {
		final Box b = mBoxes.get(0);
		if (b.mAnimationStartTime == NO_ANIMATION) return true;
		switch (b.mAnimationKind) {
			case ANIM_KIND_SCROLL:
			case ANIM_KIND_FLING:
			case ANIM_KIND_FLING_X:
				return true;
		}
		return false;
	}

	private void convertBoxToRect(final int i) {
		final Box b = mBoxes.get(i);
		final Rect r = mRects.get(i);
		final int y = b.mCurrentY + mPlatform.mCurrentY + mViewH / 2;
		final int w = widthOf(b);
		final int h = heightOf(b);
		if (i == 0) {
			final int x = mPlatform.mCurrentX + mViewW / 2;
			r.left = x - w / 2;
			r.right = r.left + w;
		} else if (i > 0) {
			final Rect a = mRects.get(i - 1);
			final Gap g = mGaps.get(i - 1);
			r.left = a.right + g.mCurrentGap;
			r.right = r.left + w;
		} else { // i < 0
			final Rect a = mRects.get(i + 1);
			final Gap g = mGaps.get(i);
			r.right = a.left - g.mCurrentGap;
			r.left = r.right - w;
		}
		r.top = y - h / 2;
		r.bottom = r.top + h;
	}

	@SuppressWarnings("unused")
	private void debugMoveBox(final int fromIndex[]) {
		final StringBuilder s = new StringBuilder("moveBox:");
		for (final int j : fromIndex) {
			if (j == Integer.MAX_VALUE) {
				s.append(" N");
			} else {
				s.append(" ");
				s.append(j);
			}
		}
		Log.d(TAG, s.toString());
	}

	private void dumpRect(final int i) {
		final StringBuilder sb = new StringBuilder();
		final Rect r = mRects.get(i);
		sb.append("Rect " + i + ":");
		sb.append("(");
		sb.append(r.centerX());
		sb.append(",");
		sb.append(r.centerY());
		sb.append(") [");
		sb.append(r.width());
		sb.append("x");
		sb.append(r.height());
		sb.append("]");
		Log.d(TAG, sb.toString());
	}

	// //////////////////////////////////////////////////////////////////////////
	// Box management
	// //////////////////////////////////////////////////////////////////////////

	@SuppressWarnings("unused")
	private void dumpState() {
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			Log.d(TAG, "Gap " + i + ": " + mGaps.get(i).mCurrentGap);
		}

		for (int i = 0; i < 2 * BOX_MAX + 1; i++) {
			dumpRect(CENTER_OUT_INDEX[i]);
		}

		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			for (int j = i + 1; j <= BOX_MAX; j++) {
				if (Rect.intersects(mRects.get(i), mRects.get(j))) {
					Log.d(TAG, "rect " + i + " and rect " + j + "intersects!");
				}
			}
		}
	}

	// Here is how we layout the boxes in the page mode.
	//
	// previous current next
	// ___________ ________________ __________
	// | _______ | | __________ | | ______ |
	// | | | | | | right->| | | | | |
	// | | |<-------->|<--left | | | | | |
	// | |_______| | | | |__________| | | |______| |
	// |___________| | |________________| |__________|
	// | <--> gapToSide()
	// |
	// IMAGE_GAP + MAX(gapToSide(previous), gapToSide(current))
	private int gapToSide(final Box b) {
		return (int) ((mViewW - getMinimalScale(b) * b.mImageW) / 2 + 0.5f);
	}

	// Returns the default gap size according the the size of the boxes around
	// the gap and the current mode.
	private int getDefaultGapSize(final int i) {
		final Box a = mBoxes.get(i);
		final Box b = mBoxes.get(i + 1);
		return IMAGE_GAP + Math.max(gapToSide(a), gapToSide(b));
	}

	private float getMaximalScale(final Box b) {
		if (mConstrained && !mConstrainedFrame.isEmpty()) return getMinimalScale(b);
		return SCALE_LIMIT;
	}

	private float getMinimalScale(final Box b) {
		final float wFactor = 1.0f;
		final float hFactor = 1.0f;
		int viewW, viewH;

		if (mConstrained && !mConstrainedFrame.isEmpty() && b == mBoxes.get(0)) {
			viewW = mConstrainedFrame.width();
			viewH = mConstrainedFrame.height();
		} else {
			viewW = mViewW;
			viewH = mViewH;
		}

		final float s = Math.min(wFactor * viewW / b.mImageW, hFactor * viewH / b.mImageH);
		return Math.min(SCALE_LIMIT, s);
	}

	private float getTargetScale(final Box b) {
		return b.mAnimationStartTime == NO_ANIMATION ? b.mCurrentScale : b.mToScale;
	}

	// Returns the display height of this box.
	private int heightOf(final Box b) {
		return (int) (b.mImageH * b.mCurrentScale + 0.5f);
	}

	// //////////////////////////////////////////////////////////////////////////
	// Public utilities
	// //////////////////////////////////////////////////////////////////////////

	// Returns the display height of this box, using the given scale.
	private int heightOf(final Box b, final float scale) {
		return (int) (b.mImageH * scale + 0.5f);
	}

	// Initialize a box to have the size of the view.
	private void initBox(final int index) {
		final Box b = mBoxes.get(index);
		b.mImageW = mViewW;
		b.mImageH = mViewH;
		b.mUseViewSize = true;
		b.mScaleMin = getMinimalScale(b);
		b.mScaleMax = getMaximalScale(b);
		b.mCurrentY = 0;
		b.mCurrentScale = b.mScaleMin;
		b.mAnimationStartTime = NO_ANIMATION;
		b.mAnimationKind = ANIM_KIND_NONE;
	}

	// Initialize a box to a given size.
	private void initBox(final int index, final Size size) {
		if (size.width == 0 || size.height == 0) {
			initBox(index);
			return;
		}
		final Box b = mBoxes.get(index);
		b.mImageW = size.width;
		b.mImageH = size.height;
		b.mUseViewSize = false;
		b.mScaleMin = getMinimalScale(b);
		b.mScaleMax = getMaximalScale(b);
		b.mCurrentY = 0;
		b.mCurrentScale = b.mScaleMin;
		b.mAnimationStartTime = NO_ANIMATION;
		b.mAnimationKind = ANIM_KIND_NONE;
	}

	// Initialize a gap. This can only be called after the boxes around the gap
	// has been initialized.
	private void initGap(final int index) {
		final Gap g = mGaps.get(index);
		g.mDefaultSize = getDefaultGapSize(index);
		g.mCurrentGap = g.mDefaultSize;
		g.mAnimationStartTime = NO_ANIMATION;
	}

	private void initGap(final int index, final int size) {
		final Gap g = mGaps.get(index);
		g.mDefaultSize = getDefaultGapSize(index);
		g.mCurrentGap = size;
		g.mAnimationStartTime = NO_ANIMATION;
	}

	// Initialize the platform to be at the view center.
	private void initPlatform() {
		mPlatform.updateDefaultXY();
		mPlatform.mCurrentX = mPlatform.mDefaultX;
		mPlatform.mCurrentY = mPlatform.mDefaultY;
		mPlatform.mAnimationStartTime = NO_ANIMATION;
	}

	// Convert the information in mPlatform and mBoxes to mRects, so the user
	// can get the position of each box by getPosition().
	//
	// Note we go from center-out because each box's X coordinate
	// is relative to its anchor box (except the focused box).
	private void layoutAndSetPosition() {
		for (int i = 0; i < 2 * BOX_MAX + 1; i++) {
			convertBoxToRect(CENTER_OUT_INDEX[i]);
		}
		// dumpState();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Redraw
	//
	// If a method changes box positions directly, redraw()
	// should be called.
	//
	// If a method may also cause a snapback to happen, snapAndRedraw() should
	// be called.
	//
	// If a method starts an animation to change the position of focused box,
	// startAnimation() should be called.
	//
	// If time advances to change the box position, advanceAnimation() should
	// be called.
	// //////////////////////////////////////////////////////////////////////////
	private void redraw() {
		layoutAndSetPosition();
		mListener.invalidate();
	}

	// Returns false if the box size doesn't change.
	private boolean setBoxSize(final int i, final int width, final int height, final boolean isViewSize) {
		final Box b = mBoxes.get(i);
		final boolean wasViewSize = b.mUseViewSize;

		// If we already have an image size, we don't want to use the view size.
		if (!wasViewSize && isViewSize) return false;

		b.mUseViewSize = isViewSize;

		if (width == b.mImageW && height == b.mImageH) return false;

		// The ratio of the old size and the new size.
		//
		// If the aspect ratio changes, we don't know if it is because one side
		// grows or the other side shrinks. Currently we just assume the view
		// angle of the longer side doesn't change (so the aspect ratio change
		// is because the view angle of the shorter side changes). This matches
		// what camera preview does.
		final float ratio = width > height ? (float) b.mImageW / width : (float) b.mImageH / height;

		b.mImageW = width;
		b.mImageH = height;

		b.mCurrentScale = getMinimalScale(b);
		b.mAnimationStartTime = NO_ANIMATION;

		if (i == 0) {
			mFocusX /= ratio;
			mFocusY /= ratio;
		}

		return true;
	}

	private void snapAndRedraw() {
		mPlatform.startSnapback();
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			mBoxes.get(i).startSnapback();
		}
		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			mGaps.get(i).startSnapback();
		}
		mFilmRatio.startSnapback();
		redraw();
	}

	private boolean startAnimation(final int targetX, final int targetY, final float targetScale, final int kind) {
		boolean changed = false;
		changed |= mPlatform.doAnimation(targetX, mPlatform.mDefaultY, kind);
		changed |= mBoxes.get(0).doAnimation(targetY, targetScale, kind);
		if (changed) {
			redraw();
		}
		return changed;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private utilities
	// //////////////////////////////////////////////////////////////////////////

	private boolean startOpeningAnimationIfNeeded() {
		if (mOpenAnimationRect == null) return false;
		final Box b = mBoxes.get(0);
		if (b.mUseViewSize) return false;

		// Start animation from the saved rectangle if we have one.
		final Rect r = mOpenAnimationRect;
		mOpenAnimationRect = null;

		mPlatform.mCurrentX = r.centerX() - mViewW / 2;
		b.mCurrentY = r.centerY() - mViewH / 2;
		b.mCurrentScale = Math.max(r.width() / (float) b.mImageW, r.height() / (float) b.mImageH);
		startAnimation(mPlatform.mDefaultX, 0, b.mScaleMin, ANIM_KIND_OPENING);

		// Animate from large gaps for neighbor boxes to avoid them
		// shown on the screen during opening animation.
		for (int i = -1; i < 1; i++) {
			final Gap g = mGaps.get(i);
			g.mCurrentGap = mViewW;
			g.doAnimation(g.mDefaultSize, ANIM_KIND_OPENING);
		}

		return true;
	}

	// This should be called whenever the scale range of boxes or the default
	// gap size may change. Currently this can happen due to change of view
	// size, image size, mFilmMode, mConstrained, and mConstrainedFrame.
	private void updateScaleAndGapLimit() {
		for (int i = -BOX_MAX; i <= BOX_MAX; i++) {
			final Box b = mBoxes.get(i);
			b.mScaleMin = getMinimalScale(b);
			b.mScaleMax = getMaximalScale(b);
		}

		for (int i = -BOX_MAX; i < BOX_MAX; i++) {
			final Gap g = mGaps.get(i);
			g.mDefaultSize = getDefaultGapSize(i);
		}
	}

	private boolean viewTallerThanScaledImage(final float scale) {
		return mViewH >= heightOf(mBoxes.get(0), scale);
	}

	private boolean viewWiderThanScaledImage(final float scale) {
		return mViewW >= widthOf(mBoxes.get(0), scale);
	}

	// Returns the display width of this box.
	private int widthOf(final Box b) {
		return (int) (b.mImageW * b.mCurrentScale + 0.5f);
	}

	// Returns the display width of this box, using the given scale.
	private int widthOf(final Box b, final float scale) {
		return (int) (b.mImageW * scale + 0.5f);
	}

	private static boolean isAlmostEqual(final float a, final float b) {
		final float diff = a - b;
		return (diff < 0 ? -diff : diff) < 0.02f;
	}

	public interface Listener {
		void invalidate();

		boolean isHoldingDown();

		void onAbsorb(int velocity, int direction);

		// EdgeView
		void onPull(int offset, int direction);

		void onRelease();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Animatable: an thing which can do animation.
	// //////////////////////////////////////////////////////////////////////////
	private abstract static class Animatable {
		public long mAnimationStartTime;
		public int mAnimationKind;
		public int mAnimationDuration;

		// Returns true if the animation values changes, so things need to be
		// redrawn.
		public boolean advanceAnimation() {
			if (mAnimationStartTime == NO_ANIMATION) return false;
			if (mAnimationStartTime == LAST_ANIMATION) {
				mAnimationStartTime = NO_ANIMATION;
				return startSnapback();
			}

			float progress;
			if (mAnimationDuration == 0) {
				progress = 1;
			} else {
				final long now = AnimationTime.get();
				progress = (float) (now - mAnimationStartTime) / mAnimationDuration;
			}

			if (progress >= 1) {
				progress = 1;
			} else {
				progress = applyInterpolationCurve(mAnimationKind, progress);
			}

			final boolean done = interpolate(progress);

			if (done) {
				mAnimationStartTime = LAST_ANIMATION;
			}

			return true;
		}

		public abstract boolean startSnapback();

		// This should be overridden in subclass to change the animation values
		// give the progress value in [0, 1].
		protected abstract boolean interpolate(float progress);

		private static float applyInterpolationCurve(final int kind, float progress) {
			final float f = 1 - progress;
			switch (kind) {
				case ANIM_KIND_SCROLL:
				case ANIM_KIND_FLING:
				case ANIM_KIND_FLING_X:
				case ANIM_KIND_DELETE:
				case ANIM_KIND_CAPTURE:
					progress = 1 - f; // linear
					break;
				case ANIM_KIND_OPENING:
				case ANIM_KIND_SCALE:
					progress = 1 - f * f; // quadratic
					break;
				case ANIM_KIND_SNAPBACK:
				case ANIM_KIND_ZOOM:
				case ANIM_KIND_SLIDE:
					progress = 1 - f * f * f * f * f; // x^5
					break;
			}
			return progress;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Box: represents a rectangular area which shows a picture.
	// //////////////////////////////////////////////////////////////////////////
	private class Box extends Animatable {
		// Size of the bitmap
		public int mImageW, mImageH;

		// This is true if we assume the image size is the same as view size
		// until we know the actual size of image. This is also used to
		// determine if there is an image ready to show.
		public boolean mUseViewSize;

		// The minimum and maximum scale we allow for this box.
		public float mScaleMin, mScaleMax;

		// The X/Y value indicates where the center of the box is on the view
		// coordinate. We always keep the mCurrent{X,Y,Scale} sync with the
		// actual values used currently. Note that the X values are implicitly
		// defined by Platform and Gaps.
		public int mCurrentY, mFromY, mToY;
		public float mCurrentScale, mFromScale, mToScale;

		// The absolute X coordinate of the center of the box. This is only used
		// during moveBox().
		public int mAbsoluteX;

		// Clamps the input scale to the range that doAnimation() can reach.
		public float clampScale(final float s) {
			return Utils.clamp(s, SCALE_MIN_EXTRA * mScaleMin, SCALE_MAX_EXTRA * mScaleMax);
		}

		@Override
		public boolean startSnapback() {
			if (mAnimationStartTime != NO_ANIMATION) return false;
			if (mAnimationKind == ANIM_KIND_SCROLL && mListener.isHoldingDown()) return false;
			if (mInScale && this == mBoxes.get(0)) return false;

			int y = mCurrentY;
			float scale;

			if (this == mBoxes.get(0)) {
				final float scaleMin = mExtraScalingRange ? mScaleMin * SCALE_MIN_EXTRA : mScaleMin;
				final float scaleMax = mExtraScalingRange ? mScaleMax * SCALE_MAX_EXTRA : mScaleMax;
				scale = Utils.clamp(mCurrentScale, scaleMin, scaleMax);
				calculateStableBound(scale, HORIZONTAL_SLACK);
				// If the picture is zoomed-in, we want to keep the focus
				// point stay in the same position on screen. See the
				// comment in Platform.startSnapback for details.
				if (!viewTallerThanScaledImage(scale)) {
					final float scaleDiff = mCurrentScale - scale;
					y += (int) (mFocusY * scaleDiff + 0.5f);
				}
				y = Utils.clamp(y, mBoundTop, mBoundBottom);
			} else {
				y = 0;
				scale = mScaleMin;
			}

			if (mCurrentY != y || mCurrentScale != scale) return doAnimation(y, scale, ANIM_KIND_SNAPBACK);
			return false;
		}

		@Override
		protected boolean interpolate(final float progress) {
			if (mAnimationKind == ANIM_KIND_FLING)
				return interpolateFlingPage(progress);
			else
				return interpolateLinear(progress);
		}

		private boolean doAnimation(final int targetY, float targetScale, final int kind) {
			targetScale = clampScale(targetScale);

			if (mCurrentY == targetY && mCurrentScale == targetScale && kind != ANIM_KIND_CAPTURE) return false;

			// Now starts an animation for the box.
			mAnimationKind = kind;
			mFromY = mCurrentY;
			mFromScale = mCurrentScale;
			mToY = targetY;
			mToScale = targetScale;
			mAnimationStartTime = AnimationTime.startTime();
			mAnimationDuration = ANIM_TIME[kind];
			advanceAnimation();
			return true;
		}

		private boolean interpolateFlingPage(final float progress) {
			mPageScroller.computeScrollOffset(progress);
			calculateStableBound(mCurrentScale);

			final int oldY = mCurrentY;
			mCurrentY = mPageScroller.getCurrY();

			// Check if we hit the edges; show edge effects if we do.
			if (oldY > mBoundTop && mCurrentY == mBoundTop) {
				final int v = (int) (-mPageScroller.getCurrVelocityY() + 0.5f);
				mListener.onAbsorb(v, EdgeView.BOTTOM);
			} else if (oldY < mBoundBottom && mCurrentY == mBoundBottom) {
				final int v = (int) (mPageScroller.getCurrVelocityY() + 0.5f);
				mListener.onAbsorb(v, EdgeView.TOP);
			}

			return progress >= 1;
		}

		private boolean interpolateLinear(final float progress) {
			if (progress >= 1) {
				mCurrentY = mToY;
				mCurrentScale = mToScale;
				return true;
			} else {
				mCurrentY = (int) (mFromY + progress * (mToY - mFromY));
				mCurrentScale = mFromScale + progress * (mToScale - mFromScale);
				if (mAnimationKind == ANIM_KIND_CAPTURE) {
					final float f = CaptureAnimation.calculateScale(progress);
					mCurrentScale *= f;
					return false;
				} else
					return mCurrentY == mToY && mCurrentScale == mToScale;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// FilmRatio: represents the progress of film mode change.
	// //////////////////////////////////////////////////////////////////////////
	private class FilmRatio extends Animatable {
		// The film ratio: 1 means switching to film mode is complete, 0 means
		// switching to page mode is complete.
		public float mCurrentRatio, mFromRatio, mToRatio;

		@Override
		public boolean startSnapback() {
			final float target = 0f;
			if (target == mToRatio) return false;
			return doAnimation(target, ANIM_KIND_SNAPBACK);
		}

		@Override
		protected boolean interpolate(final float progress) {
			if (progress >= 1) {
				mCurrentRatio = mToRatio;
				return true;
			} else {
				mCurrentRatio = mFromRatio + progress * (mToRatio - mFromRatio);
				return mCurrentRatio == mToRatio;
			}
		}

		// Starts an animation for the film ratio.
		private boolean doAnimation(final float targetRatio, final int kind) {
			mAnimationKind = kind;
			mFromRatio = mCurrentRatio;
			mToRatio = targetRatio;
			mAnimationStartTime = AnimationTime.startTime();
			mAnimationDuration = ANIM_TIME[mAnimationKind];
			advanceAnimation();
			return true;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Gap: represents a rectangular area which is between two boxes.
	// //////////////////////////////////////////////////////////////////////////
	private class Gap extends Animatable {
		// The default gap size between two boxes. The value may vary for
		// different image size of the boxes and for different modes (page or
		// film).
		public int mDefaultSize;

		// The gap size between the two boxes.
		public int mCurrentGap, mFromGap, mToGap;

		// Starts an animation for a gap.
		public boolean doAnimation(final int targetSize, final int kind) {
			if (mCurrentGap == targetSize && kind != ANIM_KIND_CAPTURE) return false;
			mAnimationKind = kind;
			mFromGap = mCurrentGap;
			mToGap = targetSize;
			mAnimationStartTime = AnimationTime.startTime();
			mAnimationDuration = ANIM_TIME[mAnimationKind];
			advanceAnimation();
			return true;
		}

		@Override
		public boolean startSnapback() {
			if (mAnimationStartTime != NO_ANIMATION) return false;
			return doAnimation(mDefaultSize, ANIM_KIND_SNAPBACK);
		}

		@Override
		protected boolean interpolate(final float progress) {
			if (progress >= 1) {
				mCurrentGap = mToGap;
				return true;
			} else {
				mCurrentGap = (int) (mFromGap + progress * (mToGap - mFromGap));
				if (mAnimationKind == ANIM_KIND_CAPTURE) {
					final float f = CaptureAnimation.calculateScale(progress);
					mCurrentGap = (int) (mCurrentGap * f);
					return false;
				} else
					return mCurrentGap == mToGap;
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Platform: captures the global X/Y movement.
	// //////////////////////////////////////////////////////////////////////////
	private class Platform extends Animatable {
		public int mCurrentX, mFromX, mToX, mDefaultX;
		public int mCurrentY, mFromY, mToY, mDefaultY;
		public int mFlingOffset;

		@Override
		public boolean startSnapback() {
			if (mAnimationStartTime != NO_ANIMATION) return false;
			if (mAnimationKind == ANIM_KIND_SCROLL && mListener.isHoldingDown()) return false;
			if (mInScale) return false;

			final Box b = mBoxes.get(0);
			final float scaleMin = mExtraScalingRange ? b.mScaleMin * SCALE_MIN_EXTRA : b.mScaleMin;
			final float scaleMax = mExtraScalingRange ? b.mScaleMax * SCALE_MAX_EXTRA : b.mScaleMax;
			final float scale = Utils.clamp(b.mCurrentScale, scaleMin, scaleMax);
			int x = mCurrentX;
			final int y = mDefaultY;
			calculateStableBound(scale, HORIZONTAL_SLACK);
			// If the picture is zoomed-in, we want to keep the focus point
			// stay in the same position on screen, so we need to adjust
			// target mCurrentX (which is the center of the focused
			// box). The position of the focus point on screen (relative the
			// the center of the view) is:
			//
			// mCurrentX + scale * mFocusX = mCurrentX' + scale' * mFocusX
			// => mCurrentX' = mCurrentX + (scale - scale') * mFocusX
			//
			if (!viewWiderThanScaledImage(scale)) {
				final float scaleDiff = b.mCurrentScale - scale;
				x += (int) (mFocusX * scaleDiff + 0.5f);
			}
			x = Utils.clamp(x, mBoundLeft, mBoundRight);
			if (mCurrentX != x || mCurrentY != y) return doAnimation(x, y, ANIM_KIND_SNAPBACK);
			return false;
		}

		// The updateDefaultXY() should be called whenever these variables
		// changes: (1) mConstrained (2) mConstrainedFrame (3) mViewW/H (4)
		// mFilmMode
		public void updateDefaultXY() {
			// We don't check mFilmMode and return 0 for mDefaultX. Because
			// otherwise if we decide to leave film mode because we are
			// centered, we will immediately back into film mode because we find
			// we are not centered.
			if (mConstrained && !mConstrainedFrame.isEmpty()) {
				mDefaultX = mConstrainedFrame.centerX() - mViewW / 2;
				mDefaultY = mConstrainedFrame.centerY() - mViewH / 2;
			} else {
				mDefaultX = 0;
				mDefaultY = 0;
			}
		}

		@Override
		protected boolean interpolate(final float progress) {
			if (mAnimationKind == ANIM_KIND_FLING)
				return interpolateFlingPage(progress);
			else if (mAnimationKind == ANIM_KIND_FLING_X)
				return interpolateFlingFilm(progress);
			else
				return interpolateLinear(progress);
		}

		// Starts an animation for the platform.
		private boolean doAnimation(final int targetX, final int targetY, final int kind) {
			if (mCurrentX == targetX && mCurrentY == targetY) return false;
			mAnimationKind = kind;
			mFromX = mCurrentX;
			mFromY = mCurrentY;
			mToX = targetX;
			mToY = targetY;
			mAnimationStartTime = AnimationTime.startTime();
			mAnimationDuration = ANIM_TIME[kind];
			mFlingOffset = 0;
			advanceAnimation();
			return true;
		}

		private boolean interpolateFlingFilm(final float progress) {
			mFilmScroller.computeScrollOffset();
			mCurrentX = mFilmScroller.getCurrX() + mFlingOffset;

			int dir = EdgeView.INVALID_DIRECTION;
			if (mCurrentX < mDefaultX) {
				if (!mHasNext) {
					dir = EdgeView.RIGHT;
				}
			} else if (mCurrentX > mDefaultX) {
				if (!mHasPrev) {
					dir = EdgeView.LEFT;
				}
			}
			if (dir != EdgeView.INVALID_DIRECTION) {
				// TODO: restore this onAbsorb call
				// int v = (int) (mFilmScroller.getCurrVelocity() + 0.5f);
				// mListener.onAbsorb(v, dir);
				mFilmScroller.forceFinished(true);
				mCurrentX = mDefaultX;
			}
			return mFilmScroller.isFinished();
		}

		private boolean interpolateFlingPage(final float progress) {
			mPageScroller.computeScrollOffset(progress);
			final Box b = mBoxes.get(0);
			calculateStableBound(b.mCurrentScale);

			final int oldX = mCurrentX;
			mCurrentX = mPageScroller.getCurrX();

			// Check if we hit the edges; show edge effects if we do.
			if (oldX > mBoundLeft && mCurrentX == mBoundLeft) {
				final int v = (int) (-mPageScroller.getCurrVelocityX() + 0.5f);
				mListener.onAbsorb(v, EdgeView.RIGHT);
			} else if (oldX < mBoundRight && mCurrentX == mBoundRight) {
				final int v = (int) (mPageScroller.getCurrVelocityX() + 0.5f);
				mListener.onAbsorb(v, EdgeView.LEFT);
			}

			return progress >= 1;
		}

		private boolean interpolateLinear(float progress) {
			// Other animations
			if (progress >= 1) {
				mCurrentX = mToX;
				mCurrentY = mToY;
				return true;
			} else {
				if (mAnimationKind == ANIM_KIND_CAPTURE) {
					progress = CaptureAnimation.calculateSlide(progress);
				}
				mCurrentX = (int) (mFromX + progress * (mToX - mFromX));
				mCurrentY = (int) (mFromY + progress * (mToY - mFromY));
				if (mAnimationKind == ANIM_KIND_CAPTURE)
					return false;
				else
					return mCurrentX == mToX && mCurrentY == mToY;
			}
		}
	}
}
