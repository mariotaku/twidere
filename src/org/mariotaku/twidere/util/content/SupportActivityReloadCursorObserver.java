package org.mariotaku.twidere.util.content;

import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;

import org.mariotaku.twidere.TwidereConstants;

public final class SupportActivityReloadCursorObserver extends ContentObserver implements TwidereConstants {

	private final FragmentActivity mActivity;
	private final int mLoaderId;
	private final LoaderCallbacks<Cursor> mCallback;

	public SupportActivityReloadCursorObserver(final FragmentActivity activity, final int loaderId,
			final LoaderCallbacks<Cursor> callback) {
		super(createHandler());
		mActivity = activity;
		mLoaderId = loaderId;
		mCallback = callback;
	}

	@Override
	public void onChange(final boolean selfChange) {
		onChange(selfChange, null);
	}

	@Override
	public void onChange(final boolean selfChange, final Uri uri) {
		if (mActivity == null || mActivity.isFinishing()) return;
		// Handle change.
		mActivity.getSupportLoaderManager().restartLoader(mLoaderId, null, mCallback);
	}

	private static Handler createHandler() {
		if (Thread.currentThread().getId() != 1) return null;
		return new Handler();
	}
}