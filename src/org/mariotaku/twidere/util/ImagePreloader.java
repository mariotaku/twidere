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

import java.io.File;

import org.mariotaku.twidere.Constants;

import android.os.Handler;

import com.nostra13.universalimageloader.cache.disc.DiscCacheAware;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author mariotaku
 * 
 */
public class ImagePreloader implements Constants {

	private final Handler mHandler;
	private final DiscCacheAware mDiscCache;
	private final ImageLoader mImageLoader;

	public ImagePreloader(final ImageLoader loader) {
		mImageLoader = loader;
		mDiscCache = loader.getDiscCache();
		mHandler = new Handler();
		reloadConnectivitySettings();
	}

	/**
	 * Cancels any downloads, shuts down the executor pool, and then purges the
	 * caches.
	 */
	public void cancel() {
		mImageLoader.destroy();
	}

	public File getCachedImageFile(final String url) {
		if (url == null) return null;
		final File cache = mDiscCache.get(url);
		if (ImageValidator.checkImageValidity(cache))
			return cache;
		else {
			preloadImage(url);
		}
		return null;
	}

	public void preloadImage(final String url) {
		mHandler.post(new Runnable() {

			@Override
			public void run() {
				mImageLoader.loadImage(url, null);
			}

		});
	}

	public void reloadConnectivitySettings() {
	}

}
