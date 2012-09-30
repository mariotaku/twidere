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

package org.mariotaku.twidere.view;

import org.mariotaku.twidere.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.CompoundButton;

/**
 * A Switch is a two-state toggle switch widget that can select between two
 * options. The user may drag the "thumb" back and forth to choose the selected
 * option, or simply tap to toggle as if it were a checkbox. The
 * {@link #setText(CharSequence) text} property controls the text displayed in
 * the label for the switch, whereas the {@link #setTextOff(CharSequence) off}
 * and {@link #setTextOn(CharSequence) on} text controls the text on the thumb.
 * Similarly, the {@link #setTextAppearance(android.content.Context, int)
 * textAppearance} and the related setTypeface() methods control the typeface
 * and style of label text, whereas the
 * {@link #setSwitchTextAppearance(android.content.Context, int)
 * switchTextAppearance} and the related seSwitchTypeface() methods control that
 * of the thumb.
 * 
 */
public class Switch extends CompoundButton {
	private static final int TOUCH_MODE_IDLE = 0;
	private static final int TOUCH_MODE_DOWN = 1;
	private static final int TOUCH_MODE_DRAGGING = 2;

	private final Drawable mThumbDrawable;
	private final Drawable mTrackDrawable;
	private final int mThumbTextPadding;
	private final int mSwitchMinWidth;
	private final int mSwitchPadding;
	private CharSequence mTextOn;
	private CharSequence mTextOff;

	private int mTouchMode;
	private final int mTouchSlop;
	private float mTouchX;
	private float mTouchY;
	private final VelocityTracker mVelocityTracker = VelocityTracker.obtain();
	private final int mMinFlingVelocity;

	private float mThumbPosition;
	private int mSwitchWidth;
	private int mSwitchHeight;
	private int mThumbWidth; // Does not include padding

	private int mSwitchLeft;
	private int mSwitchTop;
	private int mSwitchRight;
	private int mSwitchBottom;

	private final TextPaint mTextPaint;
	private ColorStateList mTextColors;
	private Layout mOnLayout;
	private Layout mOffLayout;

	private final Rect mTempRect = new Rect();

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	/**
	 * Construct a new Switch with default styling.
	 * 
	 * @param context The Context that will determine this widget's theming.
	 */
	public Switch(final Context context) {
		this(context, null);
	}

	/**
	 * Construct a new Switch with default styling, overriding specific style
	 * attributes as requested.
	 * 
	 * @param context The Context that will determine this widget's theming.
	 * @param attrs Specification of attributes that should deviate from default
	 *            styling.
	 */
	public Switch(final Context context, final AttributeSet attrs) {
		this(context, attrs, R.attr.switchStyle);
	}

	/**
	 * Construct a new Switch with a default style determined by the given theme
	 * attribute, overriding specific style attributes as requested.
	 * 
	 * @param context The Context that will determine this widget's theming.
	 * @param attrs Specification of attributes that should deviate from the
	 *            default styling.
	 * @param defStyle An attribute ID within the active theme containing a
	 *            reference to the default style for this widget. e.g.
	 *            android.R.attr.switchStyle.
	 */
	public Switch(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Switch, defStyle, 0);

		mThumbDrawable = a.getDrawable(R.styleable.Switch_thumb);
		mTrackDrawable = a.getDrawable(R.styleable.Switch_track);
		mTextOn = a.getText(R.styleable.Switch_textOn);
		mTextOff = a.getText(R.styleable.Switch_textOff);
		mThumbTextPadding = a.getDimensionPixelSize(R.styleable.Switch_thumbTextPadding, 0);
		mSwitchMinWidth = a.getDimensionPixelSize(R.styleable.Switch_switchMinWidth, 0);
		mSwitchPadding = a.getDimensionPixelSize(R.styleable.Switch_switchPadding, 0);

		final int appearance = a.getResourceId(R.styleable.Switch_switchTextAppearance, 0);
		if (appearance != 0) {
			setSwitchTextAppearance(context, appearance);
		}
		a.recycle();

		final ViewConfiguration config = ViewConfiguration.get(context);
		mTouchSlop = config.getScaledTouchSlop();
		mMinFlingVelocity = config.getScaledMinimumFlingVelocity();

