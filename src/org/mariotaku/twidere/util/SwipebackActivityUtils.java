package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.view.View;
import android.view.Window;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.app.TwidereApplication;

import java.util.WeakHashMap;

public class SwipebackActivityUtils implements TwidereConstants {

	public static void setActivityScreenshot(final Activity activity, final Intent target) {
		if (activity == null || target == null) return;
		final TwidereApplication app = TwidereApplication.getInstance(activity);
		final SwipebackScreenshotManager sm = app.getSwipebackScreenshotManager();
		final long key = System.currentTimeMillis();
		final Bitmap sc = getActivityScreenshot(activity, View.DRAWING_CACHE_QUALITY_LOW);
		sm.put(key, sc);
		target.putExtra(EXTRA_ACTIVITY_SCREENSHOT_ID, key);
	}

	public static void startSwipebackActivity(final Activity activity, final Intent intent) {
		setActivityScreenshot(activity, intent);
		activity.startActivityForResult(intent, REQUEST_SWIPEBACK_ACTIVITY);
	}

	/**
	 * 
	 * May cause OutOfMemoryError
	 * 
	 * @param activity
	 * @param cacheQuality
	 * @return Activity screenshot
	 */
	private static Bitmap getActivityScreenshot(final Activity activity, final int cacheQuality) {
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
		// Remove window background behind status bar.
		final Canvas c = new Canvas(b);
		final Paint paint = new Paint();
		paint.setColor(Color.TRANSPARENT);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		c.drawRect(frame.left, 0, frame.right, frame.top, paint);
		view.setDrawingCacheEnabled(prevState);
		view.setDrawingCacheQuality(prevQuality);
		return b;
	}

	public static class SwipebackScreenshotManager {

		private final WeakHashMap<Long, Bitmap> mCache = new WeakHashMap<Long, Bitmap>();

		public Bitmap get(final long id) {
			return mCache.get(id);
		}

		public void put(final long id, final Bitmap bitmap) {
			mCache.put(id, bitmap);
		}

		public Bitmap remove(final long id) {
			return mCache.remove(id);
		}

	}
}
