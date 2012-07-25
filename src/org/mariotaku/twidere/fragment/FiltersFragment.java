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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
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

public abstract class FiltersFragment extends BaseListFragment implements LoaderCallbacks<Cursor>,
		OnItemLongClickListener {

	private FilterListAdapter mAdapter;

	private AddItemFragment mFragment = new AddItemFragment();

	private ContentResolver mResolver;

	public abstract String[] getContentColumns();

	public abstract Uri getContentUri();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mResolver = getContentResolver();
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mAdapter = new FilterListAdapter(getActivity());
		setListAdapter(mAdapter);
		getListView().setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String[] cols = getContentColumns();
		final Uri uri = getContentUri();
		return new CursorLoader(getActivity(), uri, cols, null, null, null);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		final String where = Filters._ID + "=" + id;
		mResolver.delete(getContentUri(), where, null);
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Toast.makeText(getActivity(), R.string.longclick_to_delete, Toast.LENGTH_SHORT).show();
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD:
				mFragment.setFiltersFragment(this);
				mFragment.show(getFragmentManager(), "add_rule");
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class AddItemFragment extends BaseDialogFragment implements OnClickListener {

		private AutoCompleteTextView mEditText;

		private FiltersFragment mFragment;

		private UserAutoCompleteAdapter mUserAutoCompleteAdapter;

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (mFragment == null) return;
					final ContentValues values = new ContentValues();
					if (mEditText.length() <= 0) return;
					final String text = mEditText.getText().toString();
					values.put(Filters.TEXT, text);
					getContentResolver().insert(mFragment.getContentUri(), values);
					mFragment.getLoaderManager().restartLoader(0, null, mFragment);
					break;
			}

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Context context = getActivity();
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			final View view = LayoutInflater.from(context).inflate(R.layout.auto_complete_textview_default_style, null);
			builder.setView(view);
			mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
			if (mFragment instanceof FilteredUsersFragment) {
				mUserAutoCompleteAdapter = new UserAutoCompleteAdapter(getActivity());
				mEditText.setAdapter(mUserAutoCompleteAdapter);
				mEditText.setThreshold(1);
			}
			builder.setTitle(R.string.add_rule);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		public void setFiltersFragment(FiltersFragment fragment) {
			mFragment = fragment;
		}
	}

	public static class FilteredKeywordsFragment extends FiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Keywords.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Keywords.CONTENT_URI;
		}

	}

	public static class FilteredSourcesFragment extends FiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Sources.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Sources.CONTENT_URI;
		}

	}

	public static class FilteredUsersFragment extends FiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Users.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Users.CONTENT_URI;
		}

	}

	public static class FilterListAdapter extends SimpleCursorAdapter {

		private static final String[] from = new String[] { Filters.TEXT };

		private static final int[] to = new int[] { android.R.id.text1 };

		public FilterListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1, null, from, to, 0);
		}

	}
}
