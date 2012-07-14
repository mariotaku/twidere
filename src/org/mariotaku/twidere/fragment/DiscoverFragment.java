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
import static org.mariotaku.twidere.util.Utils.getDefaultTwitterInstance;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.MemCache;

import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class DiscoverFragment extends BaseFragment implements OnClickListener, OnItemSelectedListener {

	private long mAccountId;
	private Twitter mTwitter;
	private ListView mTrendsListView;
	private ProgressBar mTrendsLoadingProgress;
	private TrendsAdapter mTrendsAdapter;
	private LayoutInflater mInflater;
	private Spinner mTrendsSpinner;
	private ArrayAdapter<TrendsCategory> mTrendsCategoriesAdapter;
	private ImageButton mTrendsRefreshButton;
	private View mContentView;
	private MemCache mMemCache;

	private static final int TRENDS_TYPE_DAILY = 1;
	private static final int TRENDS_TYPE_WEEKLY = 2;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				final long account_id = getDefaultAccountId(context);
				if (mAccountId != account_id) {
					mTwitter = getDefaultTwitterInstance(context, false);
					if (mTwitter == null) {
						mContentView.setVisibility(View.GONE);
						return;
					}
					if (mTrendsCategoriesAdapter != null && mTrendsCategoriesAdapter != null) {
						final TrendsCategory tc = mTrendsCategoriesAdapter.getItem(mTrendsSpinner
								.getSelectedItemPosition());
						if (tc != null) {
							fetchTrends(tc.type);
						}
					}
				}
			}
		}
	};

	private OnItemClickListener mOnTrendsClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			openTweetSearch(getActivity(), mAccountId, mTrendsAdapter.getItem(position).getQuery());
		}

	};

	private AsyncTask<Void, Void, ?> mFetchTrendsTask;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mInflater = getLayoutInflater(savedInstanceState);
		mAccountId = getDefaultAccountId(getActivity());
		mTwitter = getDefaultTwitterInstance(getActivity(), false);
		mMemCache = getApplication().getMemCache();
		if (mTwitter == null) {
			mContentView.setVisibility(View.GONE);
			return;
		}
		mTrendsAdapter = new TrendsAdapter();
		mTrendsListView.setAdapter(mTrendsAdapter);
		mTrendsListView.setOnItemClickListener(mOnTrendsClickListener);
		mTrendsCategoriesAdapter = new ArrayAdapter<TrendsCategory>(getActivity(), R.layout.spinner_item_white_text);
		mTrendsCategoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mTrendsCategoriesAdapter.add(new TrendsCategory(TRENDS_TYPE_DAILY, getString(R.string.daily_trends)));
		mTrendsCategoriesAdapter.add(new TrendsCategory(TRENDS_TYPE_WEEKLY, getString(R.string.weekly_trends)));
		mTrendsSpinner.setAdapter(mTrendsCategoriesAdapter);
		mTrendsSpinner.setOnItemSelectedListener(this);
		mTrendsRefreshButton.setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.trends_refresh: {
				if (mTrendsCategoriesAdapter != null && mTrendsCategoriesAdapter != null) {
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.discover, null, false);
		mContentView = view;
		mTrendsListView = (ListView) view.findViewById(R.id.trends_list);
		mTrendsLoadingProgress = (ProgressBar) view.findViewById(R.id.trends_progress);
		mTrendsSpinner = (Spinner) view.findViewById(R.id.trends_spinner);
		mTrendsRefreshButton = (ImageButton) view.findViewById(R.id.trends_refresh);
		return view;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		fetchTrends(mTrendsCategoriesAdapter.getItem(position).type);
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	private void fetchTrends(int type) {
		if (mFetchTrendsTask != null) {
			mFetchTrendsTask.cancel(true);
		}
		mFetchTrendsTask = new FetchTrendsTask(type);
		mFetchTrendsTask.execute();
	}

	private class FetchTrendsTask extends AsyncTask<Void, Void, List<Trends>> {

		private final int type;

		FetchTrendsTask(int type) {
			this.type = type;
		}

		@Override
		protected List<Trends> doInBackground(Void... args) {
			List<Trends> result = null;
			try {
				switch (type) {
					case TRENDS_TYPE_DAILY: {
						result = mMemCache.getCachedDailyTrends();
						if (result == null) {
							result = mTwitter.getDailyTrends();
							mMemCache.cacheDailyTrends(result);
						}
						break;
					}
					case TRENDS_TYPE_WEEKLY: {
						result = mMemCache.getCachedWeeklyTrends();
						if (result == null) {
							result = mTwitter.getWeeklyTrends();
							mMemCache.cacheWeeklyTrends(result);
						}
						break;
					}
				}
			} catch (final TwitterException e) {
				e.printStackTrace();
			}
			return result;
		}

		@Override
		protected void onPostExecute(List<Trends> result) {
			mTrendsAdapter.setData(result);
			mTrendsLoadingProgress.setVisibility(View.GONE);
			mTrendsListView.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			mTrendsLoadingProgress.setVisibility(View.VISIBLE);
			mTrendsListView.setVisibility(View.GONE);
			super.onPreExecute();
		}

	}

	private class TrendsAdapter extends BaseAdapter {

		private List<Trend> mData = new ArrayList<Trend>();

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Trend getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(
					android.R.layout.simple_list_item_1, parent, false);
			((TextView) view).setText(getItem(position).getName());
			return view;
		}

		public void setData(List<Trends> data) {
			mData.clear();
			Trends trends = null;
			if (data != null && data.size() > 0) {
				for (final Trends item : data) {
					if (trends == null || item.getTrendAt().compareTo(trends.getTrendAt()) > 0) {
						trends = item;
					}
				}
			}
			if (trends != null) {
				mData.addAll(Arrays.asList(trends.getTrends()));
			}
			notifyDataSetChanged();
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
