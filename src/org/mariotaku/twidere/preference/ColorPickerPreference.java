/*
 * Copyright (C) 2011 Sergey Margaritov
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

package org.mariotaku.twidere.preference;

import static org.mariotaku.twidere.util.Utils.getColorPreviewBitmap;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.mariotaku.twidere.graphic.AlphaPatternDrawable;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.ColorPickerView;
import org.mariotaku.twidere.view.ColorPickerView.OnColorChangedListener;

public class ColorPickerPreference extends DialogPreference implements DialogInterface.OnClickListener,
		OnColorChangedListener {

	private View mView;
	protected int mDefaultValue = Color.WHITE;
	private final float mDensity;
	private boolean mAlphaSliderEnabled = false;

	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private static final String ATTR_DEFAULTVALUE = "defaultValue";
	private static final String ATTR_ALPHASLIDER = "alphaSlider";

	private ColorPickerView mColorPicker;

	public ColorPickerPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ColorPickerPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		mDensity = context.getResources().getDisplayMetrics().density;
		init(context, attrs);
	}

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final int color = mColorPicker.getColor();
				if (isPersistent()) {
					persistInt(color);
				}
				setPreviewColor();
				final OnPreferenceChangeListener listener = getOnPreferenceChangeListener();
				if (listener != null) {
					listener.onPreferenceChange(this, color);
				}
				break;
		}
	}

	@Override
	public void onColorChanged(final int color) {
		final AlertDialog dialog = (AlertDialog) getDialog();
		if (dialog == null) return;
		final Context context = getContext();
		dialog.setIcon(new BitmapDrawable(context.getResources(), getColorPreviewBitmap(context, color)));
	}

	@Override
	public void setDefaultValue(final Object value) {
		if (!(value instanceof Integer)) return;
		mDefaultValue = (Integer) value;
	}

	protected void init(final Context context, final AttributeSet attrs) {
		if (attrs != null) {
			final String defaultValue = attrs.getAttributeValue(ANDROID_NS, ATTR_DEFAULTVALUE);
			if (defaultValue != null && defaultValue.startsWith("#")) {
				try {
					setDefaultValue(Color.parseColor(defaultValue));
				} catch (final IllegalArgumentException e) {
					Log.e("ColorPickerPreference", "Wrong color: " + defaultValue);
					setDefaultValue(Color.WHITE);
				}
			} else {
				final int colorResourceId = attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DEFAULTVALUE, 0);
				if (colorResourceId != 0) {
					setDefaultValue(context.getResources().getColor(colorResourceId));
				}
			}
			mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, ATTR_ALPHASLIDER, false);
		}
	}

	@Override
	protected void onBindView(final View view) {
		super.onBindView(view);
		mView = view;
		setPreviewColor();
	}

	@Override
	protected void onPrepareDialogBuilder(final Builder builder) {
		super.onPrepareDialogBuilder(builder);
		final Context context = getContext();
		final LinearLayout view = new LinearLayout(context);
		view.setOrientation(LinearLayout.VERTICAL);

		mColorPicker = new ColorPickerView(context);
		mColorPicker.setOnColorChangedListener(this);

		view.addView(mColorPicker, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		view.setPadding(Math.round(mColorPicker.getDrawingOffset()), 0, Math.round(mColorPicker.getDrawingOffset()), 0);

		final int val = getValue();
		mColorPicker.setColor(val, true);
		mColorPicker.setAlphaSliderVisible(mAlphaSliderEnabled);
		builder.setView(view);
		builder.setIcon(new BitmapDrawable(context.getResources(), getColorPreviewBitmap(context, val)));
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {
		if (isPersistent() && defaultValue instanceof Integer) {
			persistInt(restoreValue ? getValue() : (Integer) defaultValue);
		}
	}

	private int getValue() {
		try {
			if (isPersistent()) return getPersistedInt(mDefaultValue);
		} catch (final ClassCastException e) {
			e.printStackTrace();
		}
		return mDefaultValue;
	}

	private void setPreviewColor() {
		if (mView == null) return;
		final ImageView image_view = new ImageView(getContext());
		final View widget_frame_view = mView.findViewById(android.R.id.widget_frame);
		if (!(widget_frame_view instanceof ViewGroup)) return;
		final ViewGroup widget_frame = (ViewGroup) widget_frame_view;
		// widget_frame.setPadding(widget_frame.getPaddingLeft(),
		// widget_frame.getPaddingTop(), (int) (mDensity * 8),
		// widget_frame.getPaddingBottom());
		// remove already create preview image
		widget_frame.removeAllViews();
		widget_frame.addView(image_view);
		ViewAccessor.setBackground(image_view, new AlphaPatternDrawable((int) (5 * mDensity)));
		image_view.setImageBitmap(getColorPreviewBitmap(getContext(), getValue()));
	}
}
