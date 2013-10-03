package org.mariotaku.twidere.fragment;

import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.SquareImageView;

public class SearchFragment extends BaseSupportFragment implements Panes.Left, OnPageChangeListener,
		RefreshScrollTopInterface, SupportFragmentCallback {

	private ExtendedViewPager mViewPager;
	private LinearLayout mIndicator;

	private SupportTabsAdapter mAdapter;

	private int mThemeColor;
	private Fragment mCurrentVisibleFragment;

	@Override
	public Fragment getCurrentVisibleFragment() {
		return mCurrentVisibleFragment;
	}

	public void hideIndicator() {
		if (mIndicator.getVisibility() == View.GONE) return;
		mIndicator.setVisibility(View.GONE);
		mIndicator.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		mThemeColor = ThemeUtils.getThemeColor(getActivity());
		mAdapter = new SupportTabsAdapter(getActivity(), getChildFragmentManager(), null);
		mAdapter.addTab(SearchStatusesFragment.class, args, getString(R.string.statuses), R.drawable.ic_tab_twitter, 0);
		mAdapter.addTab(SearchUsersFragment.class, args, getString(R.string.users), R.drawable.ic_tab_person, 1);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
		mViewPager.setOffscreenPageLimit(2);
		final int current = mViewPager.getCurrentItem();
		for (int i = 0, count = mAdapter.getCount(); i < count; i++) {
			final ImageView v = new SquareImageView(getActivity());
			v.setScaleType(ScaleType.CENTER_INSIDE);
			final LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			lp.weight = 0;
			mIndicator.addView(v, lp);
			v.setImageDrawable(mAdapter.getPageIcon(i));
			if (i == current) {
				v.setColorFilter(mThemeColor, Mode.MULTIPLY);
			} else {
				v.clearColorFilter();
			}
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.search, container, false);
		mViewPager = (ExtendedViewPager) view.findViewById(R.id.search_pager);
		mIndicator = (LinearLayout) view.findViewById(R.id.search_pager_indicator);
		return view;
	}

	@Override
	public void onDetachFragment(final Fragment fragment) {

	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
	}

	@Override
	public void onPageScrollStateChanged(final int state) {
		if (state == ViewPager.SCROLL_STATE_DRAGGING) {
			showIndicator();
		}
	}

	@Override
	public void onPageSelected(final int position) {
		final int count = mAdapter.getCount();
		if (count != mIndicator.getChildCount()) return;
		for (int i = 0; i < count; i++) {
			final ImageView v = (ImageView) mIndicator.getChildAt(i);
			if (i == position) {
				v.setColorFilter(mThemeColor, Mode.SRC_ATOP);
			} else {
				v.clearColorFilter();
			}
		}
	}

	@Override
	public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
		if (isVisibleToUser) {
			mCurrentVisibleFragment = fragment;
		}
	}

	@Override
	public boolean scrollToStart() {
		if (!(mCurrentVisibleFragment instanceof RefreshScrollTopInterface)) return false;
		((RefreshScrollTopInterface) mCurrentVisibleFragment).scrollToStart();
		return true;
	}

	public void showIndicator() {
		if (mIndicator.getVisibility() == View.VISIBLE) return;
		mIndicator.setVisibility(View.VISIBLE);
		mIndicator.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
	}

	@Override
	public boolean triggerRefresh() {
		if (!(mCurrentVisibleFragment instanceof RefreshScrollTopInterface)) return false;
		((RefreshScrollTopInterface) mCurrentVisibleFragment).triggerRefresh();
		return true;
	}

	@Override
	public boolean triggerRefresh(final int position) {
		// TODO Auto-generated method stub
		return false;
	}

}
