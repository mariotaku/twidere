package org.mariotaku.twidere.loader;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.StatusesCursorIndices;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.AsyncTaskLoader;

public class CursorToStatusesLoader extends AsyncTaskLoader<List<ParcelableStatus>> {

	private final Uri mUri;
	private final String[] mProjection;
	private final String mSelection;
	private final String[] mSelectionArgs;
	private final String mSortOrder;

	private LoadProgressListener mListener;

	private static final int START_LOADING = 1, PROGRESS_CHANGED = 2, FINISH_LOADING = 3;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case START_LOADING: {
					if (mListener != null) {
						mListener.onStartLoading(msg.arg1);
					}
					break;
				}
				case PROGRESS_CHANGED: {
					if (mListener != null) {
						mListener.onProgressChanged(msg.arg1);
					}
					break;
				}
				case FINISH_LOADING: {
					if (mListener != null) {
						mListener.onFinishLoading();
					}
					break;
				}
			}
			super.handleMessage(msg);
		}

	};

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
	public List<ParcelableStatus> loadInBackground() {
		Cursor cursor = getContext().getContentResolver().query(mUri, mProjection, mSelection, mSelectionArgs,
				mSortOrder);
		final int count = cursor.getCount();
		if (count > 0) {
			Message start_msg = new Message();
			start_msg.what = START_LOADING;
			start_msg.arg1 = count;
			mHandler.sendMessage(start_msg);
		}
		cursor.moveToFirst();
		final StatusesCursorIndices indices = new StatusesCursorIndices(cursor);
		final List<ParcelableStatus> result = new ArrayList<ParcelableStatus>();
		while (!cursor.isAfterLast()) {
			result.add(new ParcelableStatus(cursor, indices));
			Message progress_msg = new Message();
			progress_msg.what = PROGRESS_CHANGED;
			progress_msg.arg1 = cursor.getPosition();
			mHandler.sendMessage(progress_msg);
			cursor.moveToNext();
		}
		cursor.close();
		if (count > 0) {
			Message finish_msg = new Message();
			finish_msg.what = FINISH_LOADING;
			mHandler.sendMessage(finish_msg);
		}
		return result;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

	public void setLoadProgressListener(LoadProgressListener listener) {
		mListener = listener;
	}

	public interface LoadProgressListener {
		void onFinishLoading();

		void onProgressChanged(int progress);

		void onStartLoading(int total_count);
	}

}