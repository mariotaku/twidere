package org.mariotaku.twidere.util;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.getImageLoaderHttpClient;
import static org.mariotaku.twidere.util.Utils.getRedirectedHttpResponse;
import static org.mariotaku.twidere.util.Utils.resizeBitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mariotaku.twidere.BuildConfig;

import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

public class LazyImageLoader {

	private static final String TAG = LazyImageLoader.class.getSimpleName();

	// Both hard and soft caches are purged after 40 seconds idling.
	private static final int DELAY_BEFORE_PURGE = 40000;
	private static final int MAX_CACHE_CAPACITY = 40;

	// Maximum number of threads in the executor pool.
	// TODO: Tune POOL_SIZE for maximum performance gain
	private static final int POOL_SIZE = 5;

	private final int mFallbackRes;

	private boolean mCancelled;

	private final int mImageWidth;
	private final int mImageHeight;

	private final Runnable mPurger;
	private final Handler mPurgeHandler;
	private final ExecutorService mExecutor;

	// Soft bitmap cache for Images removed from the hard cache.
	// This gets cleared by the Garbage Collector everytime we get low on
	// memory.
	private final Map<String, SoftReference<Bitmap>> mSoftBitmapCache;
	private final Map<ImageView, Image> mImageViews;
	private final LinkedHashMap<String, Bitmap> mHardBitmapCache;
	private final ArrayList<String> mBlacklist;

	private final String mCacheDirName;

	private final Context mContext;

	private File mCacheDir;
	private HttpClientWrapper mClient;

	/**
	 * Used for loading and decoding Images from files.
	 * 
	 * @author PhilipHayes
	 * @param context Current application context.
	 */
	public LazyImageLoader(final Context context, final String cacheDirName, final int fallbackRes,
			final int ImageWidth, final int ImageHeight) {
		mContext = context;
		mCacheDirName = cacheDirName;
		mClient = getImageLoaderHttpClient(context);
		mPurger = new MemoryPurger();
		mPurgeHandler = new Handler();
		mExecutor = Executors.newFixedThreadPool(POOL_SIZE);
		mBlacklist = new ArrayList<String>();
		mSoftBitmapCache = new HashMap<String, SoftReference<Bitmap>>(MAX_CACHE_CAPACITY / 2);
		mImageViews = new WeakHashMap<ImageView, Image>();
		mHardBitmapCache = new HardBitmapCache(MAX_CACHE_CAPACITY / 2);
		mFallbackRes = fallbackRes;
		mImageWidth = ImageWidth;
		mImageHeight = ImageHeight;
		initCacheDir();
	}

	/**
	 * Cancels any downloads, shuts down the executor pool, and then purges the
	 * caches.
	 */
	public void cancel() {
		mCancelled = true;

		// We could also terminate it immediately,
		// but that may lead to synchronization issues.
		if (!mExecutor.isShutdown()) {
			mExecutor.shutdown();
		}

		stopPurgeTimer();

		clearMemoryCache();
	}

	public void clearFileCache() {
		if (mCacheDir == null || !mCacheDir.isDirectory()) return;
		for (final File file : mCacheDir.listFiles()) {
			if (file.isFile()) {
				file.delete();
			}
		}
	}

	public void clearMemoryCache() {
		mSoftBitmapCache.clear();
		mHardBitmapCache.clear();
		mBlacklist.clear();
	}

	/**
	 * @param holder The {@link File} container.
	 * @param view The ImageView from the IconifiedTextView.
	 */
	public void displayImage(final ImageView view, final Image thumb) {
		if (mCancelled || view == null || thumb == null) return;
		mImageViews.put(view, thumb);
		if (!mBlacklist.contains(thumb.getKey())) {
			// We reset the caches after every 30 or so seconds of inactivity
			// for memory efficiency.
			resetPurgeTimer();

			final Bitmap bitmap = getBitmapFromMemoryCache(thumb.getKey());
			if (bitmap != null) {
				// We're still in the UI thread so we just update the icons from
				// here.
				view.setImageBitmap(bitmap);
			} else {
				// Give a drawable based on mimetype. Generic file drawable for
				// undefined types.
				if (mFallbackRes != 0) {
					view.setImageResource(mFallbackRes);
				} else {
					view.setImageBitmap(null);
				}

				if (!mCancelled) {
					final WeakReference<GetImageRunnable> runner = new WeakReference<GetImageRunnable>(
							new GetImageRunnable(view, thumb));
					mExecutor.submit(runner.get());
				}
			}
		} else {
			if (mFallbackRes != 0) {
				view.setImageResource(mFallbackRes);
			} else {
				view.setImageBitmap(null);
			}
		}
	}

	public void displayImage(final ImageView view, final String url) {
		if (view == null) return;
		displayImage(view, new URLImage(url));
	}

