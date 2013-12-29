package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.mariotaku.twidere.view.iface.IForegroundView;

public class ForegroundImageView extends ImageView implements IForegroundView {

	private final ForegroundViewHelper mForegroundViewHelper;

	public ForegroundImageView(final Context context) {
		this(context, null);
	}

	public ForegroundImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ForegroundImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mForegroundViewHelper = new ForegroundViewHelper(this, context, attrs, defStyle);
	}

	@Override
	public Drawable getForeground() {
		return mForegroundViewHelper.getForeground();
	}

	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		mForegroundViewHelper.jumpDrawablesToCurrentState();
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
	@Override
	public void setForeground(final Drawable drawable) {
		mForegroundViewHelper.setForeground(drawable);
	}

	/**
	 * Describes how the foreground is positioned. Defaults to START and TOP.
	 * 
	 * @param foregroundGravity See {@link android.view.Gravity}
	 * 
	 * @attr ref android.R.styleable#FrameLayout_foregroundGravity
	 */
	@Override
	public void setForegroundGravity(final int foregroundGravity) {
		mForegroundViewHelper.setForegroundGravity(foregroundGravity);
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();
		mForegroundViewHelper.drawableStateChanged();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		mForegroundViewHelper.onDraw(canvas);
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		mForegroundViewHelper.onLayout(changed, left, top, right, bottom);
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mForegroundViewHelper.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	protected boolean verifyDrawable(final Drawable who) {
		return super.verifyDrawable(who) || mForegroundViewHelper.verifyDrawable(who);
	}

}
