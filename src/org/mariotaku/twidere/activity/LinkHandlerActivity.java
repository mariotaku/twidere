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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.DirectMessagesConversationFragment;
import org.mariotaku.twidere.fragment.IncomingFriendshipsFragment;
import org.mariotaku.twidere.fragment.SavedSearchesListFragment;
import org.mariotaku.twidere.fragment.StatusFragment;
import org.mariotaku.twidere.fragment.StatusRetweetersListFragment;
import org.mariotaku.twidere.fragment.StatusesListFragment;
import org.mariotaku.twidere.fragment.UserBlocksListFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserFollowersFragment;
import org.mariotaku.twidere.fragment.UserFriendsFragment;
import org.mariotaku.twidere.fragment.UserListDetailsFragment;
import org.mariotaku.twidere.fragment.UserListMembersFragment;
import org.mariotaku.twidere.fragment.UserListSubscribersFragment;
import org.mariotaku.twidere.fragment.UserListTimelineFragment;
import org.mariotaku.twidere.fragment.UserListsListFragment;
import org.mariotaku.twidere.fragment.UserMentionsFragment;
import org.mariotaku.twidere.fragment.UserProfileFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.fragment.UsersListFragment;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.matchLinkId;
import static org.mariotaku.twidere.util.Utils.parseInt;
import static org.mariotaku.twidere.util.Utils.parseLong;

public class LinkHandlerActivity extends MultiSelectActivity {

