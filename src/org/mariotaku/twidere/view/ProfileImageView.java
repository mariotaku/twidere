package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.graphic.DropShadowDrawable;

public class ProfileImageView extends HighlightImageView {

	private final Drawable mVerifiedDrawable, mProtectedDrawable;

	private boolean mIsVerified;
	private boolean mIsProtected;

	public ProfileImageView(final Context context) {
		this(context, null);
	}

	public ProfileImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ProfileImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final Resources res = context.getResources();
		mVerifiedDrawable = new DropShadowDrawable(res, R.drawable.ic_user_type_verified, 4, 0xa0000000);
		mProtectedDrawable = new DropShadowDrawable(res, R.drawable.ic_user_type_protected, 4, 0xa0000000);
	}

	public void setUserType(final boolean isVerified, final boolean isProtected) {
		mIsVerified = isVerified;
		mIsProtected = isProtected;
		invalidate();
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (mIsVerified) {
			mVerifiedDrawable.draw(canvas);
		}
		if (mIsProtected) {
			mProtectedDrawable.draw(canvas);
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		final int vw = mVerifiedDrawable.getIntrinsicWidth(), vh = mVerifiedDrawable.getIntrinsicHeight();
		final int pw = mProtectedDrawable.getIntrinsicWidth(), ph = mProtectedDrawable.getIntrinsicHeight();
		final int bottom = h, right = w;
		mVerifiedDrawable.setBounds(right - vw, bottom - vh, right, bottom);
		mProtectedDrawable.setBounds(right - pw, bottom - ph, right, bottom);
	}

}
