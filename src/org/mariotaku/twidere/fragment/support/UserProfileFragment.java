/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.support;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ContentValuesCreator.makeFilterdUserContentValues;
import static org.mariotaku.twidere.util.ParseUtils.parseLong;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.setUserColor;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getErrorMessage;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getOriginalTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openIncomingFriendships;
import static org.mariotaku.twidere.util.Utils.openSavedSearches;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserBlocks;
import static org.mariotaku.twidere.util.Utils.openUserFavorites;
import static org.mariotaku.twidere.util.Utils.openUserFollowers;
import static org.mariotaku.twidere.util.Utils.openUserFriends;
import static org.mariotaku.twidere.util.Utils.openUserListMemberships;
import static org.mariotaku.twidere.util.Utils.openUserLists;
import static org.mariotaku.twidere.util.Utils.openUserMentions;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.openUserTimeline;
import static org.mariotaku.twidere.util.Utils.setMenuItemAvailability;
import static org.mariotaku.twidere.util.Utils.showInfoMessage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.querybuilder.Where;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.support.LinkHandlerActivity;
import org.mariotaku.twidere.activity.support.UserListSelectorActivity;
import org.mariotaku.twidere.activity.support.UserProfileEditorActivity;
import org.mariotaku.twidere.adapter.ListActionAdapter;
import org.mariotaku.twidere.loader.support.ParcelableUserLoader;
import org.mariotaku.twidere.model.ListAction;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.FlymeUtils;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;
import org.mariotaku.twidere.view.ColorLabelLinearLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout;
import org.mariotaku.twidere.view.ProfileImageView;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.util.Locale;

