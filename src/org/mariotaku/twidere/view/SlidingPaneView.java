/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Scroller;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.DualPaneActivity;
import org.mariotaku.twidere.view.iface.IExtendedViewGroup.TouchInterceptor;

public class SlidingPaneView extends ViewGroup {

	/**
	 * Fade is disabled.
	 */
	public static final int FADE_NONE = 0;
	/**
	 * Fade applies to actions container.
	 */
	public static final int FADE_LEFT = 1;
	/**
	 * Fade applies to content container.
	 */
	public static final int FADE_RIGHT = 3;
	/**
	 * Fade applies to every container.
	 */
	public static final int FADE_BOTH = 3;

	private final View mViewShadow;
	private final LeftPaneLayout mLeftPaneLayout;
	private final RightPaneLayout mRightPaneLayout;
	private final ExtendedFrameLayout mRightPaneContainer;
	private final View mLeftPaneView, mRightPaneView, mRightPaneBackgroundView;
	private final FadingRightPaneContainer mFadingRightPaneContainer;

	private final ScrollTouchInterceptor mTouchInterceptor;
	private final OnTouchListener mShadowTouchListener;
	private final ContentScrollController mController;

	/**
	 * Value of spacing to use.
	 */
	private int mRightSpacing;

	/**
	 * Value of actions container spacing to use.
	 */
	private int mLeftSpacing;

	/**
	 * Value of shadow width.
	 */
	private int mShadowWidth;

	/**
	 * Indicates how long flinging will take time in milliseconds.
	 */
	private int mFlingDuration;

	/**
	 * Fade type.
	 */
	private int mFadeType = FADE_NONE;
	/**
	 * Max fade value.
	 */
	private int mFadeMax;

	/**
	 * Indicates whether refresh of content position should be done on next
	 * layout calculation.
	 */
	private boolean mForceRefresh = false;

	private boolean mShadowSlidable;

	public SlidingPaneView(final Context context) {
		this(context, null);
	}

	public SlidingPaneView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingPaneView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		final Resources res = getResources();

		setClipChildren(false);
		setClipToPadding(false);

