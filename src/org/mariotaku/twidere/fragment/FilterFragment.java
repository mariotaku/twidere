package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.widget.TabsAdapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.TabPageIndicator;

public class FilterFragment extends BaseFragment {

	private ViewPager mViewPager;
	private TabsAdapter mAdapter;
	private TabPageIndicator mIndicator;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View view = getView();
		mViewPager = (ViewPager) view.findViewById(R.id.pager);
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		mAdapter = new TabsAdapter(getSherlockActivity(), getFragmentManager());
		mAdapter.addTab(FilteredUsersFragment.class, getString(R.string.users), null);
		mAdapter.addTab(FilteredKeywordsFragment.class, getString(R.string.keywords), null);
		mAdapter.addTab(FilteredSourcesFragment.class, getString(R.string.sources), null);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filters_list, container, false);
	}

	public static abstract class BaseFilterListFragment extends BaseListFragment implements
			LoaderCallbacks<Cursor> {

		public abstract Uri getContentUri();

		public abstract String[] getContentColumns();

		private FilterListAdapter mAdapter;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			mAdapter = new FilterListAdapter(getSherlockActivity());
			setListAdapter(mAdapter);
			getLoaderManager().initLoader(0, null, this);
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			String[] cols = getContentColumns();
			Uri uri = getContentUri();
			return new CursorLoader(getSherlockActivity(), uri, cols, null, null, null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			mAdapter.changeCursor(data);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			mAdapter.changeCursor(null);
		}

		public static class FilterListAdapter extends SimpleCursorAdapter {

			private static final String[] from = new String[] { Filters.TEXT };
			private static final int[] to = new int[] { android.R.id.text1 };

			public FilterListAdapter(Context context) {
				super(context, android.R.layout.simple_list_item_1, null, from, to, 0);
			}

		}
	}

	public static class FilteredKeywordsFragment extends BaseFilterListFragment {

		@Override
		public Uri getContentUri() {
			return Filters.Keywords.CONTENT_URI;
		}

		@Override
		public String[] getContentColumns() {
			return Filters.Keywords.COLUMNS;
		}

	}

	public static class FilteredSourcesFragment extends BaseFilterListFragment {

		@Override
		public Uri getContentUri() {
			return Filters.Sources.CONTENT_URI;
		}

		@Override
		public String[] getContentColumns() {
			return Filters.Sources.COLUMNS;
		}

	}

	public static class FilteredUsersFragment extends BaseFilterListFragment {

		@Override
		public Uri getContentUri() {
			return Filters.Users.CONTENT_URI;
		}

		@Override
		public String[] getContentColumns() {
			return Filters.Users.COLUMNS;
		}

	}

}
