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
import org.mariotaku.twidere.twitter4j.http.HttpClientWrapper;
import org.mariotaku.twidere.twitter4j.http.HttpResponse;

import android.content.Context;
import android.os.Handler;

public class CachedDownloader {

	public static final String CACHE_DIR_NAME = "cached_images";

	private final Context mContext;
	private final Handler mHandler;
	private File mCacheRoot;
	private HttpClientWrapper mClient;

	public CachedDownloader(final Context context) {
		mContext = context;
		mHandler = new Handler();
		initCacheDir();
		initHttpClient();
	}

	public File downloadOrGetCache(final JobContext jc, final DownloadListener listener, final String url) {
		final File file = getCacheFile(url);
		if (file == null) return null;
		if (!file.exists() || file.length() == 0) {
			try {
				final HttpResponse resp = getRedirectedHttpResponse(mClient, parseString(url));
				final InputStream input = resp.asStream();
				final long length = resp.getContentLength();
				mHandler.post(new DownloadStartRunnable(listener, length));
				final FileOutputStream output = new FileOutputStream(file);
				try {
					dump(jc, listener, file, input, output);
					mHandler.post(new DownloadFinishRunnable(listener));
				} finally {
					GalleryUtils.closeSilently(input);
				}
				if (length != file.length()) {
					file.delete();
					return null;
				}
			} catch (final Throwable t) {
				file.delete();
				mHandler.post(new DownloadErrorRunnable(listener, t));
				return null;
			}
		}
		return file;
	}

	// public File download(final JobContext jc, final String url) {
	// return download(jc, null, url);
	// }

	public File getCacheFile(final String url) {
		if (url == null) return null;
		return new File(mCacheRoot, url.replaceAll("https?:\\/\\/", "").replaceAll("[^\\w\\d]", "_"));
	}

	private void dump(final JobContext jc, final DownloadListener listener, final File file, final InputStream is,
			final OutputStream os) throws IOException {
		final byte buffer[] = new byte[1024];
		int rc = is.read(buffer, 0, buffer.length);
		final Thread thread = Thread.currentThread();
		jc.setCancelListener(new CancelListener() {
			@Override
			public void onCancel() {
				file.delete();
				thread.interrupt();
			}
		});
		long downloaded = 0;
		while (rc > 0) {
			downloaded += rc;
			mHandler.post(new ProgressUpdateRunnable(listener, downloaded));
			if (jc.isCancelled()) {
				file.delete();
				throw new InterruptedIOException();
			}
			os.write(buffer, 0, rc);
			rc = is.read(buffer, 0, buffer.length);
		}
		jc.setCancelListener(null);
		Thread.interrupted(); // consume the interrupt signal
	}

	void initCacheDir() {
		mCacheRoot = getBestCacheDir(mContext, CACHE_DIR_NAME);
	}

	void initHttpClient() {
		mClient = getImageLoaderHttpClient(mContext);
	}

	public static interface DownloadListener {
		void onDownloadError(Throwable t);

		void onDownloadFinished();

		void onDownloadStart(long total);

		void onProgressUpdate(long downloaded);
	}

	private final static class DownloadErrorRunnable implements Runnable {

		private final DownloadListener listener;
		private final Throwable t;

		DownloadErrorRunnable(final DownloadListener listener, final Throwable t) {
			this.listener = listener;
			this.t = t;
		}

		@Override
		public void run() {
			if (listener == null) return;
			listener.onDownloadError(t);
		}
	}

	private final static class DownloadFinishRunnable implements Runnable {

		private final DownloadListener listener;

		DownloadFinishRunnable(final DownloadListener listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			if (listener == null) return;
			listener.onDownloadFinished();
		}
	}

	private final static class DownloadStartRunnable implements Runnable {

		private final DownloadListener listener;
		private final long total;

		DownloadStartRunnable(final DownloadListener listener, final long total) {
			this.listener = listener;
			this.total = total;
		}

		@Override
		public void run() {
			if (listener == null) return;
			listener.onDownloadStart(total);
		}
	}

	private final static class ProgressUpdateRunnable implements Runnable {

		private final DownloadListener listener;
		private final long current;

		ProgressUpdateRunnable(final DownloadListener listener, final long current) {
			this.listener = listener;
			this.current = current;
		}

		@Override
		public void run() {
			if (listener == null) return;
			listener.onProgressUpdate(current);
		}
	}
}