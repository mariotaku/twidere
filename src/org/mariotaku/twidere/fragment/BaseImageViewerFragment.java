package org.mariotaku.twidere.fragment;

import java.io.File;

import org.mariotaku.twidere.loader.AbstractImageLoader.DownloadListener;
import org.mariotaku.twidere.loader.AbstractImageLoader.Result;
import org.mariotaku.twidere.loader.ImageLoader;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;

public abstract class BaseImageViewerFragment extends BaseFragment implements LoaderCallbacks<ImageLoader.Result> {

	private File mImageFile;

	public File getImageFile() {
		return mImageFile;
	}

	@Override
	public final Loader<Result> onCreateLoader(int id, Bundle args) {
		final FragmentActivity activity = getActivity();
		return newLoaderInstance(activity, args, activity instanceof DownloadListener ? (DownloadListener) activity : null);
	}

	private Loader<Result> newLoaderInstance(FragmentActivity activity, Bundle args, DownloadListener downloadListener) {
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Result> loader, Result data) {
		mImageFile = data.file;
		
	}

	@Override
	public final void onLoaderReset(Loader<Result> loader) {
		mImageFile = null;
	}
}
