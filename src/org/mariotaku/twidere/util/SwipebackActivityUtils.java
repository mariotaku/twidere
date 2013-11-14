package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;

import org.mariotaku.twidere.TwidereConstants;

import java.io.ByteArrayOutputStream;

public class SwipebackActivityUtils implements TwidereConstants {

	public static Bitmap getActivityScreenshot(final Activity activity, final int cacheQuality) {
		if (activity == null) return null;
		final Window w = activity.getWindow();
		final View view = w.getDecorView();
		final boolean prevState = view.isDrawingCacheEnabled();
		final int prevQuality = view.getDrawingCacheQuality();
		view.setDrawingCacheEnabled(true);
		view.setDrawingCacheQuality(cacheQuality);
		view.buildDrawingCache();
		final Bitmap cache = view.getDrawingCache();
		if (cache == null) return null;
		final Bitmap b = Bitmap.createBitmap(cache);
		final Rect frame = new Rect();
		view.getWindowVisibleDisplayFrame(frame);
		// Why draw a black rectangle here?
		// If the activity uses light theme, the screenshot will have a white
		// bar on the top, this workaround can solve that issue.
		final Canvas c = new Canvas(b);
		final Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		c.drawRect(frame.left, 0, frame.right, frame.top, paint);
		view.setDrawingCacheEnabled(prevState);
		view.setDrawingCacheQuality(prevQuality);
		return b;
	}

	public static byte[] getEncodedActivityScreenshot(final Activity activity, final int cacheQuality,
			final Bitmap.CompressFormat encodeFormat, final int encodeQuality) {
		final Bitmap b = getActivityScreenshot(activity, cacheQuality);
		if (b == null) return null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		b.compress(encodeFormat, encodeQuality, baos);
		b.recycle();
		return baos.toByteArray();
	}

	public static void setActivityScreenshot(final Activity activity, final Intent target) {
		final byte[] encoded_screenshot = getEncodedActivityScreenshot(activity, View.DRAWING_CACHE_QUALITY_LOW,
				Bitmap.CompressFormat.JPEG, 60);
		target.putExtra(EXTRA_ACTIVITY_SCREENSHOT_ENCODED, encoded_screenshot);
	}

	public static void startSwipebackActivity(final Activity activity, final Intent intent) {
		setActivityScreenshot(activity, intent);
		activity.startActivityForResult(intent, REQUEST_SWIPEBACK_ACTIVITY);
	}
}