	public File getCachedImageFile(final Image thumb) {
		if (thumb == null) return null;
		final File file = thumb.getFile();
		if (file.exists()) return file;
		if (!mCancelled) {
			final WeakReference<CacheImageRunnable> runner = new WeakReference<CacheImageRunnable>(
					new CacheImageRunnable(thumb));
			mExecutor.submit(runner.get());
		}
		return null;
	}

	public File getCachedImageFile(final String url) {
		return getCachedImageFile(new URLImage(url));
	}

	public void initCacheDir() {
		mCacheDir = getBestCacheDir(mContext, mCacheDirName);
	}

	public void reloadConnectivitySettings() {
		mClient = getImageLoaderHttpClient(mContext);
	}

	/**
	 * Stops the cache purger from running until it is reset again.
	 */
	public void stopPurgeTimer() {
		mPurgeHandler.removeCallbacks(mPurger);
	}

	/**
	 * The file to decode.
	 * 
	 * @return The resized and resampled bitmap, if can not be decoded it
	 *         returns null.
	 */
	private Bitmap decodeFile(final File file) {
		if (file == null) return null;
		if (!mCancelled) {
			final BitmapFactory.Options options = new BitmapFactory.Options();

			options.inJustDecodeBounds = true;
			options.outWidth = 0;
			options.outHeight = 0;
			options.inSampleSize = 1;

			final String filePath = file.getAbsolutePath();
			BitmapFactory.decodeFile(filePath, options);

			if (options.outWidth > 0 && options.outHeight > 0) {
				if (!mCancelled) {
					// Now see how much we need to scale it down.
					int widthFactor = (options.outWidth + mImageWidth - 1) / mImageWidth;
					final int heightFactor = (options.outHeight + mImageHeight - 1) / mImageHeight;
					widthFactor = Math.max(widthFactor, heightFactor);
					widthFactor = Math.max(widthFactor, 1);
					// Now turn it into a power of two.
					if (widthFactor > 1) {
						if ((widthFactor & widthFactor - 1) != 0) {
							while ((widthFactor & widthFactor - 1) != 0) {
								widthFactor &= widthFactor - 1;
							}

							widthFactor <<= 1;
						}
					}
					options.inSampleSize = widthFactor;
					options.inJustDecodeBounds = false;
					final Bitmap bitmap = resizeBitmap(BitmapFactory.decodeFile(filePath, options), mImageWidth,
							mImageHeight);
					if (bitmap != null) return bitmap;
				}
			} else {
				// Must not be a bitmap, so we add it to the blacklist.
				if (!mBlacklist.contains(file.getName())) {
					mBlacklist.add(filePath);
				}
			}
		}
		return null;
	};

	/**
	 * @param key In this case the file name (used as the mapping id).
	 * @return bitmap The cached bitmap or null if it could not be located.
	 * 
	 *         As the name suggests, this method attemps to obtain a bitmap
	 *         stored in one of the caches. First it checks the hard cache for
	 *         the key. If a key is found, it moves the cached bitmap to the
	 *         head of the cache so it gets moved to the soft cache last.
	 * 
	 *         If the hard cache doesn't contain the bitmap, it checks the soft
	 *         cache for the cached bitmap. If neither of the caches contain the
	 *         bitmap, this returns null.
	 */
	private Bitmap getBitmapFromMemoryCache(final String key) {
		synchronized (mHardBitmapCache) {
			final Bitmap bitmap = mHardBitmapCache.get(key);
			if (bitmap != null) {
				// Put bitmap on top of cache so it's purged last.
				mHardBitmapCache.remove(key);
				mHardBitmapCache.put(key, bitmap);
				return bitmap;
			}
		}

		final SoftReference<Bitmap> bitmapRef = mSoftBitmapCache.get(key);
		if (bitmapRef != null) {
			final Bitmap bitmap = bitmapRef.get();
			if (bitmap != null)
				return bitmap;
			else {
				// Must have been collected by the Garbage Collector
				// so we remove the bucket from the cache.
				mSoftBitmapCache.remove(key);
			}
		}

		// Could not locate the bitmap in any of the caches, so we return null.
		return null;
	}

	private boolean imageViewRecycled(final ImageView view, final Image thumb) {
		if (view == null || thumb == null) return true;
		final Image old = mImageViews.get(view);
		return !thumb.equals(old);
	}

	/**
	 * Purges the cache every (DELAY_BEFORE_PURGE) milliseconds.
	 * 
	 * @see DELAY_BEFORE_PURGE
	 */
	private void resetPurgeTimer() {
		mPurgeHandler.removeCallbacks(mPurger);
		mPurgeHandler.postDelayed(mPurger, DELAY_BEFORE_PURGE);
	}

	public interface Image {
		boolean get();

		File getFile();

		String getKey();
	}

	private class CacheImageRunnable implements Runnable {

		final Image thumb;

