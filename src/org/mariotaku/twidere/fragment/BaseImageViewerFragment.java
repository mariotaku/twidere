package org.mariotaku.twidere.fragment;

import java.io.File;

import org.mariotaku.twidere.loader.AbsImageLoader.DownloadListener;
import org.mariotaku.twidere.loader.AbsImageLoader.Result;
import org.mariotaku.twidere.loader.ImageLoader;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;

public abstract class BaseImageViewerFragment extends BaseFragment implements LoaderCallbacks<ImageLoader.Result> {

	private File mImageFile;

	public File getImageFile() {
		return mImageFile;
	}

	@Override
	public final Loader<Result> onCreateLoader(final int id, final Bundle args) {
		final Activity activity = getActivity();
		final DownloadListener listener = activity instanceof DownloadListener ? (DownloadListener) activity : null;
		return newLoaderInstance(getActivity(), args, listener);
	}

	@Override
	public final void onLoaderReset(final Loader<Result> loader) {
		mImageFile = null;
	}

	@Override
	public void onLoadFinished(final Loader<Result> loader, final Result data) {
		mImageFile = data.file;

	}

	private Loader<Result> newLoaderInstance(final Activity activity, final Bundle args,
			final DownloadListener downloadListener) {
		return null;
	}
}
