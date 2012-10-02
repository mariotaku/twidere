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

import org.mariotaku.twidere.view.ColorPickerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * A preference type that allows a user to choose a time
 * 
 * @author Sergey Margaritov
 */
public class ColorPickerPreference extends Preference implements Preference.OnPreferenceClickListener {

	private View mView;
	private int mDefaultValue = Color.WHITE;
	private int mValue = Color.WHITE;
	private String mTitle = null;
	private float mDensity = 0;
	private boolean mAlphaSliderEnabled = false;

	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private static final String ATTR_DEFAULTVALUE = "defaultValue";
	private static final String ATTR_ALPHASLIDER = "alphaSlider";
	private static final String ATTR_DIALOGTITLE = "dialogTitle";
	private static final String ATTR_TITLE = "title";

	public ColorPickerPreference(final Context context) {
		super(context);
		init(context, null);
	}

	public ColorPickerPreference(final Context context, final AttributeSet attrs) {

		super(context, attrs);
		init(context, attrs);
	}

	public ColorPickerPreference(final Context context, final AttributeSet attrs, final int defStyle) {

		super(context, attrs, defStyle);
		init(context, attrs);
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {

		final ColorPickerDialog dialog = new ColorPickerDialog(getContext(), getValue());
		if (mTitle != null) {
			dialog.setTitle(mTitle);
		}
		if (mAlphaSliderEnabled) {
			dialog.setAlphaSliderVisible(true);
		}
		dialog.show();

		return false;
	}

	/**
	 * Toggle Alpha Slider visibility (by default it's disabled)
	 * 
	 * @param enable
	 */
	public void setAlphaSliderEnabled(final boolean enable) {

		mAlphaSliderEnabled = enable;
	}

	@Override
	protected void onBindView(final View view) {

		super.onBindView(view);
		mView = view;
		setPreviewColor();
	}

	@Override
	protected void onSetInitialValue(final boolean restoreValue, final Object defaultValue) {

		if (isPersistent()) {
			persistInt(restoreValue ? getValue() : (Integer) defaultValue);
		}

	}

	private Bitmap getPreviewBitmap() {

		final int d = (int) (mDensity * 31); // 30dip
		final int color = getValue();
		final Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
		final int w = bm.getWidth();
		final int h = bm.getHeight();
		int c = color;
		for (int i = 0; i < w; i++) {
			for (int j = i; j < h; j++) {
				c = i <= 1 || j <= 1 || i >= w - 2 || j >= h - 2 ? Color.GRAY : color;
				bm.setPixel(i, j, c);
				if (i != j) {
					bm.setPixel(j, i, c);
				}
			}
		}

		return bm;
	}

	private int getValue() {

		try {
			if (isPersistent()) {
				mValue = getPersistedInt(mDefaultValue);
			}
		} catch (final ClassCastException e) {
			mValue = mDefaultValue;
		}

		return mValue;
	}

	private void init(final Context context, final AttributeSet attrs) {

		mDensity = getContext().getResources().getDisplayMetrics().density;
		setOnPreferenceClickListener(this);
		if (attrs != null) {
			try {
				mTitle = context.getString(attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DIALOGTITLE, -1));
			} catch (final NotFoundException e) {
				mTitle = attrs.getAttributeValue(ANDROID_NS, ATTR_DIALOGTITLE);
			}

			if (mTitle == null) {
				try {
					mTitle = context.getString(attrs.getAttributeResourceValue(ANDROID_NS, ATTR_TITLE, -1));
				} catch (final NotFoundException e) {
					mTitle = attrs.getAttributeValue(ANDROID_NS, ATTR_TITLE);
				}
			}

			final String defaultValue = attrs.getAttributeValue(ANDROID_NS, ATTR_DEFAULTVALUE);
			if (defaultValue != null && defaultValue.startsWith("#")) {
				try {
					mDefaultValue = Color.parseColor(defaultValue);
				} catch (final IllegalArgumentException e) {
					Log.e("ColorPickerPreference", "Wrong color: " + defaultValue);
					mDefaultValue = Color.WHITE;
				}
			} else {
				final int colorResourceId = attrs.getAttributeResourceValue(ANDROID_NS, ATTR_DEFAULTVALUE, 0);
				if (colorResourceId != 0) {
					mDefaultValue = context.getResources().getColor(colorResourceId);
				}
			}
			mAlphaSliderEnabled = attrs.getAttributeBooleanValue(null, ATTR_ALPHASLIDER, false);
		}
		mValue = mDefaultValue;
	}

	private void setPreviewColor() {

		if (mView == null) return;
		final ImageView iView = new ImageView(getContext());
		final LinearLayout widgetFrameView = (LinearLayout) mView.findViewById(android.R.id.widget_frame);
		if (widgetFrameView == null) return;
		widgetFrameView.setPadding(widgetFrameView.getPaddingLeft(), widgetFrameView.getPaddingTop(),
				(int) (mDensity * 8), widgetFrameView.getPaddingBottom());
		// remove already create preview image
		final int count = widgetFrameView.getChildCount();
		if (count > 0) {
			widgetFrameView.removeViews(0, count);
		}
		widgetFrameView.addView(iView);
		iView.setBackgroundDrawable(new AlphaPatternDrawable((int) (5 * mDensity)));
		iView.setImageBitmap(getPreviewBitmap());
	}

