package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getDisplayName;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.Account;

public abstract class BaseAccountPreferenceFragment extends PreferenceFragment implements Constants,
		OnCheckedChangeListener, OnSharedPreferenceChangeListener {

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		final PreferenceManager pm = getPreferenceManager();
		final Account account = getArguments().getParcelable(EXTRA_ACCOUNT);
		final String pName = ACCOUNT_PREFERENCES_NAME_PREFIX + (account != null ? account.account_id : "unknown");
		pm.setSharedPreferencesName(pName);
		addPreferencesFromResource(getPreferencesResource());
		final SharedPreferences prefs = pm.getSharedPreferences();
		prefs.registerOnSharedPreferenceChangeListener(this);
		final String name = getDisplayName(getActivity(), account.account_id, account.name, account.screen_name);
		final Activity activity = getActivity();
		final Intent intent = activity.getIntent();
		if (account != null && intent.hasExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT)) {
			activity.setTitle(name);
		}
		updatePreferenceScreen();
	}

	@Override
	public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		final SharedPreferences.Editor editor = prefs.edit();
		if (prefs.getBoolean(getSwitchPreferenceKey(), getSwitchPreferenceDefault()) != isChecked) {
			editor.putBoolean(getSwitchPreferenceKey(), isChecked);
			editor.apply();
		}
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.menu_switch_preference, menu);
		final View actionView = menu.findItem(MENU_TOGGLE).getActionView();
		final Switch toggle = (Switch) actionView.findViewById(android.R.id.toggle);
		final SharedPreferences prefs = getPreferenceManager().getSharedPreferences();
		toggle.setOnCheckedChangeListener(this);
		toggle.setChecked(prefs.getBoolean(getSwitchPreferenceKey(), getSwitchPreferenceDefault()));
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (key.equals(getSwitchPreferenceKey())) {
			updatePreferenceScreen();
		}
	}

	protected long getAccountId() {
		final Account account = getArguments().getParcelable(EXTRA_ACCOUNT);
		return account != null ? account.account_id : -1;
	}

	protected abstract int getPreferencesResource();

	protected abstract boolean getSwitchPreferenceDefault();

	protected abstract String getSwitchPreferenceKey();

	private void updatePreferenceScreen() {
		final PreferenceScreen screen = getPreferenceScreen();
		final SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
		if (screen == null || sharedPreferences == null) return;
		screen.setEnabled(sharedPreferences.getBoolean(getSwitchPreferenceKey(), getSwitchPreferenceDefault()));
	}
}