		CacheImageRunnable(final Image thumb) {
			this.thumb = thumb;
		}

		@Override
		public void run() {
			if (mCancelled || thumb == null) return;
			thumb.get();
		}
	}

	private class DisplayFallbackImageRunnable implements Runnable {
		private final ImageView view;

		DisplayFallbackImageRunnable(final ImageView view) {
			this.view = view;
		}

		@Override
		public void run() {
			if (view == null || mCancelled) return;
			if (mFallbackRes != 0) {
				view.setImageResource(mFallbackRes);
			} else {
				view.setImageBitmap(null);
			}
		}
	}

	/**
	 * When run on the UI Thread, this updates the Image in the corresponding
	 * iconifiedtext and imageview.
	 */
	private class DisplayImageRunnable implements Runnable {
		private final Bitmap bitmap;
		private final ImageView view;

		public DisplayImageRunnable(final Bitmap bitmap, final ImageView view) {
			this.bitmap = bitmap;
			this.view = view;
		}

		@Override
		public void run() {
			if (view == null || mCancelled) return;
			view.setImageBitmap(bitmap);
		}
	}

	/**
	 * Decodes the bitmap and sends a ImageUpdater on the UI Thread to update
	 * the listitem and iconified text.
	 * 
	 * @see DisplayImageRunnable
	 */
	private class GetImageRunnable implements Runnable {

		final ImageView view;
		final Image thumb;

		GetImageRunnable(final ImageView view, final Image thumb) {
			this.view = view;
			this.thumb = thumb;
		}

		@Override
		public void run() {
			if (mCancelled || view == null) return;
			final Context context = view.getContext();
			if (!(context instanceof Activity)) return;

			final File file = thumb.getFile();
			if (file == null) return;
			Bitmap bitmap = decodeFile(file);
			if (bitmap == null && (!file.exists() || file.length() == 0)) {
				// Image is corrupted or not exist.
				file.delete();
				if (thumb.get()) {
					bitmap = decodeFile(file);
				}
			}

			final Activity activity = (Activity) context;

			if (mCancelled) return;
			if (!imageViewRecycled(view, thumb)) {
				if (bitmap != null) {
					// Bitmap was successfully decoded so we place it in the
					// hard cache.
					mHardBitmapCache.put(thumb.getKey(), bitmap);
					activity.runOnUiThread(new DisplayImageRunnable(bitmap, view));
				} else {
					activity.runOnUiThread(new DisplayFallbackImageRunnable(view));
				}
			}
			mImageViews.remove(view);
		}
	}

	private class HardBitmapCache extends LinkedHashMap<String, Bitmap> {

		/***/
		private static final long serialVersionUID = 1347795807259717646L;

		HardBitmapCache(final int capacity) {
			super(capacity, 0.75f, true);
		}

		@Override
		protected boolean removeEldestEntry(final LinkedHashMap.Entry<String, Bitmap> eldest) {
			// Moves the last used item in the hard cache to the soft cache.
			if (size() > MAX_CACHE_CAPACITY) {
				mSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			} else
				return false;
		}
	}

	private class MemoryPurger implements Runnable {
		@Override
		public void run() {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "Purge Timer hit; Clearing Caches.");
			}
			clearMemoryCache();
		}
	}

	abstract class BaseImage implements Image {

		@Override
		public File getFile() {
			final String key = getKey();
			if (isEmpty(key)) return null;
			return new File(mCacheDir, getFileNameForKey(key));
		}

		private String getFileNameForKey(final String key) {
			if (isEmpty(key)) return null;
			return key.replaceAll("[^\\w\\d_]", "_");
		}

	}

	class URLImage extends BaseImage {

		final String url;

		URLImage(final String url) {
			this.url = url;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (!(obj instanceof URLImage)) return false;
			final URLImage other = (URLImage) obj;
			if (!getOuterType().equals(other.getOuterType())) return false;
			if (url == null) {
				if (other.url != null) return false;
			} else if (!url.equals(other.url)) return false;
			return true;
		}

		@Override
		public boolean get() {
			FileOutputStream os = null;
			try {
				final HttpResponse response = getRedirectedHttpResponse(mClient, url);
				os = new FileOutputStream(getFile());
				copyStream(response.asStream(), os);
			} catch (final Exception e) {
				if (BuildConfig.DEBUG) {
					Log.w(TAG, e);
				}
				return false;
			} finally {
				if (os != null) {
					try {
						os.flush();
						os.close();
					} catch (final Exception e) {
						if (BuildConfig.DEBUG) {
							Log.w(TAG, e);
						}
					}
				}
			}
			return true;
		}

		@Override
		public String getKey() {
			return url;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (url == null ? 0 : url.hashCode());
			return result;
		}

		private LazyImageLoader getOuterType() {
			return LazyImageLoader.this;
		}

	}
}
