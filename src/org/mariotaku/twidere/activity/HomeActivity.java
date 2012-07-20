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

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.AccountsFragment;
import org.mariotaku.twidere.fragment.DiscoverFragment;
import org.mariotaku.twidere.fragment.HomeTimelineFragment;
import org.mariotaku.twidere.fragment.MentionsFragment;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ArrayUtils;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout.TouchInterceptor;
import org.mariotaku.twidere.view.ExtendedViewPager;
import org.mariotaku.twidere.view.TabPageIndicator;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

public class HomeActivity extends BaseActivity implements OnClickListener, OnBackStackChangedListener {

	private ExtendedViewPager mViewPager;
	private SharedPreferences mPreferences;
	private ActionBar mActionBar;
	private ProgressBar mProgress;
	private TabsAdapter mAdapter;
	private ImageButton mComposeButton;
	private ServiceInterface mService;
	private TabPageIndicator mIndicator;
	private ExtendedFrameLayout mLeftPaneContainer, mRightPaneContainer;
	private ViewGroup mLeftPaneLayer, mRightPaneLayer;

	private BroadcastReceiver mStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_REFRESHSTATE_CHANGED.equals(action)) {
				setSupportProgressBarIndeterminateVisibility(mProgressBarIndeterminateVisible);
			}
		}

	};

	private boolean mProgressBarIndeterminateVisible = false;
	private boolean mIsNavigateToDefaultAccount = false;

	private final TouchInterceptor mLeftPaneTouchInterceptor = new TouchInterceptor() {

		@Override
		public void onInterceptTouchEvent(MotionEvent event) {
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				bringLeftPaneToFront();
			}
		}
	};
	private final TouchInterceptor mRightPaneTouchInterceptor = new TouchInterceptor() {

		@Override
		public void onInterceptTouchEvent(MotionEvent event) {
			if (MotionEvent.ACTION_DOWN == event.getAction()) {
				bringRightPaneToFront();
			}
		}
	};

	public static final int TAB_POSITION_HOME = 0;
	public static final int TAB_POSITION_MENTIONS = 1;
	public static final int TAB_POSITION_DISCOVER = 2;
	public static final int TAB_POSITION_ME = 3;

	public void bringLeftPaneToFront() {
		if (mLeftPaneLayer == null || mRightPaneLayer == null || mLeftPaneContainer == null
				|| mRightPaneContainer == null) return;
		mLeftPaneLayer.bringToFront();
		final int bg_res_id = isDarkTheme() ? R.drawable.bg_two_pane_compact_dark_left
				: R.drawable.bg_two_pane_compact_light_left;
		mLeftPaneContainer.setBackgroundResource(bg_res_id);
		mRightPaneContainer.setBackgroundResource(0);
	}

	public void bringRightPaneToFront() {
		if (mLeftPaneLayer == null || mRightPaneLayer == null || mLeftPaneContainer == null
				|| mRightPaneContainer == null) return;
		mRightPaneLayer.bringToFront();
		final int bg_res_id = isDarkTheme() ? R.drawable.bg_two_pane_compact_dark_right
				: R.drawable.bg_two_pane_compact_light_right;
		mRightPaneContainer.setBackgroundResource(bg_res_id);
		mLeftPaneContainer.setBackgroundResource(0);
	}

	public boolean checkDefaultAccountSet() {
		boolean result = true;
		final long[] activated_ids = getActivatedAccountIds(this);
		final long default_account_id = mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
		if (default_account_id == -1 || !ArrayUtils.contains(activated_ids, default_account_id)) {
			if (activated_ids.length == 1) {
				mPreferences.edit().putLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, activated_ids[0]).commit();
				mIndicator.setPagingEnabled(true);
				mIsNavigateToDefaultAccount = false;
			} else if (activated_ids.length > 1) {
				mViewPager.setCurrentItem(TAB_POSITION_ME, false);
				mIndicator.setPagingEnabled(false);
				if (!mIsNavigateToDefaultAccount) {
					Toast.makeText(this, R.string.set_default_account_hint, Toast.LENGTH_LONG).show();
				}
				mIsNavigateToDefaultAccount = true;
				result = false;
			}
		} else {
			mIndicator.setPagingEnabled(true);
			mIsNavigateToDefaultAccount = false;
		}
		return result;
	}

	public boolean isDualPaneMode() {
		return findViewById(PANE_LEFT_CONTAINER) instanceof ViewGroup
				&& findViewById(PANE_RIGHT_CONTAINER) instanceof ViewGroup;
	}

	public boolean isDualPaneModeCompact() {
		final boolean is_dual_pane = isDualPaneMode();
		if (!is_dual_pane) return false;
		final View main_container = findViewById(R.id.main_container);
		return is_dual_pane && main_container != null && main_container instanceof FrameLayout;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
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
							final String where = Accounts.USER_ID + " = " + account_id;
							resolver.update(Accounts.CONTENT_URI, values, where, null);
						}
					}
					checkDefaultAccountSet();
				} else if (resultCode == RESULT_CANCELED) {
					if (getActivatedAccountIds(this).length <= 0) {
						finish();
					} else {
						checkDefaultAccountSet();
					}
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	public void onBackStackChanged() {
		if (!isDualPaneMode()) return;
		final FragmentManager fm = getSupportFragmentManager();
		final Fragment fragment = fm.findFragmentById(PANE_LEFT);
		final View main_view = findViewById(R.id.main);
		final boolean left_pane_used = fragment != null && fragment.isAdded();
		if (main_view != null) {
			main_view.setVisibility(left_pane_used ? View.GONE : View.VISIBLE);
		}
		setPagingEnabled(!left_pane_used);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.compose:
			case R.id.button_compose:
				startActivity(new Intent(INTENT_ACTION_COMPOSE));
				break;
		}

	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mService = getTwidereApplication().getServiceInterface();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		final boolean home_display_icon = getResources().getBoolean(R.bool.home_display_icon);
		final boolean tab_display_label = getResources().getBoolean(R.bool.tab_display_label);
		setContentView(R.layout.main);
		mViewPager = (ExtendedViewPager) findViewById(R.id.pager);
		mComposeButton = (ImageButton) findViewById(R.id.button_compose);
		if (isDualPaneModeCompact()) {
			mLeftPaneContainer = (ExtendedFrameLayout) findViewById(R.id.left_pane_container);
			mLeftPaneContainer.setTouchInterceptor(mLeftPaneTouchInterceptor);
			mLeftPaneLayer = (ViewGroup) findViewById(R.id.left_pane_layer);
			mRightPaneContainer = (ExtendedFrameLayout) findViewById(R.id.right_pane_container);
			mRightPaneContainer.setTouchInterceptor(mRightPaneTouchInterceptor);
			mRightPaneLayer = (ViewGroup) findViewById(R.id.right_pane_layer);
			bringLeftPaneToFront();
		}
		final long[] account_ids = getAccountIds(this);

		if (account_ids.length <= 0) {
			startActivity(new Intent(INTENT_ACTION_TWITTER_LOGIN));
			finish();
			return;
		}

		final boolean refresh_on_start = mPreferences.getBoolean(PREFERENCE_KEY_REFRESH_ON_START, false);

		final Bundle bundle = getIntent().getExtras();
		int initial_tab = -1;
		if (bundle != null) {
			final long[] refreshed_ids = bundle.getLongArray(INTENT_KEY_IDS);
			if (refreshed_ids != null && !refresh_on_start) {
				mService.getHomeTimeline(refreshed_ids, null);
				mService.getMentions(refreshed_ids, null);
			}
			initial_tab = bundle.getInt(INTENT_KEY_INITIAL_TAB, -1);
			switch (initial_tab) {
				case TAB_POSITION_HOME: {
					mService.clearNewNotificationCount(NOTIFICATION_ID_HOME_TIMELINE);
					break;
				}
				case TAB_POSITION_MENTIONS: {
					mService.clearNewNotificationCount(NOTIFICATION_ID_MENTIONS);
					break;
				}
			}
		}
		mActionBar = getSupportActionBar();
		mActionBar.setCustomView(R.layout.home_tabs);
		mActionBar.setDisplayShowTitleEnabled(false);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setDisplayShowHomeEnabled(home_display_icon);
		final View view = mActionBar.getCustomView();
		mProgress = (ProgressBar) view.findViewById(android.R.id.progress);
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		mAdapter = new TabsAdapter(this, getSupportFragmentManager());
		mAdapter.addTab(HomeTimelineFragment.class, null, tab_display_label ? getString(R.string.home) : null,
				R.drawable.ic_tab_home);
		mAdapter.addTab(MentionsFragment.class, null, tab_display_label ? getString(R.string.mentions) : null,
				R.drawable.ic_tab_connect);
		mAdapter.addTab(DiscoverFragment.class, null, tab_display_label ? getString(R.string.discover) : null,
				R.drawable.ic_tab_discover);
		mAdapter.addTab(AccountsFragment.class, null, tab_display_label ? getString(R.string.me) : null,
				R.drawable.ic_tab_me);
		mViewPager.setAdapter(mAdapter);
		mViewPager.setOffscreenPageLimit(3);
		mIndicator.setViewPager(mViewPager);
		getSupportFragmentManager().addOnBackStackChangedListener(this);

		final boolean remember_position = mPreferences.getBoolean(PREFERENCE_KEY_REMEMBER_POSITION, false);
		final long[] activated_ids = getActivatedAccountIds(this);
		if (activated_ids.length <= 0) {
			startActivityForResult(new Intent(INTENT_ACTION_SELECT_ACCOUNT), REQUEST_SELECT_ACCOUNT);
		} else if (checkDefaultAccountSet() && (remember_position || initial_tab > 0)) {
			final int position = initial_tab > 0 ? initial_tab : mPreferences.getInt(PREFERENCE_KEY_SAVED_TAB_POSITION,
					TAB_POSITION_HOME);
			if (position >= 0 || position < mViewPager.getChildCount()) {
				mViewPager.setCurrentItem(position);
			}
		}
		if (refresh_on_start) {
			mService.getHomeTimeline(activated_ids, null);
			mService.getMentions(activated_ids, null);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_home, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public void onDefaultAccountSet() {
		mIsNavigateToDefaultAccount = false;
	}

	@Override
	public void onDestroy() {
		// Delete unused items in databases.
		cleanDatabasesByItemLimit(this);
		super.onDestroy();
		if (mPreferences.getBoolean(PREFERENCE_KEY_STOP_SERVICE_AFTER_CLOSED, false)) {
			// What the f**k are you think about? Stop service causes twidere
			// slow and unstable!
			// Well, all right... If you still want to enable this option, I
			// have no responsibility for any problems occurred.
			mService.shutdownService();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				navigateToTop();
				break;
			}
			case MENU_COMPOSE: {
				startActivity(new Intent(INTENT_ACTION_COMPOSE));
				break;
			}
			case MENU_SEARCH: {
				onSearchRequested();
				break;
			}
			case MENU_DIRECT_MESSAGES: {
				startActivity(new Intent(INTENT_ACTION_DIRECT_MESSAGES));
				break;
			}
			case MENU_SELECT_ACCOUNT: {
				startActivityForResult(new Intent(INTENT_ACTION_SELECT_ACCOUNT), REQUEST_SELECT_ACCOUNT);
				break;
			}
			case MENU_SETTINGS: {
				startActivity(new Intent(INTENT_ACTION_SETTINGS));
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final boolean bottom_actions = mPreferences.getBoolean(PREFERENCE_KEY_COMPOSE_BUTTON, false);
		final boolean leftside_compose_button = mPreferences.getBoolean(PREFERENCE_KEY_LEFTSIDE_COMPOSE_BUTTON, false);
		final MenuItem composeItem = menu.findItem(MENU_COMPOSE);
		if (composeItem != null) {
			composeItem.setVisible(!bottom_actions);
		}
		if (mComposeButton != null) {
			mComposeButton.setVisibility(bottom_actions ? View.VISIBLE : View.GONE);
			if (bottom_actions) {
				final FrameLayout.LayoutParams compose_lp = (FrameLayout.LayoutParams) mComposeButton.getLayoutParams();
				compose_lp.gravity = Gravity.BOTTOM | (leftside_compose_button ? Gravity.LEFT : Gravity.RIGHT);
				mComposeButton.setLayoutParams(compose_lp);
			}
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onResume() {
		super.onResume();
		invalidateSupportOptionsMenu();
	}

	@Override
	public void onStart() {
		final FragmentManager fm = getSupportFragmentManager();
		if (!isDualPaneMode() && !FragmentManagerTrojan.isStateSaved(fm)) {
			// fm.popBackStackImmediate();
		}
		super.onStart();
		setSupportProgressBarIndeterminateVisibility(mProgressBarIndeterminateVisible);
		final IntentFilter filter = new IntentFilter(BROADCAST_REFRESHSTATE_CHANGED);
		registerReceiver(mStateReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStateReceiver);
		mPreferences.edit().putInt(PREFERENCE_KEY_SAVED_TAB_POSITION, mViewPager.getCurrentItem()).commit();
		super.onStop();
	}

	public void setPagingEnabled(boolean enabled) {
		if (mIndicator != null) {
			mIndicator.setPagingEnabled(enabled);
			mIndicator.setEnabled(enabled);
		}
	}

	@Override
	public void setSupportProgressBarIndeterminateVisibility(boolean visible) {
		mProgressBarIndeterminateVisible = visible;
		mProgress.setVisibility(visible || mService.hasActivatedTask() ? View.VISIBLE : View.INVISIBLE);
	}

	public void showAtPane(int pane, Fragment fragment, boolean addToBackStack) {
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		switch (pane) {
			case PANE_LEFT: {
				bringLeftPaneToFront();
				ft.replace(PANE_LEFT, fragment);
				break;
			}
			case PANE_RIGHT: {
				bringRightPaneToFront();
				ft.replace(PANE_RIGHT, fragment);
				break;
			}
		}
		if (addToBackStack) {
			ft.addToBackStack(null);
		}
		ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.commit();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		final Bundle bundle = intent.getExtras();
		if (bundle != null) {
			final long[] refreshed_ids = bundle.getLongArray(INTENT_KEY_IDS);
			if (refreshed_ids != null) {
				mService.getHomeTimeline(refreshed_ids, null);
				mService.getMentions(refreshed_ids, null);
			}
		}
		super.onNewIntent(intent);
	}

	@Override
	protected void onPostResume() {
		super.onPostResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void navigateToTop() {
		if (isDualPaneMode()) {
			getSupportFragmentManager().popBackStack();
		}
	}

}
