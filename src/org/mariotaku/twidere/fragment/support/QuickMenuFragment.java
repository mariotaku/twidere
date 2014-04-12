package org.mariotaku.twidere.fragment.support;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.content.TwidereContextThemeWrapper;
import org.mariotaku.twidere.util.ThemeUtils;

public class QuickMenuFragment extends BaseSupportFragment {

	private SlidingUpPanelLayout mSlidingUpPanel;
	private SharedPreferences mPreferences;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (mPreferences.getBoolean(KEY_QUICK_MENU_EXPANDED, false)) {
		} else {
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final Context context = getActivity();
		final int themeResource = ThemeUtils.getDrawerThemeResource(context);
		final int accentColor = ThemeUtils.getUserThemeColor(context);
		final Context theme = new TwidereContextThemeWrapper(context, themeResource, accentColor);
		return LayoutInflater.from(theme).inflate(R.layout.fragment_quick_menu, container, false);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(KEY_QUICK_MENU_EXPANDED, mSlidingUpPanel.isExpanded());
		editor.apply();
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		mSlidingUpPanel = (SlidingUpPanelLayout) view.findViewById(R.id.activities_drawer);
	}

}
