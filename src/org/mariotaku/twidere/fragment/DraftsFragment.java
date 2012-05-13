package org.mariotaku.twidere.fragment;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class DraftsFragment extends BaseListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener {

	private DraftsAdapter mAdapter;
	private Cursor mCursor;
	private ListView mListView;
	private ContentResolver mResolver;
	private ServiceInterface mInterface;

	private long mSelectedId;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mResolver = getSherlockActivity().getContentResolver();
		mInterface = ServiceInterface.getInstance(getSherlockActivity());
		setHasOptionsMenu(true);
		mAdapter = new DraftsAdapter(getSherlockActivity());
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_COMPOSE: {
				if (resultCode == Activity.RESULT_OK) {
					mResolver.delete(Drafts.CONTENT_URI, Drafts._ID + " = " + mSelectedId, null);
					getLoaderManager().restartLoader(0, null, this);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Drafts.CONTENT_URI;
		String[] cols = Drafts.COLUMNS;
		return new CursorLoader(getSherlockActivity(), uri, cols, null, null, null);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_drafts, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if (mCursor != null) {
			mSelectedId = id;
			DraftItem draft = new DraftItem(mCursor, position);
			composeDraft(draft);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		if (mCursor != null) {
			mSelectedId = id;
			DraftItem draft = new DraftItem(mCursor, position);
			composeDraft(draft);
		}
		return true;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		mCursor = null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.changeCursor(cursor);
		mCursor = cursor;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	private void composeDraft(DraftItem draft) {
		Intent intent = new Intent(INTENT_ACTION_COMPOSE);
		Bundle bundle = new Bundle();
		bundle.putString(INTENT_KEY_TEXT, draft.text);
		bundle.putLongArray(INTENT_KEY_IDS, draft.account_ids);
		bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, draft.in_reply_to_status_id);
		intent.putExtras(bundle);
		startActivityForResult(intent, REQUEST_COMPOSE);
	}

	private void sendDraft(DraftItem draft) {
		Uri uri = draft.media_uri == null ? null : Uri.parse(draft.media_uri);
		mInterface.updateStatus(draft.account_ids, draft.text, null, uri, draft.in_reply_to_status_id);
	}

	private class DraftItem {

		public final long[] account_ids;
		public final long in_reply_to_status_id;
		public final String text, media_uri;

		public DraftItem(Cursor cursor, int position) {
			mCursor.moveToPosition(position);
			text = mCursor.getString(mCursor.getColumnIndex(Drafts.TEXT));
			media_uri = mCursor.getString(mCursor.getColumnIndex(Drafts.MEDIA_URI));
			String account_ids_string = mCursor.getString(mCursor.getColumnIndex(Drafts.ACCOUNT_IDS));
			in_reply_to_status_id = mCursor.getLong(mCursor.getColumnIndex(Drafts.IN_REPLY_TO_STATUS_ID));
			if (account_ids_string != null) {
				String[] ids_string_array = account_ids_string.split(";");
				List<Long> ids_list = new ArrayList<Long>();
				for (String id_string : ids_string_array) {
					try {
						ids_list.add(Long.parseLong(id_string));
					} catch (NumberFormatException e) {
						// Ignore.
					}
				}
				account_ids = new long[ids_list.size()];
				for (int i = 0; i < ids_list.size(); i++) {
					account_ids[i] = ids_list.get(i);
				}
			} else {
				account_ids = null;
			}
		}
	}

	private static class DraftsAdapter extends SimpleCursorAdapter {

		private static final String[] mFrom = new String[] { Drafts.TEXT };
		private static final int[] mTo = new int[] { R.id.text };

		public DraftsAdapter(Context context) {
			super(context, R.layout.draft_list_item, null, mFrom, mTo, 0);
		}

	}
}
