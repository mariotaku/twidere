package org.mariotaku.twidere.activity;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class DraftsActivity extends BaseActivity implements LoaderCallbacks<Cursor>, OnItemClickListener,
		OnItemLongClickListener, OnMenuItemClickListener {

	private DraftsAdapter mAdapter;
	private Cursor mCursor;
	private ListView mListView;
	private ContentResolver mResolver;
	private ServiceInterface mInterface;
	private SharedPreferences mPreferences;
	private float mTextSize;
	private DraftItem mDraftItem;
	private long mSelectedId;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_DRAFTS_DATABASE_UPDATED.equals(action)) {
				getSupportLoaderManager().restartLoader(0, null, DraftsActivity.this);
			}
		}
	};

	private PopupMenu mPopupMenu;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_COMPOSE: {
				if (resultCode == Activity.RESULT_OK) {
					mResolver.delete(Drafts.CONTENT_URI, Drafts._ID + " = " + mSelectedId, null);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mResolver = getContentResolver();
		mInterface = ServiceInterface.getInstance(this);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mTextSize = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		setContentView(R.layout.drafts_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mAdapter = new DraftsAdapter(this);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final Uri uri = Drafts.CONTENT_URI;
		final String[] cols = Drafts.COLUMNS;
		return new CursorLoader(this, uri, cols, null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_drafts, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if (mCursor != null) {
			mSelectedId = id;
			final DraftItem draft = new DraftItem(mCursor, position);
			composeDraft(draft);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		if (mCursor != null && position >= 0 && position < mCursor.getCount()) {
			mDraftItem = new DraftItem(mCursor, position);
			mCursor.moveToPosition(position);
			mSelectedId = mCursor.getLong(mCursor.getColumnIndex(Drafts._ID));
		}

		mPopupMenu = PopupMenu.getInstance(this, view);
		mPopupMenu.inflate(R.menu.context_draft);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return false;
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
		mCursor = null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
		mCursor = cursor;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SEND: {
				sendDraft(mDraftItem);
				break;
			}
			case MENU_EDIT: {
				composeDraft(mDraftItem);
				break;
			}
			case MENU_DELETE: {
				mResolver.delete(Drafts.CONTENT_URI, Drafts._ID + " = " + mSelectedId, null);
				break;
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
			case MENU_SEND_ALL: {
				final List<DraftItem> drafts = new ArrayList<DraftItem>();
				if (mCursor != null) {
					mCursor.moveToFirst();
					if (!mCursor.isAfterLast()) {
						drafts.add(new DraftItem(mCursor, mCursor.getPosition()));
						mCursor.moveToNext();
					}
				}
				mResolver.delete(Drafts.CONTENT_URI, null, null);
				for (final DraftItem draft : drafts) {
					sendDraft(draft);
				}
				break;
			}
			case MENU_DELETE_ALL: {
				mResolver.delete(Drafts.CONTENT_URI, null, null);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mAdapter.setTextSize(text_size);
		if (mTextSize != text_size) {
			mTextSize = text_size;
			mListView.invalidateViews();
		}
	}

	@Override
	public void onStart() {
		final IntentFilter filter = new IntentFilter(BROADCAST_DRAFTS_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
		super.onStart();
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	private void composeDraft(DraftItem draft) {
		final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
		final Bundle bundle = new Bundle();
		bundle.putString(INTENT_KEY_TEXT, draft.text);
		bundle.putLongArray(INTENT_KEY_IDS, draft.account_ids);
		bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, draft.in_reply_to_status_id);
		intent.putExtras(bundle);
		startActivityForResult(intent, REQUEST_COMPOSE);
	}

	private void sendDraft(DraftItem draft) {
		final Uri uri = draft.media_uri == null ? null : Uri.parse(draft.media_uri);
		mInterface.updateStatus(draft.account_ids, draft.text, null, uri, draft.in_reply_to_status_id, false);
	}

	private class DraftItem {

		public final long[] account_ids;
		public final long in_reply_to_status_id;
		public final String text, media_uri;

		public DraftItem(Cursor cursor, int position) {
			mCursor.moveToPosition(position);
			text = mCursor.getString(mCursor.getColumnIndex(Drafts.TEXT));
			media_uri = mCursor.getString(mCursor.getColumnIndex(Drafts.MEDIA_URI));
			final String account_ids_string = mCursor.getString(mCursor.getColumnIndex(Drafts.ACCOUNT_IDS));
			in_reply_to_status_id = mCursor.getLong(mCursor.getColumnIndex(Drafts.IN_REPLY_TO_STATUS_ID));
			if (account_ids_string != null) {
				final String[] ids_string_array = account_ids_string.split(";");
				final List<Long> ids_list = new ArrayList<Long>();
				for (final String id_string : ids_string_array) {
					try {
						ids_list.add(Long.parseLong(id_string));
					} catch (final NumberFormatException e) {
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
		private float mTextSize;

		public DraftsAdapter(Context context) {
			super(context, R.layout.draft_list_item, null, mFrom, mTo, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			((TextView) view.findViewById(R.id.text)).setTextSize(mTextSize);
			super.bindView(view, context, cursor);
		}

		public void setTextSize(float text_size) {
			mTextSize = text_size;
		}

	}
}
