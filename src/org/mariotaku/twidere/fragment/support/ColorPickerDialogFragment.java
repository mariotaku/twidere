/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ColorPickerPresetsView;
import org.mariotaku.twidere.view.ColorPickerPresetsView.OnColorClickListener;
import org.mariotaku.twidere.view.ColorPickerView;
import org.mariotaku.twidere.view.ColorPickerView.OnColorChangedListener;

public class ColorPickerDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final FragmentActivity a = getActivity();
		final Dialog d = getDialog();
		if (!(a instanceof OnColorSelectedListener) || !(d instanceof ColorPickerDialog)) return;
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final int color = ((ColorPickerDialog) d).getColor();
				((OnColorSelectedListener) a).onColorSelected(color);
				break;
		}
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final int color;
		final Bundle args = getArguments();
		if (savedInstanceState != null) {
			color = savedInstanceState.getInt(EXTRA_COLOR, Color.WHITE);
		} else {
			color = args.getInt(EXTRA_COLOR, Color.WHITE);
		}
		final boolean showAlphaSlider = args.getBoolean(EXTRA_ALPHA_SLIDER, true);
		final ColorPickerDialog d = new ColorPickerDialog(getActivity(), color, showAlphaSlider);
		d.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), this);
		d.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), this);
		return d;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		final Dialog d = getDialog();
		if (d instanceof ColorPickerDialog) {
			outState.putInt(EXTRA_COLOR, ((ColorPickerDialog) d).getColor());
		}
		super.onSaveInstanceState(outState);
	}

	public static final class ColorPickerDialog extends AlertDialog implements OnColorChangedListener,
			OnColorClickListener {

		private ColorPickerView mColorPicker;
		private ColorPickerPresetsView mColorPresets;

		public ColorPickerDialog(final Context context, final int initialColor, final boolean showAlphaSlider) {
			super(context);
			init(context, initialColor, showAlphaSlider);
		}

		public int getColor() {
			return mColorPicker.getColor();
		}

		@Override
		public final void onColorChanged(final int color) {
			final Context context = getContext();
			setIcon(new BitmapDrawable(context.getResources(), ColorPickerView.getColorPreviewBitmap(context, color)));
		}

		@Override
		public final void onColorClick(final int color) {
			if (mColorPicker == null) return;
			mColorPicker.setColor(color, true);
		}

		public final void setAlphaSliderVisible(final boolean visible) {
			mColorPicker.setAlphaSliderVisible(visible);
		}

		public final void setColor(final int color) {
			mColorPicker.setColor(color);
		}

		public final void setColor(final int color, final boolean callback) {
			mColorPicker.setColor(color, callback);
		}

		private void init(final Context context, final int color, final boolean showAlphaSlider) {

			// To fight color branding.
			getWindow().setFormat(PixelFormat.RGBA_8888);

			final LayoutInflater inflater = LayoutInflater.from(getContext());
			final View view = inflater.inflate(R.layout.color_picker, null);

			mColorPicker = (ColorPickerView) view.findViewById(R.id.color_picker);
			mColorPresets = (ColorPickerPresetsView) view.findViewById(R.id.color_presets);

			mColorPicker.setOnColorChangedListener(this);
			mColorPresets.setOnColorClickListener(this);

			setColor(color, true);
			setAlphaSliderVisible(showAlphaSlider);

			setTitle(R.string.pick_color);
			setView(view);
		}

	}

	public interface OnColorSelectedListener {

		public void onColorSelected(int color);
	}

}