		// reading attributes
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingPaneView);
		final int spacingLeftDefault = res.getDimensionPixelSize(R.dimen.default_dualpane_spacing_left);
		mLeftSpacing = a.getDimensionPixelSize(R.styleable.SlidingPaneView_paneSpacingLeft, spacingLeftDefault);
		final int spacingRightDefault = res.getDimensionPixelSize(R.dimen.default_dualpane_spacing_right);
		mRightSpacing = a.getDimensionPixelSize(R.styleable.SlidingPaneView_paneSpacingRight, spacingRightDefault);

		final int leftPaneLayout = a.getResourceId(R.styleable.SlidingPaneView_paneLayoutLeft, 0);
		if (leftPaneLayout == 0) throw new IllegalArgumentException("The layoutLeft attribute is required");

		final int rightPaneLayout = a.getResourceId(R.styleable.SlidingPaneView_paneLayoutRight, 0);
		if (rightPaneLayout == leftPaneLayout || rightPaneLayout == 0)
			throw new IllegalArgumentException("The layoutRight attribute is required");

		final boolean shadowSlidableDefault = res.getBoolean(R.bool.default_shadow_slidable);
		final boolean shadowSlidable = a.getBoolean(R.styleable.SlidingPaneView_paneShadowSlidable,
				shadowSlidableDefault);

		mShadowWidth = a.getDimensionPixelSize(R.styleable.SlidingPaneView_paneShadowWidth, 0);
		final int shadowDrawableRes = a.getResourceId(R.styleable.SlidingPaneView_paneShadowDrawable, 0);

		mFadeType = a.getInteger(R.styleable.SlidingPaneView_paneFadeType, FADE_NONE);
		final int fadeValueDefault = res.getInteger(R.integer.default_sliding_pane_fade_max);
		mFadeMax = a.getDimensionPixelSize(R.styleable.SlidingPaneView_paneFadeMax, fadeValueDefault);

		final int flingDurationDefault = res.getInteger(R.integer.default_sliding_pane_fling_duration);
		mFlingDuration = a.getInteger(R.styleable.SlidingPaneView_paneFlingDuration, flingDurationDefault);

		a.recycle();

		mController = new ContentScrollController(new Scroller(context));
		mTouchInterceptor = new ScrollTouchInterceptor(this);
		mShadowTouchListener = new ShadowTouchListener(this);

		mLeftPaneLayout = new LeftPaneLayout(this);
		mRightPaneLayout = new RightPaneLayout(this);

		mRightPaneContainer = new ExtendedFrameLayout(context);
		mFadingRightPaneContainer = new FadingRightPaneContainer(this);
		mViewShadow = new View(context);
		mRightPaneBackgroundView = new RightPaneBackgroundView(context);

		final LayoutInflater inflater = LayoutInflater.from(context);
		if (leftPaneLayout == 0) throw new IllegalArgumentException();
		mLeftPaneView = inflater.inflate(leftPaneLayout, mLeftPaneLayout, true);

		if (rightPaneLayout == 0) throw new IllegalArgumentException();
		mRightPaneView = inflater.inflate(rightPaneLayout, mFadingRightPaneContainer, true);

		addView(mLeftPaneLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		addView(mRightPaneLayout, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		mViewShadow.setBackgroundResource(shadowDrawableRes);
		if (mShadowWidth <= 0 || shadowDrawableRes == 0) {
			mViewShadow.setVisibility(GONE);
		}
		mRightPaneLayout.addView(mViewShadow, mShadowWidth, LinearLayout.LayoutParams.MATCH_PARENT);
		mRightPaneLayout.addView(mRightPaneContainer, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		mRightPaneContainer.addView(mRightPaneBackgroundView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mRightPaneContainer.addView(mFadingRightPaneContainer, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mRightPaneContainer.setTouchInterceptor(mTouchInterceptor);
		mRightPaneLayout.setOnSwipeListener(new SwipeFadeListener());
		mViewShadow.setOnTouchListener(mShadowTouchListener);
		setShadowSlidable(shadowSlidable);
	}

	public int getFadeType() {
		return mFadeType;
	}

	public int getFadeValue() {
		return mFadeMax;
	}

	public int getFlingDuration() {
		return mFlingDuration;
	}

	public ViewGroup getLeftPaneContainer() {
		return mLeftPaneLayout;
	}

	public View getLeftPaneLayout() {
		return mLeftPaneView;
	}

	public int getLeftPaneSpacingWidth() {
		return mLeftSpacing;
	}

	public ViewGroup getRightPaneContainer() {
		return mRightPaneLayout;
	}

	public View getRightPaneLayout() {
		return mRightPaneView;
	}

	public int getRightSpacingWidth() {
		return mRightSpacing;
	}

	public int getShadowWidth() {
		return mShadowWidth;
	}

	public void hideRightPane() {
		mController.hideRightPane(mFlingDuration);
	}

	public void hideRightPaneNoAnimation() {
		mController.hideRightPane(0);
	}

	public boolean isRightPaneOpened() {
		return mController.isContentShown();
	}

	public boolean isShadowSlidable() {
		return mShadowSlidable;
	}

	public boolean isShadowVisible() {
		return mViewShadow.getVisibility() == VISIBLE;
	}

	@Override
	public void onRestoreInstanceState(final Parcelable state) {
		if (!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		final SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		mController.mIsRightPaneShown = ss.mIsRightPaneShown;

		mShadowWidth = ss.mShadowWidth;
		mFlingDuration = ss.mFlingDuration;
		mFadeType = ss.mFadeType;
		mFadeMax = ss.mFadeValue;

		// this will call requestLayout() to calculate layout according to
		// values
		setShadowVisible(ss.mIsShadowVisible);
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		final SavedState ss = new SavedState(superState);
		ss.mIsRightPaneShown = isRightPaneOpened();
		ss.mIsShadowVisible = isShadowVisible();
		ss.mShadowWidth = getShadowWidth();
		ss.mFlingDuration = getFlingDuration();
		ss.mFadeType = getFadeType();
		ss.mFadeValue = getFadeValue();
		return ss;
	}

	public void setActionsSpacingWidth(final int width) {
		if (mLeftSpacing == width) return;

		mLeftSpacing = width;
		mForceRefresh = true;
		requestLayout();
	}

	public void setFadeType(final int type) {
		if (type != FADE_NONE && type != FADE_LEFT && type != FADE_RIGHT && type != FADE_BOTH) return;

		mFadeType = type;
	}

	public void setFadeValue(final int value) {
		mFadeMax = limit(value, 0x00, 0xFF);
	}

	public void setFlingDuration(final int duration) {
		mFlingDuration = duration;
	}

	public void setRightPaneBackground(final int resId) {
		mRightPaneBackgroundView.setBackgroundResource(resId);
	}

	public void setShadowSlidable(final boolean slidable) {
		mShadowSlidable = slidable;
		mViewShadow.setOnTouchListener(slidable ? mShadowTouchListener : null);
	}

	public void setShadowVisible(final boolean visible) {
		mViewShadow.setVisibility(visible ? VISIBLE : GONE);
		mForceRefresh = true;
		requestLayout();
	}

	public void setShadowWidth(final int width) {
		if (mShadowWidth == width) return;
		mShadowWidth = width;
		mViewShadow.getLayoutParams().width = mShadowWidth;
		mForceRefresh = true;
		requestLayout();
	}

	public void setSpacingWidth(final int width) {
		if (mRightSpacing == width) return;

		mRightSpacing = width;
		mForceRefresh = true;
		requestLayout();
	}

	public void showRightPane() {
		mController.showRightPane(mFlingDuration);
	}

	public void showRightPaneNoAnimation() {
		mController.showRightPane(0);
	}

	public void toggle() {
		if (isRightPaneOpened()) {
			hideRightPane();
		} else {
			showRightPane();
		}
	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
		// putting every child view to top-left corner
		final int childrenCount = getChildCount();
		for (int i = 0; i < childrenCount; ++i) {
			final View v = getChildAt(i);
			if (v == mRightPaneLayout) {
				final int shadowWidth = isShadowVisible() ? mShadowWidth : 0;
				v.layout(l + mLeftSpacing - shadowWidth, t, l + mLeftSpacing + v.getMeasuredWidth(),
						t + v.getMeasuredHeight());
			} else {
				v.layout(l, t, l + v.getMeasuredWidth(), t + v.getMeasuredHeight());
			}
		}

		if (mForceRefresh) {
			mForceRefresh = false;
			mController.init();
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int width = MeasureSpec.getSize(widthMeasureSpec);

		final int childrenCount = getChildCount();
		for (int i = 0; i < childrenCount; ++i) {
			final View v = getChildAt(i);
			if (v == mLeftPaneLayout) {
				// setting size of actions according to spacing parameters
				mLeftPaneLayout.measure(MeasureSpec.makeMeasureSpec(width - mRightSpacing, MeasureSpec.EXACTLY),
						heightMeasureSpec);
			} else if (v == mRightPaneLayout) {
				final int shadowWidth = isShadowVisible() ? mShadowWidth : 0;
				final int contentWidth = MeasureSpec.getSize(widthMeasureSpec) - mLeftSpacing + shadowWidth;
				v.measure(MeasureSpec.makeMeasureSpec(contentWidth, MeasureSpec.EXACTLY), heightMeasureSpec);
			} else {
				v.measure(widthMeasureSpec, heightMeasureSpec);
			}
		}

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		// set correct position of content view after view size was changed
		if (w != oldw || h != oldh) {
			mController.init();
		}
	}

	private void fadeViews() {
		if (mFadeType == FADE_NONE) return;

		final float scrollFactor = mController.getScrollFactor();
		if ((mFadeType & FADE_LEFT) > 0 || mFadeType == FADE_BOTH) {
			final int fadeFactor = 0xff - (int) ((1f - scrollFactor) * mFadeMax);
			mLeftPaneLayout.setFadeFactor(fadeFactor);
		}
		if ((mFadeType & FADE_RIGHT) > 0 || mFadeType == FADE_BOTH) {
			final int fadeFactor = 0xff - (int) (scrollFactor * mFadeMax);
			mFadingRightPaneContainer.setFadeFactor(fadeFactor);
		}
	}

	private ContentScrollController getController() {
		return mController;
	}

	/**
	 * Returns right bound (limit) for scroller.
	 * 
	 * @return right bound (limit) for scroller.
	 */
	private int getRightBound() {
		return getWidth() - mRightSpacing - mLeftSpacing;
	}

	private static boolean isTouchEventHandled(final View view, final MotionEvent event) {
		if (view instanceof RightPaneBackgroundView) return false;
		if (!(view instanceof ViewGroup)) return true;
		final MotionEvent ev = MotionEvent.obtain(event);
		final float xf = ev.getX();
		final float yf = ev.getY();
		final float scrolledXFloat = xf + view.getScrollX();
		final float scrolledYFloat = yf + view.getScrollY();
		final Rect frame = new Rect();
		final int scrolledXInt = (int) scrolledXFloat;
		final int scrolledYInt = (int) scrolledYFloat;
		final int count = ((ViewGroup) view).getChildCount();
		for (int i = count - 1; i >= 0; i--) {
			final View child = ((ViewGroup) view).getChildAt(i);
			if (child.isShown() || child.getAnimation() != null) {
				child.getHitRect(frame);
				if (frame.contains(scrolledXInt, scrolledYInt)) {
					// offset the event to the view's coordinate system
					final float xc = scrolledXFloat - child.getLeft();
					final float yc = scrolledYFloat - child.getTop();
					ev.setLocation(xc, yc);
					if (isTouchEventHandled(child, ev)) return true;
				}
			}
		}
		return false;
	}

	static int limit(final int value, final int min, final int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	public static class LeftPaneLayout extends FrameLayout {

		private int mFadeFactor = 0;
		private final SlidingPaneView parent;

		public LeftPaneLayout(final SlidingPaneView parent) {
			super(parent.getContext());
			this.parent = parent;
		}

		@Override
		public boolean onInterceptTouchEvent(final MotionEvent ev) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				parent.hideRightPane();
			}
			return super.onInterceptTouchEvent(ev);
		}

		public void setFadeFactor(final int fadeFactor) {
			mFadeFactor = fadeFactor;
			setAlpha((float) mFadeFactor / 0xFF);
		}

	}

	public static class SavedState extends BaseSavedState {
		/**
		 * Indicates whether content was shown while saving state.
		 */
		private boolean mIsRightPaneShown;

		/**
		 * Indicates whether shadow is visible.
		 */
		private boolean mIsShadowVisible;

		/**
		 * Value of shadow width.
		 */
		private int mShadowWidth = 0;

		/**
		 * Indicates how long flinging will take time in milliseconds.
		 */
		private int mFlingDuration = 250;

		/**
		 * Fade type.
		 */
		private int mFadeType = FADE_NONE;
		/**
		 * Max fade value.
		 */
		private int mFadeValue;

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel source) {
				return new SavedState(source);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		public SavedState(final Parcelable superState) {
			super(superState);
		}

		SavedState(final Parcel in) {
			super(in);

			mIsRightPaneShown = in.readInt() == 1;
			mIsShadowVisible = in.readInt() == 1;
			mShadowWidth = in.readInt();
			mFlingDuration = in.readInt();
			mFadeType = in.readInt();
			mFadeValue = in.readInt();
		}

		@Override
		public void writeToParcel(final Parcel out, final int flags) {
			super.writeToParcel(out, flags);

			out.writeInt(mIsRightPaneShown ? 1 : 0);
			out.writeInt(mIsShadowVisible ? 1 : 0);
			out.writeInt(mShadowWidth);
			out.writeInt(mFlingDuration);
			out.writeInt(mFadeType);
			out.writeInt(mFadeValue);
		}
	}

	/**
	 * Used to handle scrolling events and scroll content container on top of
	 * actions one.
	 * 
	 * @author steven
	 */
	private class ContentScrollController implements Runnable {

		/**
		 * Used to auto-scroll to closest bound on touch up event.
		 */
		private final Scroller mScroller;

		private int mLastFlingX = 0;

		/**
		 * Indicates whether we need initialize position of view after measuring
		 * is finished.
		 */
		private boolean mIsRightPaneShown;

		private boolean mIsScrolling;

		private ContentScrollController(final Scroller scroller) {
			mScroller = scroller;
		}

		/**
		 * Processes auto-scrolling to bound which is closer to current
		 * position.
		 */
		@Override
		public void run() {
			if (mScroller.isFinished()) {
				mIsScrolling = false;
				return;
			}
			mIsScrolling = true;
			final boolean more = mScroller.computeScrollOffset();
			final int x = mScroller.getCurrX();
			final int diff = mLastFlingX - x;
			if (diff != 0) {
				mRightPaneLayout.scrollBy(diff, 0);
				mLastFlingX = x;
			}

			if (more) {
				mRightPaneLayout.post(this);
			}
		}

		/**
		 * Starts auto-scrolling to bound which is closer to current position.
		 * 
		 * @param delta
		 */
		private void completeScrolling(final float delta) {
			if (delta == 0) {
				final int bound = getRightBound();
				final int scroll = mRightPaneLayout.getScrollX();
				if (-scroll < bound / 2) {
					showRightPane(getFlingDuration());
				} else {
					hideRightPane(getFlingDuration());
				}
				return;
			}
			if (delta > 0) {
				showRightPane(getFlingDuration());
			} else {
				hideRightPane(getFlingDuration());
			}
		}

		private void fling(final int startX, final int dx, final int duration) {
			reset();
			if (dx == 0) return;
			mIsScrolling = true;
			if (duration <= 0) {
				mRightPaneLayout.scrollBy(-dx, 0);
				return;
			}

			mScroller.startScroll(startX, 0, dx, 0, duration);

			mLastFlingX = startX;
			mRightPaneLayout.post(this);
		}

		private float getScrollFactor() {
			return -(float) mRightPaneLayout.getScrollX() / getRightBound();
		}

		private void hideRightPane(final int duration) {
			mIsRightPaneShown = false;
			if (mRightPaneLayout.getMeasuredWidth() == 0 || mRightPaneLayout.getMeasuredHeight() == 0) return;

			final int startX = mRightPaneLayout.getScrollX();
			final int dx = getRightBound() + startX;
			fling(startX, dx, duration);
		}

		/**
		 * Initializes visibility of content after views measuring is finished.
		 */
		private void init() {
			if (mIsRightPaneShown) {
				showRightPane(0);
			} else {
				hideRightPane(0);
			}
			fadeViews();
		}

		private boolean isContentShown() {
			final int x;
			if (!mScroller.isFinished()) {
				x = mScroller.getFinalX();
			} else {
				x = mRightPaneLayout.getScrollX();
			}
			return x == 0;
		}

		private boolean isScrolling() {
			return mIsScrolling;
		}

		private void release(final float delta, final float totalMove) {
			completeScrolling(delta);
		}

		/**
		 * Resets scroller controller. Stops flinging on current position.
		 */
		private void reset() {
			mIsScrolling = false;
			if (!mScroller.isFinished()) {
				mScroller.forceFinished(true);
			}
		}

		/**
		 * Scrolling content view according by given value.
		 * 
		 * @param dx
		 */
		private void scrollBy(final int dx) {
			final int x = mRightPaneLayout.getScrollX();

			final int scrollBy;
			if (dx < 0) { // scrolling right
				final int rightBound = getRightBound();
				if (x + dx < -rightBound) {
					scrollBy = -rightBound - x;
				} else {
					scrollBy = dx;
				}
			} else { // scrolling left
				// don't scroll if we are at left bound
				if (x == 0) return;

				if (x + dx > 0) {
					scrollBy = -x;
				} else {
					scrollBy = dx;
				}
			}

			mRightPaneLayout.scrollBy(scrollBy, 0);
		}

		private void showRightPane(final int duration) {
			mIsRightPaneShown = true;
			if (mRightPaneLayout.getMeasuredWidth() == 0 || mRightPaneLayout.getMeasuredHeight() == 0) return;

			final int startX = mRightPaneLayout.getScrollX();
			final int dx = startX;
			fling(startX, dx, duration);
		}
	}

	private static class FadingRightPaneContainer extends ExtendedFrameLayout {

		private int mFadeFactor = 0;

		public FadingRightPaneContainer(final SlidingPaneView parent) {
			super(parent.getContext());
		}

		public void setFadeFactor(final int fadeFactor) {
			mFadeFactor = fadeFactor;
			invalidate();
			setAlpha((float) mFadeFactor / 0xFF);
		}
	}

	private static class RightPaneBackgroundView extends View {

		public RightPaneBackgroundView(final Context context) {
			super(context);
		}

		@Override
		public boolean onTouchEvent(final MotionEvent e) {
			return false;
		}
	}

	private static class RightPaneLayout extends ExtendedLinearLayout {

		private OnSwipeListener mOnSwipeListener;

		public RightPaneLayout(final SlidingPaneView parent) {
			super(parent.getContext());
			setOrientation(LinearLayout.HORIZONTAL);
		}

		public void setOnSwipeListener(final OnSwipeListener listener) {
			mOnSwipeListener = listener;
		}

		@Override
		protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
			super.onScrollChanged(l, t, oldl, oldt);
			if (mOnSwipeListener != null) {
				mOnSwipeListener.onSwipe(-getScrollX());
			}
		}

		public static interface OnSwipeListener {
			public void onSwipe(int scrollPosition);
		}

	}

	private static class ScrollTouchInterceptor implements TouchInterceptor {

		private final SlidingPaneView mParent;
		private final ContentScrollController mController;
		private final int mScaledTouchSlop;
		private final Context mContext;

		private float mTempDeltaX, mTotalMoveX, mTotalMoveY, mActualMoveX;
		private boolean mIsVerticalScrolling, mFirstDownHandled, mShouldDisableScroll;

		ScrollTouchInterceptor(final SlidingPaneView parent) {
			mParent = parent;
			mScaledTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
			mController = parent.getController();
			mContext = parent.getContext();
		}

		@Override
		public void dispatchTouchEvent(final ViewGroup view, final MotionEvent event) {
			if (event.getAction() != MotionEvent.ACTION_UP) return;
			if (mFirstDownHandled) {
				if (mActualMoveX != 0 && !mIsVerticalScrolling) {
					mController.release(mIsVerticalScrolling ? 0 : -mTempDeltaX, -mActualMoveX);
				} else {
					mParent.showRightPane();
				}
			}
			mTempDeltaX = 0;
			mTotalMoveX = 0;
			mTotalMoveY = 0;
			mActualMoveX = 0;
			mIsVerticalScrolling = false;
			mShouldDisableScroll = false;
			mFirstDownHandled = false;
		}

		@Override
		public boolean onInterceptTouchEvent(final ViewGroup view, final MotionEvent event) {
			mShouldDisableScroll = !(isTouchEventHandled(view, event) || mContext instanceof DualPaneActivity
					&& ((DualPaneActivity) mContext).isRightPaneUsed());
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					mFirstDownHandled = !mShouldDisableScroll;
					mTempDeltaX = 0;
					mTotalMoveX = 0;
					mTotalMoveY = 0;
					mActualMoveX = 0;
					mIsVerticalScrolling = false;
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					final int hist_size = event.getHistorySize();
					if (hist_size == 0) {
						break;
					}
					mTempDeltaX = event.getX() - event.getHistoricalX(0);
					mTotalMoveX += mTempDeltaX;
					final float deltaY = event.getY() - event.getHistoricalY(0);
					mTotalMoveY += deltaY;
					if (Math.abs(mTempDeltaX) > Math.abs(deltaY) && !mIsVerticalScrolling
							&& Math.abs(mTotalMoveX) >= mScaledTouchSlop) return true;
					if (Math.abs(mTempDeltaX) < Math.abs(deltaY) && Math.abs(mTotalMoveY) >= mScaledTouchSlop) {
						mIsVerticalScrolling = true;
						return false;
					}
					break;
				}
			}
			return false;
		}

		@Override
		public boolean onTouchEvent(final ViewGroup view, final MotionEvent event) {
			if (mIsVerticalScrolling && !mController.isScrolling() || mShouldDisableScroll) return true;
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					mTempDeltaX = 0;
					mTotalMoveX = 0;
					mTotalMoveY = 0;
					mActualMoveX = 0;
					mIsVerticalScrolling = false;
					mController.reset();
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					final int hist_size = event.getHistorySize();
					if (hist_size == 0) {
						break;
					}
					final float distanceX = mTempDeltaX = event.getX() - event.getHistoricalX(0);
					mActualMoveX = mTotalMoveX += mTempDeltaX;
					mController.scrollBy((int) -distanceX);
					break;
				}
			}
			return true;
		}
	}

	private static class ShadowTouchListener implements OnTouchListener {

		private final ContentScrollController mController;
		private final int mScaledTouchSlop;
		private final Context mContext;

		private float mTempDeltaX, mTotalMoveX;

		private boolean mIsScrolling, mShouldDisableScroll;

		ShadowTouchListener(final SlidingPaneView parent) {
			mContext = parent.getContext();
			mScaledTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
			mController = parent.getController();
		}

		@Override
		public boolean onTouch(final View view, final MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					mTempDeltaX = 0;
					mTotalMoveX = 0;
					mIsScrolling = false;
					// final View layout = mParent.getRightPaneLayout();
					mShouldDisableScroll = !(mContext instanceof DualPaneActivity && ((DualPaneActivity) mContext)
							.isRightPaneUsed());
					if (!mShouldDisableScroll) {
						mController.reset();
					} else
						return false;
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					if (mShouldDisableScroll) return false;
					final int hist_size = event.getHistorySize();
					if (hist_size == 0) {
						break;
					}
					final float distanceX = mTempDeltaX = event.getX() - event.getHistoricalX(0);
					mTotalMoveX += mTempDeltaX;
					if (Math.abs(mTotalMoveX) >= mScaledTouchSlop) {
						mIsScrolling = true;
					}
					if (mIsScrolling) {
						mController.scrollBy((int) -distanceX);
					}
					break;
				}
				case MotionEvent.ACTION_UP: {
					if (mIsScrolling) {
						mController.release(-mTempDeltaX, -mTotalMoveX);
					}
					mTempDeltaX = 0;
					mTotalMoveX = 0;
					mIsScrolling = false;
					if (mShouldDisableScroll) {
						mShouldDisableScroll = false;
						return false;
					}
					mShouldDisableScroll = false;
					break;
				}
			}
			return true;
		}

	}

	private class SwipeFadeListener implements RightPaneLayout.OnSwipeListener {

		@Override
		public void onSwipe(final int scrollPosition) {
			fadeViews();
		}

	}
}
