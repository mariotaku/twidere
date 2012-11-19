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

import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.getBrowserUserAgent;
import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.getHttpClient;
import static org.mariotaku.twidere.util.Utils.getProxy;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;

import twitter4j.TwitterException;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.widget.ImageView;

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
public final class LazyImageLoader implements Constants {

	private final URLBitmapLruCache mMemoryCache;
	private final Context mContext;
	private final FileCache mFileCache;
	private final Map<ImageView, URL> mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, URL>());
	private final ExecutorService mExecutorService;
	private final int mFallbackRes;
	private final int mRequiredWidth, mRequiredHeight;
	private final String mUserAgent;
	private final HostAddressResolver mResolver;
	private Proxy mProxy;
	private int mConnectionTimeout;
	private HttpClientWrapper mHttpClient;

	public LazyImageLoader(final Context context, final String cache_dir_name, final int fallback_image_res,
			final int required_width, final int required_height) {
		mContext = context;
		mFileCache = new FileCache(context, cache_dir_name);
		mExecutorService = Executors.newFixedThreadPool(5);
		mFallbackRes = fallback_image_res;
		mRequiredWidth = required_width % 2 == 0 ? required_width : required_width + 1;
		mRequiredHeight = required_height % 2 == 0 ? required_height : required_height + 1;
		mUserAgent = getBrowserUserAgent(context);
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mResolver = app.getHostAddressResolver();
		mMemoryCache = app.getURLBitmapLruCache();
		reloadConnectivitySettings();
	}

	public void clearFileCache() {
		mFileCache.clear();
	}

	public void clearMemoryCache() {
		mMemoryCache.evictAll();
	}

	public void displayImage(final String url, final ImageView imageview) {
		displayImage(parseURL(url), imageview);
	}

	public void displayImage(final URL url, final ImageView imageview) {
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
			queueImage(url, imageview);
			imageview.setImageResource(mFallbackRes);
		}
	}

	public String getCachedImagePath(final URL url) {
		if (mFileCache == null) return null;
		final File f = mFileCache.getFile(url);
		if (f != null && f.exists())
			return f.getPath();
		else {
			queueImage(url, null);
		}
		return null;
	}

	public void reloadConnectivitySettings() {
		mProxy = getProxy(mContext);
		mConnectionTimeout = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
				PREFERENCE_KEY_CONNECTION_TIMEOUT, 10) * 1000;
		mHttpClient = getHttpClient(mConnectionTimeout, true, mProxy, mResolver, mUserAgent);
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(final File f) {
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

	private int getFallbackImageRes() {
		return mFallbackRes;
	}

	private FileCache getFileCache() {
		return mFileCache;
	}
	
	private URLBitmapLruCache getMemoryCache() {
		return mMemoryCache;
	}

	private HttpClientWrapper getHttpClientInstance() {
		return mHttpClient;
	}

	private boolean isImageViewReused(final ImageViewHolder imagetoload) {
		final Object tag = mImageViews.get(imagetoload.view);
		if (tag == null || !tag.equals(imagetoload.url)) return true;
		return false;
	}
	
	private void queueImage(final URL url, final ImageView imageview) {
		final ImageViewHolder p = new ImageViewHolder(url, imageview);
		mExecutorService.submit(new ImageLoader(this, p));
	}

	// Used to display bitmap in the UI thread
	static final class BitmapDisplayer implements Runnable {

		private final LazyImageLoader loader;
		private final Bitmap bitmap;
		private final ImageViewHolder holder;

		BitmapDisplayer(final LazyImageLoader loader, final Bitmap bitmap, final ImageViewHolder imagetoload) {
			this.loader = loader;
			this.bitmap = bitmap;
			this.holder = imagetoload;
		}

		@Override
		public final void run() {
			if (loader.isImageViewReused(holder)) return;
			if (bitmap != null) {
				holder.view.setImageBitmap(bitmap);
			} else {
				holder.view.setImageResource(loader.getFallbackImageRes());
			}
		}
	}
	
	static final class FileCache {

		private final String mCacheDirName;

		private File mCacheDir;
		private final Context mContext;

		FileCache(final Context context, final String cache_dir_name) {
			mContext = context;
			mCacheDirName = cache_dir_name;
			init();
		}

		void clear() {
			if (mCacheDir == null) return;
			final File[] files = mCacheDir.listFiles();
			if (files == null) return;
			for (final File f : files) {
				f.delete();
			}
		}

		File getFile(final URL tag) {
			if (mCacheDir == null) return null;
			final String filename = getURLFilename(tag);
			if (filename == null) return null;
			final File file = new File(mCacheDir, filename);
			return file;
		}

		void init() {
			/* Find the dir to save cached images. */
			mCacheDir = getBestCacheDir(mContext, mCacheDirName);
			if (mCacheDir != null && !mCacheDir.exists()) {
				mCacheDir.mkdirs();
			}
		}

		static String getURLFilename(final URL url) {
			if (url == null) return null;
			return url.toString().replaceFirst("https?:\\/\\/", "").replaceAll("[^a-zA-Z0-9]", "_");
		}

	}

	static final class ImageLoader implements Runnable {
		private final ImageViewHolder holder;
		private final LazyImageLoader loader;

		ImageLoader(final LazyImageLoader loader, final ImageViewHolder holder) {
			this.loader = loader;
			this.holder = holder;
		}

		Bitmap getBitmap(final URL url) {
			if (url == null) return null;
			final File cache_file = loader.getFileCache().getFile(url);

			// from SD cache
			final Bitmap cached_bitmap = loader.decodeFile(cache_file);
			if (cached_bitmap != null) return cached_bitmap;

			int response_code = -1;

			// from web
			try {
				Bitmap bitmap = null;
				int retry_count = 0;
				String request_url = url.toString();
				HttpResponse resp = null;

				while (retry_count < 5) {
					try {
						resp = loader.getHttpClientInstance().get(request_url, null);
					} catch (final TwitterException e) {
						resp = e.getHttpResponse();
					}
					if (resp == null) {
						break;
					}
					response_code = resp.getStatusCode();
					if (response_code != 301 && response_code != 302) {
						break;
					}
					request_url = resp.getResponseHeader("Location");
					if (request_url == null) {
						break;
					}
					retry_count++;
				}
				if (resp != null && response_code == 200) {
					final InputStream is = resp.asStream();
					final OutputStream os = new FileOutputStream(cache_file);
					copyStream(is, os);
					os.close();
					bitmap = loader.decodeFile(cache_file);
					if (bitmap == null) {
						// The file is corrupted, so we remove it from cache.
						if (cache_file.isFile()) {
							cache_file.delete();
						}
					} else
						return bitmap;
				}
			} catch (final FileNotFoundException e) {
				// Storage state may changed, so call FileCache.init() again.
				loader.getFileCache().init();
			} catch (final IOException e) {
				// e.printStackTrace();
			}
			return null;
		}

		@Override
		public void run() {
			if (loader.isImageViewReused(holder) || holder.url == null) return;
			final Bitmap bitmap = getBitmap(holder.url);
			loader.getMemoryCache().put(holder.url, bitmap);
			if (loader.isImageViewReused(holder)) return;
			final BitmapDisplayer bd = new BitmapDisplayer(loader, bitmap, holder);
			final Activity a = (Activity) holder.view.getContext();
			a.runOnUiThread(bd);
		}
	}

	static final class ImageViewHolder {
		final URL url;
		final ImageView view;

		ImageViewHolder(final URL url, final ImageView view) {
			this.url = url;
			this.view = view;
		}
	}

	public static final class URLBitmapLruCache extends LruCache<URL, Bitmap> {

		public URLBitmapLruCache() {
			super((int) Debug.getNativeHeapSize() / 2);
			
		}

		@Override
		protected int sizeOf(final URL key, final Bitmap value) {
			return value.getByteCount();
		}
	}

}
