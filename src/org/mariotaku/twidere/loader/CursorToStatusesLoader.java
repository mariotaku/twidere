package org.mariotaku.twidere.loader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.StatusesCursorIndices;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;

public class CursorToStatusesLoader extends AsyncTaskLoader<List<ParcelableStatus>> {

	public static final Comparator<ParcelableStatus> TIMESTAMP_COMPARATOR = new Comparator<ParcelableStatus>() {

		@Override
		public int compare(ParcelableStatus object1, ParcelableStatus object2) {
			long diff = object2.status_timestamp - object1.status_timestamp;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

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
	public void deliverResult(List<ParcelableStatus> data) {
		if (data != null) {
			Collections.sort(data, TIMESTAMP_COMPARATOR);
		}
		super.deliverResult(data);
	}

	@Override
	public List<ParcelableStatus> loadInBackground() {
		Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection, mSelectionArgs,
				mSortOrder);
		cursor.moveToFirst();
		final StatusesCursorIndices indices = new StatusesCursorIndices(cursor);
		final List<ParcelableStatus> result = new ArrayList<ParcelableStatus>();
		while (!cursor.isAfterLast()) {
			result.add(new ParcelableStatus(cursor, indices));
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