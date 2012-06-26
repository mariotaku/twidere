package org.mariotaku.twidere.util;

import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

class MemoryCache {

	private static final int MAX_CACHE_CAPACITY = 60;

	private final Map<URL, SoftReference<Bitmap>> mSoftCache = new ConcurrentHashMap<URL, SoftReference<Bitmap>>();

	private final Map<URL, Bitmap> mHardCache = new LinkedHashMap<URL, Bitmap>(MAX_CACHE_CAPACITY / 2, 0.75f, true) {

		private static final long serialVersionUID = 1347795807259717646L;

		@Override
		protected boolean removeEldestEntry(LinkedHashMap.Entry<URL, Bitmap> eldest) {
			// Moves the last used item in the hard cache to the soft cache.
			if (size() > MAX_CACHE_CAPACITY) {
				mSoftCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
				return true;
			} else
				return false;
		}
	};

	public void clear() {
		mHardCache.clear();
		mSoftCache.clear();
	}

	public Bitmap get(final URL url) {
		synchronized (mHardCache) {
			final Bitmap bitmap = mHardCache.get(url);
			if (bitmap != null) {
				// Put bitmap on top of cache so it's purged last.
				mHardCache.remove(url);
				mHardCache.put(url, bitmap);
				return bitmap;
			}
		}

		final SoftReference<Bitmap> bitmapRef = mSoftCache.get(url);
		if (bitmapRef != null) {
			final Bitmap bitmap = bitmapRef.get();
			if (bitmap != null)
				return bitmap;
			else {
				// Must have been collected by the Garbage Collector
				// so we remove the bucket from the cache.
				mSoftCache.remove(url);
			}
		}

		// Could not locate the bitmap in any of the caches, so we return
		// null.
		return null;

	}

	public void put(final URL url, final Bitmap bitmap) {
		if (url == null || bitmap == null) return;
		mHardCache.put(url, bitmap);
	}
}