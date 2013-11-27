package org.mariotaku.twidere.util.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

import org.mariotaku.twidere.TwidereConstants;

public final class SupportFragmentReloadCursorObserver extends ContentObserver implements TwidereConstants {

	private final Fragment mFragment;
	private final int mLoaderId;
	private final LoaderCallbacks<Cursor> mCallback;

	public SupportFragmentReloadCursorObserver(final Fragment fragment, final int loaderId,
			final LoaderCallbacks<Cursor> callback) {
		super(createHandler());
		mFragment = fragment;
		mLoaderId = loaderId;
		mCallback = callback;
	}

	@Override
	public void onChange(final boolean selfChange) {
		onChange(selfChange, null);
	}

	@Override
	public void onChange(final boolean selfChange, final Uri uri) {
		if (mFragment == null || mFragment.getActivity() == null || mFragment.isDetached()) return;
		// Handle change.
		mFragment.getLoaderManager().restartLoader(mLoaderId, null, mCallback);
	}

	private static Handler createHandler() {
		if (Thread.currentThread().getId() != 1) return null;
		return new Handler();
	}
}