package org.mariotaku.twidere.loader;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.StatusesCursorIndices;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

public class CursorToStatusesLoader extends AsyncTaskLoader<ParcelableStatus[]> implements Constants {

	private final Uri mUri;
	private final String[] mProjection;
	private final String mSelection;
	private final String[] mSelectionArgs;
	private final String mSortOrder;

	public CursorToStatusesLoader(Context context, Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		super(context);
		mUri = uri;
		mProjection = projection;
		mSelection = selection;
		mSelectionArgs = selectionArgs;
		mSortOrder = sortOrder;
	}

	@Override
	public ParcelableStatus[] loadInBackground() {
		Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection, mSelectionArgs,
				mSortOrder);
		cursor.moveToFirst();
		final StatusesCursorIndices indices = new StatusesCursorIndices(cursor);
		final ParcelableStatus[] result = new ParcelableStatus[cursor.getCount()];
		while (!cursor.isAfterLast()) {
			result[cursor.getPosition()] = new ParcelableStatus(cursor, indices);
			cursor.moveToNext();
		}
		cursor.close();
		return result;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

}