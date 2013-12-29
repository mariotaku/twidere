package org.mariotaku.twidere.view.iface;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.IBinder;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ThemeUtils;

public interface ICardItemView extends IColorLabelView {

	public View getFakeOverflowButton();

	public boolean isGap();

	public void setActivatedIndicator(Drawable activatedIndicator);

	public void setIsGap(boolean isGap);

	public void setItemBackground(Drawable itemBackground);

	public void setItemSelector(Drawable itemSelector);

	public void setOnOverflowIconClickListener(final OnOverflowIconClickListener listener);

	public void setOverflowIcon(Drawable overflowIcon);

	public static final class DrawingHelper {

		private final View mView;

		private final int mCardGapHeight;
		private final String mCardGapText;
		private final Paint mGapTextPaint;

		private final Rect mGapTextBounds = new Rect();
		private final Rect mBackgroundPadding = new Rect();
		private final Rect mOverflowIconBounds = new Rect();

		private Drawable mBackground;
		private Drawable mItemSelector;
		private Drawable mActivatedIndicator;
		private Drawable mOverflowIcon;

		private boolean mIsGap;

		private final int mThemeColor;

		private final GestureDetector mOverflowIconGestureDetector;

		private OnOverflowIconClickListener mOnOverflowIconClickListener;

		private final FakeOverflowButton mFakeOverflowButton;

		public DrawingHelper(final View view, final Context context, final AttributeSet attrs, final int defStyleAttr) {
			mView = view;
			final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CardItemView, defStyleAttr,
					R.style.Widget_CardItemView);
			mGapTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCardGapHeight = a.getDimensionPixelSize(R.styleable.CardItemView_cardGapHeight, 0);
			mCardGapText = a.getString(R.styleable.CardItemView_cardGapText);
			mGapTextPaint.setColor(a.getColor(R.styleable.CardItemView_cardGapTextColor, Color.GRAY));
			mGapTextPaint.setTextSize(a.getDimensionPixelSize(R.styleable.CardItemView_cardGapTextSize, 18));
			mGapTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
			mThemeColor = ThemeUtils.getUserThemeColor(context);
			mOverflowIconGestureDetector = new GestureDetector(context, new OverflowIconGestureListener(this));
			mFakeOverflowButton = new FakeOverflowButton(this);
			setItemBackground(a.getDrawable(R.styleable.CardItemView_cardBackground));
			setItemSelector(a.getDrawable(R.styleable.CardItemView_cardSelector));
			setActivatedIndicator(a.getDrawable(R.styleable.CardItemView_cardActivatedIndicator));
			setOverflowIcon(a.getDrawable(R.styleable.CardItemView_cardOverflowIcon));
			a.recycle();
		}

		public void dispatchDrawableStateChanged() {
			final int[] state = mView.getDrawableState();
			if (mBackground != null) {
				mBackground.setState(state);
			}
			if (mItemSelector != null) {
				mItemSelector.setState(state);
			}
			if (mActivatedIndicator != null) {
				mActivatedIndicator.setState(state);
			}
		}

		public void dispatchOnSizeChanged(final int w, final int h, final int oldw, final int oldh) {
			final int paddingLeft = mView.getPaddingLeft();
			final int paddingTop = mView.getPaddingTop();
			final int paddingRight = mView.getPaddingRight();
			final int paddingBottom = mView.getPaddingBottom();
			final int l = paddingLeft, t = paddingTop, r = w - paddingRight, b = h - paddingBottom;
			if (mBackground != null) {
				if (mBackground instanceof NinePatchDrawable) {
					final NinePatchDrawable npd = (NinePatchDrawable) mBackground;
					npd.getPadding(mBackgroundPadding);
					npd.setBounds(l - mBackgroundPadding.left, t - mBackgroundPadding.top,
							r + mBackgroundPadding.right, b + mBackgroundPadding.bottom);
				} else {
					mBackground.setBounds(l, t, r, b);
				}
			}
			if (mItemSelector != null) {
				if (mIsGap) {
					mItemSelector.setBounds(0, 0, w, h);
				} else {
					mItemSelector.setBounds(l, t, r, b);
				}
			}
			if (mActivatedIndicator != null) {
				mActivatedIndicator.setBounds(l, t, r, b);
			}
			if (mOverflowIcon != null) {
				final int iw = mOverflowIcon.getIntrinsicWidth();
				final int ih = mOverflowIcon.getIntrinsicHeight();
				mOverflowIconBounds.set(w - iw, 0, w, ih);
				mOverflowIcon.setBounds(mOverflowIconBounds);
			}
		}

		public void drawBackground(final Canvas canvas) {
			if (mBackground != null && !mIsGap) {
				mBackground.draw(canvas);
			}
		}

		public void drawGap(final Canvas canvas) {
			if (mIsGap) {
				final int centerX = mView.getWidth() / 2, centerY = mView.getHeight() / 2;
				if (mCardGapText != null) {
					mGapTextPaint.getTextBounds(mCardGapText, 0, mCardGapText.length(), mGapTextBounds);
					canvas.drawText(mCardGapText, centerX - mGapTextBounds.width() / 2,
							centerY + mGapTextBounds.height() / 2, mGapTextPaint);
				}
			}
		}

		public void drawOverflowIcon(final Canvas canvas) {
			if (mOverflowIcon != null && mOnOverflowIconClickListener != null) {
				mOverflowIcon.draw(canvas);
			}
		}

