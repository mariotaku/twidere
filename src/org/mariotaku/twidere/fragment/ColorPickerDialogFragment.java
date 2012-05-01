package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.view.ColorPickerView;
import org.mariotaku.twidere.view.ColorPickerView.OnColorChangedListener;

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

public class ColorPickerDialogFragment extends BaseDialogFragment {

	private ColorPickerDialog mDialog;

	private OnColorSelectedListener mListener;

	private int mInitialColor;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			mInitialColor = savedInstanceState.getInt(Accounts.USER_COLOR, Color.WHITE);
		}
		if (getSherlockActivity() instanceof OnColorSelectedListener) {
			mListener = (OnColorSelectedListener) getSherlockActivity();
		}
		mDialog = new ColorPickerDialog(getSherlockActivity(), mInitialColor);
		return mDialog;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(Accounts.USER_COLOR, mInitialColor);
		super.onSaveInstanceState(outState);
	}

	public void setInitialColor(int color) {
		mInitialColor = color;
	}

	/**
	 * Set a OnColorChangedListener to get notified when the color selected by
	 * the user has changed.
	 * 
	 * @param listener
	 */
	public void setOnColorSelectedListener(OnColorSelectedListener listener) {
		mListener = listener;
	}

	public class ColorPickerDialog extends AlertDialog implements OnColorChangedListener, OnClickListener {

		private ColorPickerView mColorPicker;

		public ColorPickerDialog(Context context, int initialColor) {

			super(context);

			init(context, initialColor);
		}

		public int getColor() {

			return mColorPicker.getColor();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
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
		public void onColorChanged(int color) {
			mInitialColor = color;
			setIcon(new BitmapDrawable(getContext().getResources(), getPreviewBitmap(color)));

		}

		public void setAlphaSliderVisible(boolean visible) {

			mColorPicker.setAlphaSliderVisible(visible);
		}

		private Bitmap getPreviewBitmap(int color) {

			float density = getContext().getResources().getDisplayMetrics().density;
			int width = (int) (32 * density), height = (int) (32 * density);

			Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
			Canvas canvas = new Canvas(bm);

			int rectrangle_size = (int) (density * 5);
			int numRectanglesHorizontal = (int) Math.ceil(width / rectrangle_size);
			int numRectanglesVertical = (int) Math.ceil(height / rectrangle_size);
			Rect r = new Rect();
			boolean verticalStartWhite = true;
			for (int i = 0; i <= numRectanglesVertical; i++) {

				boolean isWhite = verticalStartWhite;
				for (int j = 0; j <= numRectanglesHorizontal; j++) {

					r.top = i * rectrangle_size;
					r.left = j * rectrangle_size;
					r.bottom = r.top + rectrangle_size;
					r.right = r.left + rectrangle_size;
					Paint paint = new Paint();
					paint.setColor(isWhite ? Color.WHITE : Color.GRAY);

					canvas.drawRect(r, paint);

					isWhite = !isWhite;
				}

				verticalStartWhite = !verticalStartWhite;

			}
			canvas.drawColor(color);
			Paint paint = new Paint();
			paint.setColor(Color.WHITE);
			paint.setStrokeWidth(2.0f);
			float[] points = new float[] { 0, 0, width, 0, 0, 0, 0, height, width, 0, width, height, 0, height, width,
					height };
			canvas.drawLines(points, paint);

			return bm;
		}

		private void init(Context context, int color) {

			// To fight color branding.
			getWindow().setFormat(PixelFormat.RGBA_8888);

			LinearLayout mContentView = new LinearLayout(context);
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
