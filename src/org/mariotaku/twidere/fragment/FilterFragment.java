package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.widget.TabsAdapter;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
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

	public static class FilteredKeywordsFragment extends BaseListFragment {

	}

	public static class FilteredSourcesFragment extends BaseListFragment {

	}

	public static class FilteredUsersFragment extends BaseListFragment {

	}

}
