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
import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.getImageLoaderHttpClient;
import static org.mariotaku.twidere.util.Utils.getRedirectedHttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.mariotaku.twidere.Constants;

import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;
import android.content.Context;
import android.util.Log;
import android.widget.GridView;
import android.widget.ListView;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;

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
public class ImagePreloader implements Constants {

	private static final String LOGTAG = ImagePreloader.class.getSimpleName();

	private final Context mContext;
	private final ExecutorService mExecutor;
	private final FileNameGenerator mGenerator;
	private HttpClientWrapper mClient;

	public ImagePreloader(final Context context) {
		mContext = context;
		mExecutor = Executors.newFixedThreadPool(32, new LowestPriorityThreadFactory());
		mGenerator = new URLFileNameGenerator();
		reloadConnectivitySettings();
	}

	/**
	 * Cancels any downloads, shuts down the executor pool, and then purges the
	 * caches.
	 */
	public void cancel() {

		// We could also terminate it immediately,
		// but that may lead to synchronization issues.
		if (!mExecutor.isShutdown()) {
			mExecutor.shutdown();
		}

	}

	public File getCachedImageFile(final String cache_dir_name, final String url) {
		if (cache_dir_name == null || url == null) return null;
		final File cache_dir = getBestCacheDir(mContext, cache_dir_name);
		if (cache_dir == null) return null;
		final File cache = new File(cache_dir, mGenerator.generate(url));
		if (ImageValidator.checkImageValidity(cache))
			return cache;
		else {
			preloadImage(cache_dir_name, url);
		}
		return null;
	}

	public void preloadImage(final String cache_dir_name, final String url) {
		final ImageToLoad p = new ImageToLoad(cache_dir_name, url);
		mExecutor.submit(new ImageLoader(p));
	}

	public void reloadConnectivitySettings() {
		mClient = getImageLoaderHttpClient(mContext);
	}

	class ImageLoader implements Runnable {
		private final ImageToLoad imagetoload;

		public ImageLoader(final ImageToLoad imagetoload) {
			this.imagetoload = imagetoload;
		}

		@Override
		public void run() {
			if (imagetoload == null || imagetoload.cache_dir_name == null || imagetoload.url == null) return;
			final File cache_dir = getBestCacheDir(mContext, imagetoload.cache_dir_name);
			if (cache_dir == null) return;
			if (!cache_dir.isDirectory()) {
				cache_dir.mkdir();
			}
			final File cache_file = new File(cache_dir, mGenerator.generate(imagetoload.url));
			// from SD cache
			if (DEBUG) {
				Log.d(LOGTAG, "Preload image " + imagetoload.url + " to " + cache_file);
			}
			if (ImageValidator.checkImageValidity(cache_file)) return;

			// from web
			try {
				final HttpResponse resp = getRedirectedHttpResponse(mClient, imagetoload.url);

				if (resp != null && resp.getStatusCode() == 200) {
					final InputStream is = resp.asStream();
					final OutputStream os = new FileOutputStream(cache_file);
					copyStream(is, os);
					os.flush();
					os.close();
					if (!ImageValidator.checkImageValidity(cache_file)) {
						// The file is corrupted, so we remove it from cache.
						if (cache_file.isFile() && cache_file.length() == 0) {
							cache_file.delete();
						}
					}
				}
			} catch (final Exception e) {
				Log.w(LOGTAG, e);
			}
		}
	}

	static class ImageToLoad {
		public final String url;
		public final String cache_dir_name;

		public ImageToLoad(final String cache_dir_name, final String url) {
			this.url = url;
			this.cache_dir_name = cache_dir_name;
		}
	}

	static class LowestPriorityThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = new Thread(r);
			t.setPriority(Thread.MIN_PRIORITY);
			return t;
		}

	}

}
