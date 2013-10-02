
package com.nostra13.universalimageloader.cache.memory.impl;

import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.memory.MemoryCacheAware;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A cache that holds strong references to a limited number of Bitmaps. Each
 * time a Bitmap is accessed, it is moved to the head of a queue. When a Bitmap
 * is added to a full cache, the Bitmap at the end of that queue is evicted and
 * may become eligible for garbage collection.<br />
 * <br />
 * <b>NOTE:</b> This cache uses only strong references for stored Bitmaps.
 * 
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.8.1
 */
public class LruMemoryCache implements MemoryCacheAware<String, Bitmap> {

    private final LinkedHashMap<String, Bitmap> map;

    private final int maxSize;
    /** Size of this cache in bytes */
    private int size;

    /** @param maxSize Maximum sum of the sizes of the Bitmaps in this cache */
    public LruMemoryCache(final int maxSize) {
        if (maxSize <= 0)
            throw new IllegalArgumentException("maxSize <= 0");
        this.maxSize = maxSize;
        map = new LinkedHashMap<String, Bitmap>(0, 0.75f, true);
    }

    @Override
    public void clear() {
        trimToSize(-1); // -1 will evict 0-sized elements
    }

    /**
     * Returns the Bitmap for {@code key} if it exists in the cache. If a Bitmap
     * was returned, it is moved to the head of the queue. This returns null if
     * a Bitmap is not cached.
     */
    @Override
    public final Bitmap get(final String key) {
        if (key == null)
            throw new NullPointerException("key == null");

        synchronized (this) {
            return map.get(key);
        }
    }

    @Override
    public Collection<String> keys() {
        synchronized (this) {
            return new HashSet<String>(map.keySet());
        }
    }

    /**
     * Caches {@code Bitmap} for {@code key}. The Bitmap is moved to the head of
     * the queue.
     */
    @Override
    public final boolean put(final String key, final Bitmap value) {
        if (key == null || value == null)
            throw new NullPointerException("key == null || value == null");

        synchronized (this) {
            size += sizeOf(key, value);
            final Bitmap previous = map.put(key, value);
            if (previous != null) {
                size -= sizeOf(key, previous);
            }
        }

        trimToSize(maxSize);
        return true;
    }

    /** Removes the entry for {@code key} if it exists. */
    @Override
    public final void remove(final String key) {
        if (key == null)
            throw new NullPointerException("key == null");

        synchronized (this) {
            final Bitmap previous = map.remove(key);
            if (previous != null) {
                size -= sizeOf(key, previous);
            }
        }
    }

    @Override
    public synchronized final String toString() {
        return String.format("LruCache[maxSize=%d]", maxSize);
    }

    /**
     * Returns the size {@code Bitmap} in bytes.
     * <p/>
     * An entry's size must not change while it is in the cache.
     */
    private int sizeOf(final String key, final Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    /**
     * Remove the eldest entries until the total of remaining entries is at or
     * below the requested size.
     * 
     * @param maxSize the maximum size of the cache before returning. May be -1
     *            to evict even 0-sized elements.
     */
    private void trimToSize(final int maxSize) {
        while (true) {
            String key;
            Bitmap value;
            synchronized (this) {
                if (size < 0 || map.isEmpty() && size != 0)
                    throw new IllegalStateException(getClass().getName()
                            + ".sizeOf() is reporting inconsistent results!");

                if (size <= maxSize || map.isEmpty()) {
                    break;
                }

                final Map.Entry<String, Bitmap> toEvict = map.entrySet().iterator().next();
                if (toEvict == null) {
                    break;
                }
                key = toEvict.getKey();
                value = toEvict.getValue();
                map.remove(key);
                size -= sizeOf(key, value);
            }
        }
    }
}
