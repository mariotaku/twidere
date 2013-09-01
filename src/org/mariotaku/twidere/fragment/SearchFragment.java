package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getThemeColor;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.SquareImageView;

import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
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

public class SearchFragment extends BaseFragment implements Panes.Left, OnPageChangeListener {

	private ExtendedViewPager mViewPager;
	private LinearLayout mIndicator;

	private TabsAdapter mAdapter;

	private int mThemeColor;

	public void hideIndicator() {
		if (mIndicator.getVisibility() == View.GONE) return;
		mIndicator.setVisibility(View.GONE);
		mIndicator.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		mThemeColor = getThemeColor(getActivity());
		mAdapter = new TabsAdapter(getActivity(), getChildFragmentManager(), null);
		mAdapter.addTab(SearchTweetsFragment.class, args, getString(R.string.tweets), R.drawable.ic_tab_twitter, 0);
		mAdapter.addTab(SearchUsersFragment.class, args, getString(R.string.users), R.drawable.ic_tab_person, 1);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOnPageChangeListener(this);
		final int current = mViewPager.getCurrentItem();
		final int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
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
				v.setColorFilter(mThemeColor, Mode.MULTIPLY);
			} else {
				v.clearColorFilter();
			}
		}
	}

	public void showIndicator() {
		if (mIndicator.getVisibility() == View.VISIBLE) return;
		mIndicator.setVisibility(View.VISIBLE);
		mIndicator.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
	}

}
