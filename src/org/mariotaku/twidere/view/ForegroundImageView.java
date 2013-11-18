package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;

public class ForegroundImageView extends ImageView {

	private Drawable mForeground;

	private final Rect mSelfBounds = new Rect();
	private final Rect mOverlayBounds = new Rect();

	private int mForegroundGravity = Gravity.FILL;

	protected boolean mForegroundInPadding = true;

	boolean mForegroundBoundsChanged = false;

	public ForegroundImageView(final Context context) {
		this(context, null);
	}

	public ForegroundImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ForegroundImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final TypedArray a = context.obtainStyledAttributes(attrs, new int[] { android.R.attr.foreground,
				android.R.attr.foregroundGravity }, defStyle, 0);

		mForegroundGravity = a.getInt(1, mForegroundGravity);

		final Drawable d = a.getDrawable(0);
		if (d != null) {
			setForeground(d);
		}

		mForegroundInPadding = true;

		a.recycle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void draw(final Canvas canvas) {
		super.draw(canvas);

		if (mForeground != null) {
			final Drawable foreground = mForeground;

			if (mForegroundBoundsChanged) {
				mForegroundBoundsChanged = false;
				final Rect selfBounds = mSelfBounds;
				final Rect overlayBounds = mOverlayBounds;

				final int w = getRight() - getLeft();
				final int h = getBottom() - getTop();

				if (mForegroundInPadding) {
					selfBounds.set(0, 0, w, h);
				} else {
					selfBounds.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
				}

				final int layoutDirection = ViewCompat.getLayoutDirection(this);
				GravityCompat.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
						foreground.getIntrinsicHeight(), selfBounds, overlayBounds, layoutDirection);
				foreground.setBounds(overlayBounds);
			}

			foreground.draw(canvas);
		}
	}

	/**
	 * Returns the drawable used as the foreground of this FrameLayout. The
	 * foreground drawable, if non-null, is always drawn on top of the children.
	 * 
	 * @return A Drawable or null if no foreground was set.
	 */
	public Drawable getForeground() {
		return mForeground;
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mForeground != null) {
			mForeground.jumpToCurrentState();
		}
	}

	/**
	 * Supply a Drawable that is to be rendered on top of all of the child views
	 * in the frame layout. Any padding in the Drawable will be taken into
	 * account by ensuring that the children are inset to be placed inside of
	 * the padding area.
	 * 
	 * @param drawable The Drawable to be drawn on top of the children.
	 * 
	 * @attr ref android.R.styleable#FrameLayout_foreground
	 */
	public void setForeground(final Drawable drawable) {
		if (mForeground != drawable) {
			if (mForeground != null) {
				mForeground.setCallback(null);
				unscheduleDrawable(mForeground);
			}

			mForeground = drawable;

			if (drawable != null) {
				drawable.setCallback(this);
				if (drawable.isStateful()) {
					drawable.setState(getDrawableState());
				}
				if (mForegroundGravity == Gravity.FILL) {
					final Rect padding = new Rect();
					if (drawable.getPadding(padding)) {
					}
				}
			}
			requestLayout();
			invalidate();
		}
	}

	/**
	 * Describes how the foreground is positioned. Defaults to START and TOP.
	 * 
	 * @param foregroundGravity See {@link android.view.Gravity}
	 * 
	 * @attr ref android.R.styleable#FrameLayout_foregroundGravity
	 */
	public void setForegroundGravity(int foregroundGravity) {
		if (mForegroundGravity != foregroundGravity) {
			if ((foregroundGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
				foregroundGravity |= Gravity.START;
			}

			if ((foregroundGravity & Gravity.VERTICAL_GRAVITY_MASK) == 0) {
				foregroundGravity |= Gravity.TOP;
			}

			mForegroundGravity = foregroundGravity;

			if (mForegroundGravity == Gravity.FILL && mForeground != null) {
				final Rect padding = new Rect();
				if (mForeground.getPadding(padding)) {
				}
			} else {
			}

			requestLayout();
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		if (mForeground != null && mForeground.isStateful()) {
			mForeground.setState(getDrawableState());
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (mForeground != null) {
			final Drawable foreground = mForeground;

			if (mForegroundBoundsChanged) {
				mForegroundBoundsChanged = false;
				final Rect selfBounds = mSelfBounds;
				final Rect overlayBounds = mOverlayBounds;

				final int w = getRight() - getLeft();
				final int h = getBottom() - getTop();

				if (mForegroundInPadding) {
					selfBounds.set(0, 0, w, h);
				} else {
					selfBounds.set(getPaddingLeft(), getPaddingTop(), w - getPaddingRight(), h - getPaddingBottom());
				}

				final int layoutDirection = ViewCompat.getLayoutDirection(this);
				GravityCompat.apply(mForegroundGravity, foreground.getIntrinsicWidth(),
						foreground.getIntrinsicHeight(), selfBounds, overlayBounds, layoutDirection);
				foreground.setBounds(overlayBounds);
			}

			foreground.draw(canvas);
		}
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		mForegroundBoundsChanged = true;
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mForegroundBoundsChanged = true;
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return super.verifyDrawable(who) || who == mForeground;
	}

}
