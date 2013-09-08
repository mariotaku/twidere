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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.matchLinkId;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BasePullToRefreshListFragment;
import org.mariotaku.twidere.fragment.DirectMessagesConversationFragment;
import org.mariotaku.twidere.fragment.IncomingFriendshipsFragment;
import org.mariotaku.twidere.fragment.SavedSearchesListFragment;
import org.mariotaku.twidere.fragment.SearchFragment;
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
import org.mariotaku.twidere.fragment.iface.RefreshScrollTopInterface;
import org.mariotaku.twidere.fragment.iface.SupportFragmentCallback;
import org.mariotaku.twidere.util.MultiSelectEventHandler;
import org.mariotaku.twidere.util.ParseUtils;

import android.app.ActionBar;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.widget.TextView;

public class LinkHandlerActivity extends TwidereSwipeBackActivity implements OnClickListener, OnLongClickListener {

	private MultiSelectEventHandler mMultiSelectHandler;

	private ActionBar mActionBar;

	private boolean mFinishOnly;

	private View mGoTopView;
	private TextView mTitleView, mSubtitleView;

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.go_top: {
				final Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
				if (fragment instanceof RefreshScrollTopInterface) {
					((RefreshScrollTopInterface) fragment).scrollToTop();
				} else if (fragment instanceof ListFragment) {
					((ListFragment) fragment).setSelection(0);
				}
				break;
			}
		}
	}

	@Override
	public boolean onLongClick(final View v) {
		switch (v.getId()) {
			case R.id.go_top: {
				final Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
				if (fragment instanceof RefreshScrollTopInterface) {
					((RefreshScrollTopInterface) fragment).triggerRefresh();
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				if (mFinishOnly) {
					finish();
				} else {
					NavUtils.navigateUpFromSameTask(this);
				}
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	public void setSubtitle(final CharSequence subtitle) {
		mSubtitleView.setText(subtitle);
	}

	@Override
	protected BasePullToRefreshListFragment getCurrentPullToRefreshFragment() {
		final Fragment fragment = getSupportFragmentManager().findFragmentById(android.R.id.content);
		if (fragment instanceof BasePullToRefreshListFragment)
			return (BasePullToRefreshListFragment) fragment;
		else if (fragment instanceof SupportFragmentCallback) {
			final Fragment curr = ((SupportFragmentCallback) fragment).getCurrentVisibleFragment();
			if (curr instanceof BasePullToRefreshListFragment) return (BasePullToRefreshListFragment) curr;
		}
		return null;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mMultiSelectHandler = new MultiSelectEventHandler(this);
		mMultiSelectHandler.dispatchOnCreate();
		super.onCreate(savedInstanceState);
		setOverrideExitAniamtion(false);
		mActionBar = getActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mActionBar.setDisplayShowTitleEnabled(true);
		mActionBar.setDisplayShowCustomEnabled(true);
		mActionBar.setCustomView(R.layout.link_handler_actionbar);
		final View view = mActionBar.getCustomView();
		mGoTopView = view.findViewById(R.id.go_top);
		mTitleView = (TextView) view.findViewById(R.id.actionbar_title);
		mSubtitleView = (TextView) view.findViewById(R.id.actionbar_subtitle);
		mGoTopView.setOnClickListener(this);
		mGoTopView.setOnLongClickListener(this);
		setProgressBarIndeterminateVisibility(false);
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		if (data == null || !showFragment(data)) {
			finish();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mMultiSelectHandler.dispatchOnStart();
	}

	@Override
	protected void onStop() {
		mMultiSelectHandler.dispatchOnStop();
		super.onStop();
	}

	@Override
	protected void onTitleChanged(final CharSequence title, final int color) {
		super.onTitleChanged(title, color);
		mTitleView.setText(title);
	}

	private boolean showFragment(final Uri uri) {
		final Bundle extras = getIntent().getExtras();
		Fragment fragment = null;
		if (uri != null) {
			final Bundle args = new Bundle();
			if (extras != null) {
				args.putAll(extras);
			}
			switch (matchLinkId(uri)) {
				case LINK_ID_STATUS: {
					setTitle(R.string.view_status);
					fragment = new StatusFragment();
					if (!args.containsKey(INTENT_KEY_STATUS_ID)) {
						final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
						args.putLong(INTENT_KEY_STATUS_ID, ParseUtils.parseLong(param_status_id));
					}
					break;
				}
				case LINK_ID_USER: {
					setTitle(R.string.view_user_profile);
					fragment = new UserProfileFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!args.containsKey(INTENT_KEY_SCREEN_NAME)) {
						args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!args.containsKey(INTENT_KEY_USER_ID)) {
						args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					}
					break;
				}
				case LINK_ID_USER_TIMELINE: {
					setTitle(R.string.tweets);
					fragment = new UserTimelineFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!args.containsKey(INTENT_KEY_SCREEN_NAME)) {
						args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!args.containsKey(INTENT_KEY_USER_ID)) {
						args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					}
					if (isEmpty(param_screen_name) && isEmpty(param_user_id)) {
						finish();
						return false;
					}
					break;
				}
				case LINK_ID_USER_FAVORITES: {
					setTitle(R.string.favorites);
					fragment = new UserFavoritesFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!args.containsKey(INTENT_KEY_SCREEN_NAME)) {
						args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!args.containsKey(INTENT_KEY_USER_ID)) {
						args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					}
					if (isEmpty(param_screen_name) && isEmpty(param_user_id)) {
						finish();
						return false;
					}
					break;
				}
				case LINK_ID_USER_FOLLOWERS: {
					setTitle(R.string.followers);
					fragment = new UserFollowersFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!args.containsKey(INTENT_KEY_SCREEN_NAME)) {
						args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!args.containsKey(INTENT_KEY_USER_ID)) {
						args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					}
					if (isEmpty(param_screen_name) && isEmpty(param_user_id)) {
						finish();
						return false;
					}
					break;
				}
				case LINK_ID_USER_FRIENDS: {
					setTitle(R.string.following);
					fragment = new UserFriendsFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!args.containsKey(INTENT_KEY_SCREEN_NAME)) {
						args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!args.containsKey(INTENT_KEY_USER_ID)) {
						args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					}
					if (isEmpty(param_screen_name) && isEmpty(param_user_id)) {
						finish();
						return false;
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
					final long conversation_id = ParseUtils.parseLong(param_conversation_id);
					if (conversation_id > 0) {
						args.putLong(INTENT_KEY_CONVERSATION_ID, conversation_id);
					} else if (param_screen_name != null) {
						args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
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
					args.putInt(INTENT_KEY_LIST_ID, ParseUtils.parseInt(param_list_id));
					args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					args.putString(INTENT_KEY_LIST_NAME, param_list_name);
					break;
				}
				case LINK_ID_LISTS: {
					setTitle(R.string.user_list);
					fragment = new UserListsListFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!args.containsKey(INTENT_KEY_SCREEN_NAME)) {
						args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!args.containsKey(INTENT_KEY_USER_ID)) {
						args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					}
					if (isEmpty(param_screen_name) && isEmpty(param_user_id)) {
						finish();
						return false;
					}
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
					args.putInt(INTENT_KEY_LIST_ID, ParseUtils.parseInt(param_list_id));
					args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					args.putString(INTENT_KEY_LIST_NAME, param_list_name);
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
					args.putInt(INTENT_KEY_LIST_ID, ParseUtils.parseInt(param_list_id));
					args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					args.putString(INTENT_KEY_LIST_NAME, param_list_name);
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
					args.putInt(INTENT_KEY_LIST_ID, ParseUtils.parseInt(param_list_id));
					args.putLong(INTENT_KEY_USER_ID, ParseUtils.parseLong(param_user_id));
					args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					args.putString(INTENT_KEY_LIST_NAME, param_list_name);
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
					if (!args.containsKey(INTENT_KEY_SCREEN_NAME) && !isEmpty(param_screen_name)) {
						args.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (isEmpty(args.getString(INTENT_KEY_SCREEN_NAME))) {
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
				case LINK_ID_STATUS_RETWEETERS: {
					setTitle(R.string.users_retweeted_this);
					fragment = new StatusRetweetersListFragment();
					if (!args.containsKey(INTENT_KEY_STATUS_ID)) {
						final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
						args.putLong(INTENT_KEY_STATUS_ID, ParseUtils.parseLong(param_status_id));
					}
					break;
				}
				case LINK_ID_SEARCH: {
					setTitle(android.R.string.search_go);
					final String param_query = uri.getQueryParameter(QUERY_PARAM_QUERY);
					if (isEmpty(param_query)) {
						finish();
						return false;
					}
					args.putString(INTENT_KEY_QUERY, param_query);
					setSubtitle(param_query);
					fragment = new SearchFragment();
					break;
				}
				default: {
					finish();
					return false;
				}
			}
			final String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
			mFinishOnly = Boolean.parseBoolean(uri.getQueryParameter(QUERY_PARAM_FINISH_ONLY));
			if (param_account_id != null) {
				args.putLong(INTENT_KEY_ACCOUNT_ID, ParseUtils.parseLong(param_account_id));
			} else {
				final String param_account_name = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_NAME);
				if (param_account_name != null) {
					args.putLong(INTENT_KEY_ACCOUNT_ID, getAccountId(this, param_account_name));
				} else {
					final long account_id = getDefaultAccountId(this);
					if (isMyAccount(this, account_id)) {
						args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					} else {
						// finish();
						// return false;
					}
				}
			}
			if (fragment != null) {
				fragment.setArguments(args);
			}
		}
		if (fragment != null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, fragment);
			ft.commit();
			return true;
		}
		return false;
	}

}
