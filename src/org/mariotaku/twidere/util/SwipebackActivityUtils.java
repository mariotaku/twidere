package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SwipebackActivityUtils implements TwidereConstants {

	public static void setActivityScreenshot(final Activity activity, final Intent target) {
		if (activity == null || target == null) return;
		final SharedPreferences prefs = activity.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (!prefs.getBoolean(PREFERENCE_KEY_SWIPE_BACK, true)) return;
		final TwidereApplication app = TwidereApplication.getInstance(activity);
		final SwipebackScreenshotManager sm = app.getSwipebackScreenshotManager();
		final long key = System.currentTimeMillis();
		final Bitmap sc = getActivityScreenshot(activity, View.DRAWING_CACHE_QUALITY_LOW);
		sm.put(key, sc, ThemeUtils.isTransparentBackground(activity));
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
		try {
			return getActivityScreenshotInternal(activity, cacheQuality);
		} catch (final OutOfMemoryError oom) {
			return null;
		} catch (final StackOverflowError sof) {
			return null;
		}
	}

	/**
	 * 
	 * May cause OutOfMemoryError
	 * 
	 * @param activity
	 * @param cacheQuality
	 * @return Activity screenshot
	 */
	private static Bitmap getActivityScreenshotInternal(final Activity activity, final int cacheQuality) {
		if (activity == null) return null;
		final Window w = activity.getWindow();
		final View view = w.getDecorView();
		final int width = view.getWidth(), height = view.getHeight();
		if (width <= 0 || height <= 0) return null;
		final Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		final Rect frame = new Rect();
		view.getWindowVisibleDisplayFrame(frame);
		// Remove window background behind status bar.
		final Canvas c = new Canvas(b);
		view.draw(c);
		final Paint paint = new Paint();
		paint.setColor(Color.TRANSPARENT);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		c.drawRect(frame.left, 0, frame.right, frame.top, paint);
		return b;
	}

	public static class SwipebackScreenshotManager {

		private final static String DIR_NAME_SWIPEBACK_CACHE = "swipeback_cache";
		private static final CompressFormat COMPRESS_FORMAT = Bitmap.CompressFormat.JPEG;
		private static final CompressFormat COMPRESS_FORMAT_TRANSPARENT = Bitmap.CompressFormat.PNG;
		private final File mCacheDir;

		public SwipebackScreenshotManager(final Context context) {
			mCacheDir = Utils.getBestCacheDir(context, DIR_NAME_SWIPEBACK_CACHE);
		}

		public Bitmap get(final long id) {
			final File f = new File(mCacheDir, String.valueOf(id));
			if (!f.exists()) return null;
			try {
				return BitmapFactory.decodeFile(f.getAbsolutePath());
			} catch (final OutOfMemoryError e) {
				return null;
			} finally {
				System.gc();
			}
		}

		public void put(final long id, final Bitmap bitmap, final boolean alphaChannel) {
			if (bitmap == null || bitmap.isRecycled()) return;
			try {
				final OutputStream os = new FileOutputStream(new File(mCacheDir, String.valueOf(id)));
				bitmap.compress(alphaChannel ? COMPRESS_FORMAT_TRANSPARENT : COMPRESS_FORMAT, 75, os);
				bitmap.recycle();
			} catch (final IOException e) {
				e.printStackTrace();
			} finally {
				System.gc();
			}
		}

		public boolean remove(final long id) {
			final File f = new File(mCacheDir, String.valueOf(id));
			if (!f.exists()) return false;
			try {
				return f.delete();
			} finally {
				System.gc();
			}
		}

	}
}
