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

import static org.mariotaku.twidere.util.CompareUtils.classEquals;
import static org.mariotaku.twidere.util.CustomTabUtils.getAddedTabPosition;
import static org.mariotaku.twidere.util.CustomTabUtils.getHomeTabs;
import static org.mariotaku.twidere.util.Utils.cleanDatabasesByItemLimit;
import static org.mariotaku.twidere.util.Utils.createFragmentForIntent;
import static org.mariotaku.twidere.util.Utils.getAccountIds;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.openDirectMessagesConversation;
import static org.mariotaku.twidere.util.Utils.openSearch;

import android.app.ActionBar;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnClosedListener;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.OnOpenedListener;
import com.readystatesoftware.viewbadger.BadgeView;

import edu.ucdavis.earlybird.ProfilingUtil;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.DualPaneActivity;
import org.mariotaku.twidere.activity.support.SignInActivity;
import org.mariotaku.twidere.adapter.support.SupportTabsAdapter;
import org.mariotaku.twidere.fragment.iface.IBaseFragment;
import org.mariotaku.twidere.fragment.iface.IBasePullToRefreshFragment;
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.fragment.support.DirectMessagesFragment;
import org.mariotaku.twidere.fragment.support.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.support.MentionsFragment;
import org.mariotaku.twidere.fragment.support.TrendsSuggectionsFragment;
import org.mariotaku.twidere.model.SupportTabSpec;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.MathUtils;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.UnreadCountUtils;
import org.mariotaku.twidere.util.accessor.ViewAccessor;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.LeftDrawerFrameLayout;
import org.mariotaku.twidere.view.TabPageIndicator;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends DualPaneActivity implements OnClickListener, OnPageChangeListener,
		SupportFragmentCallback, OnOpenedListener, OnClosedListener {

	private SharedPreferences mPreferences;

	private AsyncTwitterWrapper mTwitterWrapper;
	private NotificationManager mNotificationManager;
	private MultiSelectEventHandler mMultiSelectHandler;
	private ActionBar mActionBar;

	private SupportTabsAdapter mPagerAdapter;
	private ExtendedViewPager mViewPager;

	private TabPageIndicator mIndicator;
	private SlidingMenu mSlidingMenu;
	private View mActionsActionView, mActionsButtonLayout, mEmptyTabHint;
	private boolean mBottomActionsButton;

	private Fragment mCurrentVisibleFragment;

	private UpdateUnreadCountTask mUpdateUnreadCountTask;

	private final ArrayList<SupportTabSpec> mCustomTabs = new ArrayList<SupportTabSpec>();

	private final SparseArray<Fragment> mAttachedFragments = new SparseArray<Fragment>();
	private final BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				updateActionsButton();
			} else if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				notifyAccountsChanged();
				updateUnreadCount();
			} else if (BROADCAST_UNREAD_COUNT_UPDATED.equals(action)) {
				updateUnreadCount();
			}
		}

	};

	public void closeAccountsDrawer() {
		if (mSlidingMenu == null) return;
		mSlidingMenu.showContent();
	}

	@Override
	public Fragment getCurrentVisibleFragment() {
		return mCurrentVisibleFragment;
	}

	public SlidingMenu getSlidingMenu() {
		return mSlidingMenu;
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
	public void onAttachFragment(final Fragment fragment) {
		if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
			mAttachedFragments.put(((IBaseFragment) fragment).getTabPosition(), fragment);
		}
	}

	@Override
	public void onBackPressed() {
		if (mSlidingMenu != null && mSlidingMenu.isMenuShowing()) {
			mSlidingMenu.showContent();
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onBackStackChanged() {
		super.onBackStackChanged();
		if (!isDualPaneMode()) return;
		final FragmentManager fm = getSupportFragmentManager();
		setPagingEnabled(!isLeftPaneUsed());
		final int count = fm.getBackStackEntryCount();
		if (count == 0) {
			showLeftPane();
		}
		updateActionsButton();
		updateSlidingMenuTouchMode();
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.actions_item:
			case R.id.actions_button:
				if (mViewPager == null || mPagerAdapter == null) return;
				final int position = mViewPager.getCurrentItem();
				final SupportTabSpec tab = mPagerAdapter.getTab(position);
				if (tab == null) {
					startActivity(new Intent(INTENT_ACTION_COMPOSE));
				} else {
					if (classEquals(DirectMessagesFragment.class, tab.cls)) {
						openDirectMessagesConversation(this, -1, -1, null);
					} else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
						onSearchRequested();
					} else {
						startActivity(new Intent(INTENT_ACTION_COMPOSE));
					}
				}
				break;
		}
	}

	@Override
	public void onClosed() {
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mViewPager = (ExtendedViewPager) findViewById(R.id.main);
		mActionsButtonLayout = findViewById(R.id.actions_button);
		mEmptyTabHint = findViewById(R.id.empty_tab_hint);
		if (mSlidingMenu == null) {
			mSlidingMenu = new SlidingMenu(this);
		}
	}

	@Override
	public void onDetachFragment(final Fragment fragment) {
		if (fragment instanceof IBaseFragment && ((IBaseFragment) fragment).getTabPosition() != -1) {
			mAttachedFragments.remove(((IBaseFragment) fragment).getTabPosition());
		}
	}

	@Override
	public boolean onKeyUp(final int keyCode, final KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_MENU: {
				if (mSlidingMenu != null) {
					mSlidingMenu.toggle(true);
					return true;
				}
				break;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onOpened() {
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				final FragmentManager fm = getSupportFragmentManager();
				final int count = fm.getBackStackEntryCount();

				if (mSlidingMenu.isMenuShowing()) {
					mSlidingMenu.showContent();
					return true;
				} else if (count == 0 && !mSlidingMenu.isMenuShowing()) {
					mSlidingMenu.showMenu();
					return true;
				}
				if (isDualPaneMode() && !FragmentManagerTrojan.isStateSaved(fm)) {
					for (int i = 0; i < count; i++) {
						fm.popBackStackImmediate();
					}
					updateActionsButton();
				}
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
		clearNotification(position);
		if (mSlidingMenu.isMenuShowing()) {
			mSlidingMenu.showContent();
		}
		updateSlidingMenuTouchMode();
		updateActionsButton();
	}

	@Override
	public void onSetUserVisibleHint(final Fragment fragment, final boolean isVisibleToUser) {
		if (isVisibleToUser) {
			mCurrentVisibleFragment = fragment;
		}
		updateRefreshingState();
	}

	public void setHomeProgressBarIndeterminateVisibility(final boolean visible) {
		final View view = mBottomActionsButton ? mActionsButtonLayout : mActionsActionView;
		if (view == null) return;
		final boolean has_task = hasActivatedTask();
		final ImageView actions_icon = (ImageView) view.findViewById(R.id.actions_icon);
		final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
		actions_icon.setVisibility(has_task ? View.GONE : View.VISIBLE);
		progress.setVisibility(has_task ? View.VISIBLE : View.GONE);
	}

	@Override
	public boolean shouldOverrideActivityAnimation() {
		return false;
	}

	@Override
	public boolean triggerRefresh(final int position) {
		final Fragment f = mAttachedFragments.get(position);
		return f instanceof RefreshScrollTopInterface && !f.isDetached()
				&& ((RefreshScrollTopInterface) f).triggerRefresh();
	}

	public void updateUnreadCount() {
		if (mIndicator == null || mUpdateUnreadCountTask != null
				&& mUpdateUnreadCountTask.getStatus() == AsyncTask.Status.RUNNING) return;
		mUpdateUnreadCountTask = new UpdateUnreadCountTask(mIndicator, mPreferences.getBoolean(
				PREFERENCE_KEY_UNREAD_COUNT, true));
		mUpdateUnreadCountTask.execute();
	}

	@Override
	protected IBasePullToRefreshFragment getCurrentPullToRefreshFragment() {
		if (mCurrentVisibleFragment instanceof IBasePullToRefreshFragment)
			return (IBasePullToRefreshFragment) mCurrentVisibleFragment;
		else if (mCurrentVisibleFragment instanceof SupportFragmentCallback) {
			final Fragment curr = ((SupportFragmentCallback) mCurrentVisibleFragment).getCurrentVisibleFragment();
			if (curr instanceof IBasePullToRefreshFragment) return (IBasePullToRefreshFragment) curr;
		}
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

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_SWIPEBACK_ACTIVITY: {
				closeAccountsDrawer();
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mTwitterWrapper = getTwitterWrapper();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mMultiSelectHandler = new MultiSelectEventHandler(this);
		mMultiSelectHandler.dispatchOnCreate();
		final Resources res = getResources();
		final boolean home_display_icon = res.getBoolean(R.bool.home_display_icon);
		super.onCreate(savedInstanceState);
		final long[] account_ids = getAccountIds(this);
		if (account_ids.length == 0) {
			final Intent sign_in_intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
			sign_in_intent.setClass(this, SignInActivity.class);
			startActivity(sign_in_intent);
			finish();
			return;
		} else {
			notifyAccountsChanged();
		}
		final Intent intent = getIntent();
		if (openSettingsWizard()) {
			finish();
			return;
		}
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONCREATE));
		final boolean refresh_on_start = mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ON_START, false);
		final int initial_tab = handleIntent(intent, savedInstanceState == null);
		mActionBar = getActionBar();
		mActionBar.setCustomView(R.layout.home_tabs);

		final View view = mActionBar.getCustomView();
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		mActionsActionView = view.findViewById(R.id.actions_item);
		ThemeUtils.applyBackground(mIndicator);
		mPagerAdapter = new SupportTabsAdapter(this, getSupportFragmentManager(), mIndicator);
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setOnPageChangeListener(this);
		mIndicator.setDisplayLabel(res.getBoolean(R.bool.tab_display_label));
		mActionsActionView.setOnClickListener(this);
		mActionsButtonLayout.setOnClickListener(this);
		initTabs();
		final boolean tabs_not_empty = mPagerAdapter.getCount() != 0;
		mEmptyTabHint.setVisibility(tabs_not_empty ? View.GONE : View.VISIBLE);
		mActionBar.setDisplayShowHomeEnabled(home_display_icon || !tabs_not_empty);
		mActionBar.setHomeButtonEnabled(home_display_icon || !tabs_not_empty);
		mActionBar.setDisplayShowTitleEnabled(!tabs_not_empty);
		mActionBar.setDisplayShowCustomEnabled(tabs_not_empty);
		setTabPosition(initial_tab);
		if (refresh_on_start && savedInstanceState == null) {
			mTwitterWrapper.refreshAll();
		}
		setupSlidingMenu();
		showDataProfilingRequest();
		initUnreadCount();
		updateActionsButton();
		updateSlidingMenuTouchMode();
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
		final int tab_position = handleIntent(intent, false);
		if (tab_position >= 0) {
			mViewPager.setCurrentItem(MathUtils.clamp(tab_position, mPagerAdapter.getCount(), 0));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		mViewPager.setEnabled(!mPreferences.getBoolean(PREFERENCE_KEY_DISABLE_TAB_SWIPE, false));
		mBottomActionsButton = mPreferences.getBoolean(PREFERENCE_KEY_BOTTOM_COMPOSE_BUTTON, false);
		invalidateOptionsMenu();
		updateActionsButtonStyle();
		updateActionsButton();
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMultiSelectHandler.dispatchOnStart();
		sendBroadcast(new Intent(BROADCAST_HOME_ACTIVITY_ONSTART));
		final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
		filter.addAction(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_UNREAD_COUNT_UPDATED);
		registerReceiver(mStateReceiver, filter);
		if (isTabsChanged(getHomeTabs(this))) {
			restart();
		}
		// UCD
		ProfilingUtil.profile(this, ProfilingUtil.FILE_NAME_APP, "App onStart");
		updateUnreadCount();
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
			mViewPager.setEnabled(!mPreferences.getBoolean(PREFERENCE_KEY_DISABLE_TAB_SWIPE, false));
			mIndicator.setSwitchingEnabled(enabled);
			mIndicator.setEnabled(enabled);
		}
	}

	private void clearNotification(final int position) {
		if (mPagerAdapter == null || mTwitterWrapper == null) return;
		final SupportTabSpec tab = mPagerAdapter.getTab(position);
		if (classEquals(HomeTimelineFragment.class, tab.cls)) {
			mTwitterWrapper.clearNotification(NOTIFICATION_ID_HOME_TIMELINE);
		} else if (classEquals(MentionsFragment.class, tab.cls)) {
			mTwitterWrapper.clearNotification(NOTIFICATION_ID_MENTIONS);
		} else if (classEquals(DirectMessagesFragment.class, tab.cls)) {
			mTwitterWrapper.clearNotification(NOTIFICATION_ID_DIRECT_MESSAGES);
		}
	}

	private int handleIntent(final Intent intent, final boolean first_create) {
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
		final long[] refreshed_ids = extras != null ? extras.getLongArray(EXTRA_IDS) : null;
		if (refreshed_ids != null) {
			mTwitterWrapper.refreshAll(refreshed_ids);
		} else if (first_create && refresh_on_start) {
			mTwitterWrapper.refreshAll();
		}

		final int initial_tab;
		if (extras != null) {
			final int tab = extras.getInt(EXTRA_INITIAL_TAB, -1);
			initial_tab = tab != -1 ? tab : getAddedTabPosition(this, extras.getString(EXTRA_TAB_TYPE));
			if (initial_tab != -1 && mViewPager != null) {
				clearNotification(initial_tab);
			}
			final Intent extra_intent = extras.getParcelable(EXTRA_EXTRA_INTENT);
			if (extra_intent != null) {
				if (isTwidereLink(extra_intent.getData()) && isDualPaneMode()) {
					showFragment(createFragmentForIntent(this, extra_intent), true);
				} else {
					startActivity(extra_intent);
				}
			}
		} else {
			initial_tab = -1;
		}
		return initial_tab;
	}

	private boolean hasActivatedTask() {
		if (mTwitterWrapper == null) return false;
		return mTwitterWrapper.hasActivatedTask();
	}

	private void initTabs() {
		final List<SupportTabSpec> tabs = getHomeTabs(this);
		mCustomTabs.clear();
		mCustomTabs.addAll(tabs);
		mPagerAdapter.clear();
		mPagerAdapter.addTabs(tabs);
	}

	private void initUnreadCount() {
		for (int i = 0, j = mIndicator.getTabCount(); i < j; i++) {
			final BadgeView badge = new BadgeView(this, mIndicator.getTabItem(i).findViewById(R.id.tab_item_content));
			badge.setId(R.id.unread_count);
			badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
			badge.setTextSize(getResources().getInteger(R.integer.unread_count_text_size));
			badge.hide();
		}
	}

	private boolean isTabsChanged(final List<SupportTabSpec> tabs) {
		if (mCustomTabs.size() == 0 && tabs == null) return false;
		if (mCustomTabs.size() != tabs.size()) return true;
		for (int i = 0, size = mCustomTabs.size(); i < size; i++) {
			if (!mCustomTabs.get(i).equals(tabs.get(i))) return true;
		}
		return false;
	}

	private boolean isTwidereLink(final Uri data) {
		return data != null && SCHEME_TWIDERE.equals(data.getScheme());
	}

	private boolean openSettingsWizard() {
		if (mPreferences == null || mPreferences.getBoolean(PREFERENCE_KEY_SETTINGS_WIZARD_COMPLETED, false))
			return false;
		startActivity(new Intent(this, SettingsWizardActivity.class));
		return true;
	}

	private void setTabPosition(final int initial_tab) {
		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, true);
		if (initial_tab >= 0) {
			mViewPager.setCurrentItem(MathUtils.clamp(initial_tab, mPagerAdapter.getCount(), 0));
		} else if (remember_position) {
			final int position = mPreferences.getInt(PREFERENCE_KEY_SAVED_TAB_POSITION, 0);
			mViewPager.setCurrentItem(MathUtils.clamp(position, mPagerAdapter.getCount(), 0));
		}
	}

	private void setupSlidingMenu() {
		if (mSlidingMenu == null) return;
		final int marginThreshold = getResources().getDimensionPixelSize(R.dimen.default_sliding_menu_margin_threshold);
		mSlidingMenu.setMode(SlidingMenu.LEFT);
		mSlidingMenu.setShadowWidthRes(R.dimen.default_sliding_menu_shadow_width);
		mSlidingMenu.setShadowDrawable(R.drawable.shadow_holo);
		mSlidingMenu.setBehindWidthRes(R.dimen.left_drawer_width);
		mSlidingMenu.setTouchmodeMarginThreshold(marginThreshold);
		mSlidingMenu.setFadeDegree(0.5f);
		mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
		mSlidingMenu.setMenu(R.layout.home_left_drawer_container);
		mSlidingMenu.setOnOpenedListener(this);
		mSlidingMenu.setOnClosedListener(this);
		if (isDualPaneMode()) {
			mSlidingMenu.addIgnoredView(getSlidingPane().getRightPaneContainer());
		}
		final LeftDrawerFrameLayout leftDrawerContainer = (LeftDrawerFrameLayout) mSlidingMenu.getMenu().findViewById(
				R.id.left_drawer_container);
		final boolean isTransparentBackground = ThemeUtils.isTransparentBackground(this);
		leftDrawerContainer.setClipEnabled(isTransparentBackground);
		leftDrawerContainer.setScrollScale(mSlidingMenu.getBehindScrollScale());
		mSlidingMenu.setBehindCanvasTransformer(new ListenerCanvasTransformer(leftDrawerContainer));
		if (isTransparentBackground) {
			ViewAccessor.setBackground(mSlidingMenu.getContent(), null);
		}
	}

	private void showDataProfilingRequest() {
		if (mPreferences.getBoolean(PREFERENCE_KEY_SHOW_UCD_DATA_PROFILING_REQUEST, true)) {
			final Intent intent = new Intent(this, DataProfilingSettingsActivity.class);
			final PendingIntent content_intent = PendingIntent.getActivity(this, 0, intent, 0);
			final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
			builder.setAutoCancel(true);
			builder.setSmallIcon(R.drawable.ic_stat_info);
			builder.setTicker(getString(R.string.data_profiling_notification_ticker));
			builder.setContentTitle(getString(R.string.data_profiling_notification_title));
			builder.setContentText(getString(R.string.data_profiling_notification_desc));
			builder.setContentIntent(content_intent);
			mNotificationManager.notify(NOTIFICATION_ID_DATA_PROFILING, builder.build());
		}
	}

	private void updateActionsButton() {
		if (mViewPager == null || mPagerAdapter == null) return;
		final int button_icon, title;
		final int position = mViewPager.getCurrentItem();
		final SupportTabSpec tab = mPagerAdapter.getTab(position);
		if (tab == null) {
			title = R.string.compose;
			button_icon = R.drawable.ic_menu_status_compose;
		} else {
			if (classEquals(DirectMessagesFragment.class, tab.cls)) {
				button_icon = R.drawable.ic_menu_compose;
				title = R.string.compose;
			} else if (classEquals(TrendsSuggectionsFragment.class, tab.cls)) {
				button_icon = android.R.drawable.ic_menu_search;
				title = android.R.string.search_go;
			} else {
				button_icon = R.drawable.ic_menu_status_compose;
				title = R.string.compose;
			}
		}
		final View view = mBottomActionsButton ? mActionsButtonLayout : mActionsActionView;
		if (view == null) return;
		final boolean has_task = hasActivatedTask();
		final ImageView actions_icon = (ImageView) view.findViewById(R.id.actions_icon);
		final ProgressBar progress = (ProgressBar) view.findViewById(R.id.progress);
		actions_icon.setImageResource(mBottomActionsButton ? button_icon : button_icon);
		actions_icon.setContentDescription(getString(title));
		actions_icon.setVisibility(has_task ? View.GONE : View.VISIBLE);
		progress.setVisibility(has_task ? View.VISIBLE : View.GONE);
	}

	private void updateActionsButtonStyle() {
		if (mActionsButtonLayout == null || mActionsActionView == null) return;
		final boolean leftside_compose_button = mPreferences.getBoolean(PREFERENCE_KEY_LEFTSIDE_COMPOSE_BUTTON, false);
		mActionsActionView.setVisibility(mBottomActionsButton ? View.GONE : View.VISIBLE);
		mActionsButtonLayout.setVisibility(mBottomActionsButton ? View.VISIBLE : View.GONE);
		final FrameLayout.LayoutParams compose_lp = (LayoutParams) mActionsButtonLayout.getLayoutParams();
		compose_lp.gravity = Gravity.BOTTOM | (leftside_compose_button ? Gravity.LEFT : Gravity.RIGHT);
		mActionsButtonLayout.setLayoutParams(compose_lp);
	}

	private void updateSlidingMenuTouchMode() {
		if (mViewPager == null || mSlidingMenu == null) return;
		final int position = mViewPager.getCurrentItem();
		final int mode = position == 0 && !isLeftPaneUsed() ? SlidingMenu.TOUCHMODE_FULLSCREEN
				: SlidingMenu.TOUCHMODE_MARGIN;
		mSlidingMenu.setTouchModeAbove(mode);
	}

	private static class ListenerCanvasTransformer implements CanvasTransformer {
		private final LeftDrawerFrameLayout mLeftDrawerContainer;

		public ListenerCanvasTransformer(final LeftDrawerFrameLayout leftDrawerContainer) {
			mLeftDrawerContainer = leftDrawerContainer;
		}

		@Override
		public void transformCanvas(final Canvas canvas, final float percentOpen) {
			mLeftDrawerContainer.setPercentOpen(percentOpen);
		}

	}

	private static class UpdateUnreadCountTask extends AsyncTask<Void, Void, int[]> {
		private final Context mContext;
		private final TabPageIndicator mIndicator;
		private final boolean mEnabled;

		UpdateUnreadCountTask(final TabPageIndicator indicator, final boolean enabled) {
			mIndicator = indicator;
			mContext = indicator.getContext();
			mEnabled = enabled;
		}

		@Override
		protected int[] doInBackground(final Void... params) {
			final int tab_count = mIndicator.getTabCount();
			final int[] result = new int[tab_count];
			for (int i = 0, j = tab_count; i < j; i++) {
				result[i] = UnreadCountUtils.getUnreadCount(mContext, i);
			}
			return result;
		}

		@Override
		protected void onPostExecute(final int[] result) {
			final int tab_count = mIndicator.getTabCount();
			if (result == null || result.length != tab_count) return;
			for (int i = 0, j = tab_count; i < j; i++) {
				final BadgeView badge = (BadgeView) mIndicator.getTabItem(i).findViewById(R.id.unread_count);
				if (!mEnabled) {
					badge.setCount(0);
					badge.hide();
					continue;
				}
				final int count = result[i];
				if (count > 0) {
					badge.setCount(count);
					badge.show();
				} else if (count == 0) {
					badge.setCount(0);
					badge.hide();
				} else {
					badge.setText("\u0387");
					badge.show();
				}
			}
		}

	}
}
