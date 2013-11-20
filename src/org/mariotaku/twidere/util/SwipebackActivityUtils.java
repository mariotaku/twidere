package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.ThemeUtils.isTransparentBackground;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.fragment.support.BaseSupportDialogFragment;
import org.mariotaku.twidere.task.AsyncTask;

import java.io.ByteArrayOutputStream;

public class SwipebackActivityUtils implements TwidereConstants {

	/**
	 * 
	 * May cause OutOfMemoryError
	 * 
	 * @param activity
	 * @param cacheQuality
	 * @return Activity screenshot
	 */
	@Deprecated
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

	public static byte[] getEncodedActivityScreenshot(final Activity activity, final int cacheQuality,
			final Bitmap.CompressFormat encodeFormat, final int encodeQuality) {
		if (activity == null) return null;
		final Window w = activity.getWindow();
		final View view = w.getDecorView();
		final boolean prevState = view.isDrawingCacheEnabled();
		final int prevQuality = view.getDrawingCacheQuality();
		view.setDrawingCacheEnabled(true);
		view.setDrawingCacheQuality(cacheQuality);
		view.buildDrawingCache();
		final Bitmap b = view.getDrawingCache();
		if (b == null) return null;
		final Rect frame = new Rect();
		view.getWindowVisibleDisplayFrame(frame);
		// Remove window background behind status bar.
		final Canvas c = new Canvas(b);
		final Paint paint = new Paint();
		paint.setColor(Color.TRANSPARENT);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		c.drawRect(frame.left, 0, frame.right, frame.top, paint);
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		b.compress(encodeFormat, encodeQuality, baos);
		view.setDrawingCacheEnabled(prevState);
		view.setDrawingCacheQuality(prevQuality);
		if (!b.isRecycled()) {
			b.recycle();
		}
		return baos.toByteArray();
	}

	public static void setActivityScreenshot(final Activity activity, final Intent target) {
		final CompressFormat format = isTransparentBackground(activity) ? CompressFormat.PNG : CompressFormat.JPEG;
		final byte[] encoded_screenshot = getEncodedActivityScreenshot(activity, View.DRAWING_CACHE_QUALITY_LOW,
				format, 80);
		target.putExtra(EXTRA_ACTIVITY_SCREENSHOT_ENCODED, encoded_screenshot);
	}

	public static void startSwipebackActivity(final Activity activity, final Intent intent) {
		// setActivityScreenshot(activity, intent);
		// activity.startActivityForResult(intent, REQUEST_SWIPEBACK_ACTIVITY);
		new StartSwipebackActivityTask(activity, intent).execute();
	}

	public static class StartSwipebackActivityTask extends AsyncTask<Void, Void, byte[]> {

		private static final String PROGRESS_DIALOG_FRAGMENT_TAG = "open_activity_progress";

		private final Activity mActivity;
		private final Intent mTarget;
		private final View mDecorView;
		private boolean mPrevState;
		private int mPrevQuality;
		private Bitmap mCache;
		private final Rect mRect;

		public StartSwipebackActivityTask(final Activity activity, final Intent target) {
			mActivity = activity;
			mTarget = target;
			mRect = new Rect();
			mDecorView = mActivity.getWindow().getDecorView();
		}

		@Override
		protected byte[] doInBackground(final Void... params) {
			if (mCache == null || mCache.isRecycled()) return null;
			// Remove window background behind status bar.
			final Canvas c = new Canvas(mCache);
			final Paint paint = new Paint();
			paint.setColor(Color.TRANSPARENT);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
			c.drawRect(mRect.left, 0, mRect.right, mRect.top, paint);
			final CompressFormat format = isTransparentBackground(mActivity) ? CompressFormat.PNG : CompressFormat.JPEG;
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			mCache.compress(format, 80, baos);
			return baos.toByteArray();
		}

		@Override
		protected void onPostExecute(final byte[] result) {
			mDecorView.setDrawingCacheEnabled(mPrevState);
			mDecorView.setDrawingCacheQuality(mPrevQuality);
			if (mCache != null && !mCache.isRecycled()) {
				mCache.recycle();
			}
			mTarget.putExtra(EXTRA_ACTIVITY_SCREENSHOT_ENCODED, result);
			if (mActivity instanceof FragmentActivity) {
				final FragmentActivity a = (FragmentActivity) mActivity;
				final BaseSupportDialogFragment f = (BaseSupportDialogFragment) a.getSupportFragmentManager()
						.findFragmentByTag(PROGRESS_DIALOG_FRAGMENT_TAG);
				if (f != null) {
					f.dismiss();
				}
			} else {
				final BaseDialogFragment f = (BaseDialogFragment) mActivity.getFragmentManager().findFragmentByTag(
						PROGRESS_DIALOG_FRAGMENT_TAG);
				if (f != null) {
					f.dismiss();
				}
			}
			mActivity.startActivityForResult(mTarget, REQUEST_SWIPEBACK_ACTIVITY);
		}

		@Override
		protected void onPreExecute() {
			if (mActivity instanceof FragmentActivity) {
				SupportOpenActivityProgressDialogFragment.show((FragmentActivity) mActivity);
			} else {
				OpenActivityProgressDialogFragment.show(mActivity);
			}
			mPrevState = mDecorView.isDrawingCacheEnabled();
			mPrevQuality = mDecorView.getDrawingCacheQuality();
			mDecorView.setDrawingCacheEnabled(true);
			mDecorView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
			mDecorView.buildDrawingCache();
			mCache = mDecorView.getDrawingCache();
			mDecorView.getWindowVisibleDisplayFrame(mRect);
		}

		public static class OpenActivityProgressDialogFragment extends BaseDialogFragment {

			public OpenActivityProgressDialogFragment() {
				super();
				setStyle(STYLE_NO_FRAME, 0);
				setCancelable(false);
			}

			@Override
			public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
					final Bundle savedInstanceState) {
				return new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleLarge);
			}

			public static OpenActivityProgressDialogFragment show(final Activity activity) {
				final OpenActivityProgressDialogFragment f = new OpenActivityProgressDialogFragment();
				f.show(activity.getFragmentManager(), PROGRESS_DIALOG_FRAGMENT_TAG);
				return f;
			}

		}

		public static class SupportOpenActivityProgressDialogFragment extends BaseSupportDialogFragment {

			public SupportOpenActivityProgressDialogFragment() {
				super();
				setStyle(STYLE_NO_FRAME, 0);
				setCancelable(false);
			}

			@Override
			public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
					final Bundle savedInstanceState) {
				return new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleLarge);
			}

			public static SupportOpenActivityProgressDialogFragment show(final FragmentActivity activity) {
				final SupportOpenActivityProgressDialogFragment f = new SupportOpenActivityProgressDialogFragment();
				f.show(activity.getSupportFragmentManager(), PROGRESS_DIALOG_FRAGMENT_TAG);
				return f;
			}

		}
	}
}
