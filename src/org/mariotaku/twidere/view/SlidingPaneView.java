package org.mariotaku.twidere.view;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ExtendedViewGroupInterface.TouchInterceptor;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
	public static final int FADE_RIGHT = 2;
	/**
	 * Fade applies to every container.
	 */
	public static final int FADE_BOTH = 3;

	private final View mViewShadow;
	private final LeftPaneLayout mViewLeftPaneContainer;
	private final RightPaneLayout mViewRightPaneContainer;
	private final ExtendedFrameLayout mRightPaneContent;

	final ContentScrollController mController;

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
	private int mFadeMax;

	/**
	 * Indicates whether refresh of content position should be done on next
	 * layout calculation.
	 */
	private boolean mForceRefresh = false;

	private final View mLeftPaneLayout, mRightPaneLayout;

	public SlidingPaneView(final Context context) {
		this(context, null);
	}

	public SlidingPaneView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SlidingPaneView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		setClipChildren(false);
		setClipToPadding(false);

		// reading attributes
		final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ActionsContentView);
		final int spacingRightDefault = getResources().getDimensionPixelSize(R.dimen.default_slidepane_spacing_right);
		mRightSpacing = a.getDimensionPixelSize(R.styleable.ActionsContentView_spacingRight, spacingRightDefault);
		final int spacingLeftDefault = getResources().getDimensionPixelSize(R.dimen.default_slidepane_spacing_left);
		mLeftSpacing = a.getDimensionPixelSize(R.styleable.ActionsContentView_spacingLeft, spacingLeftDefault);

		final int leftPaneLayout = a.getResourceId(R.styleable.ActionsContentView_layoutLeft, 0);
		if (leftPaneLayout == 0) throw new IllegalArgumentException("The layoutLeft attribute is required");

		final int rightPaneLayout = a.getResourceId(R.styleable.ActionsContentView_layoutRight, 0);
		if (rightPaneLayout == leftPaneLayout || rightPaneLayout == 0)
			throw new IllegalArgumentException("The layoutRight attribute is required");

		mShadowWidth = a.getDimensionPixelSize(R.styleable.ActionsContentView_shadowWidth, 0);
		final int shadowDrawableRes = a.getResourceId(R.styleable.ActionsContentView_shadowDrawable, 0);

		mFadeType = a.getInteger(R.styleable.ActionsContentView_fadeType, FADE_NONE);
		final int fadeValueDefault = getResources().getInteger(R.integer.default_sliding_pane_fade_max);
		mFadeMax = a.getDimensionPixelSize(R.styleable.ActionsContentView_fadeMax, fadeValueDefault);

		final int flingDurationDefault = getResources().getInteger(R.integer.default_sliding_pane_fling_duration);
		mFlingDuration = a.getInteger(R.styleable.ActionsContentView_flingDuration, flingDurationDefault);

		a.recycle();

		mController = new ContentScrollController(new Scroller(context));
		final LayoutInflater inflater = LayoutInflater.from(context);
		mViewLeftPaneContainer = new LeftPaneLayout(this);
		if (leftPaneLayout == 0) throw new IllegalArgumentException();
		mLeftPaneLayout = inflater.inflate(leftPaneLayout, mViewLeftPaneContainer, true);

		addView(mViewLeftPaneContainer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		mViewRightPaneContainer = new RightPaneLayout(this);
		mViewRightPaneContainer.setOnSwipeListener(new SwipeFadeListener());

		mViewShadow = new View(context);
		mViewShadow.setBackgroundResource(shadowDrawableRes);
		mViewRightPaneContainer.addView(mViewShadow, mShadowWidth, LinearLayout.LayoutParams.MATCH_PARENT);

		if (mShadowWidth <= 0 || shadowDrawableRes == 0) {
			mViewShadow.setVisibility(GONE);
		}
		mRightPaneContent = new ExtendedFrameLayout(context);

		mRightPaneContent.setTouchInterceptor(new ScrollTouchInterceptor(this));

		if (rightPaneLayout == 0) throw new IllegalArgumentException();
		mRightPaneLayout = inflater.inflate(rightPaneLayout, mRightPaneContent, true);
		mViewRightPaneContainer.addView(mRightPaneContent, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		addView(mViewRightPaneContainer, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}

	public void animateClose() {
		mController.hideRightPane(mFlingDuration);
	}

	public void animateOpen() {
		mController.showRightPane(mFlingDuration);
	}

	public void close() {
		mController.hideRightPane(0);
	}

	public ViewGroup getLeftPaneContainer() {
		return mViewLeftPaneContainer;
	}

	public int getLeftPaneSpacingWidth() {
		return mLeftSpacing;
	}

	public ViewGroup getRightPaneContainer() {
		return mViewRightPaneContainer;
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

	public View getLeftPaneLayout() {
		return mLeftPaneLayout;
	}

	public View getRightPaneLayout() {
		return mRightPaneLayout;
	}

	public int getShadowWidth() {
		return mShadowWidth;
	}

	public int getSpacingWidth() {
		return mRightSpacing;
	}

	public boolean isContentShown() {
		return mController.isContentShown();
	}

	public boolean isOpened() {
		return !mController.isContentShown();
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

		mRightSpacing = ss.mSpacing;
		mLeftSpacing = ss.mLeftPaneSpacing;
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
		ss.mIsRightPaneShown = isContentShown();
		ss.mSpacing = getSpacingWidth();
		ss.mLeftPaneSpacing = getLeftPaneSpacingWidth();
		ss.mIsShadowVisible = isShadowVisible();
		ss.mShadowWidth = getShadowWidth();
		ss.mFlingDuration = getFlingDuration();
		ss.mFadeType = getFadeType();
		ss.mFadeValue = getFadeValue();
		return ss;
	}

	public void open() {
		mController.showRightPane(0);
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

	public void toggle() {
		if (isOpened()) {
			animateOpen();
		} else {
			animateClose();
		}
	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
		// putting every child view to top-left corner
		final int childrenCount = getChildCount();
		for (int i = 0; i < childrenCount; ++i) {
			final View v = getChildAt(i);
			if (v == mViewRightPaneContainer) {
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
			if (v == mViewLeftPaneContainer) {
				// setting size of actions according to spacing parameters
				mViewLeftPaneContainer.measure(MeasureSpec.makeMeasureSpec(width - mRightSpacing, MeasureSpec.EXACTLY),
						heightMeasureSpec);
			} else if (v == mViewRightPaneContainer) {
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
		if ((mFadeType & FADE_LEFT) > 0) {
			final int fadeFactor = (int) (scrollFactor * mFadeMax);
			mViewLeftPaneContainer.invalidate(fadeFactor);
		}
		if ((mFadeType & FADE_RIGHT) > 0) {
			final int fadeFactor = (int) ((1f - scrollFactor) * mFadeMax);
			mViewRightPaneContainer.invalidate(fadeFactor);
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

	static int limit(final int value, final int min, final int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	public static class LeftPaneLayout extends FrameLayout {

		private final Paint mFadePaint = new Paint();
		private int mFadeFactor = 0;
		private final SlidingPaneView parent;

		public LeftPaneLayout(final SlidingPaneView parent) {
			super(parent.getContext());
			this.parent = parent;
		}

		public void invalidate(final int fadeFactor) {
			mFadeFactor = fadeFactor;
			invalidate();
		}

		@Override
		public boolean onInterceptTouchEvent(final MotionEvent ev) {
			if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				parent.animateClose();
			}
			return super.onInterceptTouchEvent(ev);
		}

		@Override
		protected void dispatchDraw(final Canvas canvas) {
			super.dispatchDraw(canvas);

			if (mFadeFactor > 0f) {
				mFadePaint.setColor(Color.argb(mFadeFactor, 0, 0, 0));
				canvas.drawRect(0, 0, getWidth(), getHeight(), mFadePaint);
			}
		}
	}

	public static class SavedState extends BaseSavedState {
		/**
		 * Indicates whether content was shown while saving state.
		 */
		private boolean mIsRightPaneShown;

		/**
		 * Value of spacing to use.
		 */
		private int mSpacing;

		/**
		 * Value of actions container spacing to use.
		 */
		private int mLeftPaneSpacing;

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
			mSpacing = in.readInt();
			mLeftPaneSpacing = in.readInt();
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
			out.writeInt(mSpacing);
			out.writeInt(mLeftPaneSpacing);
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
	 * 
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
		private boolean mIsRightPaneShown = false;

		public ContentScrollController(final Scroller scroller) {
			mScroller = scroller;
		}

		public float getScrollFactor() {
			return 1f + (float) mViewRightPaneContainer.getScrollX() / (float) getRightBound();
		}

		public void hideRightPane(final int duration) {
			mIsRightPaneShown = false;
			if (mViewRightPaneContainer.getMeasuredWidth() == 0 || mViewRightPaneContainer.getMeasuredHeight() == 0)
				return;

			final int startX = mViewRightPaneContainer.getScrollX();
			final int dx = getRightBound() + startX;
			fling(startX, dx, duration);
		}

		/**
		 * Initializes visibility of content after views measuring is finished.
		 */
		public void init() {
			if (mIsRightPaneShown) {
				showRightPane(0);
			} else {
				hideRightPane(0);
			}
			fadeViews();
		}

		public boolean isContentShown() {
			final int x;
			if (!mScroller.isFinished()) {
				x = mScroller.getFinalX();
			} else {
				x = mViewRightPaneContainer.getScrollX();
			}

			return x == 0;
		}

		public void release(final float delta, final float totalMove) {
			completeScrolling(delta);
		}

		/**
		 * Resets scroller controller. Stops flinging on current position.
		 */
		public void reset() {
			if (!mScroller.isFinished()) {
				mScroller.forceFinished(true);
			}
		}

		/**
		 * Processes auto-scrolling to bound which is closer to current
		 * position.
		 */
		@Override
		public void run() {
			if (mScroller.isFinished()) return;

			final boolean more = mScroller.computeScrollOffset();
			final int x = mScroller.getCurrX();
			final int diff = mLastFlingX - x;
			if (diff != 0) {
				mViewRightPaneContainer.scrollBy(diff, 0);
				mLastFlingX = x;
			}

			if (more) {
				mViewRightPaneContainer.post(this);
			}
		}

		public void showRightPane(final int duration) {
			mIsRightPaneShown = true;
			if (mViewRightPaneContainer.getMeasuredWidth() == 0 || mViewRightPaneContainer.getMeasuredHeight() == 0)
				return;

			final int startX = mViewRightPaneContainer.getScrollX();
			final int dx = startX;
			fling(startX, dx, duration);
		}

		/**
		 * Starts auto-scrolling to bound which is closer to current position.
		 * 
		 * @param delta
		 */
		private void completeScrolling(final float delta) {
			// if (delta == 0) return;
			if (delta > 0) {
				showRightPane(getFlingDuration());
			} else {
				hideRightPane(getFlingDuration());
			}
		}

		private void fling(final int startX, final int dx, final int duration) {
			reset();

			if (dx == 0) return;

			if (duration <= 0) {
				mViewRightPaneContainer.scrollBy(-dx, 0);
				return;
			}

			mScroller.startScroll(startX, 0, dx, 0, duration);

			mLastFlingX = startX;
			mViewRightPaneContainer.post(this);
		}

		/**
		 * Scrolling content view according by given value.
		 * 
		 * @param dx
		 */
		private void scrollBy(final int dx) {
			final int x = mViewRightPaneContainer.getScrollX();

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

			mViewRightPaneContainer.scrollBy(scrollBy, 0);
		}
	}
	
	private class SwipeFadeListener implements RightPaneLayout.OnSwipeListener {
		
		@Override
		public void onSwipe(final int scrollPosition) {
			fadeViews();
		}
		
	}
	
	private static class RightPaneLayout extends LinearLayout {

		private final Paint mFadePaint = new Paint();

		private int mFadeFactor = 0;
		private OnSwipeListener mOnSwipeListener;

		public RightPaneLayout(final SlidingPaneView parent) {
			super(parent.getContext());
			setOrientation(LinearLayout.HORIZONTAL);
		}

		public void invalidate(final int fadeFactor) {
			mFadeFactor = fadeFactor;
			invalidate();
		}

		public void setOnSwipeListener(final OnSwipeListener listener) {
			mOnSwipeListener = listener;
		}

		@Override
		protected void dispatchDraw(final Canvas canvas) {
			super.dispatchDraw(canvas);

			if (mFadeFactor > 0f) {
				mFadePaint.setColor(Color.argb(mFadeFactor, 0, 0, 0));
				canvas.drawRect(0, 0, getWidth(), getHeight(), mFadePaint);
			}
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

		private float mTempDelta, mTotalMove;
		private final int mScaledTouchSlop;
		ContentScrollController mController;

		public ScrollTouchInterceptor(final SlidingPaneView parent) {
			mScaledTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
			mController = parent.getController();
		}

		@Override
		public boolean onInterceptTouchEvent(final ViewGroup view, final MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					mTempDelta = 0;
					mTotalMove = 0;
					mController.reset();
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					final int hist_size = event.getHistorySize();
					if (hist_size == 0) {
						break;
					}
					mTempDelta = event.getX() - event.getHistoricalX(0);
					mTotalMove += mTempDelta;
					if (Math.abs(mTotalMove) >= mScaledTouchSlop) return true;
					break;
				}
				case MotionEvent.ACTION_UP: {
					mTempDelta = 0;
					mTotalMove = 0;
					break;
				}
				default: {
					mTempDelta = 0;
					mTotalMove = 0;
					break;
				}
			}
			return false;
		}

		@Override
		public boolean onTouchEvent(final ViewGroup view, final MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					mTempDelta = 0;
					mTotalMove = 0;
					mController.reset();
					break;
				}
				case MotionEvent.ACTION_MOVE: {
					final int hist_size = event.getHistorySize();
					if (hist_size == 0) {
						break;
					}
					final float distanceX = mTempDelta = event.getX() - event.getHistoricalX(0);
					mTotalMove += mTempDelta;
					mController.scrollBy((int) -distanceX);
					break;
				}
				case MotionEvent.ACTION_UP: {
					mController.release(-mTempDelta, -mTotalMove);
					mTempDelta = 0;
					mTotalMove = 0;
					break;
				}
				default: {
					mTempDelta = 0;
					mTotalMove = 0;
					break;
				}
			}
			return false;
		}
	}
}
