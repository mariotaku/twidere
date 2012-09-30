package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getTabIconDrawable;
import static org.mariotaku.twidere.util.Utils.getTabIconObject;
import static org.mariotaku.twidere.util.Utils.getTabTypeName;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CustomTabsFragment extends BaseListFragment implements LoaderCallbacks<Cursor>, OnItemLongClickListener,
		OnMenuItemClickListener, OnItemClickListener, Panes.Right {

	private ListView mListView;
	private CustomTabsAdapter mAdapter;
	private ContentResolver mResolver;
	private long mSelectedId;
	private int mSelectedPos;
	private Cursor mCursor;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TABS_UPDATED.equals(action)) {
				mSelectedId = -1;
				getLoaderManager().restartLoader(0, null, CustomTabsFragment.this);
			}
		}

	};

	private PopupMenu mPopupMenu;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mResolver = getContentResolver();
		mAdapter = new CustomTabsAdapter(getActivity());
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		switch (requestCode) {
			case REQUEST_ADD_TAB: {
				if (resultCode == Activity.RESULT_OK) {
					final Bundle extras = intent.getExtras();
					if (extras == null) {
						break;
					}
					final ContentValues values = new ContentValues();
					values.put(Tabs.ARGUMENTS, extras.getString(INTENT_KEY_ARGUMENTS));
					values.put(Tabs.NAME, extras.getString(INTENT_KEY_NAME));
					values.put(Tabs.POSITION, mAdapter.getCount());
					values.put(Tabs.TYPE, extras.getString(INTENT_KEY_TYPE));
					values.put(Tabs.ICON, extras.getString(INTENT_KEY_ICON));
					mResolver.insert(Tabs.CONTENT_URI, values);
					getLoaderManager().restartLoader(0, null, this);
				}
				break;
			}
			case REQUEST_EDIT_TAB: {
				if (resultCode == Activity.RESULT_OK && mSelectedId != -1) {
					final Bundle extras = intent.getExtras();
					if (extras == null) {
						break;
					}
					final ContentValues values = new ContentValues();
					values.put(Tabs.ARGUMENTS, extras.getString(INTENT_KEY_ARGUMENTS));
					values.put(Tabs.NAME, extras.getString(INTENT_KEY_NAME));
					values.put(Tabs.TYPE, extras.getString(INTENT_KEY_TYPE));
					values.put(Tabs.ICON, extras.getString(INTENT_KEY_ICON));
					mResolver.update(Tabs.CONTENT_URI, values, Tabs._ID + " = " + mSelectedId, null);
					getLoaderManager().restartLoader(0, null, this);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(getActivity(), Tabs.CONTENT_URI, Tabs.COLUMNS, null, null, Tabs.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.menu_custom_tabs, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		if (mCursor == null || mCursor.isClosed()) return;
		final int _id_idx = mCursor.getColumnIndex(Tabs._ID);
		final int icon_idx = mCursor.getColumnIndex(Tabs.ICON);
		final int name_idx = mCursor.getColumnIndex(Tabs.NAME);
		final int type_idx = mCursor.getColumnIndex(Tabs.TYPE);
		final int arguments_idx = mCursor.getColumnIndex(Tabs.ARGUMENTS);
		mCursor.moveToPosition(position);
		mSelectedId = mCursor.getLong(_id_idx);
		final Intent intent = new Intent(INTENT_ACTION_EDIT_CUSTOM_TAB);
		final Bundle extras = new Bundle();
		extras.putString(INTENT_KEY_ICON, mCursor.getString(icon_idx));
		extras.putString(INTENT_KEY_NAME, mCursor.getString(name_idx));
		extras.putString(INTENT_KEY_TYPE, mCursor.getString(type_idx));
		extras.putString(INTENT_KEY_ARGUMENTS, mCursor.getString(arguments_idx));
		intent.setPackage(getActivity().getPackageName());
		intent.putExtras(extras);
		startActivityForResult(intent, REQUEST_EDIT_TAB);

	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mSelectedId = id;
		mSelectedPos = position;
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
		mPopupMenu.inflate(R.menu.action_custom_tab);
		final Menu menu = mPopupMenu.getMenu();
		final MenuItem upItem = menu.findItem(MENU_UP);
		if (upItem != null) {
			upItem.setVisible(position != 0);
		}
		final MenuItem downItem = menu.findItem(MENU_DOWN);
		if (downItem != null) {
			final int count = mAdapter.getCount();
			downItem.setVisible(count > 1 && position != count - 1);
		}
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
		mCursor = null;
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		mAdapter.changeCursor(cursor);
		mCursor = cursor;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mSelectedPos < 0 || mSelectedId < 0 || mCursor == null || mCursor.isClosed()) return false;
		final int _id_idx = mCursor.getColumnIndex(Tabs._ID);
		final int icon_idx = mCursor.getColumnIndex(Tabs.ICON);
		final int name_idx = mCursor.getColumnIndex(Tabs.NAME);
		final int type_idx = mCursor.getColumnIndex(Tabs.TYPE);
		final int arguments_idx = mCursor.getColumnIndex(Tabs.ARGUMENTS);
		switch (item.getItemId()) {
			case MENU_UP: {
				if (mSelectedPos > 0 && mSelectedPos < mAdapter.getCount()) {
					mCursor.moveToPosition(mSelectedPos);
					final long selected_id = mCursor.getInt(_id_idx);
					mCursor.moveToPrevious();
					final long _id = mCursor.getLong(_id_idx);
					final ContentValues values = new ContentValues();
					values.put(Tabs.POSITION, mSelectedPos - 1);
					mResolver.update(Tabs.CONTENT_URI, values, Tabs._ID + " = " + selected_id, null);
					values.put(Tabs.POSITION, mSelectedPos);
					mResolver.update(Tabs.CONTENT_URI, values, Tabs._ID + " = " + _id, null);
				}
				break;
			}
			case MENU_DOWN: {
				if (mSelectedPos >= 0 && mSelectedPos < mAdapter.getCount() - 1) {
					mCursor.moveToPosition(mSelectedPos);
					final long selected_id = mCursor.getInt(_id_idx);
					mCursor.moveToNext();
					final long _id = mCursor.getLong(_id_idx);
					final ContentValues values = new ContentValues();
					values.put(Tabs.POSITION, mSelectedPos + 1);
					mResolver.update(Tabs.CONTENT_URI, values, Tabs._ID + " = " + selected_id, null);
					values.put(Tabs.POSITION, mSelectedPos);
					mResolver.update(Tabs.CONTENT_URI, values, Tabs._ID + " = " + _id, null);
				}
				break;
			}
			case MENU_EDIT: {
				mCursor.moveToPosition(mSelectedPos);
				final Intent intent = new Intent(INTENT_ACTION_EDIT_CUSTOM_TAB);
				final Bundle extras = new Bundle();
				extras.putString(INTENT_KEY_ICON, mCursor.getString(icon_idx));
				extras.putString(INTENT_KEY_NAME, mCursor.getString(name_idx));
				extras.putString(INTENT_KEY_TYPE, mCursor.getString(type_idx));
				extras.putString(INTENT_KEY_ARGUMENTS, mCursor.getString(arguments_idx));
				intent.setPackage(getActivity().getPackageName());
				intent.putExtras(extras);
				startActivityForResult(intent, REQUEST_EDIT_TAB);
				break;
			}
			case MENU_DELETE: {
				mResolver.delete(Tabs.CONTENT_URI, Tabs._ID + " = " + mSelectedId, null);
				break;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD: {
				final Intent intent = new Intent(INTENT_ACTION_NEW_CUSTOM_TAB);
				final Bundle extras = new Bundle();
				extras.putInt(INTENT_KEY_POSITION, mAdapter.getCount());
				intent.setPackage(getActivity().getPackageName());
				intent.putExtras(extras);
				startActivityForResult(intent, REQUEST_ADD_TAB);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_TABS_UPDATED);
		registerReceiver(mStateReceiver, filter);
	}

	@Override
	public void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}

	public static class CustomTabsAdapter extends SimpleCursorAdapter {

		private int mTypeNameIdx = -1, mIconIdx = -1;

		public CustomTabsAdapter(final Context context) {
			super(context, R.layout.two_line_with_icon_list_item, null, new String[] { Tabs.NAME },
					new int[] { android.R.id.text1 }, 0);
		}

		@Override
		public void bindView(final View view, final Context context, final Cursor cursor) {
			super.bindView(view, context, cursor);
			final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
			final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
			if (mTypeNameIdx != -1) {
				text2.setText(getTabTypeName(context, cursor.getString(mTypeNameIdx)));
			}
			if (mIconIdx != -1) {
				icon.setBackgroundResource(R.drawable.gallery_selected_default);
				icon.setImageDrawable(getTabIconDrawable(mContext, getTabIconObject(cursor.getString(mIconIdx))));
			}
		}

		@Override
		public void changeCursor(final Cursor cursor) {
			super.changeCursor(cursor);
			if (cursor != null) {
				mTypeNameIdx = cursor.getColumnIndex(Tabs.TYPE);
				mIconIdx = cursor.getColumnIndex(Tabs.ICON);
			}
		}

	}
}