	/**
	 * This drawable that draws a simple white and gray chessboard pattern. It's
	 * pattern you will often see as a background behind a partly transparent
	 * image in many applications.
	 * 
	 * @author Daniel Nilsson
	 */
	public class AlphaPatternDrawable extends Drawable {

		private int mRectangleSize = 10;

		private int numRectanglesHorizontal;
		private int numRectanglesVertical;

		/**
		 * Bitmap in which the pattern will be cahched.
		 */
		private Bitmap mBitmap;

		public AlphaPatternDrawable(final int rectangleSize) {

			mRectangleSize = rectangleSize;
		}

		@Override
		public void draw(final Canvas canvas) {

			canvas.drawBitmap(mBitmap, null, getBounds(), new Paint());
		}

		@Override
		public int getOpacity() {

			return 0;
		}

		@Override
		public void setAlpha(final int alpha) {

		}

		@Override
		public void setColorFilter(final ColorFilter cf) {

		}

		@Override
		protected void onBoundsChange(final Rect bounds) {

			super.onBoundsChange(bounds);

			final int height = bounds.height();
			final int width = bounds.width();

			numRectanglesHorizontal = (int) Math.ceil(width / mRectangleSize);
			numRectanglesVertical = (int) Math.ceil(height / mRectangleSize);

			generatePatternBitmap();

		}

		/**
		 * This will generate a bitmap with the pattern as big as the rectangle
		 * we were allow to draw on. We do this to chache the bitmap so we don't
		 * need to recreate it each time draw() is called since it takes a few
		 * milliseconds.
		 */
		private void generatePatternBitmap() {

			if (getBounds().width() <= 0 || getBounds().height() <= 0) return;

			mBitmap = Bitmap.createBitmap(getBounds().width(), getBounds().height(), Config.ARGB_8888);
			final Canvas canvas = new Canvas(mBitmap);

			final Rect r = new Rect();
			boolean verticalStartWhite = true;
			for (int i = 0; i <= numRectanglesVertical; i++) {

				boolean isWhite = verticalStartWhite;
				for (int j = 0; j <= numRectanglesHorizontal; j++) {

					r.top = i * mRectangleSize;
					r.left = j * mRectangleSize;
					r.bottom = r.top + mRectangleSize;
					r.right = r.left + mRectangleSize;

					final Paint paint = new Paint();
					paint.setColor(isWhite ? Color.WHITE : Color.GRAY);
					canvas.drawRect(r, paint);

					isWhite = !isWhite;
				}

				verticalStartWhite = !verticalStartWhite;

			}

		}

	}

	public class ColorPickerDialog extends AlertDialog implements OnClickListener {

		private ColorPickerView mColorPicker;

		public ColorPickerDialog(final Context context, final int initialColor) {

			super(context);

			init(context, initialColor);
		}

		public int getColor() {

			return mColorPicker.getColor();
		}

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case BUTTON_POSITIVE:
					final int color = mColorPicker.getColor();
					if (isPersistent()) {
						persistInt(color);
					}
					mValue = color;
					setPreviewColor();
					if (getOnPreferenceChangeListener() != null) {
						getOnPreferenceChangeListener().onPreferenceChange(ColorPickerPreference.this, color);
					}
					break;
			}
			dismiss();

		}

		public void onColorChanged(final int color) {

			setIcon(new BitmapDrawable(getContext().getResources(), getPreviewBitmap(color)));

		}

		public void setAlphaSliderVisible(final boolean visible) {

			mColorPicker.setAlphaSliderVisible(visible);
		}

		private Bitmap getPreviewBitmap(final int color) {

			final float density = getContext().getResources().getDisplayMetrics().density;
			final int width = (int) (32 * density), height = (int) (32 * density);

			final Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			final Canvas canvas = new Canvas(bm);

			final int rectrangle_size = (int) (density * 5);
			final int numRectanglesHorizontal = (int) Math.ceil(width / rectrangle_size);
			final int numRectanglesVertical = (int) Math.ceil(height / rectrangle_size);
			final Rect r = new Rect();
			boolean verticalStartWhite = true;
			for (int i = 0; i <= numRectanglesVertical; i++) {

				boolean isWhite = verticalStartWhite;
				for (int j = 0; j <= numRectanglesHorizontal; j++) {

					r.top = i * rectrangle_size;
					r.left = j * rectrangle_size;
					r.bottom = r.top + rectrangle_size;
					r.right = r.left + rectrangle_size;
					final Paint paint = new Paint();
					paint.setColor(isWhite ? Color.WHITE : Color.GRAY);

					canvas.drawRect(r, paint);

					isWhite = !isWhite;
				}

				verticalStartWhite = !verticalStartWhite;

			}
			canvas.drawColor(color);
			final Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setStrokeWidth(2.0f);
			final float[] points = new float[] { 0, 0, width, 0, 0, 0, 0, height, width, 0, width, height, 0, height,
					width, height };
			canvas.drawLines(points, paint);

			return bm;
		}

		private void init(final Context context, final int color) {

			// To fight color branding.
			getWindow().setFormat(PixelFormat.RGBA_8888);

			final LinearLayout mContentView = new LinearLayout(context);
			mContentView.setOrientation(LinearLayout.VERTICAL);

			mColorPicker = new ColorPickerView(context);

			mContentView.addView(mColorPicker, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			mContentView.setPadding(Math.round(mColorPicker.getDrawingOffset()), 0,
					Math.round(mColorPicker.getDrawingOffset()), 0);

			mColorPicker.setColor(color, true);

			setView(mContentView);

			setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
			setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

		}

	}
}
