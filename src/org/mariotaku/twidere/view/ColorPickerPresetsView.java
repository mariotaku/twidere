package org.mariotaku.twidere.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

public class ColorPickerPresetsView extends LinearLayout implements View.OnClickListener, Constants {

	private final static int[] COLORS = { HOLO_RED_DARK, HOLO_RED_LIGHT, HOLO_ORANGE_DARK, HOLO_ORANGE_LIGHT,
			HOLO_GREEN_LIGHT, HOLO_GREEN_DARK, HOLO_BLUE_LIGHT, HOLO_BLUE_DARK, HOLO_PURPLE_DARK, HOLO_PURPLE_LIGHT,
			Color.WHITE };

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
		final LayoutInflater inflater = LayoutInflater.from(context);
		for (final int color : COLORS) {
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
