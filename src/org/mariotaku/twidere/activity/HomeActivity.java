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
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.getTabs;
import static org.mariotaku.twidere.util.Utils.openDirectMessagesConversation;
import static org.mariotaku.twidere.util.Utils.openSearch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.APIUpgradeConfirmDialog;
import org.mariotaku.twidere.fragment.BasePullToRefreshListFragment;
import org.mariotaku.twidere.fragment.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.MentionsFragment;
import org.mariotaku.twidere.fragment.TrendsFragment;
import org.mariotaku.twidere.fragment.iface.FragmentCallback;
import org.mariotaku.twidere.model.TabSpec;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.FragmentManagerAccessor;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.TabPageIndicator;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import edu.ucdavis.earlybird.ProfilingUtil;

public class HomeActivity extends DualPaneActivity implements OnClickListener, OnPageChangeListener, FragmentCallback {

	private SharedPreferences mPreferences;
	private AsyncTwitterWrapper mTwitterWrapper;
	private NotificationManager mNotificationManager;
	private MultiSelectEventHandler mMultiSelectHandler;

	private ActionBar mActionBar;
	private TabsAdapter mAdapter;

	private ExtendedViewPager mViewPager;
	private ImageButton mComposeButton;
	private TabPageIndicator mIndicator;
	private DrawerLayout mDrawerLayout;
	private View mLeftDrawerContainer;

	private boolean mDisplayAppIcon;
	private boolean mShowHomeTab, mShowMentionsTab, mShowMessagesTab, mShowTrendsTab;

	private Fragment mCurrentVisibleFragment;

	public static final int TAB_POSITION_HOME = 0;
	public static final int TAB_POSITION_MENTIONS = 1;
	public static final int TAB_POSITION_MESSAGES = 2;
	public static final int TAB_POSITION_TRENDS = 3;

