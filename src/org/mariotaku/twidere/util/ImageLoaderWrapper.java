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
import static org.mariotaku.twidere.util.Utils.resizeBitmap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.mariotaku.twidere.BuildConfig;
import org.mariotaku.twidere.Constants;
 
import com.nostra13.universalimageloader.core.ImageLoader;

import twitter4j.TwitterException;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import org.mariotaku.twidere.R;

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
public class ImageLoaderWrapper implements Constants {

	private final Context mContext;
	private final ImageLoader mImageLoader;
	private final DisplayImageOptions mProfileImageDisplayOptions, mPreviewImageDisplayOptions;

	public ImageLoaderWrapper(final Context context, final ImageLoader loader) {
		mContext = context;
		mImageLoader = loader;
		final DisplayImageOptions.Builder profile_opts_builder = new DisplayImageOptions.Builder();
		profile_opts_builder.cacheInMemory();
		profile_opts_builder.cacheOnDisc();
		profile_opts_builder.showStubImage(R.drawable.ic_profile_image_default);
		profile_opts_builder.bitmapConfig(Bitmap.Config.ARGB_8888);
		mProfileImageDisplayOptions = profile_opts_builder.build();
		final DisplayImageOptions.Builder preview_opts_builder = new DisplayImageOptions.Builder();
		preview_opts_builder.cacheInMemory();
		preview_opts_builder.cacheOnDisc();
		preview_opts_builder.bitmapConfig(Bitmap.Config.RGB_565);
		mPreviewImageDisplayOptions = preview_opts_builder.build();
	}

	public void clearFileCache() {
		mImageLoader.clearDiscCache();
	}

	public void clearMemoryCache() {
		mImageLoader.clearMemoryCache();
	}

	public void displayPreviewImage(final ImageView view, final String url) {
		mImageLoader.displayImage(url, view, mPreviewImageDisplayOptions);
	}

	public void displayProfileImage(final ImageView view, final String url) {
		mImageLoader.displayImage(url, view, mProfileImageDisplayOptions);
	}

	public void reloadConnectivitySettings() {
	}

}
