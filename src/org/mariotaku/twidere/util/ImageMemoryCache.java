package org.mariotaku.twidere.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;

public class ImageMemoryCache implements MemoryCacheAware<String, Bitmap> {

	private static final String LOGTAG = ImageMemoryCache.class.getSimpleName();
	private static final int MEMORY_PURGE_DELAY = 30 * 1000;

	private final Map<String, SoftReference<Bitmap>> mSoftCache;
	private final LinkedHashMap<String, Bitmap> mHardCache;
	private final Handler mHandler;
	private final MemoryPurger mPurger;

	public ImageMemoryCache(final int max_capacity) {
		mSoftCache = new HashMap<String, SoftReference<Bitmap>>(max_capacity / 2);
		mHardCache = new HardBitmapCache(mSoftCache, max_capacity / 2);
		mHandler = new Handler();
		mPurger = new MemoryPurger(this);
	}

	@Override
	public void clear() {
		try {
			mHardCache.clear();
			mSoftCache.clear();
			System.gc();
		} catch (final Exception e) {
			Log.e(LOGTAG, "Unknown exception", e);
		}
	}

	@Override
	public Bitmap get(final String key) {
		if (key == null) return null;
		resetMemoryPurger();
		try {
			synchronized (mHardCache) {
				final Bitmap bitmap = mHardCache.get(key);
				if (bitmap != null && key != null) {
					// Put bitmap on top of cache so it's purged last.
					mHardCache.remove(key);
					mHardCache.put(key, bitmap);
					return bitmap;
				}
			}
			final Reference<Bitmap> bitmapRef = mSoftCache.get(key);
			if (bitmapRef != null) {
				final Bitmap bitmap = bitmapRef.get();
				if (bitmap != null)
					return bitmap;
				else {
					// Must have been collected by the Garbage Collector
					// so we remove the bucket from the cache.
					mSoftCache.remove(key);
				}
			}
		} catch (final Exception e) {
			Log.e(LOGTAG, "Unknown exception", e);
		}
		// Could not locate the bitmap in any of the caches, so we return
		// null.
		return null;

	}

	@Override
	public Collection<String> keys() {
		return mHardCache.keySet();
	}

	@Override
	public boolean put(final String key, final Bitmap bitmap) {
		if (key == null || bitmap == null) return false;
		resetMemoryPurger();
		try {
			return mHardCache.put(key, bitmap) != null;
		} catch (final Exception e) {
			Log.e(LOGTAG, "Unknown exception", e);
		}
		return false;
	}

	@Override
	public void remove(final String key) {
		final Bitmap bitmap = mHardCache.remove(key);
		if (bitmap != null) {
			bitmap.recycle();
		}
		final SoftReference<Bitmap> ref = mSoftCache.remove(key);
		if (ref.get() != null) {
			ref.get().recycle();
		}
	}

	private void resetMemoryPurger() {
		mHandler.removeCallbacks(mPurger);
		mHandler.postDelayed(mPurger, MEMORY_PURGE_DELAY);
	}

	private static final class MemoryPurger implements Runnable {

		final ImageMemoryCache cache;

		MemoryPurger(final ImageMemoryCache cache) {
			this.cache = cache;
		}

		@Override
		public void run() {
			cache.clear();
		}

	}

	static class HardBitmapCache extends LinkedHashMap<String, Bitmap> {

		private static final long serialVersionUID = 1347795807259717646L;
		private final Map<String, SoftReference<Bitmap>> soft_cache;
		private final int capacity;

		HardBitmapCache(final Map<String, SoftReference<Bitmap>> soft_cache, final int capacity) {
			super(capacity);
			this.soft_cache = soft_cache;
			this.capacity = capacity;
		}

		@Override
		protected boolean removeEldestEntry(final LinkedHashMap.Entry<String, Bitmap> eldest) {
			// Moves the last used item in the hard cache to the soft cache.
			if (size() > capacity) {
				soft_cache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			} else
				return false;
		}
	}
}