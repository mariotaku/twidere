/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.mariotaku.gallery3d.util;

import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.getImageLoaderHttpClient;
import static org.mariotaku.twidere.util.Utils.getRedirectedHttpResponse;
import static org.mariotaku.twidere.util.Utils.parseString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;

import org.mariotaku.gallery3d.util.ThreadPool.CancelListener;
import org.mariotaku.gallery3d.util.ThreadPool.JobContext;
import org.mariotaku.twidere.twitter4j.TwitterException;
import org.mariotaku.twidere.twitter4j.http.HttpClientWrapper;

import android.content.Context;

public class CachedDownloader {

	public static final String CACHE_DIR_NAME = "cached_images";

	private final Context mContext;
	private File mCacheRoot;
	private HttpClientWrapper mClient;

	public CachedDownloader(final Context context) {
		mContext = context;
		initCacheDir();
		initHttpClient();
	}

	public File download(final JobContext jc, final String url) throws IOException, TwitterException {
		final File file = getCacheFile(url);
		if (file == null) return null;
		if (!file.exists() || file.length() == 0) {
			final InputStream input = getRedirectedHttpResponse(mClient, parseString(url)).asStream();
			final FileOutputStream output = new FileOutputStream(file);
			try {
				dump(jc, input, output);
			} finally {
				Utils.closeSilently(input);
			}
		}
		return file;
	}

	public File getCacheFile(final String url) {
		if (url == null) return null;
		return new File(mCacheRoot, url.replaceAll("https?:\\/\\/", "").replaceAll("[^\\w\\d]", "_"));
	}

	void initCacheDir() {
		mCacheRoot = getBestCacheDir(mContext, CACHE_DIR_NAME);
	}

	void initHttpClient() {
		mClient = getImageLoaderHttpClient(mContext);
	}

	private static void dump(final JobContext jc, final InputStream is, final OutputStream os) throws IOException {
		final byte buffer[] = new byte[8192];
		int rc = is.read(buffer, 0, buffer.length);
		final Thread thread = Thread.currentThread();
		jc.setCancelListener(new CancelListener() {
			@Override
			public void onCancel() {
				thread.interrupt();
			}
		});
		while (rc > 0) {
			if (jc.isCancelled()) throw new InterruptedIOException();
			os.write(buffer, 0, rc);
			rc = is.read(buffer, 0, buffer.length);
		}
		jc.setCancelListener(null);
		Thread.interrupted(); // consume the interrupt signal
	}
}