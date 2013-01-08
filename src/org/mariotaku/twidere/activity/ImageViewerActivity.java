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

import static org.mariotaku.twidere.util.Utils.showErrorToast;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;

import java.io.File;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.loader.AbstractImageLoader.DownloadListener;
import org.mariotaku.twidere.loader.ImageLoader;
import org.mariotaku.twidere.util.SaveImageTask;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class ImageViewerActivity extends FragmentActivity implements Constants, OnClickListener,
		LoaderCallbacks<ImageLoader.Result>, DownloadListener {

	private ImageViewTouch mImageView;
	private ProgressBar mProgress;
	private ImageButton mRefreshStopSaveButton;
	private boolean mImageLoaded;
	private File mImageFile;
	private long mContentLength;

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
					new SaveImageTask(this, mImageFile).execute();
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
		mImageView = (ImageViewTouch) findViewById(R.id.image_viewer);
		mRefreshStopSaveButton = (ImageButton) findViewById(R.id.refresh_stop_save);
		mProgress = (ProgressBar) findViewById(R.id.progress);
	}

	@Override
	public Loader<ImageLoader.Result> onCreateLoader(final int id, final Bundle args) {
		mProgress.setVisibility(View.VISIBLE);
		mProgress.setIndeterminate(true);
		mRefreshStopSaveButton.setImageResource(R.drawable.ic_menu_stop);
		final Uri uri = args != null ? (Uri) args.getParcelable(INTENT_KEY_URI) : null;
		return new ImageLoader(this, this, uri);
	}

	@Override
	public void onDownloadError(final Throwable t) {
		mContentLength = 0;
	}

	@Override
	public void onDownloadFinished() {
		mContentLength = 0;
	}

	@Override
	public void onDownloadStart(final long total) {
		mContentLength = total;
		mProgress.setIndeterminate(false);
		mProgress.setMax(100);
	}

	@Override
	public void onLoaderReset(final Loader<ImageLoader.Result> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<ImageLoader.Result> loader, final ImageLoader.Result data) {
		if (data != null && data.bitmap != null) {
			mImageView.setImageBitmap(data.bitmap);
			mImageFile = data.file;
			mImageLoaded = true;
			mRefreshStopSaveButton.setImageResource(R.drawable.ic_menu_save);
		} else {
			mImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.broken_image));
			mImageFile = null;
			mImageLoaded = false;
			mRefreshStopSaveButton.setImageResource(R.drawable.ic_menu_refresh);
			if (data != null) {
				showErrorToast(this, null, data.exception, true);
			}
		}
		mProgress.setVisibility(View.GONE);
		mProgress.setProgress(0);
	}

	@Override
	public void onProgressUpdate(final long downloaded) {
		if (mContentLength == 0) {
			mProgress.setIndeterminate(true);
			return;
		}
		mProgress.setIndeterminate(false);
		mProgress.setProgress((int) (downloaded * 100 / mContentLength));
	}

	@Override
	protected void onCreate(final Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.image_viewer);
		loadImage(true);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
		mImageView.setImageBitmap(null);
		final Bundle args = new Bundle();
		args.putParcelable(INTENT_KEY_URI, uri);
		if (init) {
			getSupportLoaderManager().initLoader(0, args, this);
		} else {
			getSupportLoaderManager().restartLoader(0, args, this);
		}
	}

	private void stopLoading() {
		getSupportLoaderManager().destroyLoader(0);
		if (!mImageLoaded) {
			mRefreshStopSaveButton.setImageResource(R.drawable.ic_menu_refresh);
			mImageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.refresh_image));
			mProgress.setVisibility(View.GONE);
		}
	}

}