		// Refresh display with current params
		refreshDrawableState();
		setChecked(isChecked());
	}

	@Override
	public int getCompoundPaddingRight() {
		int padding = super.getCompoundPaddingRight() + mSwitchWidth;
		if (!TextUtils.isEmpty(getText())) {
			padding += mSwitchPadding;
		}
		return padding;
	}

	/**
	 * Returns the text displayed when the button is not in the checked state.
	 */
	public CharSequence getTextOff() {
		return mTextOff;
	}

	/**
	 * Returns the text displayed when the button is in the checked state.
	 */
	public CharSequence getTextOn() {
		return mTextOn;
	}

	@Override
	public void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		if (mOnLayout == null) {
			mOnLayout = makeLayout(mTextOn);
		}
		if (mOffLayout == null) {
			mOffLayout = makeLayout(mTextOff);
		}

		mTrackDrawable.getPadding(mTempRect);
		final int maxTextWidth = Math.max(mOnLayout.getWidth(), mOffLayout.getWidth());
		final int switchWidth = Math.max(mSwitchMinWidth, maxTextWidth * 2 + mThumbTextPadding * 4 + mTempRect.left
				+ mTempRect.right);
		final int switchHeight = mTrackDrawable.getIntrinsicHeight();

		mThumbWidth = maxTextWidth + mThumbTextPadding * 2;

		switch (widthMode) {
			case MeasureSpec.AT_MOST:
				widthSize = Math.min(widthSize, switchWidth);
				break;

			case MeasureSpec.UNSPECIFIED:
				widthSize = switchWidth;
				break;

			case MeasureSpec.EXACTLY:
				// Just use what we were given
				break;
		}

		switch (heightMode) {
			case MeasureSpec.AT_MOST:
				heightSize = Math.min(heightSize, switchHeight);
				break;

			case MeasureSpec.UNSPECIFIED:
				heightSize = switchHeight;
				break;

			case MeasureSpec.EXACTLY:
				// Just use what we were given
				break;
		}

		mSwitchWidth = switchWidth;
		mSwitchHeight = switchHeight;

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int measuredHeight = getMeasuredHeight();
		if (measuredHeight < switchHeight) {
			setMeasuredDimension(getMeasuredWidth(), switchHeight);
		}
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		mVelocityTracker.addMovement(ev);
		final int action = ev.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				final float x = ev.getX();
				final float y = ev.getY();
				if (isEnabled() && hitThumb(x, y)) {
					mTouchMode = TOUCH_MODE_DOWN;
					mTouchX = x;
					mTouchY = y;
				}
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				switch (mTouchMode) {
					case TOUCH_MODE_IDLE:
						// Didn't target the thumb, treat normally.
						break;

					case TOUCH_MODE_DOWN: {
						final float x = ev.getX();
						final float y = ev.getY();
						if (Math.abs(x - mTouchX) > mTouchSlop || Math.abs(y - mTouchY) > mTouchSlop) {
							mTouchMode = TOUCH_MODE_DRAGGING;
							getParent().requestDisallowInterceptTouchEvent(true);
							mTouchX = x;
							mTouchY = y;
							return true;
						}
						break;
					}

					case TOUCH_MODE_DRAGGING: {
						final float x = ev.getX();
						final float dx = x - mTouchX;
						final float newPos = Math.max(0, Math.min(mThumbPosition + dx, getThumbScrollRange()));
						if (newPos != mThumbPosition) {
							mThumbPosition = newPos;
							mTouchX = x;
							invalidate();
						}
						return true;
					}
				}
				break;
			}

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL: {
				if (mTouchMode == TOUCH_MODE_DRAGGING) {
					stopDrag(ev);
					return true;
				}
				mTouchMode = TOUCH_MODE_IDLE;
				mVelocityTracker.clear();
				break;
			}
		}

		return super.onTouchEvent(ev);
	}

	@Override
	public void setChecked(final boolean checked) {
		super.setChecked(checked);
		mThumbPosition = checked ? getThumbScrollRange() : 0;
		invalidate();
	}

	/**
	 * Sets the switch text color, size, style, hint color, and highlight color
	 * from the specified TextAppearance resource.
	 */
	public void setSwitchTextAppearance(final Context context, final int resid) {
		final TypedArray appearance = context.obtainStyledAttributes(resid, R.styleable.TextAppearance);

		ColorStateList colors;
		int ts;

		colors = appearance.getColorStateList(R.styleable.TextAppearance_textColor);
		if (colors != null) {
			mTextColors = colors;
		} else {
			// If no color set in TextAppearance, default to the view's
			// textColor
			mTextColors = getTextColors();
		}

		ts = appearance.getDimensionPixelSize(R.styleable.TextAppearance_textSize, 0);
		if (ts != 0) {
			if (ts != mTextPaint.getTextSize()) {
				mTextPaint.setTextSize(ts);
				requestLayout();
			}
		}

		appearance.recycle();
	}

	/**
	 * Sets the typeface in which the text should be displayed on the switch.
	 * Note that not all Typeface families actually have bold and italic
	 * variants, so you may need to use
	 * {@link #setSwitchTypeface(Typeface, int)} to get the appearance that you
	 * actually want.
	 * 
	 * @attr ref android.R.styleable#TextView_typeface
	 * @attr ref android.R.styleable#TextView_textStyle
	 */
	public void setSwitchTypeface(final Typeface tf) {
		if (mTextPaint.getTypeface() != tf) {
			mTextPaint.setTypeface(tf);

			requestLayout();
			invalidate();
		}
	}

	/**
	 * Sets the typeface and style in which the text should be displayed on the
	 * switch, and turns on the fake bold and italic bits in the Paint if the
	 * Typeface that you provided does not have all the bits in the style that
	 * you specified.
	 */
	public void setSwitchTypeface(Typeface tf, final int style) {
		if (style > 0) {
			if (tf == null) {
				tf = Typeface.defaultFromStyle(style);
			} else {
				tf = Typeface.create(tf, style);
			}

			setSwitchTypeface(tf);
			// now compute what (if any) algorithmic styling is needed
			final int typefaceStyle = tf != null ? tf.getStyle() : 0;
			final int need = style & ~typefaceStyle;
			mTextPaint.setFakeBoldText((need & Typeface.BOLD) != 0);
			mTextPaint.setTextSkewX((need & Typeface.ITALIC) != 0 ? -0.25f : 0);
		} else {
			mTextPaint.setFakeBoldText(false);
			mTextPaint.setTextSkewX(0);
			setSwitchTypeface(tf);
		}
	}

	/**
	 * Sets the text displayed when the button is not in the checked state.
	 */
	public void setTextOff(final CharSequence textOff) {
		mTextOff = textOff;
		requestLayout();
	}

	/**
	 * Sets the text displayed when the button is in the checked state.
	 */
	public void setTextOn(final CharSequence textOn) {
		mTextOn = textOn;
		requestLayout();
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		final int[] myDrawableState = getDrawableState();

		// Set the state of the Drawable
		// Drawable may be null when checked state is set from XML, from super
		// constructor
		if (mThumbDrawable != null) {
			mThumbDrawable.setState(myDrawableState);
		}
		if (mTrackDrawable != null) {
			mTrackDrawable.setState(myDrawableState);
		}

		invalidate();
	}

	@Override
	protected int[] onCreateDrawableState(final int extraSpace) {
		final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
		if (isChecked()) {
			mergeDrawableStates(drawableState, CHECKED_STATE_SET);
		}
		return drawableState;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);

		// Draw the switch
		final int switchLeft = mSwitchLeft;
		final int switchTop = mSwitchTop;
		final int switchRight = mSwitchRight;
		final int switchBottom = mSwitchBottom;

		mTrackDrawable.setBounds(switchLeft, switchTop, switchRight, switchBottom);
		mTrackDrawable.draw(canvas);

		canvas.save();

		mTrackDrawable.getPadding(mTempRect);
		final int switchInnerLeft = switchLeft + mTempRect.left;
		final int switchInnerTop = switchTop + mTempRect.top;
		final int switchInnerRight = switchRight - mTempRect.right;
		final int switchInnerBottom = switchBottom - mTempRect.bottom;
		canvas.clipRect(switchInnerLeft, switchTop, switchInnerRight, switchBottom);

		mThumbDrawable.getPadding(mTempRect);
		final int thumbPos = (int) (mThumbPosition + 0.5f);
		final int thumbLeft = switchInnerLeft - mTempRect.left + thumbPos;
		final int thumbRight = switchInnerLeft + thumbPos + mThumbWidth + mTempRect.right;

		mThumbDrawable.setBounds(thumbLeft, switchTop, thumbRight, switchBottom);
		mThumbDrawable.draw(canvas);

		// mTextColors should not be null, but just in case
		if (mTextColors != null) {
			mTextPaint.setColor(mTextColors.getColorForState(getDrawableState(), mTextColors.getDefaultColor()));
		}
		mTextPaint.drawableState = getDrawableState();

		final Layout switchText = getTargetCheckedState() ? mOnLayout : mOffLayout;

		canvas.translate((thumbLeft + thumbRight) / 2 - switchText.getWidth() / 2, (switchInnerTop + switchInnerBottom)
				/ 2 - switchText.getHeight() / 2);
		switchText.draw(canvas);

		canvas.restore();
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		mThumbPosition = isChecked() ? getThumbScrollRange() : 0;

		final int switchRight = getWidth() - getPaddingRight();
		final int switchLeft = switchRight - mSwitchWidth;
		int switchTop = 0;
		int switchBottom = 0;
		switch (getGravity() & Gravity.VERTICAL_GRAVITY_MASK) {
			default:
			case Gravity.TOP:
				switchTop = getPaddingTop();
				switchBottom = switchTop + mSwitchHeight;
				break;

			case Gravity.CENTER_VERTICAL:
				switchTop = (getPaddingTop() + getHeight() - getPaddingBottom()) / 2 - mSwitchHeight / 2;
				switchBottom = switchTop + mSwitchHeight;
				break;

			case Gravity.BOTTOM:
				switchBottom = getHeight() - getPaddingBottom();
				switchTop = switchBottom - mSwitchHeight;
				break;
		}

		mSwitchLeft = switchLeft;
		mSwitchTop = switchTop;
		mSwitchBottom = switchBottom;
		mSwitchRight = switchRight;
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return super.verifyDrawable(who) || who == mThumbDrawable || who == mTrackDrawable;
	}

	private void animateThumbToCheckedState(final boolean newCheckedState) {
		setChecked(newCheckedState);
	}

	private void cancelSuperTouch(final MotionEvent ev) {
		final MotionEvent cancel = MotionEvent.obtain(ev);
		cancel.setAction(MotionEvent.ACTION_CANCEL);
		super.onTouchEvent(cancel);
		cancel.recycle();
	}

	private boolean getTargetCheckedState() {
		return mThumbPosition >= getThumbScrollRange() / 2;
	}

	private int getThumbScrollRange() {
		if (mTrackDrawable == null) return 0;
		mTrackDrawable.getPadding(mTempRect);
		return mSwitchWidth - mThumbWidth - mTempRect.left - mTempRect.right;
	}

	/**
	 * @return true if (x, y) is within the target area of the switch thumb
	 */
	private boolean hitThumb(final float x, final float y) {
		mThumbDrawable.getPadding(mTempRect);
		final int thumbTop = mSwitchTop - mTouchSlop;
		final int thumbLeft = mSwitchLeft + (int) (mThumbPosition + 0.5f) - mTouchSlop;
		final int thumbRight = thumbLeft + mThumbWidth + mTempRect.left + mTempRect.right + mTouchSlop;
		final int thumbBottom = mSwitchBottom + mTouchSlop;
		return x > thumbLeft && x < thumbRight && y > thumbTop && y < thumbBottom;
	}

	@SuppressLint("FloatMath")
	private Layout makeLayout(final CharSequence text) {
		return new StaticLayout(text, mTextPaint, (int) Math.ceil(Layout.getDesiredWidth(text, mTextPaint)),
				Layout.Alignment.ALIGN_NORMAL, 1.f, 0, true);
	}

	/**
	 * Called from onTouchEvent to end a drag operation.
	 * 
	 * @param ev Event that triggered the end of drag mode - ACTION_UP or
	 *            ACTION_CANCEL
	 */
	private void stopDrag(final MotionEvent ev) {
		mTouchMode = TOUCH_MODE_IDLE;
		// Up and not canceled, also checks the switch has not been disabled
		// during the drag
		final boolean commitChange = ev.getAction() == MotionEvent.ACTION_UP && isEnabled();

		cancelSuperTouch(ev);

		if (commitChange) {
			boolean newState;
			mVelocityTracker.computeCurrentVelocity(1000);
			final float xvel = mVelocityTracker.getXVelocity();
			if (Math.abs(xvel) > mMinFlingVelocity) {
				newState = xvel > 0;
			} else {
				newState = getTargetCheckedState();
			}
			animateThumbToCheckedState(newState);
		} else {
			animateThumbToCheckedState(isChecked());
		}
	}
}