	private final ArrayList<TabSpec> mCustomTabs = new ArrayList<TabSpec>();

	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				setRefreshing(hasActivatedTask());
			} else if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				notifyAccountsChanged();
			}
		}

	};

	public void closeAccountsDrawer() {
		if (mDrawerLayout == null) return;
		mDrawerLayout.closeDrawer(Gravity.LEFT);
	}

	public void notifyAccountsChanged() {
		if (mPreferences == null) return;
		final long[] account_ids = getAccountIds(this);
		final long default_id = mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
		if (account_ids == null || account_ids.length == 0) {
			finish();
		} else if (account_ids.length > 0 && !ArrayUtils.contains(account_ids, default_id)) {
			mPreferences.edit().putLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, account_ids[0]).commit();
		}
	}

	@Override
	public void onBackStackChanged() {
		super.onBackStackChanged();
		if (!isDualPaneMode()) return;
		final FragmentManager fm = getFragmentManager();
		final Fragment left_pane_fragment = fm.findFragmentById(PANE_LEFT);
		final boolean left_pane_used = left_pane_fragment != null && left_pane_fragment.isAdded();
		setPagingEnabled(!left_pane_used);
		final int count = fm.getBackStackEntryCount();
		if (count == 0) {
			showLeftPane();
		}
		invalidateOptionsMenu();
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
						case TAB_POSITION_TRENDS:
							onSearchRequested();
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
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mLeftDrawerContainer = findViewById(R.id.left_drawer_container);
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
				final FragmentManager fm = getFragmentManager();
				final int count = fm.getBackStackEntryCount();
				if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
					return true;
				} else if (count == 0 && !mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.openDrawer(Gravity.LEFT);
					return true;
				}
				if (isDualPaneMode() && !FragmentManagerAccessor.isStateSaved(fm)) {
					for (int i = 0; i < count; i++) {
						fm.popBackStackImmediate();
					}
					setRefreshing(hasActivatedTask());
				}
				return true;
			}
			case MENU_ACTIONS: {
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
				if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
					mDrawerLayout.closeDrawer(Gravity.LEFT);
				} else {
					mDrawerLayout.openDrawer(Gravity.LEFT);
				}
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
		if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
			mDrawerLayout.closeDrawer(Gravity.LEFT);
		}
		invalidateOptionsMenu();
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
				switch (tab.position) {
					case TAB_POSITION_MESSAGES:
						icon = R.drawable.ic_menu_compose;
						title = R.string.compose;
						break;
					case TAB_POSITION_TRENDS:
						icon = android.R.drawable.ic_menu_search;
						title = android.R.string.search_go;
						break;
					default:
						icon = R.drawable.ic_menu_tweet;
						title = R.string.compose;
				}
			}

			final MenuItem actionsItem = menu.findItem(MENU_ACTIONS);
			if (actionsItem != null) {
				actionsItem.setIcon(icon);
				actionsItem.setTitle(title);
				actionsItem.setEnabled(mViewPager.getVisibility() == View.VISIBLE);
				actionsItem.setVisible(!bottom_actions);
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

	@Override
	public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
		if (isVisibleToUser) {
			mCurrentVisibleFragment = fragment;
		}
		updateRefreshingState();
	}

	@Override
	protected BasePullToRefreshListFragment getCurrentPullToRefreshFragment() {
		if (mCurrentVisibleFragment instanceof BasePullToRefreshListFragment)
			return (BasePullToRefreshListFragment) mCurrentVisibleFragment;
		return null;
	}

	@Override
	protected int getDualPaneLayoutRes() {
		return R.layout.home_dual_pane;
	}

	@Override
	protected int getNormalLayoutRes() {
		return R.layout.home;
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mTwitterWrapper = getTwitterWrapper();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mMultiSelectHandler = new MultiSelectEventHandler(this);
		mMultiSelectHandler.dispatchOnCreate();
		super.onCreate(savedInstanceState);
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONCREATE));
		final Resources res = getResources();
		mDisplayAppIcon = res.getBoolean(R.bool.home_display_icon);
		final long[] account_ids = getAccountIds(this);
		if (account_ids.length == 0) {
			final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
			intent.setClass(this, SignInActivity.class);
			startActivity(intent);
			finish();
			return;
		} else {
			notifyAccountsChanged();
		}
		final boolean refresh_on_start = mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ON_START, false);
		final int initial_tab = handleIntent(getIntent(), savedInstanceState == null);
		mActionBar = getActionBar();
		mActionBar.setCustomView(R.layout.base_tabs);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(mDisplayAppIcon);
		if (mDisplayAppIcon) {
			mActionBar.setHomeButtonEnabled(true);
		}
		final View view = mActionBar.getCustomView();

		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		ThemeUtils.applyBackground(mIndicator);
		final boolean tab_display_label = res.getBoolean(R.bool.tab_display_label);
		mAdapter = new TabsAdapter(this, getFragmentManager(), mIndicator);
		mShowHomeTab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_HOME_TAB, true);
		mShowMentionsTab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_MENTIONS_TAB, true);
		mShowMessagesTab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_MESSAGES_TAB, true);
		mShowTrendsTab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_TRENDS_TAB, true);
		initTabs(getTabs(this));
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setOnPageChangeListener(this);
		mIndicator.setDisplayLabel(tab_display_label);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.LEFT);
		mLeftDrawerContainer.setBackgroundResource(getPaneBackground());
		getFragmentManager().addOnBackStackChangedListener(this);

		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, true);
		final long[] activated_ids = getActivatedAccountIds(this);
		if (activated_ids.length <= 0) {
			startActivityForResult(new Intent(INTENT_ACTION_SELECT_ACCOUNT), REQUEST_SELECT_ACCOUNT);
		} else if (initial_tab >= 0) {
			mViewPager.setCurrentItem(MathUtils.clamp(initial_tab, mViewPager.getChildCount(), 0));
		} else if (remember_position) {
			final int position = mPreferences.getInt(PREFERENCE_KEY_SAVED_TAB_POSITION, TAB_POSITION_HOME);
			mViewPager.setCurrentItem(MathUtils.clamp(position, mViewPager.getChildCount(), 0));
		}
		if (refresh_on_start && savedInstanceState == null) {
			mTwitterWrapper.refreshAll();
		}
		showAPIUpgradeNotice();
		showDataProfilingRequest();
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
		handleIntent(intent, false);
	}

	@Override
	protected void onResume() {
		super.onResume();
		invalidateOptionsMenu();
		mViewPager.setPagingEnabled(!mPreferences.getBoolean(PREFERENCE_KEY_DISABLE_TAB_SWIPE, false));
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMultiSelectHandler.dispatchOnStart();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTART));
		setRefreshing(hasActivatedTask());
		final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
		registerReceiver(mStateReceiver, filter);
		final boolean show_home_tab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_HOME_TAB, true);
		final boolean show_mentions_tab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_MENTIONS_TAB, true);
		final boolean show_messages_tab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_MESSAGES_TAB, true);
		final boolean show_trends_tab = mPreferences.getBoolean(PREFERENCE_KEY_SHOW_TRENDS_TAB, true);

		final List<TabSpec> tabs = getTabs(this);
		if (isTabsChanged(tabs) || show_home_tab != mShowHomeTab || show_mentions_tab != mShowMentionsTab
				|| show_messages_tab != mShowMessagesTab || show_trends_tab != mShowTrendsTab) {
			restart();
		}
		// UCD
		ProfilingUtil.profile(this, ProfilingUtil.FILE_NAME_APP, "App onStart");
	}

	@Override
	protected void onStop() {
		mMultiSelectHandler.dispatchOnStop();
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

	private int handleIntent(final Intent intent, final boolean first_create) {
		Log.d(LOGTAG, String.format("Intent: %s", intent));
		// Reset intent
		setIntent(new Intent(this, HomeActivity.class));
		final String action = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(action)) {
			final String query = intent.getStringExtra(SearchManager.QUERY);
			final long account_id = getDefaultAccountId(this);
			openSearch(this, account_id, query);
			return -1;
		}
		final Bundle extras = intent.getExtras();
		final boolean refresh_on_start = mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ON_START, false);
		final long[] refreshed_ids = extras != null ? extras.getLongArray(INTENT_KEY_IDS) : null;
		if (refreshed_ids != null) {
			mTwitterWrapper.refreshAll(refreshed_ids);
		} else if (first_create && refresh_on_start) {
			mTwitterWrapper.refreshAll();
		}

		int initial_tab = -1;
		if (extras != null) {
			initial_tab = extras.getInt(INTENT_KEY_INITIAL_TAB, -1);
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
			}
			final Intent extra_intent = extras.getParcelable(INTENT_KEY_EXTRA_INTENT);
			if (extra_intent != null) {
				startActivity(extra_intent);
			}
		}
		return initial_tab;
	}

	private boolean hasActivatedTask() {
		if (mTwitterWrapper == null) return false;
		return mTwitterWrapper.hasActivatedTask();
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
		if (mShowTrendsTab) {
			mAdapter.addTab(TrendsFragment.class, null, getString(R.string.trends), R.drawable.ic_tab_trends,
					TAB_POSITION_TRENDS);
		}
		mAdapter.addTabs(tabs);
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

	private void showAPIUpgradeNotice() {
		if (!mPreferences.getBoolean(PREFERENCE_KEY_API_UPGRADE_CONFIRMED, false)) {
			final FragmentManager fm = getFragmentManager();
			if (fm.findFragmentByTag(FRAGMENT_TAG_API_UPGRADE_NOTICE) == null
					|| !fm.findFragmentByTag(FRAGMENT_TAG_API_UPGRADE_NOTICE).isAdded()) {
				new APIUpgradeConfirmDialog().show(getFragmentManager(), "api_upgrade_notice");
			}
		}
	}

	private void showDataProfilingRequest() {
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

}
