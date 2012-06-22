package org.mariotaku.twidere.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * 
 * @author mariotaku
 * 
 *         Get the main color from a {@link Bitmap}<br>
 * <br>
 * 
 *         <b>Important</b>: I recommand using this method in different thread.
 *         Or it will make your application laggy or unresponceable.
 */
public class ColorAnalyser {

	/**
	 * 
	 * Get the main color from a {@link Bitmap}.<br>
	 * 
	 * @param bitmap The {@link Bitmap} to analyse
	 * @return The rgb {@link Color} in integer (no alpha)
	 */
	public static int analyse(Bitmap bitmap) {

		return analyse(bitmap, 18, 28);
	}

	/**
	 * 
	 * Get the main color from a {@link Bitmap}.<br>
	 * 
	 * @param bitmap The {@link Bitmap} to analyse
	 * @param width The desired width of scaled bitmap
	 * @param height The desired height of scaled bitmap
	 * @return The rgb {@link Color} in integer (no alpha)
	 */
	public static int analyse(Bitmap bitmap, int width, int height) {

		if (bitmap == null) return Color.WHITE;

		int color = 0;

		final HashMap<Float, Integer> colorsMap = new HashMap<Float, Integer>();
		final ArrayList<Float> colorsScore = new ArrayList<Float>();

		final Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, false);

		for (int y = 0; y < resized.getHeight(); y++) {
			for (int x = 0; x < resized.getWidth(); x++) {
				final int temp_color = resized.getPixel(x, y);
				color = Color.argb(0xFF, Color.red(temp_color), Color.green(temp_color), Color.blue(temp_color));
				final float[] hsv = new float[3];
				Color.colorToHSV(color, hsv);

				final float score = (hsv[1] * hsv[1] + 0.001f) * (hsv[2] * hsv[2]);

				colorsMap.put(score, color);
				colorsScore.add(score);
			}
		}

		Collections.sort(colorsScore);
		bitmap.recycle();
		resized.recycle();
		return colorsMap.get(colorsScore.get(colorsScore.size() - 1));

	}

	/**
	 * 
	 * Get the main color from a {@link Bitmap}.<br>
	 * 
	 * @param bitmap The {@link Bitmap} to analyse
	 * @param width The desired width of scaled bitmap
	 * @param height The desired height of scaled bitmap
	 * @param def The default color returned, if bitmap is null
	 * @return The rgb {@link Color} in integer (no alpha)
	 */
	public static int analyse(Bitmap bitmap, int width, int height, int def) {

		if (bitmap == null) return def;

		int color = 0;

		final HashMap<Float, Integer> colorsMap = new HashMap<Float, Integer>();
		final ArrayList<Float> colorsScore = new ArrayList<Float>();

		final Bitmap resized = Bitmap.createScaledBitmap(bitmap, width, height, false);

		for (int y = 0; y < resized.getHeight(); y++) {
			for (int x = 0; x < resized.getWidth(); x++) {
				final int temp_color = resized.getPixel(x, y);
				color = Color.argb(0xFF, Color.red(temp_color), Color.green(temp_color), Color.blue(temp_color));
				final float[] hsv = new float[3];
				Color.colorToHSV(color, hsv);

				final float score = (hsv[1] * hsv[1] + 0.001f) * (hsv[2] * hsv[2]);

				colorsMap.put(score, color);
				colorsScore.add(score);
			}
		}

		Collections.sort(colorsScore);
		bitmap.recycle();
		resized.recycle();
		return colorsMap.get(colorsScore.get(colorsScore.size() - 1));

	}
}