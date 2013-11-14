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
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.view.ColorPickerView;
import org.mariotaku.twidere.view.ColorPickerView.OnColorChangedListener;

public class ColorPickerDialogFragment extends BaseSupportDialogFragment {

	private ColorPickerDialog mDialog;

	private OnColorSelectedListener mListener;

	private int mInitialColor;

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mInitialColor = savedInstanceState.getInt(EXTRA_COLOR, Color.WHITE);
		} else {
			mInitialColor = getArguments().getInt(EXTRA_COLOR, Color.WHITE);
		}
		if (getActivity() instanceof OnColorSelectedListener) {
			mListener = (OnColorSelectedListener) getActivity();
		}
		mDialog = new ColorPickerDialog(getActivity(), mInitialColor);
		return mDialog;
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putInt(Accounts.USER_COLOR, mInitialColor);
		super.onSaveInstanceState(outState);
	}

	public class ColorPickerDialog extends AlertDialog implements OnColorChangedListener, OnClickListener {

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
					if (mListener != null) {
						mListener.onColorSelected(mColorPicker.getColor());
					}
					break;
			}
			dismiss();

		}

		@Override
		public void onColorChanged(final int color) {
			mInitialColor = color;
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
			mContentView.setGravity(Gravity.CENTER);

			mColorPicker = new ColorPickerView(context);

			mContentView.addView(mColorPicker, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			mContentView.setPadding(Math.round(mColorPicker.getDrawingOffset()), 0,
					Math.round(mColorPicker.getDrawingOffset()), 0);

			mColorPicker.setOnColorChangedListener(this);
			mColorPicker.setColor(color, true);
			mColorPicker.setAlphaSliderVisible(true);

			setTitle(R.string.pick_color);
			setView(mContentView);

			setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), this);
			setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), this);

		}

	}

	public interface OnColorSelectedListener {

		public void onColorSelected(int color);
	}

}
