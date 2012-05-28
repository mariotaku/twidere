package org.mariotaku.twidere.fragment;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ServiceInterface;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class DraftsFragment extends BaseListFragment implements LoaderCallbacks<Cursor>, OnItemClickListener {

	private DraftsAdapter mAdapter;
	private Cursor mCursor;
	private ListView mListView;
	private ContentResolver mResolver;
	private ServiceInterface mInterface;
	private DeleteAllConfirmFragment mDeleteAllConfirmFragment = new DeleteAllConfirmFragment();
	private SendAllConfirmFragment mSendAllConfirmFragment = new SendAllConfirmFragment();
	private DeleteDraftConfirmFragment mDeleteDraftConfirmFragment = new DeleteDraftConfirmFragment();
	private SharedPreferences mPreferences;
	private float mTextSize;
	private DraftItem mDraftItem;
	private long mSelectedId;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_DRAFTS_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, DraftsFragment.this);
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mResolver = getSherlockActivity().getContentResolver();
		mInterface = ServiceInterface.getInstance(getSherlockActivity());
		mPreferences = getSherlockActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mTextSize = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		setHasOptionsMenu(true);
		mAdapter = new DraftsAdapter(getSherlockActivity());
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		mListView.setOnCreateContextMenuListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

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
	public boolean onContextItemSelected(android.view.MenuItem item) {
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
				mDeleteDraftConfirmFragment.show(getFragmentManager(), "delete_draft_confirm");
				break;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		new android.view.MenuInflater(getSherlockActivity()).inflate(R.menu.context_draft, menu);
		AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo) menuInfo;

		if (mCursor != null && adapterinfo.position >= 0 && adapterinfo.position < mCursor.getCount()) {
			mDraftItem = new DraftItem(mCursor, adapterinfo.position);
			mCursor.moveToPosition(adapterinfo.position);
			mSelectedId = mCursor.getLong(mCursor.getColumnIndex(Drafts._ID));
		}
		super.onCreateContextMenu(menu, v, menuInfo);
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
		switch (item.getItemId()) {
			case MENU_SEND_ALL: {
				mSendAllConfirmFragment.show(getFragmentManager(), "send_all_confirm");
				break;
			}
			case MENU_DELETE_ALL: {
				mDeleteAllConfirmFragment.show(getFragmentManager(), "delete_all_confirm");
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onResume() {
		super.onResume();
		float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mAdapter.setTextSize(text_size);
		if (mTextSize != text_size) {
			mTextSize = text_size;
			mListView.invalidateViews();
		}
	}

	@Override
	public void onStart() {
		IntentFilter filter = new IntentFilter(BROADCAST_DRAFTS_DATABASE_UPDATED);
		if (getSherlockActivity() != null) {
			getSherlockActivity().registerReceiver(mStatusReceiver, filter);
		}
		super.onStart();
	}

	@Override
	public void onStop() {
		if (getSherlockActivity() != null) {
			getSherlockActivity().unregisterReceiver(mStatusReceiver);
		}
		super.onStop();
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

	private class DeleteAllConfirmFragment extends BaseDialogFragment implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mResolver.delete(Drafts.CONTENT_URI, null, null);
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
			builder.setTitle(R.string.delete_all);
			builder.setMessage(R.string.delete_all_confirm);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}
	}

	private class DeleteDraftConfirmFragment extends BaseDialogFragment implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mResolver.delete(Drafts.CONTENT_URI, Drafts._ID + " = " + mSelectedId, null);
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
			builder.setTitle(R.string.delete);
			builder.setMessage(R.string.delete_draft_confirm);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}
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

	private class SendAllConfirmFragment extends BaseDialogFragment implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					List<DraftItem> drafts = new ArrayList<DraftItem>();
					if (mCursor != null) {
						mCursor.moveToFirst();
						if (!mCursor.isAfterLast()) {
							drafts.add(new DraftItem(mCursor, mCursor.getPosition()));
							mCursor.moveToNext();
						}
					}
					mResolver.delete(Drafts.CONTENT_URI, null, null);
					for (DraftItem draft : drafts) {
						sendDraft(draft);
					}
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
			builder.setTitle(R.string.send_all);
			builder.setMessage(R.string.send_all_confirm);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}
	}
}
