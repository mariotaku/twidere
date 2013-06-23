/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.cleanDatabasesByItemLimit;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getTabs;
import static org.mariotaku.twidere.util.Utils.openDirectMessagesConversation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.APIUpgradeConfirmDialog;
import org.mariotaku.twidere.fragment.AccountsFragment;
import org.mariotaku.twidere.fragment.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.MentionsFragment;
import org.mariotaku.twidere.model.TabSpec;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ActivityAccessor;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.TabPageIndicator;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import edu.ucdavis.earlybird.ProfilingUtil;

public class HomeActivity extends MultiSelectActivity implements OnClickListener, OnPageChangeListener {

	private SharedPreferences mPreferences;
	private AsyncTwitterWrapper mTwitterWrapper;
	private NotificationManager mNotificationManager;

	private ActionBar mActionBar;
	private TabsAdapter mAdapter;

	private ExtendedViewPager mViewPager;
	private ImageButton mComposeButton;
	private TabPageIndicator mIndicator;
	private ProgressBar mProgress;

	private boolean mProgressBarIndeterminateVisible = false;

	private boolean mDisplayAppIcon;
	private boolean mShowHomeTab, mShowMentionsTab, mShowMessagesTab, mShowAccountsTab;

	public static final int TAB_POSITION_HOME = 0;

