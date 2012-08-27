/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;
import static org.mariotaku.twidere.util.Utils.getProxy;
import static org.mariotaku.twidere.util.Utils.parseURL;
import static org.mariotaku.twidere.util.Utils.setIgnoreSSLError;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mariotaku.twidere.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Lazy image loader for {@link ListView} and {@link GridView} etc.</br> </br>
 * Inspired by <a href="https://github.com/thest1/LazyList">LazyList</a>, this
 * class has extra features like image loading/caching image to
 * /mnt/sdcard/Android/data/[package name]/cache features.</br> </br> Requires
 * Android 2.2, you can modify {@link Context#getExternalCacheDir()} to other to
 * support Android 2.1 and below.
 * 
 * @author mariotaku
 * 
 */
public class LazyImageLoader implements Constants {

	private final MemoryCache mMemoryCache;
	private final Context mContext;
	private final FileCache mFileCache;
	private final Map<ImageView, URL> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, URL>());
	private final ExecutorService mExecutorService;
	private final int mFallbackRes;
	private final int mRequiredWidth, mRequiredHeight;
	private boolean mIgnoreSSLError;
	private Proxy mProxy;
	private final SharedPreferences mPreferences;

	public LazyImageLoader(Context context, String cache_dir_name, int fallback_image_res, int required_width,
			int required_height, int mem_cache_capacity) {
		mContext = context;
		mMemoryCache = new MemoryCache(mem_cache_capacity);
		mFileCache = new FileCache(context, cache_dir_name);
		mExecutorService = Executors.newFixedThreadPool(5);
		mFallbackRes = fallback_image_res;
		mRequiredWidth = required_width % 2 == 0 ? required_width : required_width + 1;
		mRequiredHeight = required_height % 2 == 0 ? required_height : required_height + 1;
		mPreferences = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mProxy = getProxy(context);
		mIgnoreSSLError = mPreferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
	}

	public void clearFileCache() {
		mFileCache.clear();
	}

	public void clearMemoryCache() {
		mMemoryCache.clear();
	}

	public void displayImage(String url, ImageView imageview) {
		displayImage(parseURL(url), imageview);
	}

	public void displayImage(URL url, ImageView imageview) {
		if (imageview == null) return;
		if (url == null) {
			imageview.setImageResource(mFallbackRes);
			return;
		}
		mImageViews.put(imageview, url);
		final Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null) {
			imageview.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageview);
			imageview.setImageResource(mFallbackRes);
		}
	}

	public String getCachedImagePath(URL url) {
		if (mFileCache == null) return null;
		final File f = mFileCache.getFile(url);
		if (f != null && f.exists())
			return f.getPath();
		else {
			queuePhoto(url, null);
		}
		return null;
	}

	public void reloadConnectivitySettings() {
		mProxy = getProxy(mContext);
		mIgnoreSSLError = mPreferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
	}

	private void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			final byte[] bytes = new byte[buffer_size];
			int count = is.read(bytes, 0, buffer_size);
			while (count != -1) {
				os.write(bytes, 0, count);
				count = is.read(bytes, 0, buffer_size);
			}
		} catch (final IOException e) {
			// e.printStackTrace();
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, options);

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = options.outWidth, height_tmp = options.outHeight;
			int scale = 1;
			while (width_tmp / 2 >= mRequiredWidth || height_tmp / 2 >= mRequiredHeight) {
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			final BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale / 2;
			final Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
			if (bitmap == null) {
				// The file is corrupted, so we remove it from cache.
				if (f.isFile()) {
					f.delete();
				}
			}
			return bitmap;
		} catch (final FileNotFoundException e) {
			// e.printStackTrace();
		}
		return null;
	}

	private void queuePhoto(URL url, ImageView imageview) {
		final ImageToLoad p = new ImageToLoad(url, imageview);
		mExecutorService.submit(new ImageLoader(p));
	}

	boolean imageViewReused(ImageToLoad imagetoload) {
		final Object tag = mImageViews.get(imagetoload.imageview);
		if (tag == null || !tag.equals(imagetoload.source)) return true;
		return false;
	}

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {

		Bitmap bitmap;
		ImageToLoad imagetoload;

		public BitmapDisplayer(Bitmap b, ImageToLoad p) {
			bitmap = b;
			imagetoload = p;
		}

		@Override
		public final void run() {
			if (imageViewReused(imagetoload)) return;
			if (bitmap != null) {
				imagetoload.imageview.setImageBitmap(bitmap);
			} else {
				imagetoload.imageview.setImageResource(mFallbackRes);
			}
		}
	}

	static class FileCache {

		private final String mCacheDirName;

		private File mCacheDir;
		private Context mContext;

		public FileCache(Context context, String cache_dir_name) {
			mContext = context;
			mCacheDirName = cache_dir_name;
			init();
		}

		public void clear() {
			if (mCacheDir == null) return;
			final File[] files = mCacheDir.listFiles();
			if (files == null) return;
			for (final File f : files) {
				f.delete();
			}
		}

		public File getFile(URL tag) {
			if (mCacheDir == null) return null;
			final String filename = getURLFilename(tag);
			if (filename == null) return null;
			final File file = new File(mCacheDir, filename);
			return file;
		}

		public void init() {
			/* Find the dir to save cached images. */
			if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				mCacheDir = new File(
						Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor.getExternalCacheDir(mContext)
								: new File(getExternalStorageDirectory().getPath() + "/Android/data/"
										+ mContext.getPackageName() + "/cache/"), mCacheDirName);
			} else {
				mCacheDir = new File(mContext.getCacheDir(), mCacheDirName);
			}
			if (mCacheDir != null && !mCacheDir.exists()) {
				mCacheDir.mkdirs();
			}
		}

		private String getURLFilename(URL url) {
			if (url == null) return null;
			return url.toString().replaceFirst("https?:\\/\\/", "").replaceAll("[^a-zA-Z0-9]", "_");
		}

	}

	class ImageLoader implements Runnable {
		private ImageToLoad imagetoload;

		public ImageLoader(ImageToLoad imagetoload) {
			this.imagetoload = imagetoload;
		}

		public Bitmap getBitmap(URL url) {
			if (url == null) return null;
			final File f = mFileCache.getFile(url);

			// from SD cache
			final Bitmap b = decodeFile(f);
			if (b != null) return b;

			// from web
			try {
				Bitmap bitmap = null;
				final HttpURLConnection conn = (HttpURLConnection) url.openConnection(mProxy);
				if (mIgnoreSSLError) {
					setIgnoreSSLError(conn);
				}
				conn.setConnectTimeout(30000);
				conn.setReadTimeout(30000);
				conn.setInstanceFollowRedirects(true);
				final InputStream is = conn.getInputStream();
				final OutputStream os = new FileOutputStream(f);
				copyStream(is, os);
				os.close();
				bitmap = decodeFile(f);
				return bitmap;
			} catch (final FileNotFoundException e) {
				// Storage state may changed, so call FileCache.init() again.
				// e.printStackTrace();
				mFileCache.init();
			} catch (final IOException e) {
				// e.printStackTrace();
			}
			return null;
		}

		@Override
		public void run() {
			if (imageViewReused(imagetoload) || imagetoload.source == null) return;
			final Bitmap bmp = getBitmap(imagetoload.source);
			mMemoryCache.put(imagetoload.source, bmp);
			if (imageViewReused(imagetoload)) return;
			final BitmapDisplayer bd = new BitmapDisplayer(bmp, imagetoload);
			final Activity a = (Activity) imagetoload.imageview.getContext();
			a.runOnUiThread(bd);
		}
	}

	static class ImageToLoad {
		public final URL source;
		public final ImageView imageview;

		public ImageToLoad(final URL source, final ImageView imageview) {
			this.source = source;
			this.imageview = imageview;
		}
	}

	static class MemoryCache {

		private final int mMaxCapacity;
		private final Map<URL, SoftReference<Bitmap>> mSoftCache;
		private final Map<URL, Bitmap> mHardCache;

		public MemoryCache(int max_capacity) {
			mMaxCapacity = max_capacity;
			mSoftCache = new ConcurrentHashMap<URL, SoftReference<Bitmap>>();
			mHardCache = new LinkedHashMap<URL, Bitmap>(mMaxCapacity / 3, 0.75f, true) {

				private static final long serialVersionUID = 1347795807259717646L;

				@Override
				protected boolean removeEldestEntry(LinkedHashMap.Entry<URL, Bitmap> eldest) {
					// Moves the last used item in the hard cache to the soft
					// cache.
					if (size() > mMaxCapacity) {
						mSoftCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
						return true;
					} else
						return false;
				}
			};
		}

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

}
