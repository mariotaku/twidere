package org.mariotaku.twidere.view;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.mariotaku.twidere.R;

public class ColorPickerPresetsView extends LinearLayout implements View.OnClickListener {

	private final static int[] COLORS_RES = { android.R.color.holo_red_dark, android.R.color.holo_red_light,
			android.R.color.holo_orange_dark, android.R.color.holo_orange_light, android.R.color.holo_green_light,
			android.R.color.holo_green_dark, android.R.color.holo_blue_bright, android.R.color.holo_blue_light,
			android.R.color.holo_blue_dark, android.R.color.holo_purple, android.R.color.white };

	private OnColorClickListener mOnColorClickListener;

	public ColorPickerPresetsView(final Context context) {
		this(context, null);
	}

	public ColorPickerPresetsView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ColorPickerPresetsView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setOrientation(HORIZONTAL);
		final Resources res = getResources();
		final LayoutInflater inflater = LayoutInflater.from(context);
		for (final int resId : COLORS_RES) {
			final int color = res.getColor(resId);
			final ColorView v = (ColorView) inflater.inflate(R.layout.color_picker_preset_item, this, false);
			v.setColor(color);
			v.setOnClickListener(this);
			addView(v);
		}
	}

	@Override
	public void onClick(final View v) {
		if (!(v instanceof ColorView) || mOnColorClickListener == null) return;
		mOnColorClickListener.onColorClick(((ColorView) v).getColor());
	}

	public void setOnColorClickListener(final OnColorClickListener listener) {
		mOnColorClickListener = listener;
	}

	public interface OnColorClickListener {
		void onColorClick(int color);
	}
}
