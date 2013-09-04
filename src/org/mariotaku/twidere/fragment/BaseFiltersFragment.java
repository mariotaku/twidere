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

import static org.mariotaku.twidere.util.Utils.showInfoMessage;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Filters;

import android.app.LoaderManager.LoaderCallbacks;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public abstract class BaseFiltersFragment extends BaseListFragment implements LoaderCallbacks<Cursor>,
		OnItemLongClickListener {

	private FilterListAdapter mAdapter;

	private ContentResolver mResolver;

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
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
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = super.onCreateView(inflater, container, savedInstanceState);
		final View lv = view.findViewById(android.R.id.list);
		final Resources res = getResources();
		final float density = res.getDisplayMetrics().density;
		final int padding = (int) density * 16;
		lv.setId(android.R.id.list);
		lv.setPadding(padding, 0, padding, 0);
		return view;
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
		showInfoMessage(getActivity(), R.string.longclick_to_delete, false);
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

	protected abstract String[] getContentColumns();

	protected abstract Uri getContentUri();

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

	public static final class FilteredLinksFragment extends BaseFiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Links.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Links.CONTENT_URI;
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

		private static final String[] from = new String[] { Filters.VALUE };

		private static final int[] to = new int[] { android.R.id.text1 };

		public FilterListAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_1, null, from, to, 0);
		}

	}
}
