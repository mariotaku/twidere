/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.util.imageloader;

import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.getImageLoaderHttpClient;
import static org.mariotaku.twidere.util.Utils.getRedirectedHttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mariotaku.twidere.BuildConfig;

import twitter4j.TwitterException;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * A simple subclass of {@link ImageResizer} that fetches and resizes images
 * fetched from a URL.
 */
public class ImageFetcher extends ImageResizer {

	private static final String TAG = "ImageFetcher";
	private static final int HTTP_CACHE_SIZE = 10 * 1024 * 1024; // 10MB
	public static final String HTTP_CACHE_DIR = "http";

	private HttpClientWrapper mClient;

	/**
	 * Initialize providing a single target image size (used for both width and
	 * height);
	 * 
	 * @param context
	 * @param imageSize
	 */
	public ImageFetcher(final Context context, final int imageSize) {
		super(context, imageSize);
	}

	/**
	 * Initialize providing a target image width and height for the processing
	 * images.
	 * 
	 * @param context
	 * @param imageWidth
	 * @param imageHeight
	 */
	public ImageFetcher(final Context context, final int imageWidth, final int imageHeight) {
		super(context, imageWidth, imageHeight);
	}

	/**
	 * Download a bitmap from a URL, write it to a disk and return the File
	 * pointer. This implementation uses a simple disk cache.
	 * 
	 * @param context The context to use
	 * @param urlString The URL to fetch
	 * @return A File pointing to the fetched bitmap
	 */
	public File downloadBitmap(final Context context, final String urlString) {
		final File cacheDir = DiskLruCache.getDiskCacheDir(context, HTTP_CACHE_DIR);

		final DiskLruCache cache = DiskLruCache.openCache(context, cacheDir, HTTP_CACHE_SIZE);

		final File cacheFile = new File(cache.createFilePath(urlString));

		if (cache.containsKey(urlString)) {
			if (BuildConfig.DEBUG) {
				Log.d(TAG, "downloadBitmap - found in http cache - " + urlString);
			}
			return cacheFile;
		}

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "downloadBitmap - downloading - " + urlString);
		}

		// ImageLoaderUtils.disableConnectionReuseIfNecessary();
		FileOutputStream out = null;

		try {
			final HttpResponse resp = getRedirectedHttpResponse(mClient, urlString);
			if (resp == null) return null;
			final InputStream in = resp.asStream();
			out = new FileOutputStream(cacheFile);
			copyStream(in, out);
			if (cacheFile.length() > 0) return cacheFile;
			cacheFile.delete();
		} catch (final IOException e) {
			Log.e(TAG, "Error in downloadBitmap - " + e);
		} catch (final TwitterException e) {
			Log.e(TAG, "Error in downloadBitmap - " + e.getMessage());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (final IOException e) {
					Log.e(TAG, "Error in downloadBitmap - " + e);
				}
			}
		}

		return null;
	}

	@Override
	public void init() {
		mClient = getImageLoaderHttpClient(mContext);
		// checkConnection(context);
	}

	@Override
	protected Bitmap processBitmap(final Object data) {
		return processBitmap(String.valueOf(data));
	}

	/**
	 * The main process method, which will be called by the ImageWorker in the
	 * AsyncTask background thread.
	 * 
	 * @param data The data to load the bitmap, in this case, a regular http URL
	 * @return The downloaded and resized bitmap
	 */
	private Bitmap processBitmap(final String data) {
		if (BuildConfig.DEBUG) {
			Log.d(TAG, "processBitmap - " + data);
		}

		// Download a bitmap, write it to a file
		final File f = downloadBitmap(mContext, data);

		if (f != null) // Return a sampled down version
			return decodeSampledBitmapFromFile(f.toString(), mImageWidth, mImageHeight);

		return null;
	}

}