	public static final int TAB_POSITION_MENTIONS = 1;
	public static final int TAB_POSITION_MESSAGES = 2;
	private final ArrayList<TabSpec> mCustomTabs = new ArrayList<TabSpec>();

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				setSupportProgressBarIndeterminateVisibility(mProgressBarIndeterminateVisible);
			}
		}

	};

	@Override
	public void onBackStackChanged() {
		super.onBackStackChanged();
		if (!isDualPaneMode()) return;
		final FragmentManager fm = getSupportFragmentManager();
		final Fragment left_pane_fragment = fm.findFragmentById(PANE_LEFT);
		final boolean left_pane_used = left_pane_fragment != null && left_pane_fragment.isAdded();
		setPagingEnabled(!left_pane_used);
		final int count = fm.getBackStackEntryCount();
		if (count == 0) {
			showLeftPane();
		}
		invalidateSupportOptionsMenu();
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.compose:
			case R.id.button_compose:
				if (mViewPager == null || mAdapter == null) return;
				final int position = mViewPager.getCurrentItem();
				final TabSpec tab = mAdapter.getTab(position);
				if (tab == null) {
					startActivity(new Intent(INTENT_ACTION_COMPOSE));
				} else {
					switch (tab.position) {
						case TAB_POSITION_MESSAGES:
							openDirectMessagesConversation(this, -1, -1, null);
							break;
						default:
							startActivity(new Intent(INTENT_ACTION_COMPOSE));
					}
				}
				break;
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ExtendedViewPager) findViewById(R.id.main);
		mComposeButton = (ImageButton) findViewById(R.id.button_compose);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_home, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				final FragmentManager fm = getSupportFragmentManager();
				if (isDualPaneMode() && !FragmentManagerTrojan.isStateSaved(fm)) {
					final int count = fm.getBackStackEntryCount();
					for (int i = 0; i < count; i++) {
						fm.popBackStackImmediate();
					}
					setSupportProgressBarIndeterminateVisibility(false);
				}
				return true;
			}
			case MENU_COMPOSE: {
				if (mComposeButton != null) {
					onClick(mComposeButton);
				}
				return true;
			}
			case MENU_SEARCH: {
				onSearchRequested();
				return true;
			}
			case MENU_SELECT_ACCOUNT: {
				startActivityForResult(new Intent(INTENT_ACTION_SELECT_ACCOUNT), REQUEST_SELECT_ACCOUNT);
				return true;
			}
			case MENU_SETTINGS: {
				final Intent intent = new Intent(this, SettingsActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {

	}

	@Override
	public void onPageScrollStateChanged(final int state) {

	}

	@Override
	public void onPageSelected(final int position) {
		final TabSpec tab = mAdapter.getTab(position);
		switch (tab.position) {
			case TAB_POSITION_HOME: {
				mTwitterWrapper.clearNotification(NOTIFICATION_ID_HOME_TIMELINE);
				break;
			}
			case TAB_POSITION_MENTIONS: {
				mTwitterWrapper.clearNotification(NOTIFICATION_ID_MENTIONS);
				break;
			}
			case TAB_POSITION_MESSAGES: {
				mTwitterWrapper.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);
				break;
			}
		}
		invalidateSupportOptionsMenu();
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final boolean bottom_actions = mPreferences.getBoolean(PREFERENCE_KEY_BOTTOM_COMPOSE_BUTTON, false);
		final boolean leftside_compose_button = mPreferences.getBoolean(PREFERENCE_KEY_LEFTSIDE_COMPOSE_BUTTON, false);
		int icon = R.drawable.ic_menu_tweet, title = R.string.compose;
		if (mViewPager != null && mAdapter != null) {
			final int position = mViewPager.getCurrentItem();
			final TabSpec tab = mAdapter.getTab(position);
			if (tab == null) {
				title = R.string.compose;
				icon = R.drawable.ic_menu_tweet;
			} else {
				title = R.string.compose;
				switch (tab.position) {
					case TAB_POSITION_MESSAGES:
						icon = R.drawable.ic_menu_compose;
						break;
					default:
						icon = R.drawable.ic_menu_tweet;
				}
			}

			final MenuItem composeItem = menu.findItem(MENU_COMPOSE);
			if (composeItem != null) {
				composeItem.setIcon(icon);
				composeItem.setTitle(title);
				composeItem.setVisible(!bottom_actions && mViewPager.getVisibility() == View.VISIBLE);
			}
			if (mComposeButton != null) {
				mComposeButton.setImageResource(icon);
				mComposeButton.setVisibility(bottom_actions && !isRightPaneUsed() ? View.VISIBLE : View.GONE);
				if (bottom_actions) {
					final FrameLayout.LayoutParams compose_lp = (FrameLayout.LayoutParams) mComposeButton
							.getLayoutParams();
					compose_lp.gravity = Gravity.BOTTOM | (leftside_compose_button ? Gravity.LEFT : Gravity.RIGHT);
					mComposeButton.setLayoutParams(compose_lp);
				}
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	public void setDefaultAccount() {
		if (mPreferences == null) return;
		final long[] activated_ids = getActivatedAccountIds(this);
		final long default_account_id = mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
		if (activated_ids != null && activated_ids.length > 0
				&& !ArrayUtils.contains(activated_ids, default_account_id)) {
			mPreferences.edit().putLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, activated_ids[0]).commit();
		}
	}

	@Override
	public void setSupportProgressBarIndeterminateVisibility(final boolean visible) {
		mProgressBarIndeterminateVisible = visible;
		mProgress.setVisibility(visible || mTwitterWrapper.hasActivatedTask() ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	protected int getDualPaneLayoutRes() {
		return R.layout.home_dual_pane;
	}

	@Override
	protected int getNormalLayoutRes() {
		return R.layout.home;
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		final ContentResolver resolver = getContentResolver();
		ContentValues values;
		switch (requestCode) {
			case REQUEST_SELECT_ACCOUNT: {
				if (resultCode == RESULT_OK) {
					if (intent == null || intent.getExtras() == null) {
						break;
					}
					final Bundle bundle = intent.getExtras();
					if (bundle == null) {
						break;
					}
					final long[] account_ids = bundle.getLongArray(INTENT_KEY_IDS);
					if (account_ids != null) {
						values = new ContentValues();
						values.put(Accounts.IS_ACTIVATED, 0);
						resolver.update(Accounts.CONTENT_URI, values, null, null);
						values = new ContentValues();
						values.put(Accounts.IS_ACTIVATED, 1);
						for (final long account_id : account_ids) {
							final String where = Accounts.ACCOUNT_ID + " = " + account_id;
							resolver.update(Accounts.CONTENT_URI, values, where, null);
						}
					}
					setDefaultAccount();
				} else if (resultCode == RESULT_CANCELED) {
					if (getActivatedAccountIds(this).length <= 0) {
						finish();
					} else {
						setDefaultAccount();
					}
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mTwitterWrapper = getTwitterWrapper();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		super.onCreate(savedInstanceState);
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONCREATE));
		final Resources res = getResources();
		mDisplayAppIcon = res.getBoolean(R.bool.home_display_icon);
		final long[] account_ids = getAccountIds(this);
		if (account_ids.length <= 0) {
			final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
			intent.setClass(this, SignInActivity.class);
			startActivity(intent);
			finish();
			return;
		} else {
			setDefaultAccount();
		}
		final boolean refresh_on_start = mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ON_START, false);
		final Bundle bundle = getIntent().getExtras();
		int initial_tab = -1;
		if (bundle != null) {
			final long[] refreshed_ids = bundle.getLongArray(INTENT_KEY_IDS);
			if (refreshed_ids != null && !refresh_on_start && savedInstanceState == null) {
				mTwitterWrapper.refreshAll();
			}
			initial_tab = bundle.getInt(INTENT_KEY_INITIAL_TAB, -1);
			switch (initial_tab) {
				case TAB_POSITION_HOME: {
					mTwitterWrapper.clearNotification(NOTIFICATION_ID_HOME_TIMELINE);
					break;
				}
				case TAB_POSITION_MENTIONS: {
					mTwitterWrapper.clearNotification(NOTIFICATION_ID_MENTIONS);
					break;
				}
				case TAB_POSITION_MESSAGES: {
					mTwitterWrapper.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);
					break;
				}
			}
		}
		mActionBar = getSupportActionBar();
		mActionBar.setCustomView(R.layout.base_tabs);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(mDisplayAppIcon);
		if (mDisplayAppIcon) {
			ActivityAccessor.setHomeButtonEnabled(this, true);
		}
		final View view = mActionBar.getCustomView();

		mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		final boolean tab_display_label = res.getBoolean(R.bool.tab_display_label);
		mAdapter = new TabsAdapter(this, getSupportFragmentManager(), mIndicator);
		mShowHomeTab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_HOME_TAB, true);
		mShowMentionsTab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_MENTIONS_TAB, true);
		mShowMessagesTab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_MESSAGES_TAB, true);
		mShowAccountsTab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ACCOUNTS_TAB, true);
		initTabs(getTabs(this));
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setOnPageChangeListener(this);
		mIndicator.setDisplayLabel(tab_display_label);
		getSupportFragmentManager().addOnBackStackChangedListener(this);

		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, true);
		final long[] activated_ids = getActivatedAccountIds(this);
		if (activated_ids.length <= 0) {
			startActivityForResult(new Intent(INTENT_ACTION_SELECT_ACCOUNT), REQUEST_SELECT_ACCOUNT);
		} else if (remember_position || initial_tab >= 0) {
			final int position = initial_tab >= 0 ? initial_tab : mPreferences.getInt(
					PREFERENCE_KEY_SAVED_TAB_POSITION, TAB_POSITION_HOME);
			if (position >= 0 || position < mViewPager.getChildCount()) {
				mViewPager.setCurrentItem(position);
			}
		}
		if (refresh_on_start && savedInstanceState == null) {
			mTwitterWrapper.refreshAll();
		}
		if (!mPreferences.getBoolean(PREFERENCE_KEY_API_UPGRADE_CONFIRMED, false)) {
			final FragmentManager fm = getSupportFragmentManager();
			if (fm.findFragmentByTag(FRAGMENT_TAG_API_UPGRADE_NOTICE) == null
					|| !fm.findFragmentByTag(FRAGMENT_TAG_API_UPGRADE_NOTICE).isAdded()) {
				new APIUpgradeConfirmDialog().show(getSupportFragmentManager(), "api_upgrade_notice");
			}
		}

		if (mPreferences.getBoolean(PREFERENCE_KEY_SHOW_UCD_DATA_PROFILING_REQUEST, true)) {
			final Intent intent = new Intent(this, DataProfilingSettingsActivity.class);
			final PendingIntent content_intent = PendingIntent.getActivity(this, 0, intent, 0);
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			builder.setAutoCancel(true);
			builder.setSmallIcon(R.drawable.ic_stat_question_mark);
			builder.setTicker(getString(R.string.data_profiling_notification_ticker));
			builder.setContentTitle(getString(R.string.data_profiling_notification_title));
			builder.setContentText(getString(R.string.data_profiling_notification_desc));
			builder.setContentIntent(content_intent);
			mNotificationManager.notify(NOTIFICATION_ID_DATA_PROFILING, builder.build());
		}
	}

	@Override
	protected void onDestroy() {
		// Delete unused items in databases.
		cleanDatabasesByItemLimit(this);
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONDESTROY));
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(final Intent intent) {
		final Bundle bundle = intent.getExtras();
		if (bundle != null) {
			final long[] refreshed_ids = bundle.getLongArray(INTENT_KEY_IDS);
			if (refreshed_ids != null) {
				// TODO should I refresh inbox too?
				mTwitterWrapper.refreshAll();
			}
			final int initial_tab = bundle.getInt(INTENT_KEY_INITIAL_TAB, -1);
			if (initial_tab != -1 && mViewPager != null) {
				switch (initial_tab) {
					case TAB_POSITION_HOME: {
						if (mShowHomeTab) {
							mTwitterWrapper.clearNotification(NOTIFICATION_ID_HOME_TIMELINE);
						}
						break;
					}
					case TAB_POSITION_MENTIONS: {
						mTwitterWrapper.clearNotification(NOTIFICATION_ID_MENTIONS);
						break;
					}
					case TAB_POSITION_MESSAGES: {
						mTwitterWrapper.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);
						break;
					}
				}
				if (initial_tab >= 0 || initial_tab < mViewPager.getChildCount()) {
					mViewPager.setCurrentItem(initial_tab);
				}
			}
		}
		super.onNewIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		invalidateSupportOptionsMenu();
		mViewPager.setPagingEnabled(!mPreferences.getBoolean(PREFERENCE_KEY_DISABLE_TAB_SWIPE, false));
	}

	@Override
	protected void onStart() {
		super.onStart();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTART));
		setSupportProgressBarIndeterminateVisibility(mProgressBarIndeterminateVisible);
		final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
		registerReceiver(mStateReceiver, filter);
		final boolean show_home_tab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_HOME_TAB, true);
		final boolean show_mentions_tab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_MENTIONS_TAB, true);
		final boolean show_messages_tab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_MESSAGES_TAB, true);
		final boolean show_accounts_tab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_ACCOUNTS_TAB, true);

		final List<TabSpec> tabs = getTabs(this);
		if (isTabsChanged(tabs) || show_home_tab != mShowHomeTab || show_mentions_tab != mShowMentionsTab
				|| show_messages_tab != mShowMessagesTab || show_accounts_tab != mShowAccountsTab) {
			restart();
		}
		// UCD
		ProfilingUtil.profile(this, ProfilingUtil.FILE_NAME_APP, "App onStart");
	}

	@Override
	protected void onStop() {
		unregisterReceiver(mStateReceiver);
		mPreferences.edit().putInt(PREFERENCE_KEY_SAVED_TAB_POSITION, mViewPager.getCurrentItem()).commit();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTOP));

		// UCD
		ProfilingUtil.profile(this, ProfilingUtil.FILE_NAME_APP, "App onStop");
		super.onStop();
	}

	protected void setPagingEnabled(final boolean enabled) {
		if (mIndicator != null && mViewPager != null) {
			mViewPager.setPagingEnabled(!mPreferences.getBoolean(PREFERENCE_KEY_DISABLE_TAB_SWIPE, false));
			mIndicator.setSwitchingEnabled(enabled);
			mIndicator.setEnabled(enabled);
		}
	}

	private void initTabs(final Collection<? extends TabSpec> tabs) {
		mCustomTabs.clear();
		mCustomTabs.addAll(tabs);
		mAdapter.clear();
		if (mShowHomeTab) {
			mAdapter.addTab(HomeTimelineFragment.class, null, getString(R.string.home), R.drawable.ic_tab_home,
					TAB_POSITION_HOME);
		}
		if (mShowMentionsTab) {
			mAdapter.addTab(MentionsFragment.class, null, getString(R.string.mentions), R.drawable.ic_tab_mention,
					TAB_POSITION_MENTIONS);
		}
		if (mShowMessagesTab) {
			mAdapter.addTab(DirectMessagesFragment.class, null, getString(R.string.direct_messages),
					R.drawable.ic_tab_message, TAB_POSITION_MESSAGES);
		}
		mAdapter.addTabs(tabs);
		if (mShowAccountsTab) {
			mAdapter.addTab(AccountsFragment.class, null, getString(R.string.accounts), R.drawable.ic_tab_accounts,
					Integer.MAX_VALUE);
		}

	}

	private boolean isTabsChanged(final List<TabSpec> tabs) {
		if (mCustomTabs.size() == 0 && tabs == null) return false;
		if (mCustomTabs.size() != tabs.size()) return true;
		final int size = mCustomTabs.size();
		for (int i = 0; i < size; i++) {
			if (!mCustomTabs.get(i).equals(tabs.get(i))) return true;
		}
		return false;
	}

}
