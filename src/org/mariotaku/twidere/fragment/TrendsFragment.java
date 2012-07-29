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

import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getTableNameForContentUri;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.util.ServiceInterface;

import android.content.BroadcastReceiver;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;

public class TrendsFragment extends BaseListFragment implements OnClickListener, OnItemSelectedListener,
		OnItemClickListener, LoaderCallbacks<Cursor>, Panes.Left {

	private long mAccountId;
	private ListView mListView;
	private TrendsAdapter mTrendsAdapter;
	private Spinner mTrendsSpinner;
	private ArrayAdapter<TrendsCategory> mTrendsCategoriesAdapter;
	private ImageButton mTrendsRefreshButton;
	private ServiceInterface mService;
	private SharedPreferences mPreferences;

	private static final int TRENDS_TYPE_DAILY = 1;
	private static final int TRENDS_TYPE_WEEKLY = 2;
	private static final int TRENDS_TYPE_LOCAL = 3;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TRENDS_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, TrendsFragment.this);
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mService = getApplication().getServiceInterface();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onActivityCreated(savedInstanceState);
		mAccountId = getDefaultAccountId(getActivity());
		mTrendsAdapter = new TrendsAdapter(getActivity());
		setListAdapter(mTrendsAdapter);
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		mTrendsCategoriesAdapter = new ArrayAdapter<TrendsCategory>(getActivity(), R.layout.spinner_item_white_text);
		mTrendsCategoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTrendsCategoriesAdapter.add(new TrendsCategory(TRENDS_TYPE_DAILY, getString(R.string.daily_trends)));
		mTrendsCategoriesAdapter.add(new TrendsCategory(TRENDS_TYPE_WEEKLY, getString(R.string.weekly_trends)));
		mTrendsCategoriesAdapter.add(new TrendsCategory(TRENDS_TYPE_LOCAL, getString(R.string.local_trends)));
		mTrendsSpinner.setAdapter(mTrendsCategoriesAdapter);
		getLoaderManager().initLoader(0, null, this);
		mTrendsSpinner.setOnItemSelectedListener(this);
		mTrendsRefreshButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.trends_refresh: {
				if (mTrendsCategoriesAdapter != null) {
					final TrendsCategory tc = mTrendsCategoriesAdapter
							.getItem(mTrendsSpinner.getSelectedItemPosition());
					if (tc != null) {
						fetchTrends(tc.type);
					}
				}
				break;
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = TweetStore.NULL_CONTENT_URI;
		if (mTrendsCategoriesAdapter != null && mTrendsSpinner != null) {
			final TrendsCategory tc = mTrendsCategoriesAdapter.getItem(mTrendsSpinner.getSelectedItemPosition());
			if (tc != null) {
				uri = getTrendsQueryUri(tc.type);
			}
		}
		final String table = getTableNameForContentUri(uri);
		final String where = table != null ? CachedTrends.TIMESTAMP + " = " + "(SELECT " + CachedTrends.TIMESTAMP
				+ " FROM " + table + " ORDER BY " + CachedTrends.TIMESTAMP + " DESC LIMIT 1)" : null;
		return new CursorLoader(getActivity(), uri, CachedTrends.COLUMNS, where, null, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.discover, null, false);
		mTrendsSpinner = (Spinner) view.findViewById(R.id.trends_spinner);
		mTrendsRefreshButton = (ImageButton) view.findViewById(R.id.trends_refresh);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Cursor cur = (Cursor) mTrendsAdapter.getItem(position);
		if (cur == null) return;
		openTweetSearch(getActivity(), mAccountId, cur.getString(cur.getColumnIndex(CachedTrends.NAME)));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (isAdded()) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mTrendsAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mTrendsAdapter.swapCursor(cursor);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_TRENDS_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	private void fetchTrends(int type) {
		switch (type) {
			case TRENDS_TYPE_DAILY: {
				mService.getDailyTrends(mAccountId);
				break;
			}
			case TRENDS_TYPE_WEEKLY: {
				mService.getWeeklyTrends(mAccountId);
				break;
			}
			case TRENDS_TYPE_LOCAL: {
				mService.getLocalTrends(mAccountId, mPreferences.getInt(PREFERENCE_KEY_LOCAL_TRENDS_WOEID, 1));
				break;
			}

		}
	}

	private Uri getTrendsQueryUri(int type) {
		switch (type) {
			case TRENDS_TYPE_DAILY: {
				return CachedTrends.Daily.CONTENT_URI;
			}
			case TRENDS_TYPE_WEEKLY: {
				return CachedTrends.Weekly.CONTENT_URI;
			}
			case TRENDS_TYPE_LOCAL: {
				return CachedTrends.Local.CONTENT_URI;
			}
		}
		return null;
	}

	private class TrendsAdapter extends SimpleCursorAdapter {

		public TrendsAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1, null, new String[] { CachedTrends.NAME },
					new int[] { android.R.id.text1 }, 0);
		}

	}

	private static class TrendsCategory {
		public final int type;
		public final String name;

		public TrendsCategory(int type, String name) {
			this.type = type;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
