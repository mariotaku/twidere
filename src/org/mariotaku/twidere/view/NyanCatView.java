package org.mariotaku.twidere.view;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class NyanCatView extends ImageView {

	public NyanCatView(final Context context) {
		super(context);
	}

	public NyanCatView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public NyanCatView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		setBackgroundResource(R.drawable.nyan_sakamoto);
		((AnimationDrawable) getBackground()).start();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		setBackgroundResource(0);
	}

}
