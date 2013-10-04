package org.mariotaku.twidere.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.BaseFragment;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.LinePageIndicator;

public class SettingsWizardActivity extends Activity {

	private ViewPager mViewPager;

	private TabsAdapter mAdapter;
	private LinePageIndicator mIndicator;

	@Override
	public void onBackPressed() {
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mIndicator = (LinePageIndicator) findViewById(R.id.indicator);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setActionBarBackground();
		setContentView(R.layout.settings_wizard);
		// getActionBar().hide();
		mAdapter = new TabsAdapter(this, getFragmentManager(), null);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
		initPages();
	}

	private void initPages() {
		mAdapter.addTab(WizardPage1Fragment.class, null, getString(R.string.wizard_page_1_title), null, 0);
		mAdapter.addTab(WizardPage2Fragment.class, null, getString(R.string.wizard_page_2_title), null, 0);
	}

	private final void setActionBarBackground() {
		final ActionBar ab = getActionBar();
		if (ab == null) return;
		ab.setBackgroundDrawable(ThemeUtils.getActionBarBackground(this));
	}

	public static class WizardPage1Fragment extends BaseFragment {

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View view = inflater.inflate(R.layout.wizard_page_1, container, false);
			return view;
		}
	}

	public static class WizardPage2Fragment extends BaseFragment {

		private View mThemePreviewContentView;

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			ThemeUtils.setPreviewView(getActivity(), mThemePreviewContentView, R.style.Theme_Twidere);
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View view = inflater.inflate(R.layout.wizard_page_2, container, false);
			mThemePreviewContentView = view.findViewById(R.id.theme_preview_content);
			return view;
		}
	}
}
