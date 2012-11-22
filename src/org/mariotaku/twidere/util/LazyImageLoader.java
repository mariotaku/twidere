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
import org.mariotaku.twidere.util.imageloader.ImageCache;
import org.mariotaku.twidere.util.imageloader.ImageFetcher;
import org.mariotaku.twidere.util.imageloader.ImageWorker;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * @author mariotaku
 * 
 */
public final class LazyImageLoader implements Constants {

	private final ImageWorker mImageWorker;
	private final ImageCache mCache;

	public LazyImageLoader(final Context context, final String cache_dir_name, final int fallback_image_res,
			final int required_width, final int required_height) {
		this(context, cache_dir_name, fallback_image_res, required_width, required_height,
				ImageCache.DEFAULT_MEM_CACHE_SIZE);
	}

	public LazyImageLoader(final Context context, final String cache_dir_name, final int fallback_image_res,
			final int required_width, final int required_height, final int max_memory_size) {
		mImageWorker = new ImageFetcher(context, required_width, required_height);
		mImageWorker.setLoadingImage(fallback_image_res);
		final ImageCache.ImageCacheParams params = new ImageCache.ImageCacheParams(cache_dir_name);
		params.diskCacheEnabled = true;
		params.compressFormat = Bitmap.CompressFormat.PNG;
		params.memoryCacheEnabled = true;
		params.memCacheSize = max_memory_size;
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

	public File getCachedImageFile(final String url) {
		if (url == null) return null;
		return mImageWorker.getCachedImageFile(url);
	}

	public void reloadConnectivitySettings() {
		mImageWorker.init();
	}
}
