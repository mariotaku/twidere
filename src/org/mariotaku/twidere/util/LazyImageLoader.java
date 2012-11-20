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

import static org.mariotaku.twidere.util.Utils.parseString;

import java.net.URL;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.imageloader.ImageWorker;
import org.mariotaku.twidere.util.imageloader.ImageFetcher;
import org.mariotaku.twidere.util.imageloader.ImageCache;
import org.mariotaku.twidere.util.imageloader.ImageLoaderUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
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

	private final ImageWorker mImageWorker;
	private final ImageCache mCache;

	public LazyImageLoader(final Context context, final String cache_dir_name, final int fallback_image_res,
			final int required_width, final int required_height) {
		mImageWorker = new ImageFetcher(context, required_width, required_height);
		mImageWorker.setLoadingImage(fallback_image_res);
		final ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(cache_dir_name);
		params.diskCacheEnabled = true;
		params.compressFormat = Bitmap.CompressFormat.PNG;
		params.memoryCacheEnabled = true;
		params.memCacheSize = ImageLoaderUtils.getMemoryClass(context) * 1024 * 1024 / 4;
		mCache = ImageCache.findOrCreateCache(context, params);
		mImageWorker.setImageCache(mCache);
		reloadConnectivitySettings();
	}

	public void clearFileCache() {
		mCache.clearDiskCache();
	}

	public void clearMemoryCache() {
		mCache.clearMemoryCache();
	}

	public void displayImage(final String url, final ImageView imageview) {
		if (imageview == null) return;
		mImageWorker.loadImage(url, imageview);
	}

	public void displayImage(final URL url, final ImageView imageview) {
		if (imageview == null) return;
		mImageWorker.loadImage(parseString(url), imageview);
	}

	public String getCachedImagePath(final URL url) {
		//TODO
		return null;
	}

	public void reloadConnectivitySettings() {
		
	}

//	static final class ImageLoader implements Runnable {
//		private final ImageViewHolder holder;
//		private final LazyImageLoader loader;
//
//		ImageLoader(final LazyImageLoader loader, final ImageViewHolder holder) {
//			this.loader = loader;
//			this.holder = holder;
//		}
//
//		Bitmap getBitmap(final URL url) {
//			if (url == null) return null;
//			final File cache_file = loader.getFileCache().getFile(url);
//
//			// from SD cache
//			final Bitmap cached_bitmap = loader.decodeFile(cache_file);
//			if (cached_bitmap != null) return cached_bitmap;
//
//			int response_code = -1;
//
//			// from web
//			try {
//				Bitmap bitmap = null;
//				int retry_count = 0;
//				String request_url = url.toString();
//				HttpResponse resp = null;
//
//				while (retry_count < 5) {
//					try {
//						resp = loader.getHttpClientInstance().get(request_url, null);
//					} catch (final TwitterException e) {
//						resp = e.getHttpResponse();
//					}
//					if (resp == null) {
//						break;
//					}
//					response_code = resp.getStatusCode();
//					if (response_code != 301 && response_code != 302) {
//						break;
//					}
//					request_url = resp.getResponseHeader("Location");
//					if (request_url == null) {
//						break;
//					}
//					retry_count++;
//				}
//				if (resp != null && response_code == 200) {
//					final InputStream is = resp.asStream();
//					final OutputStream os = new FileOutputStream(cache_file);
//					copyStream(is, os);
//					os.close();
//					bitmap = loader.decodeFile(cache_file);
//					if (bitmap == null) {
//						// The file is corrupted, so we remove it from cache.
//						if (cache_file.isFile()) {
//							cache_file.delete();
//						}
//					} else
//						return bitmap;
//				}
//			} catch (final FileNotFoundException e) {
//				// Storage state may changed, so call FileCache.init() again.
//				loader.getFileCache().init();
//			} catch (final IOException e) {
//				// e.printStackTrace();
//			}
//			return null;
//		}
//
//		@Override
//		public void run() {
//			if (loader.isImageViewReused(holder) || holder.url == null) return;
//			final Bitmap bitmap = getBitmap(holder.url);
//			loader.getMemoryCache().put(holder.url, bitmap);
//			if (loader.isImageViewReused(holder)) return;
//			final BitmapDisplayer bd = new BitmapDisplayer(loader, bitmap, holder);
//			final Activity a = (Activity) holder.view.getContext();
//			a.runOnUiThread(bd);
//		}
//	}
//
//	static final class ImageViewHolder {
//		final URL url;
//		final ImageView view;
//
//		ImageViewHolder(final URL url, final ImageView view) {
//			this.url = url;
//			this.view = view;
//		}
//	}
//
//	public static final class URLBitmapLruCache extends LruCache<URL, Bitmap> {
//
//		public URLBitmapLruCache() {
//			super((int) Debug.getNativeHeapSize() / 2);
//			
//		}
//		
//		public URLBitmapLruCache(final int maxSize) {
//			super(maxSize);
//
//		}
//
//		@Override
//		protected int sizeOf(final URL key, final Bitmap value) {
//			return value.getByteCount();
//		}
//	}
//
}
