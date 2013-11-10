package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.CompareUtils.classEquals;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.BaseFragment;
import org.mariotaku.twidere.fragment.BasePreferenceFragment;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.support.MentionsFragment;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.provider.TweetStore.Tabs;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.CustomTabUtils;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.view.LinePageIndicator;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SettingsWizardActivity extends Activity implements Constants {

	public static final CharSequence WIZARD_PREFERENCE_KEY_NEXT_PAGE = "next_page";
	public static final CharSequence WIZARD_PREFERENCE_KEY_USE_DEFAULTS = "use_defaults";
	public static final CharSequence WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS = "edit_custom_tabs";
	private ViewPager mViewPager;
	private LinePageIndicator mIndicator;

	private TabsAdapter mAdapter;
	private AbsInitialSettingsTask mTask;

	public void applyInitialSettings() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
		mTask = new InitialSettingsTask(this);
		mTask.execute();
	}

	public void applyInitialTabSettings() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) return;
		mTask = new InitialTabSettingsTask(this);
		mTask.execute();
	}

	public void exitWizard() {
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		prefs.edit().putBoolean(PREFERENCE_KEY_SETTINGS_WIZARD_COMPLETED, true).apply();
		startActivity(new Intent(this, HomeActivity.class));
		finish();
	}

	public void gotoLastPage() {
		if (mViewPager == null || mAdapter == null) return;
		final int last = mAdapter.getCount() - 1;
		mViewPager.setCurrentItem(Math.max(last, 0));
	}

	public void gotoNextPage() {
		if (mViewPager == null || mAdapter == null) return;
		final int current = mViewPager.getCurrentItem();
		mViewPager.setCurrentItem(MathUtils.clamp(current + 1, mAdapter.getCount() - 1, 0));
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
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
		setContentView(R.layout.settings_wizard);
		mAdapter = new TabsAdapter(this, getFragmentManager(), null);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setEnabled(false);
		mIndicator.setViewPager(mViewPager);
		initPages();
	}

	private void initPages() {
		mAdapter.addTab(WizardPageWelcomeFragment.class, null, getString(R.string.wizard_page_welcome_title), null, 0);
		mAdapter.addTab(WizardPageThemeFragment.class, null, getString(R.string.theme), null, 0);
		mAdapter.addTab(WizardPageTabsFragment.class, null, getString(R.string.tabs), null, 0);
		mAdapter.addTab(WizardPageStatusFragment.class, null, getString(R.string.status), null, 0);
		mAdapter.addTab(WizardPageFinishedFragment.class, null, getString(R.string.wizard_page_finished_title), null, 0);
	}

	public static class BaseWizardPageFragment extends BasePreferenceFragment {

		public void gotoLastPage() {
			final Activity a = getActivity();
			if (a instanceof SettingsWizardActivity) {
				((SettingsWizardActivity) a).gotoLastPage();
			}
		}

		public void gotoNextPage() {
			final Activity a = getActivity();
			if (a instanceof SettingsWizardActivity) {
				((SettingsWizardActivity) a).gotoNextPage();
			}
		}
	}

	public static class WizardPageFinishedFragment extends BaseFragment implements OnClickListener {

		@Override
		public void onClick(final View v) {
			final Activity a = getActivity();
			if (a instanceof SettingsWizardActivity) {
				((SettingsWizardActivity) a).exitWizard();
			}
		}

		@Override
		public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
				final Bundle savedInstanceState) {
			final View view = inflater.inflate(R.layout.wizard_page_finished, container, false);
			view.findViewById(R.id.exit_wizard).setOnClickListener(this);
			return view;
		}

	}

	public static class WizardPageStatusFragment extends BaseWizardPageFragment implements OnPreferenceClickListener {

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			addPreferencesFromResource(R.xml.settings_wizard_page_status);
			findPreference(WIZARD_PREFERENCE_KEY_NEXT_PAGE).setOnPreferenceClickListener(this);
		}

		@Override
		public boolean onPreferenceClick(final Preference preference) {
			if (WIZARD_PREFERENCE_KEY_NEXT_PAGE.equals(preference.getKey())) {
				gotoNextPage();
			}
			return true;
		}
	}

	public static class WizardPageTabsFragment extends BaseWizardPageFragment implements OnPreferenceClickListener {

		private static final int REQUEST_CUSTOM_TABS = 1;

		public void applyInitialTabSettings() {
			final Activity a = getActivity();
			if (a instanceof SettingsWizardActivity) {
				((SettingsWizardActivity) a).applyInitialTabSettings();
			}
		}

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			addPreferencesFromResource(R.xml.settings_wizard_page_tab);
			findPreference(WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS).setOnPreferenceClickListener(this);
			findPreference(WIZARD_PREFERENCE_KEY_USE_DEFAULTS).setOnPreferenceClickListener(this);
		}

		@Override
		public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
			switch (requestCode) {
				case REQUEST_CUSTOM_TABS:
					if (resultCode != RESULT_OK) {
						Toast.makeText(getActivity(), R.string.wizard_page_tabs_unchanged_message, Toast.LENGTH_SHORT)
								.show();
					}
					gotoNextPage();
					break;
			}
			super.onActivityResult(requestCode, resultCode, data);
		}

		@Override
		public boolean onPreferenceClick(final Preference preference) {
			final String key = preference.getKey();
			if (WIZARD_PREFERENCE_KEY_EDIT_CUSTOM_TABS.equals(key)) {
				startActivityForResult(new Intent(getActivity(), CustomTabsActivity.class), REQUEST_CUSTOM_TABS);
			} else if (WIZARD_PREFERENCE_KEY_USE_DEFAULTS.equals(key)) {
				applyInitialTabSettings();
			}
			return true;
		}
	}

	public static class WizardPageThemeFragment extends BaseWizardPageFragment implements OnPreferenceClickListener {

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			addPreferencesFromResource(R.xml.settings_wizard_page_theme);
			findPreference(WIZARD_PREFERENCE_KEY_NEXT_PAGE).setOnPreferenceClickListener(this);
		}

		@Override
		public boolean onPreferenceClick(final Preference preference) {
			if (WIZARD_PREFERENCE_KEY_NEXT_PAGE.equals(preference.getKey())) {
				gotoNextPage();
			}
			return true;
		}
	}

	public static class WizardPageWelcomeFragment extends BaseWizardPageFragment implements OnPreferenceClickListener {

		public void applyInitialSettings() {
			final Activity a = getActivity();
			if (a instanceof SettingsWizardActivity) {
				((SettingsWizardActivity) a).applyInitialSettings();
			}
		}

		@Override
		public void onActivityCreated(final Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);
			addPreferencesFromResource(R.xml.settings_wizard_page_welcome);
			findPreference(WIZARD_PREFERENCE_KEY_NEXT_PAGE).setOnPreferenceClickListener(this);
			findPreference(WIZARD_PREFERENCE_KEY_USE_DEFAULTS).setOnPreferenceClickListener(this);
		}

		@Override
		public boolean onPreferenceClick(final Preference preference) {
			if (WIZARD_PREFERENCE_KEY_NEXT_PAGE.equals(preference.getKey())) {
				gotoNextPage();
			} else if (WIZARD_PREFERENCE_KEY_USE_DEFAULTS.equals(preference.getKey())) {
				applyInitialSettings();
			}
			return true;
		}
	}

	static abstract class AbsInitialSettingsTask extends AsyncTask<Void, Void, Void> {

		private static final String FRAGMENT_TAG = "initial_settings_dialog";

		private static final String[] DEFAULT_TAB_TYPES = { TAB_TYPE_HOME_TIMELINE, TAB_TYPE_MENTIONS_TIMELINE,
				TAB_TYPE_TRENDS_SUGGESTIONS, TAB_TYPE_DIRECT_MESSAGES };

		private final SettingsWizardActivity mActivity;

		AbsInitialSettingsTask(final SettingsWizardActivity activity) {
			mActivity = activity;
		}

		@Override
		protected Void doInBackground(final Void... params) {
			final ContentResolver resolver = mActivity.getContentResolver();
			final List<SupportTabSpec> tabs = CustomTabUtils.getHomeTabs(mActivity);
			if (wasConfigured(tabs)) return null;
			Collections.sort(tabs);
			int i = 0;
			final List<ContentValues> values_list = new ArrayList<ContentValues>();
			for (final String type : DEFAULT_TAB_TYPES) {
				final ContentValues values = new ContentValues();
				final CustomTabConfiguration conf = CustomTabUtils.getTabConfiguration(type);
				values.put(Tabs.TYPE, type);
				values.put(Tabs.NAME, mActivity.getString(conf.getDefaultTitle()));
				values.put(Tabs.ICON, CustomTabUtils.findTabIconKey(conf.getDefaultIcon()));
				values.put(Tabs.POSITION, i++);
				values_list.add(values);
			}
			for (final SupportTabSpec spec : tabs) {
				final String type = CustomTabUtils.findTabType(spec.cls);
				if (type != null) {
					final ContentValues values = new ContentValues();
					values.put(Tabs.TYPE, type);
					values.put(Tabs.ARGUMENTS, ParseUtils.bundleToJSON(spec.args));
					values.put(Tabs.NAME, spec.name);
					if (spec.icon instanceof Integer) {
						values.put(Tabs.ICON, CustomTabUtils.findTabIconKey((Integer) spec.icon));
					} else if (spec.icon instanceof File) {
						values.put(Tabs.ICON, ((File) spec.icon).getPath());
					}
					values.put(Tabs.POSITION, i++);
				}
			}
			resolver.delete(Tabs.CONTENT_URI, null, null);
			resolver.bulkInsert(Tabs.CONTENT_URI, values_list.toArray(new ContentValues[values_list.size()]));
			return null;
		}

		protected SettingsWizardActivity getActivity() {
			return mActivity;
		}

		protected abstract void nextStep();

		@Override
		protected void onPostExecute(final Void result) {
			final FragmentManager fm = mActivity.getFragmentManager();
			final DialogFragment f = (DialogFragment) fm.findFragmentByTag(FRAGMENT_TAG);
			if (f != null) {
				f.dismiss();
			}
			nextStep();
		}

		@Override
		protected void onPreExecute() {
			ProgressDialogFragment.show(mActivity, FRAGMENT_TAG).setCancelable(false);
		}

		private boolean wasConfigured(final List<SupportTabSpec> tabs) {
			for (final SupportTabSpec spec : tabs) {
				if (classEquals(spec.cls, HomeTimelineFragment.class) || classEquals(spec.cls, MentionsFragment.class)
						|| classEquals(spec.cls, DirectMessagesFragment.class)) return true;
			}
			return false;
		}

	}

	static class InitialSettingsTask extends AbsInitialSettingsTask {

		InitialSettingsTask(final SettingsWizardActivity activity) {
			super(activity);
		}

		@Override
		protected void nextStep() {
			getActivity().gotoLastPage();
		}

	}

	static class InitialTabSettingsTask extends AbsInitialSettingsTask {

		InitialTabSettingsTask(final SettingsWizardActivity activity) {
			super(activity);
		}

		@Override
		protected void nextStep() {
			getActivity().gotoNextPage();
		}

	}

}
