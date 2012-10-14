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

package org.mariotaku.twidere.activity;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;
import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.getBrowserUserAgent;
import static org.mariotaku.twidere.util.Utils.getConnection;
import static org.mariotaku.twidere.util.Utils.getProxy;
import static org.mariotaku.twidere.util.Utils.parseURL;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

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
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.BitmapDecodeHelper;
import org.mariotaku.twidere.util.GetExternalCacheDirAccessor;
import org.mariotaku.twidere.view.ImageViewer;

import twitter4j.http.HostAddressResolver;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class ImageViewerActivity extends FragmentActivity implements Constants, OnClickListener,
		LoaderCallbacks<ImageViewerActivity.ImageLoader.Result> {

	private ImageViewer mImageView;
	private View mProgress;
	private ImageButton mRefreshStopSaveButton;
	private boolean mImageLoaded;
	private File mImageFile;
	private String mUserAgent;

	private HostAddressResolver mResolver;

	@Override
	public void onClick(final View view) {
		final Uri uri = getIntent().getData();
		switch (view.getId()) {
			case R.id.close: {
				onBackPressed();
				break;
			}
			case R.id.refresh_stop_save: {
				final LoaderManager lm = getSupportLoaderManager();
				if (!mImageLoaded && !lm.hasRunningLoaders()) {
					loadImage(false);
				} else if (!mImageLoaded && lm.hasRunningLoaders()) {
					stopLoading();
				} else if (mImageLoaded) {
					saveImage();
				}
				break;
			}
			case R.id.share: {
				if (uri == null) {
					break;
				}
				final Intent intent = new Intent(Intent.ACTION_SEND);
				if (mImageFile != null && mImageFile.exists()) {
					intent.setType("image/*");
					intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(mImageFile));
				} else {
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, uri.toString());
				}
				startActivity(Intent.createChooser(intent, getString(R.string.share)));
				break;
			}
			case R.id.open_in_browser: {
				if (uri == null) {
					break;
				}
				final String scheme = uri.getScheme();
				if ("http".equals(scheme) || "https".equals(scheme)) {
					final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					intent.addCategory(Intent.CATEGORY_BROWSABLE);
					try {
						startActivity(intent);
					} catch (final ActivityNotFoundException e) {
						// Ignore.
					}
				}
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mImageView = (ImageViewer) findViewById(R.id.image_viewer);
		mRefreshStopSaveButton = (ImageButton) findViewById(R.id.refresh_stop_save);
		mProgress = findViewById(R.id.progress);
	}

	@Override
	public Loader<ImageLoader.Result> onCreateLoader(final int id, final Bundle args) {
		mProgress.setVisibility(View.VISIBLE);
		mRefreshStopSaveButton.setImageResource(R.drawable.ic_menu_stop);
		final Uri uri = (Uri) (args != null ? args.getParcelable(INTENT_KEY_URI) : null);
		return new ImageLoader(this, mResolver, uri, mUserAgent);
	}

	@Override
	public void onLoaderReset(final Loader<ImageLoader.Result> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<ImageLoader.Result> loader, final ImageLoader.Result data) {
		if (data != null && data.bitmap != null) {
			mImageView.setBitmap(data.bitmap);
			mImageFile = data.file;
			mImageLoaded = true;
			mRefreshStopSaveButton.setImageResource(R.drawable.ic_menu_save);
		} else {
			mImageView.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.broken_image));
			mImageFile = null;
			mImageLoaded = false;
			mRefreshStopSaveButton.setImageResource(R.drawable.ic_menu_refresh);
			if (data != null) {
				showErrorToast(this, null, data.exception, true);
			}
		}
		mProgress.setVisibility(View.GONE);
	}

	@Override
	protected void onCreate(final Bundle icicle) {
		mResolver = TwidereApplication.getInstance(this).getHostAddressResolver();
		super.onCreate(icicle);
		mUserAgent = getBrowserUserAgent(this);
		setContentView(R.layout.image_viewer);
		loadImage(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageView.recycle();
		stopLoading();
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		loadImage(false);
	}

	private void loadImage(final boolean init) {
		getSupportLoaderManager().destroyLoader(0);
		final Uri uri = getIntent().getData();
		if (uri == null) {
			finish();
			return;
		}
		mImageView.setBitmap(null);
		final Bundle args = new Bundle();
		args.putParcelable(INTENT_KEY_URI, uri);
		if (init) {
			getSupportLoaderManager().initLoader(0, args, this);
		} else {
			getSupportLoaderManager().restartLoader(0, args, this);
		}
	}

	private void saveImage() {
		if (mImageFile != null && mImageFile.exists()) {
			final Uri uri = getIntent().getData();
			if (uri == null) return;
			final String file_name = uri.getLastPathSegment();
			final BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(mImageFile.getPath(), o);
			final String mime_type = o.outMimeType;
			String file_name_with_suffix = null;
			if (file_name.matches("(.*/)*.+\\.(png|jpg|gif|bmp|jpeg|PNG|JPG|JPEG|GIF|BMP)$")) {
				file_name_with_suffix = file_name;
			} else {
				if (mime_type == null) return;
				if (mime_type.startsWith("image/") && !"image/*".equals(mime_type)) {
					file_name_with_suffix = file_name + "." + mime_type.substring(5);
				}
			}
			final Intent intent = new Intent(INTENT_ACTION_SAVE_FILE);
			intent.setPackage(getPackageName());
			intent.putExtra(INTENT_KEY_FILE_SOURCE, mImageFile.getPath());
			intent.putExtra(INTENT_KEY_FILENAME, file_name_with_suffix);
			startActivity(intent);
		}
	}

	private void stopLoading() {
		getSupportLoaderManager().destroyLoader(0);
		if (!mImageLoaded) {
			mRefreshStopSaveButton.setImageResource(R.drawable.ic_menu_refresh);
			mImageView.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.refresh_image));
			mProgress.setVisibility(View.GONE);
		}
	}

	public static class ImageLoader extends AsyncTaskLoader<ImageLoader.Result> {

		private static final String CACHE_DIR_NAME = "cached_images";

		private final Uri uri;
		private final int connection_timeout;

		private final Context context;
		private final String user_agent;
		private final HostAddressResolver resolver;
		private File mCacheDir;

		public ImageLoader(final Context context, final HostAddressResolver resolver, final Uri uri,
				final String user_agent) {
			super(context);
			this.context = context;
			this.resolver = resolver;
			this.uri = uri;
			connection_timeout = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).getInt(
					PREFERENCE_KEY_CONNECTION_TIMEOUT, 10) * 1000;
			this.user_agent = user_agent;
			init();
		}

		@Override
		public Result loadInBackground() {

			if (uri == null) return new Result(null, null, null);
			final String scheme = uri.getScheme();
			if ("http".equals(scheme) || "https".equals(scheme)) {
				final URL url = parseURL(uri.toString());
				if (url == null) return new Result(null, null, null);
				if (mCacheDir == null || !mCacheDir.exists()) {
					init();
				}
				final File cache_file = new File(mCacheDir, getURLFilename(url));

				// from SD cache
				final Bitmap cached_bitmap = decodeFile(cache_file);
				if (cached_bitmap != null) return new Result(cached_bitmap, cache_file, null);

				int response_code = -1;
				// from web
				try {
					Bitmap bitmap = null;
					HttpURLConnection conn = null;
					int retryCount = 0;
					URL request_url = url;

					while (retryCount < 5) {
						conn = getConnection(request_url, connection_timeout, true, getProxy(context), resolver);
						conn.addRequestProperty("User-Agent", user_agent);
						conn.setConnectTimeout(30000);
						conn.setReadTimeout(30000);
						conn.setInstanceFollowRedirects(false);
						response_code = conn.getResponseCode();
						if (response_code != 301 && response_code != 302) {
							break;
						}
						final String loc = conn.getHeaderField("Location");
						if (loc == null) {
							break;
						}
						request_url = new URL(loc);
						retryCount++;
					}
					if (conn != null) {
						final InputStream is = conn.getInputStream();
						final OutputStream os = new FileOutputStream(cache_file);
						copyStream(is, os);
						os.close();
						bitmap = decodeFile(cache_file);
						if (bitmap == null) {
							// The file is corrupted, so we remove it from
							// cache.
							if (cache_file.isFile()) {
								cache_file.delete();
							}
						} else
							return new Result(bitmap, cache_file, null);
					}
				} catch (final FileNotFoundException e) {
					init();
				} catch (final IOException e) {
					return new Result(null, null, e);
				} catch (final NullPointerException e) {
					return new Result(null, null, e);
				}
			} else if ("file".equals(scheme)) {
				final File file = new File(uri.getPath());
				return new Result(decodeFile(file), file, null);
			}
			return new Result(null, null, null);
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		private Bitmap decodeFile(final File f) {
			if (f == null) return null;
			final BitmapFactory.Options o = new BitmapFactory.Options();
			o.inSampleSize = 1;
			Bitmap bitmap = null;
			while (bitmap == null) {
				try {
					final BitmapFactory.Options o2 = new BitmapFactory.Options();
					o2.inSampleSize = o.inSampleSize;
					bitmap = BitmapDecodeHelper.decode(f.getPath(), o2);
				} catch (final OutOfMemoryError e) {
					o.inSampleSize++;
					continue;
				}
				if (bitmap == null) {
					break;
				}
				return bitmap;
			}
			return null;
		}

		private String getURLFilename(final URL url) {
			if (url == null) return null;
			return url.toString().replaceFirst("https?:\\/\\/", "").replaceAll("[^a-zA-Z0-9]", "_");
		}

		private void init() {
			/* Find the dir to save cached images. */
			if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				mCacheDir = new File(
						Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor.getExternalCacheDir(context)
								: new File(getExternalStorageDirectory().getPath() + "/Android/data/"
										+ context.getPackageName() + "/cache/"), CACHE_DIR_NAME);
			} else {
				mCacheDir = new File(context.getCacheDir(), CACHE_DIR_NAME);
			}
			if (mCacheDir != null && !mCacheDir.exists()) {
				mCacheDir.mkdirs();
			}
		}

		public static class Result {
			public final Bitmap bitmap;
			public final File file;
			public final Exception exception;

			public Result(final Bitmap bitmap, final File file, final Exception exception) {
				this.bitmap = bitmap;
				this.file = file;
				this.exception = exception;
			}
		}
	}
}