		public void drawSelector(final Canvas canvas) {
			if (mActivatedIndicator != null) {
				mActivatedIndicator.draw(canvas);
			}
			if (mItemSelector != null) {
				mItemSelector.draw(canvas);
			}
		}

		public int getCardGapHeight() {
			return mCardGapHeight;
		}

		public View getFakeOverflowButton() {
			return mFakeOverflowButton;
		}

		public boolean handleOverflowTouchEvent(final MotionEvent ev) {
			return mOverflowIconGestureDetector.onTouchEvent(ev);
		}

		public boolean isGap() {
			return mIsGap;
		}

		public boolean isOverflowIconClicked(final MotionEvent ev) {
			final int x = Math.round(ev.getX()), y = Math.round(ev.getY());
			if (mOverflowIconBounds.contains(x, y)) return true;
			return false;
		}

		public void setActivatedIndicator(final Drawable activatedIndicator) {
			preSetDrawable(mActivatedIndicator);
			mActivatedIndicator = activatedIndicator;
			if (activatedIndicator != null) {
				activatedIndicator.setAlpha(0x80);
			}
			postSetDrawable(activatedIndicator);
		}

		public void setIsGap(final boolean isGap) {
			mIsGap = isGap;
			mView.requestLayout();
		}

		public void setItemBackground(final Drawable itemBackground) {
			preSetDrawable(mBackground);
			mBackground = itemBackground;
			if (itemBackground != null) {
				itemBackground.setAlpha(0x80);
			}
			postSetDrawable(itemBackground);
		}

		public void setItemSelector(final Drawable itemSelector) {
			preSetDrawable(mItemSelector);
			mItemSelector = itemSelector;
			if (itemSelector != null) {
				itemSelector.setAlpha(0x80);
			}
			postSetDrawable(itemSelector);
		}

		public void setOnOverflowIconClickListener(final OnOverflowIconClickListener listener) {
			mOnOverflowIconClickListener = listener;
		}

		public void setOverflowIcon(final Drawable overflowIcon) {
			preSetDrawable(mOverflowIcon);
			mOverflowIcon = overflowIcon;
			if (mOverflowIcon != null) {
				mOverflowIcon.mutate();
			}
			postSetDrawable(overflowIcon);
		}

		public boolean verifyDrawable(final Drawable who) {
			return who == mBackground || who == mItemSelector || who == mActivatedIndicator || who == mOverflowIcon;
		}

		private void postSetDrawable(final Drawable curr) {
			mView.setWillNotDraw(verifyDrawable(curr));
			if (curr != null) {
				if (curr.isStateful()) {
					curr.setState(mView.getDrawableState());
				}
				curr.setCallback(mView);
			}
		}

		private void preSetDrawable(final Drawable prev) {
			if (prev != null) {
				mView.unscheduleDrawable(prev);
				prev.setCallback(null);
			}
		}

		static class FakeOverflowButton extends View {

			private final DrawingHelper mHelper;

			public FakeOverflowButton(final DrawingHelper helper) {
				super(helper.mView.getContext());
				mHelper = helper;
			}

			@Override
			public void getLocationInWindow(final int[] location) {
				mHelper.mView.getLocationInWindow(location);
				location[0] += mHelper.mOverflowIconBounds.left;
				location[1] += mHelper.mOverflowIconBounds.top;
			}

			@Override
			public void getLocationOnScreen(final int[] location) {
				mHelper.mView.getLocationOnScreen(location);
				location[0] += mHelper.mOverflowIconBounds.left;
				location[1] += mHelper.mOverflowIconBounds.top;
			}

			@Override
			public View getRootView() {
				return mHelper.mView.getRootView();
			}

			@Override
			public Object getTag() {
				return mHelper.mView.getTag();
			}

			@Override
			public IBinder getWindowToken() {
				return mHelper.mView.getWindowToken();
			}

			@Override
			public void getWindowVisibleDisplayFrame(final Rect outRect) {
				mHelper.mView.getWindowVisibleDisplayFrame(outRect);
			}

		}

		static class OverflowIconGestureListener extends SimpleOnGestureListener {

			private final DrawingHelper mDrawingHelper;

			public OverflowIconGestureListener(final DrawingHelper drawingHelper) {
				mDrawingHelper = drawingHelper;
			}

			@Override
			public boolean onDown(final MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
					final float velocityY) {
				return clearHighlight();
			}

			@Override
			public void onLongPress(final MotionEvent e) {
				clearHighlight();
			}

			@Override
			public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX,
					final float distanceY) {
				return clearHighlight();
			}

			@Override
			public void onShowPress(final MotionEvent e) {
				final Drawable d = mDrawingHelper.mOverflowIcon;
				final int c = mDrawingHelper.mThemeColor;
				if (d != null) {
					d.setColorFilter(c, PorterDuff.Mode.SRC_ATOP);
				}
			}

			@Override
			public boolean onSingleTapConfirmed(final MotionEvent e) {
				final OnOverflowIconClickListener l = mDrawingHelper.mOnOverflowIconClickListener;
				if (clearHighlight() && l != null) {
					l.onOverflowIconClick(mDrawingHelper.mFakeOverflowButton);
				}
				return false;
			}

			@Override
			public boolean onSingleTapUp(final MotionEvent e) {
				return clearHighlight();
			}

			private boolean clearHighlight() {
				final Drawable d = mDrawingHelper.mOverflowIcon;
				if (d != null) {
					d.clearColorFilter();
					return true;
				}
				return false;
			}

		}
	}

	public static interface OnOverflowIconClickListener {

		public void onOverflowIconClick(View view);

	}
}