	private Fragment mFragment;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		setSupportProgressBarIndeterminateVisibility(false);
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		if (data != null) {
			if (setFragment(data)) {
				if (mFragment != null) {
					if (isDualPaneMode()) {
						showFragment(mFragment, true);
					} else {
						final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
						ft.replace(R.id.main, mFragment);
						ft.commit();
					}
					return;
				} else {
					finish();
				}
			}
		} else {
			finish();
		}
	}

	/**
	 * Base action bar-aware implementation for
	 * {@link Activity#onCreateOptionsMenu(android.view.Menu)}.
	 * 
	 * Note: marking menu items as invisible/visible is not currently supported.
	 */
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		super.onCreateOptionsMenu(menu);
		if (mAttachedFragment != null) {
			mAttachedFragment.onCreateOptionsMenu(menu, getMenuInflater());
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				final FragmentManager fm = getSupportFragmentManager();
				if (isDualPaneMode()) {
					final int count = fm.getBackStackEntryCount();
					if (count == 0) {
						NavUtils.navigateUpFromSameTask(this);
						// onBackPressed();
					} else if (!FragmentManagerTrojan.isStateSaved(fm)) {
						for (int i = 0; i < count; i++) {
							fm.popBackStackImmediate();
						}
						setSupportProgressBarIndeterminateVisibility(false);
					}
				} else {
					NavUtils.navigateUpFromSameTask(this);
					// onBackPressed();
				}
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected int getDarkThemeRes() {
		return R.style.Theme_Twidere_DialogWhenLarge;
	}

	// This simply disables dual pane layout.
	@Override
	protected int getDualPaneLayoutRes() {
		return getNormalLayoutRes();
	}

	@Override
	protected int getLightThemeRes() {
		return R.style.Theme_Twidere_Light_DialogWhenLarge;
	}

	@Override
	protected int getNormalLayoutRes() {
		return R.layout.link_handler;
	}

	@Override
	protected void onStart() {
		if (isDualPaneMode() && mFragment != null) {
			final FragmentManager fm = getSupportFragmentManager();
			final Fragment f = fm.findFragmentById(R.id.main);
			if (f != null) {
				final FragmentTransaction ft = fm.beginTransaction();
				ft.remove(f);
				ft.commit();
			}
		}
		super.onStart();
	}

	@Override
	protected boolean shouldDisableDialogWhenLargeMode() {
		return false;
	}

	private boolean setFragment(final Uri uri) {
		final Bundle extras = getIntent().getExtras();
		Fragment fragment = null;
		if (uri != null) {
			final Bundle bundle = new Bundle();
			if (extras != null) {
				bundle.putAll(extras);
			}
			switch (matchLinkId(uri)) {
				case LINK_ID_STATUS: {
					setTitle(R.string.view_status);
					fragment = new StatusFragment();
					final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					break;
				}
				case LINK_ID_USER: {
					setTitle(R.string.view_user_profile);
					fragment = new UserProfileFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case LINK_ID_USER_TIMELINE: {
					setTitle(R.string.tweets);
					fragment = new UserTimelineFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case LINK_ID_USER_FAVORITES: {
					setTitle(R.string.favorites);
					fragment = new UserFavoritesFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case LINK_ID_USER_FOLLOWERS: {
					setTitle(R.string.followers);
					fragment = new UserFollowersFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case LINK_ID_USER_FRIENDS: {
					setTitle(R.string.following);
					fragment = new UserFriendsFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case LINK_ID_USER_BLOCKS: {
					setTitle(R.string.blocked_users);
					fragment = new UserBlocksListFragment();
					break;
				}
				case LINK_ID_DIRECT_MESSAGES_CONVERSATION: {
					setTitle(R.string.direct_messages);
					fragment = new DirectMessagesConversationFragment();
					final String param_conversation_id = uri.getQueryParameter(QUERY_PARAM_CONVERSATION_ID);
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final long conversation_id = parseLong(param_conversation_id);
					if (conversation_id > 0) {
						bundle.putLong(INTENT_KEY_CONVERSATION_ID, conversation_id);
					} else if (param_screen_name != null) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					break;
				}
				case LINK_ID_LIST_DETAILS: {
					setTitle(R.string.user_list);
					fragment = new UserListDetailsFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					final String param_list_id = uri.getQueryParameter(QUERY_PARAM_LIST_ID);
					final String param_list_name = uri.getQueryParameter(QUERY_PARAM_LIST_NAME);
					if (isEmpty(param_list_id)
							&& (isEmpty(param_list_name) || isEmpty(param_screen_name) && isEmpty(param_user_id))) {
						finish();
						return false;
					}
					bundle.putInt(INTENT_KEY_LIST_ID, parseInt(param_list_id));
					bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					bundle.putString(INTENT_KEY_LIST_NAME, param_list_name);
					break;
				}
				case LINK_ID_LISTS: {
					setTitle(R.string.user_list);
					fragment = new UserListsListFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (isEmpty(param_screen_name) && isEmpty(param_user_id)) {
						finish();
						return false;
					}
					bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					break;
				}
				case LINK_ID_LIST_TIMELINE: {
					setTitle(R.string.list_timeline);
					fragment = new UserListTimelineFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					final String param_list_id = uri.getQueryParameter(QUERY_PARAM_LIST_ID);
					final String param_list_name = uri.getQueryParameter(QUERY_PARAM_LIST_NAME);
					if (isEmpty(param_list_id)
							&& (isEmpty(param_list_name) || isEmpty(param_screen_name) && isEmpty(param_user_id))) {
						finish();
						return false;
					}
					bundle.putInt(INTENT_KEY_LIST_ID, parseInt(param_list_id));
					bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					bundle.putString(INTENT_KEY_LIST_NAME, param_list_name);
					break;
				}
				case LINK_ID_LIST_MEMBERS: {
					setTitle(R.string.list_members);
					fragment = new UserListMembersFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					final String param_list_id = uri.getQueryParameter(QUERY_PARAM_LIST_ID);
					final String param_list_name = uri.getQueryParameter(QUERY_PARAM_LIST_NAME);
					if (isEmpty(param_list_id)
							&& (isEmpty(param_list_name) || isEmpty(param_screen_name) && isEmpty(param_user_id))) {
						finish();
						return false;
					}
					bundle.putInt(INTENT_KEY_LIST_ID, parseInt(param_list_id));
					bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					bundle.putString(INTENT_KEY_LIST_NAME, param_list_name);
					break;
				}
				case LINK_ID_LIST_SUBSCRIBERS: {
					setTitle(R.string.list_subscribers);
					fragment = new UserListSubscribersFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					final String param_list_id = uri.getQueryParameter(QUERY_PARAM_LIST_ID);
					final String param_list_name = uri.getQueryParameter(QUERY_PARAM_LIST_NAME);
					if (isEmpty(param_list_id)
							&& (isEmpty(param_list_name) || isEmpty(param_screen_name) && isEmpty(param_user_id))) {
						finish();
						return false;
					}
					bundle.putInt(INTENT_KEY_LIST_ID, parseInt(param_list_id));
					bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					bundle.putString(INTENT_KEY_LIST_NAME, param_list_name);
					break;
				}
				case LINK_ID_SAVED_SEARCHES: {
					setTitle(R.string.saved_searches);
					fragment = new SavedSearchesListFragment();
					break;
				}
				case LINK_ID_USER_MENTIONS: {
					setTitle(R.string.user_mentions);
					fragment = new UserMentionsFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					if (!isEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					} else {
						finish();
						return false;
					}
					break;
				}
				case LINK_ID_INCOMING_FRIENDSHIPS: {
					setTitle(R.string.incoming_friendships);
					fragment = new IncomingFriendshipsFragment();
					break;
				}
				case LINK_ID_USERS: {
					fragment = new UsersListFragment();
					break;
				}
				case LINK_ID_STATUSES: {
					fragment = new StatusesListFragment();
					break;
				}
				case LINK_ID_RETWEETERS: {
					fragment = new StatusRetweetersListFragment();
					break;
				}
				default: {
					break;
				}
			}
			final String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
			if (param_account_id != null) {
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
			} else {
				final String param_account_name = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_NAME);
				if (param_account_name != null) {
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, getAccountId(this, param_account_name));
				} else {
					final long account_id = getDefaultAccountId(this);
					if (isMyAccount(this, account_id)) {
						bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					} else {
						//finish();
						//return false;
					}
				}
			}
			if (fragment != null) {
				fragment.setArguments(bundle);
			}
		}
		mFragment = fragment;
		return true;
	}

}
