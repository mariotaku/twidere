/*
 * Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012  Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.parseURL;
import static org.mariotaku.twidere.util.Utils.setIgnoreSSLError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.view.ImageViewer;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;

public class ImageViewerActivity extends FragmentActivity implements Constants, OnClickListener {

	private ImageViewer mImageView;
	private ImageLoader mImageLoader;
	private View mProgress;

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.close: {
				onBackPressed();
				break;
			}
			case R.id.refresh: {
				if (mImageLoader == null || mImageLoader.getStatus() != Status.RUNNING) {
					loadImage();
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.image_viewer);
		mImageView = (ImageViewer) findViewById(R.id.image_viewer);
		mProgress = findViewById(R.id.progress);
		loadImage();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageView.recycle();
		if (mImageLoader != null && !mImageLoader.isCancelled()) {
			mImageLoader.cancel(true);
		}
	}

	private void loadImage() {
		if (mImageLoader != null && mImageLoader.getStatus() == Status.RUNNING) {
			mImageLoader.cancel(true);
		}
		final Uri uri = getIntent().getData();
		if (uri == null) {
			finish();
			return;
		}
		mImageLoader = new ImageLoader(uri, mImageView, this);
		mImageLoader.execute();
	}

	private static class ImageLoader extends AsyncTask<Void, Void, Bitmap> {

		private static final String CACHE_DIR_NAME = "cached_images";

		private final Uri uri;
		private final ImageViewer image_view;
		private final ImageViewerActivity activity;
		private final boolean ignore_ssl_error;
		private File mCacheDir;

		public ImageLoader(Uri uri, ImageViewer image_view, ImageViewerActivity activity) {
			this.uri = uri;
			this.image_view = image_view;
			this.activity = activity;
			ignore_ssl_error = activity.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).getBoolean(
					PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
			init();
		}

		@Override
		protected Bitmap doInBackground(Void... args) {

			if (uri == null) return null;
			final String scheme = uri.getScheme();
			if ("http".equals(scheme) || "https".equals(scheme)) {
				final URL url = parseURL(uri.toString());
				if (url == null) return null;
				if (mCacheDir == null || !mCacheDir.exists()) {
					init();
				}
				final File f = new File(mCacheDir, getURLFilename(url));

				// from SD cache
				final Bitmap b = decodeFile(f);
				if (b != null) return b;

				// from web
				try {
					Bitmap bitmap = null;
					final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					if (ignore_ssl_error) {
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
					if (bitmap == null) {
						// The file is corrupted, so we remove it from cache.
						if (f.isFile()) {
							f.delete();
						}
					}
					return bitmap;
				} catch (final FileNotFoundException e) {
					init();
				} catch (final IOException e) {
					// e.printStackTrace();
				}
			} else if ("file".equals(scheme)) return decodeFile(new File(uri.getPath()));
			return null;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (image_view != null) {
				if (result != null) {
					image_view.setBitmap(result);
				} else {
					image_view
							.setBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.broken_image));
				}
			}
			// activity.mRefresh.setVisibility(View.VISIBLE);
			activity.mProgress.setVisibility(View.INVISIBLE);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// activity.mRefresh.setVisibility(View.INVISIBLE);
			activity.mProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
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

		private Bitmap decodeFile(File f) {
			if (f == null) return null;
			final BitmapFactory.Options o = new BitmapFactory.Options();
			o.inSampleSize = 1;
			Bitmap bitmap = null;
			while (bitmap == null) {
				try {
					final BitmapFactory.Options o2 = new BitmapFactory.Options();
					o2.inSampleSize = o.inSampleSize;
					bitmap = BitmapFactory.decodeFile(f.getPath(), o2);
				} catch (final OutOfMemoryError e) {
					o.inSampleSize++;
					continue;
				}
				if (bitmap == null) {
					break;
				} else
					return bitmap;
			}
			return null;
		}

		private String getURLFilename(URL url) {
			if (url == null) return null;
			return url.toString().replaceAll("[^a-zA-Z0-9]", "_");
		}

		private void init() {
			/* Find the dir to save cached images. */
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				mCacheDir = new File(
						Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor.getExternalCacheDir(activity)
								: new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/"
										+ activity.getPackageName() + "/cache/"), CACHE_DIR_NAME);
			} else {
				mCacheDir = new File(activity.getCacheDir(), CACHE_DIR_NAME);
			}
			if (mCacheDir != null && !mCacheDir.exists()) {
				mCacheDir.mkdirs();
			}
		}

		private static class GetExternalCacheDirAccessor {

			@TargetApi(8)
			public static File getExternalCacheDir(Context context) {
				return context.getExternalCacheDir();
			}
		}
	}
}