public class UserProfileFragment extends BaseSupportListFragment implements OnClickListener, OnItemClickListener,
		OnItemLongClickListener, OnMenuItemClickListener, OnLinkClickListener, Panes.Right, OnSizeChangedListener,
		OnSharedPreferenceChangeListener, OnTouchListener {

	private static final int LOADER_ID_USER = 1;
	private static final int LOADER_ID_FRIENDSHIP = 2;

	private ImageLoaderWrapper mProfileImageLoader;
	private SharedPreferences mPreferences;

	private ProfileImageView mProfileImageView;
	private ImageView mProfileBannerView;
	private TextView mNameView, mScreenNameView, mDescriptionView, mLocationView, mURLView, mCreatedAtView,
			mTweetCount, mFollowersCount, mFriendsCount, mErrorMessageView;
	private View mDescriptionContainer, mLocationContainer, mURLContainer, mTweetsContainer, mFollowersContainer,
			mFriendsContainer;
	private Button mRetryButton;
	private ColorLabelLinearLayout mProfileNameContainer;
	private ListView mListView;
	private View mHeaderView;
	private View mErrorRetryContainer;
	private View mFollowingYouIndicator;
	private View mMainContent;
	private View mProfileBannerSpace;
	private ProgressBar mDetailsLoadProgress;
	private MenuBar mMenuBar;

	private ListActionAdapter mAdapter;

	private Relationship mFriendship;
	private ParcelableUser mUser = null;
	private Locale mLocale;

	private boolean mGetUserInfoLoaderInitialized, mGetFriendShipLoaderInitialized;

	private int mBannerWidth;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			if (mUser == null) return;
			final String action = intent.getAction();
			if (BROADCAST_FRIENDSHIP_CHANGED.equals(action)) {
				if (intent.getLongExtra(EXTRA_USER_ID, -1) == mUser.id && intent.getBooleanExtra(EXTRA_SUCCEED, false)) {
					getFriendship();
				}
			} else if (BROADCAST_BLOCKSTATE_CHANGED.equals(action)) {
				if (intent.getLongExtra(EXTRA_USER_ID, -1) == mUser.id && intent.getBooleanExtra(EXTRA_SUCCEED, false)) {
					getFriendship();
				}
			} else if (BROADCAST_PROFILE_UPDATED.equals(action) || BROADCAST_PROFILE_IMAGE_UPDATED.equals(action)
					|| BROADCAST_PROFILE_BANNER_UPDATED.equals(action)) {
				if (intent.getLongExtra(EXTRA_USER_ID, -1) == mUser.id && intent.getBooleanExtra(EXTRA_SUCCEED, false)) {
					getUserInfo(true);
				}
			} else if (BROADCAST_TASK_STATE_CHANGED.equals(action)) {
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				final boolean is_creating_friendship = twitter != null
						&& twitter.isCreatingFriendship(mUser.account_id, mUser.id);
				final boolean is_destroying_friendship = twitter != null
						&& twitter.isDestroyingFriendship(mUser.account_id, mUser.id);
				setProgressBarIndeterminateVisibility(is_creating_friendship || is_destroying_friendship);
				invalidateOptionsMenu();
			}
		}
	};

	private final LoaderCallbacks<SingleResponse<ParcelableUser>> mUserInfoLoaderCallbacks = new LoaderCallbacks<SingleResponse<ParcelableUser>>() {

		@Override
		public Loader<SingleResponse<ParcelableUser>> onCreateLoader(final int id, final Bundle args) {
			if (mUser == null) {
				mMainContent.setVisibility(View.GONE);
				mErrorRetryContainer.setVisibility(View.GONE);
				mDetailsLoadProgress.setVisibility(View.VISIBLE);
				mErrorMessageView.setText(null);
				mErrorMessageView.setVisibility(View.GONE);
			}
			setProgressBarIndeterminateVisibility(true);
			final ParcelableUser user = mUser;
			final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
			final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
			final long userId = args.getLong(EXTRA_USER_ID, -1);
			final String screenName = args.getString(EXTRA_SCREEN_NAME);
			return new ParcelableUserLoader(getActivity(), accountId, userId, screenName, getArguments(),
					omitIntentExtra, user == null || !user.is_cache && userId != user.id);
		}

		@Override
		public void onLoaderReset(final Loader<SingleResponse<ParcelableUser>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<SingleResponse<ParcelableUser>> loader,
				final SingleResponse<ParcelableUser> data) {
			if (getActivity() == null) return;
			if (data.data != null && data.data.id > 0) {
				final ParcelableUser user = data.data;
				displayUser(user);
				mMainContent.setVisibility(View.VISIBLE);
				mErrorRetryContainer.setVisibility(View.GONE);
				mDetailsLoadProgress.setVisibility(View.GONE);
				if (user.is_cache) {
					final Bundle args = new Bundle();
					args.putLong(EXTRA_ACCOUNT_ID, user.account_id);
					args.putLong(EXTRA_USER_ID, user.id);
					args.putString(EXTRA_SCREEN_NAME, user.screen_name);
					args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
					getLoaderManager().restartLoader(LOADER_ID_USER, args, this);
				}
			} else if (mUser != null && mUser.is_cache) {
				mMainContent.setVisibility(View.VISIBLE);
				mErrorRetryContainer.setVisibility(View.GONE);
				mDetailsLoadProgress.setVisibility(View.GONE);
				displayUser(mUser);
			} else {
				if (data.exception != null) {
					mErrorMessageView.setText(getErrorMessage(getActivity(), data.exception));
					mErrorMessageView.setVisibility(View.VISIBLE);
				}
				mMainContent.setVisibility(View.GONE);
				mErrorRetryContainer.setVisibility(View.VISIBLE);
			}
			setProgressBarIndeterminateVisibility(false);
		}

	};

	private final LoaderCallbacks<SingleResponse<Relationship>> mFriendshipLoaderCallbacks = new LoaderCallbacks<SingleResponse<Relationship>>() {

		@Override
		public Loader<SingleResponse<Relationship>> onCreateLoader(final int id, final Bundle args) {
			invalidateOptionsMenu();
			final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
			final long userId = args.getLong(EXTRA_USER_ID, -1);
			return new FriendshipLoader(getActivity(), accountId, userId);
		}

		@Override
		public void onLoaderReset(final Loader<SingleResponse<Relationship>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<SingleResponse<Relationship>> loader,
				final SingleResponse<Relationship> data) {
			mFriendship = null;
			final ParcelableUser user = mUser;
			final Relationship relationship = mFriendship = data.data;
			if (user == null) return;
			invalidateOptionsMenu();
			setMenu(mMenuBar.getMenu());
			mMenuBar.show();
			if (relationship != null) {
				final boolean isMyself = user.account_id == user.id;
				final boolean isFollowingYou = relationship.isTargetFollowingSource();
				mFollowingYouIndicator.setVisibility(!isMyself && isFollowingYou ? View.VISIBLE : View.GONE);
				final ContentResolver resolver = getContentResolver();
				final String where = Where.equals(CachedUsers.USER_ID, user.id).getSQL();
				resolver.delete(CachedUsers.CONTENT_URI, where, null);
				// I bet you don't want to see blocked user in your auto
				// complete list.
				if (!data.data.isSourceBlockingTarget()) {
					final ContentValues cachedValues = ParcelableUser.makeCachedUserContentValues(user);
					if (cachedValues != null) {
						resolver.insert(CachedUsers.CONTENT_URI, cachedValues);
					}
				}
			} else {
				mFollowingYouIndicator.setVisibility(View.GONE);
			}
		}

	};

	public void displayUser(final ParcelableUser user) {
		mFriendship = null;
		mUser = null;
		mAdapter.clear();
		if (user == null || user.id <= 0 || getActivity() == null) return;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_USER);
		lm.destroyLoader(LOADER_ID_FRIENDSHIP);
		final boolean userIsMe = user.account_id == user.id;
		mErrorRetryContainer.setVisibility(View.GONE);
		mUser = user;
		mProfileNameContainer.drawStart(getUserColor(getActivity(), user.id, true));
		mProfileNameContainer.drawEnd(getAccountColor(getActivity(), user.account_id));
		final String nick = getUserNickname(getActivity(), user.id, true);
		mNameView
				.setText(TextUtils.isEmpty(nick) ? user.name : getString(R.string.name_with_nickname, user.name, nick));
		mProfileImageView.setUserType(user.is_verified, user.is_protected);
		mScreenNameView.setText("@" + user.screen_name);
		mDescriptionContainer.setVisibility(userIsMe || !isEmpty(user.description_html) ? View.VISIBLE : View.GONE);
		mDescriptionView.setText(user.description_html != null ? Html.fromHtml(user.description_html) : null);
		final TwidereLinkify linkify = new TwidereLinkify(this);
		linkify.setLinkTextColor(ThemeUtils.getUserLinkTextColor(getActivity()));
		linkify.applyAllLinks(mDescriptionView, user.account_id, false);
		mDescriptionView.setMovementMethod(null);
		mLocationContainer.setVisibility(userIsMe || !isEmpty(user.location) ? View.VISIBLE : View.GONE);
		mLocationView.setText(user.location);
		mURLContainer.setVisibility(userIsMe || !isEmpty(user.url) || !isEmpty(user.url_expanded) ? View.VISIBLE
				: View.GONE);
		mURLView.setText(isEmpty(user.url_expanded) ? user.url : user.url_expanded);
		mURLView.setMovementMethod(null);
		final String created_at = formatToLongTimeString(getActivity(), user.created_at);
		final double total_created_days = (System.currentTimeMillis() - user.created_at) / 1000 / 60 / 60 / 24;
		final long daily_tweets = Math.round(user.statuses_count / Math.max(1, total_created_days));
		mCreatedAtView.setText(getResources().getQuantityString(R.plurals.daily_statuses_count, created_at, daily_tweets, daily_tweets));
		mTweetCount.setText(getLocalizedNumber(mLocale, user.statuses_count));
		mFollowersCount.setText(getLocalizedNumber(mLocale, user.followers_count));
		mFriendsCount.setText(getLocalizedNumber(mLocale, user.friends_count));
		if (mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)) {
			mProfileImageLoader.displayProfileImage(mProfileImageView,
					getOriginalTwitterProfileImage(user.profile_image_url));
			final int def_width = getResources().getDisplayMetrics().widthPixels;
			final int width = mBannerWidth > 0 ? mBannerWidth : def_width;
			mProfileBannerView.setImageBitmap(null);
			mProfileImageLoader.displayProfileBanner(mProfileBannerView, user.profile_banner_url, width);
		} else {
			mProfileImageView.setImageResource(R.drawable.ic_profile_image_default);
			mProfileBannerView.setImageResource(android.R.color.transparent);
		}
		if (isMyAccount(getActivity(), user.id)) {
			final ContentResolver resolver = getContentResolver();
			final ContentValues values = new ContentValues();
			values.put(Accounts.NAME, user.name);
			values.put(Accounts.SCREEN_NAME, user.screen_name);
			values.put(Accounts.PROFILE_IMAGE_URL, user.profile_image_url);
			values.put(Accounts.PROFILE_BANNER_URL, user.profile_banner_url);
			final String where = Accounts.ACCOUNT_ID + " = " + user.id;
			resolver.update(Accounts.CONTENT_URI, values, where, null);
		}
		mAdapter.add(new FavoritesAction(1));
		mAdapter.add(new UserMentionsAction(2));
		mAdapter.add(new UserListsAction(3));
		mAdapter.add(new UserListMembershipsAction(4));
		if (userIsMe) {
			mAdapter.add(new SavedSearchesAction(11));
			if (user.is_protected) {
				mAdapter.add(new IncomingFriendshipsAction(12));
			}
			mAdapter.add(new UserBlocksAction(13));
		}
		mAdapter.notifyDataSetChanged();
		if (!user.is_cache) {
			getFriendship();
		}
		invalidateOptionsMenu();
		setMenu(mMenuBar.getMenu());
		mMenuBar.show();
	}

	public void getUserInfo(final long accountId, final long userId, final String screenName,
			final boolean omitIntentExtra) {
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_USER);
		lm.destroyLoader(LOADER_ID_FRIENDSHIP);
		if (!isMyAccount(getActivity(), accountId)) {
			mMainContent.setVisibility(View.GONE);
			mErrorRetryContainer.setVisibility(View.GONE);
			return;
		}
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ACCOUNT_ID, accountId);
		args.putLong(EXTRA_USER_ID, userId);
		args.putString(EXTRA_SCREEN_NAME, screenName);
		args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omitIntentExtra);
		if (!mGetUserInfoLoaderInitialized) {
			lm.initLoader(LOADER_ID_USER, args, mUserInfoLoaderCallbacks);
			mGetUserInfoLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_USER, args, mUserInfoLoaderCallbacks);
		}
		if (accountId == -1 || userId == -1 && screenName == null) {
			mMainContent.setVisibility(View.GONE);
			mErrorRetryContainer.setVisibility(View.GONE);
			return;
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(this);
		getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(this);
		mLocale = getResources().getConfiguration().locale;
		final Bundle args = getArguments();
		long account_id = -1, user_id = -1;
		String screen_name = null;
		if (args != null) {
			if (savedInstanceState != null) {
				args.putAll(savedInstanceState);
			}
			account_id = args.getLong(EXTRA_ACCOUNT_ID, -1);
			user_id = args.getLong(EXTRA_USER_ID, -1);
			screen_name = args.getString(EXTRA_SCREEN_NAME);
		}
		mProfileImageLoader = getApplication().getImageLoaderWrapper();
		mAdapter = new ListActionAdapter(getActivity());
		mProfileImageView.setOnClickListener(this);
		mProfileBannerView.setOnClickListener(this);
		mTweetsContainer.setOnClickListener(this);
		mFollowersContainer.setOnClickListener(this);
		mFriendsContainer.setOnClickListener(this);
		mRetryButton.setOnClickListener(this);
		setListAdapter(null);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		mMenuBar.setVisibility(shouldUseNativeMenu() ? View.GONE : View.VISIBLE);
		mMenuBar.inflate(R.menu.menu_user_profile);
		mMenuBar.setIsBottomBar(true);
		mMenuBar.setOnMenuItemClickListener(this);

		mProfileBannerSpace.setOnTouchListener(this);

		setListAdapter(mAdapter);
		getUserInfo(account_id, user_id, screen_name, false);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		final ParcelableUser user = mUser;
		switch (requestCode) {
			case REQUEST_SET_COLOR: {
				if (user == null) return;
				if (resultCode == Activity.RESULT_OK) {
					if (data == null) return;
					final int color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT);
					setUserColor(getActivity(), mUser.id, color);
				} else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
					clearUserColor(getActivity(), mUser.id);
				}
				break;
			}
			case REQUEST_ADD_TO_LIST: {
				if (user == null) return;
				if (resultCode == Activity.RESULT_OK && data != null) {
					final AsyncTwitterWrapper twitter = getTwitterWrapper();
					final ParcelableUserList list = data.getParcelableExtra(EXTRA_USER_LIST);
					if (list == null || twitter == null) return;
					twitter.addUserListMembersAsync(user.account_id, list.id, user);
				}
				break;
			}
			case REQUEST_SELECT_ACCOUNT: {
				if (user == null) return;
				if (resultCode == Activity.RESULT_OK) {
					if (data == null || !data.hasExtra(EXTRA_ID)) return;
					final long accountId = data.getLongExtra(EXTRA_ID, -1);
					openUserProfile(getActivity(), accountId, user.id, null);
				}
				break;
			}
		}

	}

	@Override
	public void onClick(final View view) {
		final FragmentActivity activity = getActivity();
		final ParcelableUser user = mUser;
		if (activity == null || user == null) return;
		switch (view.getId()) {
			case R.id.retry: {
				getUserInfo(true);
				break;
			}
			case R.id.profile_image: {
				final String profile_image_url_string = getOriginalTwitterProfileImage(mUser.profile_image_url);
				openImage(activity, profile_image_url_string, false);
				break;
			}
			case R.id.profile_banner:
			case R.id.profile_banner_space: {
				final String profile_banner_url = mUser.profile_banner_url;
				if (profile_banner_url == null) return;
				openImage(getActivity(), profile_banner_url + "/ipad_retina", false);
				break;
			}
			case R.id.tweets_container: {
				if (mUser == null) return;
				openUserTimeline(getActivity(), user.account_id, user.id, user.screen_name);
				break;
			}
			case R.id.followers_container: {
				if (mUser == null) return;
				openUserFollowers(getActivity(), user.account_id, user.id, user.screen_name);
				break;
			}
			case R.id.friends_container: {
				if (mUser == null) return;
				openUserFriends(getActivity(), user.account_id, user.id, user.screen_name);
				break;
			}
			case R.id.name_container: {
				if (user.account_id != user.id) return;
				startActivity(new Intent(getActivity(), UserProfileEditorActivity.class));
				break;
			}
		}

	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		if (!shouldUseNativeMenu()) return;
		inflater.inflate(R.menu.menu_user_profile, menu);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_details_page, null, false);
		final ExtendedFrameLayout detailsContainer = (ExtendedFrameLayout) view.findViewById(R.id.details_container);
		inflater.inflate(R.layout.header_user_profile_banner, detailsContainer, true);
		detailsContainer.addView(super.onCreateView(inflater, container, savedInstanceState));
		mHeaderView = inflater.inflate(R.layout.header_user_profile, null, false);
		return view;
	}

	@Override
	public void onDestroyView() {
		mUser = null;
		mFriendship = null;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_USER);
		lm.destroyLoader(LOADER_ID_FRIENDSHIP);
		super.onDestroyView();
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final ListAction action = mAdapter.findItem(id);
		if (action != null) {
			action.onClick();
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final ListAction action = mAdapter.findItem(id);
		if (action != null) return action.onLongClick();
		return false;
	}

	@Override
	public void onLinkClick(final String link, final String orig, final long account_id, final int type,
			final boolean sensitive) {
		final ParcelableUser user = mUser;
		if (user == null) return;
		switch (type) {
			case TwidereLinkify.LINK_TYPE_MENTION: {
				openUserProfile(getActivity(), user.account_id, -1, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_HASHTAG: {
				openTweetSearch(getActivity(), user.account_id, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LINK: {
				final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
				startActivity(intent);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LIST: {
				final String[] mention_list = link.split("\\/");
				if (mention_list == null || mention_list.length != 2) {
					break;
				}
				break;
			}
			case TwidereLinkify.LINK_TYPE_STATUS: {
				openStatus(getActivity(), account_id, parseLong(link));
				break;
			}
		}
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		return handleMenuItemClick(item);

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		return handleMenuItemClick(item);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		if (!shouldUseNativeMenu() || !menu.hasVisibleItems()) return;
		setMenu(menu);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putParcelable(EXTRA_USER, mUser);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
			final int totalItemCount) {
		super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
		final View headerView = mHeaderView, profileBannerView = mProfileBannerView;
		if (headerView == null || profileBannerView == null) return;
		final float factor = -headerView.getTop() / (headerView.getWidth() * 0.5f);
		profileBannerView.setAlpha(1.0f - factor);
		profileBannerView.setTranslationY(headerView.getTop() / 2);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (mUser == null || !ParseUtils.parseString(mUser.id).equals(key)) return;
		displayUser(mUser);
	}

	@Override
	public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
		mBannerWidth = w;
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_TASK_STATE_CHANGED);
		filter.addAction(BROADCAST_FRIENDSHIP_CHANGED);
		filter.addAction(BROADCAST_BLOCKSTATE_CHANGED);
		filter.addAction(BROADCAST_PROFILE_UPDATED);
		filter.addAction(BROADCAST_PROFILE_IMAGE_UPDATED);
		filter.addAction(BROADCAST_PROFILE_BANNER_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event) {
		return mProfileBannerView.dispatchTouchEvent(event);
	}

	@Override
	public void onViewCreated(final View view, final Bundle savedInstanceState) {
		final Context context = view.getContext();
		super.onViewCreated(view, savedInstanceState);
		mMainContent = view.findViewById(R.id.content);
		mDetailsLoadProgress = (ProgressBar) view.findViewById(R.id.details_load_progress);
		mMenuBar = (MenuBar) view.findViewById(R.id.menu_bar);
		mErrorRetryContainer = view.findViewById(R.id.error_retry_container);
		mRetryButton = (Button) view.findViewById(R.id.retry);
		mErrorMessageView = (TextView) view.findViewById(R.id.error_message);
		mProfileBannerView = (ImageView) view.findViewById(R.id.profile_banner);
		mNameView = (TextView) mHeaderView.findViewById(R.id.name);
		mScreenNameView = (TextView) mHeaderView.findViewById(R.id.screen_name);
		mDescriptionView = (TextView) mHeaderView.findViewById(R.id.description);
		mLocationView = (TextView) mHeaderView.findViewById(R.id.location);
		mURLView = (TextView) mHeaderView.findViewById(R.id.url);
		mCreatedAtView = (TextView) mHeaderView.findViewById(R.id.created_at);
		mTweetsContainer = mHeaderView.findViewById(R.id.tweets_container);
		mTweetCount = (TextView) mHeaderView.findViewById(R.id.statuses_count);
		mFollowersContainer = mHeaderView.findViewById(R.id.followers_container);
		mFollowersCount = (TextView) mHeaderView.findViewById(R.id.followers_count);
		mFriendsContainer = mHeaderView.findViewById(R.id.friends_container);
		mFriendsCount = (TextView) mHeaderView.findViewById(R.id.friends_count);
		mProfileNameContainer = (ColorLabelLinearLayout) mHeaderView.findViewById(R.id.profile_name_container);
		mProfileImageView = (ProfileImageView) mHeaderView.findViewById(R.id.profile_image);
		mDescriptionContainer = mHeaderView.findViewById(R.id.description_container);
		mLocationContainer = mHeaderView.findViewById(R.id.location_container);
		mURLContainer = mHeaderView.findViewById(R.id.url_container);
		mFollowingYouIndicator = mHeaderView.findViewById(R.id.following_you_indicator);
		mProfileBannerSpace = mHeaderView.findViewById(R.id.profile_banner_space);
		final View cardView = mHeaderView.findViewById(R.id.card);
		ThemeUtils.applyThemeAlphaToDrawable(context, cardView.getBackground());
	}

	private void getFriendship() {
		final ParcelableUser user = mUser;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_FRIENDSHIP);
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ACCOUNT_ID, user.account_id);
		args.putLong(EXTRA_USER_ID, user.id);
		if (!mGetFriendShipLoaderInitialized) {
			lm.initLoader(LOADER_ID_FRIENDSHIP, args, mFriendshipLoaderCallbacks);
			mGetFriendShipLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_FRIENDSHIP, args, mFriendshipLoaderCallbacks);
		}
	}

	private void getUserInfo(final boolean omitIntentExtra) {
		final ParcelableUser user = mUser;
		if (user == null) return;
		getUserInfo(user.account_id, user.id, user.screen_name, omitIntentExtra);
	}

	private boolean handleMenuItemClick(final MenuItem item) {
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		final ParcelableUser user = mUser;
		final Relationship relationship = mFriendship;
		if (user == null || twitter == null) return false;
		switch (item.getItemId()) {
			case MENU_BLOCK: {
				if (mFriendship != null) {
					if (mFriendship.isSourceBlockingTarget()) {
						twitter.destroyBlockAsync(user.account_id, user.id);
					} else {
						CreateUserBlockDialogFragment.show(getFragmentManager(), user);
					}
				}
				break;
			}
			case MENU_REPORT_SPAM: {
				ReportSpamDialogFragment.show(getFragmentManager(), user);
				break;
			}
			case MENU_MUTE_USER: {
				final ContentResolver resolver = getContentResolver();
				resolver.delete(Filters.Users.CONTENT_URI, Where.equals(Filters.Users.USER_ID, user.id).getSQL(), null);
				resolver.insert(Filters.Users.CONTENT_URI, makeFilterdUserContentValues(user));
				showInfoMessage(getActivity(), R.string.user_muted, false);
				break;
			}
			case MENU_MENTION: {
				final Intent intent = new Intent(INTENT_ACTION_MENTION);
				final Bundle bundle = new Bundle();
				bundle.putParcelable(EXTRA_USER, user);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_SEND_DIRECT_MESSAGE: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
				builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(user.account_id));
				builder.appendQueryParameter(QUERY_PARAM_RECIPIENT_ID, String.valueOf(user.id));
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				break;
			}
			case MENU_SET_COLOR: {
				final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
				intent.putExtra(EXTRA_COLOR, getUserColor(getActivity(), user.id, true));
				intent.putExtra(EXTRA_ALPHA_SLIDER, false);
				intent.putExtra(EXTRA_CLEAR_BUTTON, true);
				startActivityForResult(intent, REQUEST_SET_COLOR);
				break;
			}
			case MENU_CLEAR_NICKNAME: {
				clearUserNickname(getActivity(), user.id);
				break;
			}
			case MENU_SET_NICKNAME: {
				final String nick = getUserNickname(getActivity(), user.id, true);
				SetUserNicknameDialogFragment.show(getFragmentManager(), user.id, nick);
				break;
			}
			case MENU_ADD_TO_LIST: {
				final Intent intent = new Intent(INTENT_ACTION_SELECT_USER_LIST);
				intent.setClass(getActivity(), UserListSelectorActivity.class);
				intent.putExtra(EXTRA_ACCOUNT_ID, user.account_id);
				intent.putExtra(EXTRA_SCREEN_NAME, getAccountScreenName(getActivity(), user.account_id));
				startActivityForResult(intent, REQUEST_ADD_TO_LIST);
				break;
			}
			case MENU_OPEN_WITH_ACCOUNT: {
				final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
				intent.setClass(getActivity(), AccountSelectorActivity.class);
				intent.putExtra(EXTRA_SINGLE_SELECTION, true);
				startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
				break;
			}
			case MENU_EDIT: {
				final Bundle extras = new Bundle();
				extras.putLong(EXTRA_ACCOUNT_ID, user.account_id);
				final Intent intent = new Intent(INTENT_ACTION_EDIT_USER_PROFILE);
				intent.setClass(getActivity(), UserProfileEditorActivity.class);
				intent.putExtras(extras);
				startActivity(intent);
				return true;
			}
			case MENU_FOLLOW: {
				if (relationship == null) return false;
				final boolean isFollowing = relationship.isSourceFollowingTarget();
				final boolean isCreatingFriendship = twitter.isCreatingFriendship(user.account_id, user.id);
				final boolean isDestroyingFriendship = twitter.isDestroyingFriendship(user.account_id, user.id);
				if (!isCreatingFriendship && !isDestroyingFriendship) {
					if (isFollowing) {
						DestroyFriendshipDialogFragment.show(getFragmentManager(), user);
					} else {
						twitter.createFriendshipAsync(user.account_id, user.id);
					}
				}
				return true;
			}
			default: {
				if (item.getIntent() != null) {
					try {
						startActivity(item.getIntent());
					} catch (final ActivityNotFoundException e) {
						Log.w(LOGTAG, e);
						return false;
					}
				}
				break;
			}
		}
		return true;
	}

	private void setMenu(final Menu menu) {
		final AsyncTwitterWrapper twitter = getTwitterWrapper();
		final ParcelableUser user = mUser;
		final Relationship relationship = mFriendship;
		if (twitter == null || user == null) return;
		final boolean isMyself = user.account_id == user.id;
		final boolean isFollowing = relationship != null && relationship.isSourceFollowingTarget();
		final boolean isProtected = user.is_protected;
		final boolean creatingFriendship = twitter.isCreatingFriendship(user.account_id, user.id);
		final boolean destroyingFriendship = twitter.isDestroyingFriendship(user.account_id, user.id);
		setMenuItemAvailability(menu, MENU_EDIT, isMyself);
		final MenuItem followItem = menu.findItem(MENU_FOLLOW);
		followItem.setVisible(!isMyself);
		final boolean shouldShowFollowItem = !creatingFriendship && !destroyingFriendship && !isMyself
				&& relationship != null;
		followItem.setEnabled(shouldShowFollowItem);
		if (shouldShowFollowItem) {
			followItem.setTitle(isFollowing ? R.string.unfollow : isProtected ? R.string.send_follow_request
					: R.string.follow);
			followItem.setIcon(isFollowing ? R.drawable.ic_iconic_action_cancel : R.drawable.ic_iconic_action_add);
		} else {
			followItem.setTitle(null);
			followItem.setIcon(null);
		}
		if (user.id != user.account_id) {
			setMenuItemAvailability(menu, MENU_BLOCK, mFriendship != null);
			final MenuItem blockItem = menu.findItem(MENU_BLOCK);
			if (mFriendship != null && blockItem != null) {
				final Drawable blockIcon = blockItem.getIcon();
				if (mFriendship.isSourceBlockingTarget()) {
					blockItem.setTitle(R.string.unblock);
					blockIcon.mutate().setColorFilter(ThemeUtils.getUserThemeColor(getActivity()),
							PorterDuff.Mode.MULTIPLY);
				} else {
					blockItem.setTitle(R.string.block);
					blockIcon.clearColorFilter();
				}
			}
			final boolean is_following_me = mFriendship != null && mFriendship.isTargetFollowingSource();
			setMenuItemAvailability(menu, MENU_SEND_DIRECT_MESSAGE, is_following_me);
		} else {
			setMenuItemAvailability(menu, MENU_MENTION, false);
			setMenuItemAvailability(menu, MENU_SEND_DIRECT_MESSAGE, false);
			setMenuItemAvailability(menu, MENU_BLOCK, false);
			setMenuItemAvailability(menu, MENU_REPORT_SPAM, false);
		}
		final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
		final Bundle extras = new Bundle();
		extras.putParcelable(EXTRA_USER, user);
		intent.putExtras(extras);
		menu.removeGroup(MENU_GROUP_USER_EXTENSION);
		addIntentToMenu(getActivity(), menu, intent, MENU_GROUP_USER_EXTENSION);
	}

	private boolean shouldUseNativeMenu() {
		final boolean isInLinkHandler = getActivity() instanceof LinkHandlerActivity;
		return isInLinkHandler && FlymeUtils.hasSmartBar();
	}

	final class FavoritesAction extends ListAction {

		public FavoritesAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.favorites);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return getLocalizedNumber(mLocale, mUser.favorites_count);
		}

		@Override
		public void onClick() {
			final ParcelableUser user = mUser;
			if (user == null) return;
			openUserFavorites(getActivity(), user.account_id, user.id, user.screen_name);
		}

	}

	static class FriendshipLoader extends AsyncTaskLoader<SingleResponse<Relationship>> {

		private final Context context;
		private final long account_id, user_id;

		public FriendshipLoader(final Context context, final long account_id, final long user_id) {
			super(context);
			this.context = context;
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		public SingleResponse<Relationship> loadInBackground() {
			if (account_id == user_id) return new SingleResponse<Relationship>(null, null);
			final Twitter twitter = getTwitterInstance(context, account_id, false);
			if (twitter == null) return new SingleResponse<Relationship>(null, null);
			try {
				final Relationship result = twitter.showFriendship(account_id, user_id);
				return new SingleResponse<Relationship>(result, null);
			} catch (final TwitterException e) {
				return new SingleResponse<Relationship>(null, e);
			}
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}
	}

	final class IncomingFriendshipsAction extends ListAction {

		public IncomingFriendshipsAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.incoming_friendships);
		}

		@Override
		public void onClick() {
			final ParcelableUser user = mUser;
			if (user == null) return;
			openIncomingFriendships(getActivity(), user.account_id);
		}

	}

	final class SavedSearchesAction extends ListAction {

		public SavedSearchesAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.saved_searches);
		}

		@Override
		public void onClick() {
			final ParcelableUser user = mUser;
			if (user == null) return;
			openSavedSearches(getActivity(), user.account_id);
		}

	}

	final class UserBlocksAction extends ListAction {

		public UserBlocksAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.blocked_users);
		}

		@Override
		public void onClick() {
			final ParcelableUser user = mUser;
			if (user == null) return;
			openUserBlocks(getActivity(), user.account_id);
		}

	}

	final class UserListMembershipsAction extends ListAction {
		public UserListMembershipsAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			if (mUser == null) return getString(R.string.lists_following_user);
			final String display_name = getDisplayName(getActivity(), mUser.id, mUser.name, mUser.screen_name);
			return getString(R.string.lists_following_user_with_name, display_name);
		}

		@Override
		public void onClick() {
			final ParcelableUser user = mUser;
			if (user == null) return;
			openUserListMemberships(getActivity(), user.account_id, user.id, user.screen_name);
		}
	}

	final class UserListsAction extends ListAction {

		public UserListsAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			if (mUser == null) return getString(R.string.users_lists);
			final String display_name = getDisplayName(getActivity(), mUser.id, mUser.name, mUser.screen_name);
			return getString(R.string.users_lists_with_name, display_name);
		}

		@Override
		public void onClick() {
			final ParcelableUser user = mUser;
			if (user == null) return;
			openUserLists(getActivity(), user.account_id, user.id, user.screen_name);
		}

	}

	final class UserMentionsAction extends ListAction {

		public UserMentionsAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.user_mentions);
		}

		@Override
		public void onClick() {
			final ParcelableUser user = mUser;
			if (user == null) return;
			openUserMentions(getActivity(), user.account_id, user.screen_name);
		}

	}

}
