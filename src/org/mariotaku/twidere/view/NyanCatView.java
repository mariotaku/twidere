package org.mariotaku.twidere.view;

import org.mariotaku.twidere.R;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class NyanCatView extends ImageView {

	public NyanCatView(final Context context) {
		this(context, null);
	}

	public NyanCatView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public NyanCatView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setBackgroundResource(R.drawable.nyan_sakamoto_8x);
		((AnimationDrawable) getBackground()).start();
	}

}
