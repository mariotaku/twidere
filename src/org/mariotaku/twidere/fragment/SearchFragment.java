package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getThemeColor;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.view.ExtendedViewPager;

import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SearchFragment extends BaseFragment {

	private ExtendedViewPager mViewPager;
	private PagerTabStrip mIndicator;

	private TabsAdapter mAdapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		mAdapter = new TabsAdapter(getActivity(), getChildFragmentManager(), null);
		mAdapter.addTab(SearchTweetsFragment.class, args != null ? args : savedInstanceState, getString(R.string.tweets), R.drawable.ic_tab_twitter, 0);
		mAdapter.addTab(SearchUsersFragment.class, args != null ? args : savedInstanceState, getString(R.string.users), R.drawable.ic_tab_person, 1);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setTabIndicatorColor(getThemeColor(getActivity()));
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.search, container, false);
		mViewPager = (ExtendedViewPager) view.findViewById(R.id.search_pager);
		mIndicator = (PagerTabStrip) view.findViewById(R.id.search_pager_tab);
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		final Bundle args = getArguments();
		if (args != null) {
			outState.putAll(args);
		}
	}

}
