package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.CustomTabUtils.getHomeTabs;

import android.app.FragmentManager;
import android.os.Bundle;

import org.mariotaku.twidere.fragment.CustomTabsFragment;
import org.mariotaku.twidere.model.SupportTabSpec;

import java.util.ArrayList;
import java.util.List;

public class CustomTabsActivity extends BaseActivity {

	private final List<SupportTabSpec> mCustomTabs = new ArrayList<SupportTabSpec>();

	@Override
	public void onBackPressed() {
		final List<SupportTabSpec> tabs = getHomeTabs(this);
		setResult(isTabsChanged(tabs) && !tabs.isEmpty() ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final FragmentManager fm = getFragmentManager();
		fm.beginTransaction().replace(android.R.id.content, new CustomTabsFragment()).commit();
		initTabs();
	}

	private void initTabs() {
		mCustomTabs.clear();
		mCustomTabs.addAll(getHomeTabs(this));
	}

	private boolean isTabsChanged(final List<SupportTabSpec> tabs) {
		if (mCustomTabs.size() != tabs.size()) return true;
		for (int i = 0, size = mCustomTabs.size(); i < size; i++) {
			if (!mCustomTabs.get(i).equals(tabs.get(i))) return true;
		}
		return false;
	}
}
