/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.UserAutoCompleteAdapter;
import org.mariotaku.twidere.provider.TweetStore.Filters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.Toast;

public abstract class BaseFiltersFragment extends BaseListFragment implements LoaderCallbacks<Cursor>,
		OnItemLongClickListener {

	private static final String INTENT_KEY_AUTO_COMPLETE = "auto_complete";
			
	private FilterListAdapter mAdapter;

	private ContentResolver mResolver;

	protected abstract String[] getContentColumns();

	protected abstract Uri getContentUri();

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_FILTERS_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, BaseFiltersFragment.this);
			}
		}
	
	};
	
	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mResolver = getContentResolver();
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mAdapter = new FilterListAdapter(getActivity());
		setListAdapter(mAdapter);
		getListView().setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		final String[] cols = getContentColumns();
		final Uri uri = getContentUri();
		return new CursorLoader(getActivity(), uri, cols, null, null, null);
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final String where = Filters._ID + " = " + id;
		mResolver.delete(getContentUri(), where, null);
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onListItemClick(final ListView l, final View v, final int position, final long id) {
		Toast.makeText(getActivity(), R.string.longclick_to_delete, Toast.LENGTH_SHORT).show();
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD:
				final Bundle args = new Bundle();
				args.putBoolean(INTENT_KEY_AUTO_COMPLETE, this instanceof FilteredUsersFragment);
				args.putParcelable(INTENT_KEY_URI, getContentUri());
				final AddItemFragment fragment = new AddItemFragment();
				fragment.setArguments(args);
				fragment.show(getFragmentManager(), "add_rule");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_FILTERS_UPDATED);
		registerReceiver(mStateReceiver, filter);
	}
	
	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		super.onStop();
	}
	
	public static final class AddItemFragment extends BaseDialogFragment implements OnClickListener {

		private AutoCompleteTextView mEditText;

		private UserAutoCompleteAdapter mUserAutoCompleteAdapter;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (mEditText.length() <= 0) return;
					final ContentValues values = new ContentValues();
					final String text = mEditText.getText().toString();
					values.put(Filters.TEXT, text);
					final Bundle args = getArguments();
					final Uri uri = args.getParcelable(INTENT_KEY_URI);
					getContentResolver().insert(uri, values);
					break;
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final Context context = getActivity();
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			final View view = LayoutInflater.from(context).inflate(R.layout.auto_complete_textview_default_style, null);
			builder.setView(view);
			mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
			final Bundle args = getArguments();
			if (args != null && args.getBoolean(INTENT_KEY_AUTO_COMPLETE)) {
				mUserAutoCompleteAdapter = new UserAutoCompleteAdapter(getActivity());
				mEditText.setAdapter(mUserAutoCompleteAdapter);
				mEditText.setThreshold(1);
			}
			builder.setTitle(R.string.add_rule);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

	}

	public static final class FilteredKeywordsFragment extends BaseFiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Keywords.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Keywords.CONTENT_URI;
		}

	}

	public static final class FilteredSourcesFragment extends BaseFiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Sources.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Sources.CONTENT_URI;
		}

	}

	public static final class FilteredUsersFragment extends BaseFiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Users.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Users.CONTENT_URI;
		}

	}

	public static final class FilterListAdapter extends SimpleCursorAdapter {

		private static final String[] from = new String[] { Filters.TEXT };

		private static final int[] to = new int[] { android.R.id.text1 };

		public FilterListAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_1, null, from, to, 0);
		}

	}
}
