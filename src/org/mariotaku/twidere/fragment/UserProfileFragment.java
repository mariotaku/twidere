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

package org.mariotaku.twidere.fragment;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;
import static org.mariotaku.twidere.util.Utils.clearUserColor;
import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getConnection;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getOriginalTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getProxy;
import static org.mariotaku.twidere.util.Utils.getTimestampFromDate;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.openIncomingFriendships;
import static org.mariotaku.twidere.util.Utils.openSavedSearches;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserBlocks;
import static org.mariotaku.twidere.util.Utils.openUserFavorites;
import static org.mariotaku.twidere.util.Utils.openUserFollowers;
import static org.mariotaku.twidere.util.Utils.openUserFriends;
import static org.mariotaku.twidere.util.Utils.openUserLists;
import static org.mariotaku.twidere.util.Utils.openUserMentions;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.openUserTimeline;
import static org.mariotaku.twidere.util.Utils.parseString;
import static org.mariotaku.twidere.util.Utils.parseURL;
import static org.mariotaku.twidere.util.Utils.setUserColor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.SetColorActivity;
import org.mariotaku.twidere.adapter.ListActionAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ListAction;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.GetExternalCacheDirAccessor;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.http.HostAddressResolver;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UserProfileFragment extends BaseListFragment implements OnClickListener, OnLongClickListener,
		OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener, OnLinkClickListener, Panes.Right {

	private LazyImageLoader mProfileImageLoader;

	private ImageView mProfileImageView;
	private TextView mNameView, mScreenNameView, mDescriptionView, mLocationView, mURLView, mCreatedAtView,
			mTweetCount, mFollowersCount, mFriendsCount, mFollowingYouIndicator, mErrorMessageView;
	private View mNameContainer, mProfileImageContainer, mDescriptionContainer, mLocationContainer, mURLContainer,
			mTweetsContainer, mFollowersContainer, mFriendsContainer, mFollowContainer, mProfileNameBannerContainer;
	private ProgressBar mFollowProgress, mMoreOptionsProgress;
	private Button mFollowButton, mMoreOptionsButton, mRetryButton;
	private ColorLabelRelativeLayout mProfileNameContainer;
	private ListActionAdapter mAdapter;

	private ListView mListView;
	private View mHeaderView;
	private long mAccountId;
	private Relationship mFriendship;
	private final DialogFragment mDialogFragment = new EditTextDialogFragment();
	private Uri mImageUri;
	private User mUser = null;

	private static final int TYPE_NAME = 1;

	private static final int TYPE_URL = 2;

	private static final int TYPE_LOCATION = 3;

	private static final int TYPE_DESCRIPTION = 4;

	private long mUserId;

	private String mScreenName;

	private ServiceInterface mService;

	private PopupMenu mPopupMenu;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (mUser == null) return;
			final String action = intent.getAction();
			if (BROADCAST_FRIENDSHIP_CHANGED.equals(action)) {
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mUser.getId()
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					getFriendship();
				}
			}
			if (BROADCAST_BLOCKSTATE_CHANGED.equals(action)) {
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mUser.getId()
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					getFriendship();
				}
			}
			if (BROADCAST_PROFILE_UPDATED.equals(action)) {
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mUser.getId()
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					reloadUserInfo();
				}
			}
		}
	};

	private View mListContainer, mErrorRetryContainer;

	private boolean mGetUserInfoLoaderInitialized;
	private boolean mGetFriendShipLoaderInitialized;
	private boolean mBannerImageLoaderInitialized;

	private static final int LOADER_ID_USER = 1;
	private static final int LOADER_ID_FRIENDSHIP = 2;
	private static final int LOADER_ID_BANNER = 3;

	private final LoaderCallbacks<Response<User>> mUserInfoLoaderCallbacks = new LoaderCallbacks<Response<User>>() {

		@Override
		public Loader<Response<User>> onCreateLoader(final int id, final Bundle args) {
			mListContainer.setVisibility(View.VISIBLE);
			mErrorRetryContainer.setVisibility(View.GONE);
			mErrorMessageView.setText(null);
			mErrorMessageView.setVisibility(View.GONE);
			setListShown(false);
			setProgressBarIndeterminateVisibility(true);
			return new UserInfoLoader(getActivity(), mAccountId, mUserId, mScreenName);
		}

		@Override
		public void onLoaderReset(final Loader<Response<User>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<Response<User>> loader, final Response<User> data) {
			if (getActivity() == null) return;
			if (data.value != null && data.value.getId() > 0) {
				setListShown(true);
				changeUser(mAccountId, data.value);
				mErrorRetryContainer.setVisibility(View.GONE);
			} else {
				if (data.exception != null) {
					mErrorMessageView.setText(data.exception.getMessage());
					mErrorMessageView.setVisibility(View.VISIBLE);
				}
				mListContainer.setVisibility(View.GONE);
				mErrorRetryContainer.setVisibility(View.VISIBLE);
			}
			setProgressBarIndeterminateVisibility(false);
		}

	};

	private final LoaderCallbacks<Response<Relationship>> mFriendshipLoaderCallbacks = new LoaderCallbacks<Response<Relationship>>() {

		@Override
		public Loader<Response<Relationship>> onCreateLoader(final int id, final Bundle args) {
			final boolean user_is_me = mUserId == mAccountId;
			mFollowingYouIndicator.setVisibility(View.GONE);
			mFollowContainer.setVisibility(user_is_me ? View.GONE : View.VISIBLE);
			mFollowButton.setVisibility(View.GONE);
			mFollowProgress.setVisibility(View.VISIBLE);
			mMoreOptionsButton.setVisibility(View.GONE);
			mMoreOptionsProgress.setVisibility(View.VISIBLE);
			return new FriendshipLoader(getActivity(), mAccountId, mUserId);
		}

		@Override
		public void onLoaderReset(final Loader<Response<Relationship>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<Response<Relationship>> loader, final Response<Relationship> data) {
			mFriendship = null;
			if (mUser == null) return;
			final boolean user_is_me = mAccountId == mUser.getId();
			if (data.value != null) {
				mFriendship = data.value;
				final boolean followed_by_user = data.value.isTargetFollowingSource();
				mFollowButton.setVisibility(View.VISIBLE);
				if (data.value.isSourceFollowingTarget()) {
					mFollowButton.setText(R.string.unfollow);
				} else {
					if (mUser.isProtected()) {
						mFollowButton.setText(mUser.isFollowRequestSent() ? R.string.follow_request_sent
								: R.string.send_follow_request);
					} else {
						mFollowButton.setText(R.string.follow);
					}
				}
				mFollowingYouIndicator.setVisibility(followed_by_user && !user_is_me ? View.VISIBLE : View.GONE);
				final ContentResolver resolver = getContentResolver();
				final String where = CachedUsers.USER_ID + " = " + mUserId;
				resolver.delete(CachedUsers.CONTENT_URI, where, null);
				// I bet you don't want to see blocked user in your auto
				// complete list.
				if (!data.value.isSourceBlockingTarget()) {
					final ContentValues cached_values = makeCachedUserContentValues(mUser);
					if (cached_values != null) {
						resolver.insert(CachedUsers.CONTENT_URI, cached_values);
					}
				}
			}
			mFollowContainer.setVisibility(data.value == null || user_is_me ? View.GONE : View.VISIBLE);
			mMoreOptionsButton.setVisibility(data.value != null || user_is_me ? View.VISIBLE : View.GONE);
			mFollowProgress.setVisibility(View.GONE);
			mMoreOptionsProgress.setVisibility(View.GONE);
		}

	};

	private final LoaderCallbacks<Bitmap> mBannerImageCallback = new LoaderCallbacks<Bitmap>() {

		@Override
		public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {
			final LayoutParams lp = mProfileNameBannerContainer.getLayoutParams();
			lp.height = LayoutParams.WRAP_CONTENT;
			mProfileNameBannerContainer.setBackgroundDrawable(null);
			mProfileNameBannerContainer.setLayoutParams(lp);
			final int screen_width = getResources().getDisplayMetrics().widthPixels;
			final String type;
			if (screen_width > 320) {
				type = "web";
			} else {
				type = "mobile";
			}
			return new BannerImageLoader(getActivity(), mUser, type, screen_width < 320);
		}

		@Override
		public void onLoaderReset(final Loader<Bitmap> loader) {
		}

		@Override
		public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap data) {
			if (data == null) return;
			final Drawable d = new BitmapDrawable(getResources(), data);
			final LayoutParams lp = mProfileNameBannerContainer.getLayoutParams();
			final float ratio = (float) data.getHeight() / (float) data.getWidth();
			lp.height = (int) (mProfileNameContainer.getWidth() * ratio);
			mProfileNameBannerContainer.setLayoutParams(lp);
			mProfileNameBannerContainer.setBackgroundDrawable(d);
		}

	};

	public void changeUser(final long account_id, final User user) {
		mFriendship = null;
		mUser = null;
		mUserId = -1;
		mAccountId = -1;
		mAdapter.clear();
		if (user == null || user.getId() <= 0 || getActivity() == null || !isMyAccount(getActivity(), account_id))
			return;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_USER);
		lm.destroyLoader(LOADER_ID_FRIENDSHIP);
		final boolean user_is_me = account_id == user.getId();
		mErrorRetryContainer.setVisibility(View.GONE);
		mAccountId = account_id;
		mUser = user;
		mUserId = user.getId();
		mScreenName = user.getScreenName();
		mProfileNameContainer.drawLeft(getUserColor(getActivity(), mUserId));
		mProfileNameContainer.drawRight(getAccountColor(getActivity(), account_id));
		mNameView.setText(user.getName());
		mNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
				getUserTypeIconRes(user.isVerified(), user.isProtected()), 0);
		mScreenNameView.setText("@" + user.getScreenName());
		final String description = user.getDescription();
		mDescriptionContainer.setVisibility(user_is_me || !isNullOrEmpty(description) ? View.VISIBLE : View.GONE);
		mDescriptionContainer.setOnLongClickListener(this);
		mDescriptionView.setText(description);
		final TwidereLinkify linkify = new TwidereLinkify(mDescriptionView);
		linkify.setOnLinkClickListener(this);
		linkify.addAllLinks();
		mDescriptionView.setMovementMethod(null);
		final String location = user.getLocation();
		mLocationContainer.setVisibility(user_is_me || !isNullOrEmpty(location) ? View.VISIBLE : View.GONE);
		mLocationContainer.setOnLongClickListener(this);
		mLocationView.setText(location);
		final String url = user.getURL() != null ? user.getURL().toString() : null;
		mURLContainer.setVisibility(user_is_me || !isNullOrEmpty(url) ? View.VISIBLE : View.GONE);
		mURLContainer.setOnLongClickListener(this);
		mURLView.setText(url);
		mURLView.setMovementMethod(null);
		mCreatedAtView.setText(formatToLongTimeString(getActivity(), getTimestampFromDate(user.getCreatedAt())));
		mTweetCount.setText(String.valueOf(user.getStatusesCount()));
		mFollowersCount.setText(String.valueOf(user.getFollowersCount()));
		mFriendsCount.setText(String.valueOf(user.getFriendsCount()));
		// final boolean display_profile_image =
		// mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		// mProfileImageView.setVisibility(display_profile_image ? View.VISIBLE
		// : View.GONE);
		// if (display_profile_image) {
		final String profile_image_url_string = parseString(user.getProfileImageURL());
		final boolean hires_profile_image = getResources().getBoolean(R.bool.hires_profile_image);
		mProfileImageLoader.displayImage(
				parseURL(hires_profile_image ? getBiggerTwitterProfileImage(profile_image_url_string)
						: profile_image_url_string), mProfileImageView);
		// }
		mUser = user;
		if (isMyAccount(getActivity(), user.getId())) {
			final ContentResolver resolver = getContentResolver();
			final ContentValues values = new ContentValues();
			final URL profile_image_url = user.getProfileImageURL();
			if (profile_image_url != null) {
				values.put(Accounts.PROFILE_IMAGE_URL, profile_image_url.toString());
			}
			values.put(Accounts.NAME, user.getName());
			values.put(Accounts.SCREEN_NAME, user.getScreenName());
			final String where = Accounts.ACCOUNT_ID + " = " + user.getId();
			resolver.update(Accounts.CONTENT_URI, values, where, null);
		}
		mAdapter.add(new FavoritesAction());
		mAdapter.add(new UserMentionsAction());
		mAdapter.add(new UserListsAction());
		if (user.getId() == mAccountId) {
			mAdapter.add(new SavedSearchesAction());
			if (user.isProtected()) {
				mAdapter.add(new IncomingFriendshipsAction());
			}
			mAdapter.add(new UserBlocksAction());
		}
		mAdapter.notifyDataSetChanged();
		getFriendship();
		getBannerImage();
	}

	public void getUserInfo(final long account_id, final long user_id, final String screen_name) {
		mAccountId = account_id;
		mUserId = user_id;
		mScreenName = screen_name;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_USER);
		lm.destroyLoader(LOADER_ID_FRIENDSHIP);
		if (!isMyAccount(getActivity(), mAccountId)) {
			mListContainer.setVisibility(View.GONE);
			mErrorRetryContainer.setVisibility(View.GONE);
			return;
		}
		if (!mGetUserInfoLoaderInitialized) {
			lm.initLoader(LOADER_ID_USER, null, mUserInfoLoaderCallbacks);
			mGetUserInfoLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_USER, null, mUserInfoLoaderCallbacks);
		}

		if (account_id == -1 || user_id == -1 && screen_name == null) {
			mListContainer.setVisibility(View.GONE);
			mErrorRetryContainer.setVisibility(View.GONE);
			return;
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mService = getApplication().getServiceInterface();
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		long account_id = -1, user_id = -1;
		String screen_name = null;
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		}
		mProfileImageLoader = getApplication().getProfileImageLoader();
		mAdapter = new ListActionAdapter(getActivity());
		mProfileImageContainer.setOnClickListener(this);
		mProfileImageContainer.setOnLongClickListener(this);
		mNameContainer.setOnClickListener(this);
		mNameContainer.setOnLongClickListener(this);
		mFollowButton.setOnClickListener(this);
		mTweetsContainer.setOnClickListener(this);
		mFollowersContainer.setOnClickListener(this);
		mFriendsContainer.setOnClickListener(this);
		mRetryButton.setOnClickListener(this);
		mMoreOptionsButton.setOnClickListener(this);
		setListAdapter(null);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		setListAdapter(mAdapter);
		getUserInfo(account_id, user_id, screen_name);

	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		if (intent == null) return;
		switch (requestCode) {
			case REQUEST_TAKE_PHOTO: {
				if (resultCode == Activity.RESULT_OK) {
					final String path = mImageUri.getPath();
					final File file = path != null ? new File(path) : null;
					if (file != null && file.exists()) {
						mService.updateProfileImage(mUser.getId(), mImageUri, true);
					}
				}
				break;
			}
			case REQUEST_PICK_IMAGE: {
				if (resultCode == Activity.RESULT_OK && intent != null) {
					final Uri uri = intent.getData();
					final String image_path = getImagePathFromUri(getActivity(), uri);
					final File file = image_path != null ? new File(image_path) : null;
					if (file != null && file.exists()) {
						mService.updateProfileImage(mUser.getId(), Uri.fromFile(file), false);
					}
				}
				break;
			}
			case REQUEST_SET_COLOR: {
				if (resultCode == Activity.RESULT_OK && intent != null) {
					final int color = intent.getIntExtra(Accounts.USER_COLOR, Color.TRANSPARENT);
					setUserColor(getActivity(), mUserId, color);
					mProfileNameContainer.drawLeft(getUserColor(getActivity(), mUserId));
				}
				break;
			}
		}

	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.follow: {
				if (mUser == null || mAccountId == mUserId || mUser.isFollowRequestSent()) return;
				if (mFriendship.isSourceFollowingTarget()) {
					mPopupMenu = PopupMenu.getInstance(getActivity(), view);
					mPopupMenu.inflate(R.menu.action_user_profile_follow);
					mPopupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

						@Override
						public boolean onMenuItemClick(final MenuItem item) {
							switch (item.getItemId()) {
								case R.id.unfollow: {
									mFollowProgress.setVisibility(View.VISIBLE);
									mFollowButton.setVisibility(View.GONE);
									mService.destroyFriendship(mAccountId, mUser.getId());
									return true;
								}
							}
							return false;
						}
					});
					mPopupMenu.show();
				} else {
					mFollowProgress.setVisibility(View.VISIBLE);
					mFollowButton.setVisibility(View.GONE);
					mService.createFriendship(mAccountId, mUser.getId());
				}
				break;
			}
			case R.id.retry: {
				reloadUserInfo();
				break;
			}
			case R.id.profile_image_container: {
				final Uri uri = Uri.parse(getOriginalTwitterProfileImage(parseString(mUser.getProfileImageURL())));
				final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, uri);
				intent.setPackage(getActivity().getPackageName());
				startActivity(intent);
				break;
			}
			case R.id.tweets_container: {
				if (mUser == null) return;
				openUserTimeline(getActivity(), mAccountId, mUser.getId(), mUser.getScreenName());
				break;
			}
			case R.id.followers_container: {
				if (mUser == null) return;
				openUserFollowers(getActivity(), mAccountId, mUser.getId(), mUser.getScreenName());
				break;
			}
			case R.id.friends_container: {
				if (mUser == null) return;
				openUserFriends(getActivity(), mAccountId, mUser.getId(), mUser.getScreenName());
				break;
			}
			case R.id.more_options: {
				if (mUser == null) return;
				mPopupMenu = PopupMenu.getInstance(getActivity(), view);
				mPopupMenu.inflate(R.menu.action_user_profile);
				final Menu menu = mPopupMenu.getMenu();
				if (mUser.getId() != mAccountId) {
					if (mFriendship == null) return;
					final MenuItem blockItem = menu.findItem(MENU_BLOCK);
					if (blockItem != null) {
						final Drawable blockIcon = blockItem.getIcon();
						if (mFriendship.isSourceBlockingTarget()) {
							blockItem.setTitle(R.string.unblock);
							blockIcon.mutate().setColorFilter(getResources().getColor(R.color.holo_blue_bright),
									PorterDuff.Mode.MULTIPLY);
						} else {
							blockItem.setTitle(R.string.block);
							blockIcon.clearColorFilter();
						}
					}
					final MenuItem sendDirectMessageItem = menu.findItem(MENU_SEND_DIRECT_MESSAGE);
					if (sendDirectMessageItem != null) {
						sendDirectMessageItem.setVisible(mFriendship.isTargetFollowingSource());
					}
				} else {
					final int size = menu.size();
					for (int i = 0; i < size; i++) {
						final MenuItem item = menu.getItem(i);
						final int id = item.getItemId();
						item.setVisible(id == R.id.set_color_submenu || id == MENU_EXTENSIONS);
					}
				}
				mPopupMenu.setOnMenuItemClickListener(this);
				mPopupMenu.show();
				break;
			}
		}

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mHeaderView = inflater.inflate(R.layout.user_profile_header, null, false);
		mNameContainer = mHeaderView.findViewById(R.id.name_container);
		mNameView = (TextView) mHeaderView.findViewById(R.id.name);
		mScreenNameView = (TextView) mHeaderView.findViewById(R.id.screen_name);
		mDescriptionView = (TextView) mHeaderView.findViewById(R.id.description);
		mLocationView = (TextView) mHeaderView.findViewById(R.id.location);
		mURLView = (TextView) mHeaderView.findViewById(R.id.url);
		mCreatedAtView = (TextView) mHeaderView.findViewById(R.id.created_at);
		mTweetsContainer = mHeaderView.findViewById(R.id.tweets_container);
		mTweetCount = (TextView) mHeaderView.findViewById(R.id.tweet_count);
		mFollowersContainer = mHeaderView.findViewById(R.id.followers_container);
		mFollowersCount = (TextView) mHeaderView.findViewById(R.id.followers_count);
		mFriendsContainer = mHeaderView.findViewById(R.id.friends_container);
		mFriendsCount = (TextView) mHeaderView.findViewById(R.id.friends_count);
		mProfileNameContainer = (ColorLabelRelativeLayout) mHeaderView.findViewById(R.id.profile_name_container);
		mProfileImageView = (ImageView) mHeaderView.findViewById(R.id.profile_image);
		mProfileImageContainer = mHeaderView.findViewById(R.id.profile_image_container);
		mDescriptionContainer = mHeaderView.findViewById(R.id.description_container);
		mLocationContainer = mHeaderView.findViewById(R.id.location_container);
		mURLContainer = mHeaderView.findViewById(R.id.url_container);
		mFollowContainer = mHeaderView.findViewById(R.id.follow_container);
		mFollowButton = (Button) mHeaderView.findViewById(R.id.follow);
		mFollowProgress = (ProgressBar) mHeaderView.findViewById(R.id.follow_progress);
		mMoreOptionsButton = (Button) mHeaderView.findViewById(R.id.more_options);
		mMoreOptionsProgress = (ProgressBar) mHeaderView.findViewById(R.id.more_options_progress);
		mFollowingYouIndicator = (TextView) mHeaderView.findViewById(R.id.following_you_indicator);
		mProfileNameBannerContainer = mHeaderView.findViewById(R.id.profile_name_banner_container);
		mListContainer = super.onCreateView(inflater, container, savedInstanceState);
		final View container_view = inflater.inflate(R.layout.list_with_error_message, null);
		((FrameLayout) container_view.findViewById(R.id.list_container)).addView(mListContainer);
		mErrorRetryContainer = container_view.findViewById(R.id.error_retry_container);
		mRetryButton = (Button) container_view.findViewById(R.id.retry);
		mErrorMessageView = (TextView) container_view.findViewById(R.id.error_message);
		return container_view;
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
	public void onLinkClick(final String link, final int type) {
		if (mUser == null) return;
		switch (type) {
			case TwidereLinkify.LINK_TYPE_MENTION_LIST: {
				openUserProfile(getActivity(), mAccountId, -1, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_HASHTAG: {
				openTweetSearch(getActivity(), mAccountId, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_LINK_WITH_IMAGE_EXTENSION: {
				final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, Uri.parse(link));
				intent.setPackage(getActivity().getPackageName());
				startActivity(intent);
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
		}
	}

	@Override
	public boolean onLongClick(final View view) {
		if (mUser == null || mAccountId != mUserId) return false;
		switch (view.getId()) {
			case R.id.profile_image_container: {
				mPopupMenu = PopupMenu.getInstance(getActivity(), view);
				mPopupMenu.inflate(R.menu.action_profile_image);
				mPopupMenu.setOnMenuItemClickListener(this);
				mPopupMenu.show();
				return true;
			}
			case R.id.name_container: {
				final Bundle args = new Bundle();
				args.putLong(INTENT_KEY_ACCOUNT_ID, mUser.getId());
				args.putString(INTENT_KEY_TEXT, mUser.getName());
				args.putString(INTENT_KEY_TITLE, getString(R.string.name));
				args.putInt(INTENT_KEY_TYPE, TYPE_NAME);
				mDialogFragment.setArguments(args);
				mDialogFragment.show(getFragmentManager(), "edit_name");
				return true;
			}
			case R.id.description_container: {
				final Bundle args = new Bundle();
				args.putLong(INTENT_KEY_ACCOUNT_ID, mUser.getId());
				args.putString(INTENT_KEY_TEXT, mUser.getDescription());
				args.putString(INTENT_KEY_TITLE, getString(R.string.description));
				args.putInt(INTENT_KEY_TYPE, TYPE_DESCRIPTION);
				mDialogFragment.setArguments(args);
				mDialogFragment.show(getFragmentManager(), "edit_description");
				return true;
			}
			case R.id.location_container: {
				final Bundle args = new Bundle();
				args.putLong(INTENT_KEY_ACCOUNT_ID, mUser.getId());
				args.putString(INTENT_KEY_TEXT, mUser.getLocation());
				args.putString(INTENT_KEY_TITLE, getString(R.string.location));
				args.putInt(INTENT_KEY_TYPE, TYPE_LOCATION);
				mDialogFragment.setArguments(args);
				mDialogFragment.show(getFragmentManager(), "edit_location");
				return true;
			}
			case R.id.url_container: {
				final URL url = mUser.getURL();
				final Bundle args = new Bundle();
				args.putLong(INTENT_KEY_ACCOUNT_ID, mUser.getId());
				args.putString(INTENT_KEY_TEXT, url != null ? url.toString() : null);
				args.putString(INTENT_KEY_TITLE, getString(R.string.url));
				args.putInt(INTENT_KEY_TYPE, TYPE_URL);
				mDialogFragment.setArguments(args);
				mDialogFragment.show(getFragmentManager(), "edit_url");
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		if (mUser == null || mService == null) return false;
		switch (item.getItemId()) {
			case MENU_TAKE_PHOTO: {
				takePhoto();
				break;
			}
			case MENU_ADD_IMAGE: {
				pickImage();
				break;
			}
			case MENU_BLOCK: {
				if (mService == null || mFriendship == null) {
					break;
				}
				if (mFriendship.isSourceBlockingTarget()) {
					mService.destroyBlock(mAccountId, mUser.getId());
				} else {
					mService.createBlock(mAccountId, mUser.getId());
				}
				break;
			}
			case MENU_REPORT_SPAM: {
				mService.reportSpam(mAccountId, mUser.getId());
				break;
			}
			case MENU_MUTE_USER: {
				final String screen_name = mUser.getScreenName();
				final Uri uri = Filters.Users.CONTENT_URI;
				final ContentValues values = new ContentValues();
				final ContentResolver resolver = getContentResolver();
				values.put(Filters.Users.TEXT, screen_name);
				resolver.delete(uri, Filters.Users.TEXT + " = '" + screen_name + "'", null);
				resolver.insert(uri, values);
				Toast.makeText(getActivity(), R.string.user_muted, Toast.LENGTH_SHORT).show();
				break;
			}
			case MENU_MENTION: {
				final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				final Bundle bundle = new Bundle();
				final String name = mUser.getName();
				final String screen_name = mUser.getScreenName();
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
				bundle.putString(INTENT_KEY_TEXT, "@" + screen_name + " ");
				bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_SEND_DIRECT_MESSAGE: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_DIRECT_MESSAGES_CONVERSATION);
				builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(mAccountId));
				builder.appendQueryParameter(QUERY_PARAM_CONVERSATION_ID, String.valueOf(mUser.getId()));
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				break;
			}
			case MENU_EXTENSIONS: {
				final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER);
				final Bundle extras = new Bundle();
				extras.putParcelable(INTENT_KEY_USER, new ParcelableUser(mUser, mAccountId));
				intent.putExtras(extras);
				startActivity(Intent.createChooser(intent, getString(R.string.open_with_extensions)));
				break;
			}
			case MENU_SET_COLOR: {
				final Intent intent = new Intent(getActivity(), SetColorActivity.class);
				startActivityForResult(intent, REQUEST_SET_COLOR);
				break;
			}
			case MENU_CLEAR_COLOR: {
				clearUserColor(getActivity(), mUserId);
				mProfileNameContainer.drawLeft(getUserColor(getActivity(), mUserId));
				break;
			}
		}
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_FRIENDSHIP_CHANGED);
		filter.addAction(BROADCAST_BLOCKSTATE_CHANGED);
		filter.addAction(BROADCAST_PROFILE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
		mProfileNameContainer.drawLeft(getUserColor(getActivity(), mUserId));
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	private void getBannerImage() {
		final LoaderManager lm = getLoaderManager();
		if (mBannerImageLoaderInitialized) {
			lm.restartLoader(LOADER_ID_BANNER, null, mBannerImageCallback);
		} else {
			lm.initLoader(LOADER_ID_BANNER, null, mBannerImageCallback);
			mBannerImageLoaderInitialized = true;
		}
	}

	private void getFriendship() {
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_FRIENDSHIP);
		if (!mGetFriendShipLoaderInitialized) {
			lm.initLoader(LOADER_ID_FRIENDSHIP, null, mFriendshipLoaderCallbacks);
			mGetFriendShipLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_FRIENDSHIP, null, mFriendshipLoaderCallbacks);
		}
	}

	private void pickImage() {
		final Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_PICK_IMAGE);
	}

	private void reloadUserInfo() {
		getUserInfo(mAccountId, mUserId, mScreenName);
	}

	private void takePhoto() {
		final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			final File cache_dir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor
					.getExternalCacheDir(getActivity()) : new File(getExternalStorageDirectory().getPath()
					+ "/Android/data/" + getActivity().getPackageName() + "/cache/");
			final File file = new File(cache_dir, "tmp_photo_" + System.currentTimeMillis() + ".jpg");
			mImageUri = Uri.fromFile(file);
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageUri);
			startActivityForResult(intent, REQUEST_TAKE_PHOTO);
		}
	}

	public static class EditTextDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener {
		private EditText mEditText;
		private String mText;
		private int mType;
		private String mTitle;
		private long mAccountId;
		private ServiceInterface mService;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mText = mEditText.getText().toString();
					switch (mType) {
						case TYPE_NAME: {
							mService.updateProfile(mAccountId, mText, null, null, null);
							break;
						}
						case TYPE_URL: {
							mService.updateProfile(mAccountId, null, mText, null, null);
							break;
						}
						case TYPE_LOCATION: {
							mService.updateProfile(mAccountId, null, null, mText, null);
							break;
						}
						case TYPE_DESCRIPTION: {
							mService.updateProfile(mAccountId, null, null, null, mText);
							break;
						}
					}
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			mService = getApplication().getServiceInterface();
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mAccountId = bundle != null ? bundle.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
			mText = bundle != null ? bundle.getString(INTENT_KEY_TEXT) : null;
			mType = bundle != null ? bundle.getInt(INTENT_KEY_TYPE, -1) : -1;
			mTitle = bundle != null ? bundle.getString(INTENT_KEY_TITLE) : null;
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final View view = LayoutInflater.from(getActivity()).inflate(R.layout.edittext_default_style, null);
			builder.setView(view);
			mEditText = (EditText) view.findViewById(R.id.edit_text);
			if (mText != null) {
				mEditText.setText(mText);
			}
			int limit = 140;
			switch (mType) {
				case TYPE_NAME: {
					limit = 20;
					break;
				}
				case TYPE_URL: {
					limit = 100;
					break;
				}
				case TYPE_LOCATION: {
					limit = 30;
					break;
				}
				case TYPE_DESCRIPTION: {
					limit = 160;
					break;
				}
			}
			mEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(limit) });
			builder.setTitle(mTitle);
			builder.setView(view);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(final Bundle outState) {
			outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
			outState.putString(INTENT_KEY_TEXT, mText);
			outState.putInt(INTENT_KEY_TYPE, mType);
			outState.putString(INTENT_KEY_TITLE, mTitle);
			super.onSaveInstanceState(outState);
		}

	}

	static class BannerImageLoader extends AsyncTaskLoader<Bitmap> {

		private static final String CACHE_DIR = "cached_images";

		private final User user;
		private final String type;
		private final Context context;
		private final HostAddressResolver resolver;
		private final boolean scale_down;
		private final int connection_timeout;

		public BannerImageLoader(final Context context, final User user, final String type, final boolean scale_down) {
			super(context);
			this.context = context;
			this.user = user;
			this.type = type;
			resolver = TwidereApplication.getInstance(context).getHostAddressResolver();
			connection_timeout = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getInt(
					PREFERENCE_KEY_CONNECTION_TIMEOUT, 10) * 1000;
			this.scale_down = scale_down;
		}

		@Override
		public Bitmap loadInBackground() {
			if (user == null) return null;
			try {
				final URL url = new URL(user.getProfileBannerImageUrl() + "/" + type);
				final File cache_dir = getCacheDir();
				final File cache_file = cache_dir != null && cache_dir.isDirectory() ? new File(cache_dir,
						getURLFilename(url)) : null;
				if (cache_file != null && cache_file.isFile()) {
					final BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = scale_down ? 2 : 1;
					final Bitmap cache_bitmap = BitmapFactory.decodeFile(cache_file.getPath(), o);
					if (cache_bitmap != null) return createAlphaGradientBanner(cache_bitmap);
				}
				final HttpURLConnection conn = getConnection(url, connection_timeout, true, getProxy(context), resolver);
				if (cache_file != null) {
					final FileOutputStream fos = new FileOutputStream(cache_file);
					final InputStream is = conn.getInputStream();
					copyStream(is, fos);
					final BitmapFactory.Options o = new BitmapFactory.Options();
					o.inSampleSize = scale_down ? 2 : 1;
					final Bitmap bitmap = BitmapFactory.decodeFile(cache_file.getPath(), o);
					return createAlphaGradientBanner(bitmap);
				} else {
					final Bitmap bitmap = BitmapFactory.decodeStream(conn.getInputStream());
					return createAlphaGradientBanner(bitmap);
				}
			} catch (final IOException e) {
				return null;
			}
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		private File getCacheDir() {
			final File cache_dir;
			if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				final File app_cache_dir = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor
						.getExternalCacheDir(context) : new File(getExternalStorageDirectory(), "/Android/data/"
						+ context.getPackageName() + "/cache/");
				cache_dir = new File(app_cache_dir, CACHE_DIR);
			} else {
				cache_dir = new File(context.getCacheDir(), CACHE_DIR);
			}
			if (cache_dir != null && !cache_dir.exists()) {
				cache_dir.mkdirs();
			}
			return cache_dir;
		}

		private String getURLFilename(final URL url) {
			if (url == null) return null;
			return url.toString().replaceFirst("https?:\\/\\/", "").replaceAll("[^a-zA-Z0-9]", "_");
		}

		public static Bitmap createAlphaGradientBanner(final Bitmap orig) {
			if (orig == null) return null;
			final int width = orig.getWidth(), height = orig.getHeight();
			final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			final Canvas canvas = new Canvas(bitmap);
			final Paint paint = new Paint();
			final LinearGradient shader = new LinearGradient(width / 2, 0, width / 2, height, 0xffffffff, 0x00ffffff,
					Shader.TileMode.CLAMP);
			paint.setShader(shader);
			paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
			canvas.drawBitmap(orig, 0, 0, null);
			canvas.drawRect(0, 0, width, height, paint);
			return bitmap;
		}
	}

	final class FavoritesAction extends ListAction {

		@Override
		public long getId() {
			return 1;
		}

		@Override
		public String getName() {
			return getString(R.string.favorites);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return String.valueOf(mUser.getFavouritesCount());
		}

		@Override
		public void onClick() {
			if (mUser == null) return;
			openUserFavorites(getActivity(), mAccountId, mUser.getId(), mUser.getScreenName());
		}

	}

	static class FriendshipLoader extends AsyncTaskLoader<Response<Relationship>> {

		private final Context context;
		private final long account_id, user_id;

		public FriendshipLoader(final Context context, final long account_id, final long user_id) {
			super(context);
			this.context = context;
			this.account_id = account_id;
			this.user_id = user_id;
		}

		@Override
		public Response<Relationship> loadInBackground() {
			return getFriendship();
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		private Response<Relationship> getFriendship() {
			if (account_id == user_id) return new Response<Relationship>(null, null);
			final Twitter twitter = getTwitterInstance(context, account_id, false);
			try {
				final Relationship result = twitter.showFriendship(account_id, user_id);
				return new Response<Relationship>(result, null);
			} catch (final TwitterException e) {
				return new Response<Relationship>(null, e);
			}
		}
	}

	final class IncomingFriendshipsAction extends ListAction {

		@Override
		public long getId() {
			return 5;
		}

		@Override
		public String getName() {
			return getString(R.string.incoming_friendships);
		}

		@Override
		public void onClick() {
			if (mUser == null) return;
			openIncomingFriendships(getActivity(), mAccountId);
		}

	}

	static final class Response<T> {
		public final T value;
		public final Exception exception;

		public Response(final T value1, final Exception exception) {
			this.value = value1;
			this.exception = exception;
		}
	}

	final class SavedSearchesAction extends ListAction {

		@Override
		public long getId() {
			return 4;
		}

		@Override
		public String getName() {
			return getString(R.string.saved_searches);
		}

		@Override
		public void onClick() {
			if (mUser == null) return;
			openSavedSearches(getActivity(), mAccountId);
		}

	}

	final class UserBlocksAction extends ListAction {

		@Override
		public long getId() {
			return 6;
		}

		@Override
		public String getName() {
			return getString(R.string.blocked_users);
		}

		@Override
		public void onClick() {
			if (mUser == null) return;
			openUserBlocks(getActivity(), mAccountId);
		}

	}

	static final class UserInfoLoader extends AsyncTaskLoader<Response<User>> {

		private final Twitter twitter;
		private final long user_id;
		private final String screen_name;

		private UserInfoLoader(final Context context, final long account_id, final long user_id,
				final String screen_name) {
			super(context);
			twitter = getTwitterInstance(context, account_id, true);
			this.user_id = user_id;
			this.screen_name = screen_name;
		}

		@Override
		public Response<User> loadInBackground() {
			if (twitter == null) return new Response<User>(null, null);
			try {
				if (user_id != -1)
					return new Response<User>(twitter.showUser(user_id), null);
				else if (screen_name != null) return new Response<User>(twitter.showUser(screen_name), null);
			} catch (final TwitterException e) {
				return new Response<User>(null, e);
			}
			return new Response<User>(null, null);
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

	}

	final class UserListsAction extends ListAction {

		@Override
		public long getId() {
			return 3;
		}

		@Override
		public String getName() {
			return getString(R.string.user_list);
		}

		@Override
		public void onClick() {
			if (mUser == null) return;
			openUserLists(getActivity(), mAccountId, mUser.getId(), mUser.getScreenName());
		}

	}

	final class UserMentionsAction extends ListAction {

		@Override
		public long getId() {
			return 2;
		}

		@Override
		public String getName() {
			return getString(R.string.user_mentions);
		}

		@Override
		public void onClick() {
			if (mUser == null) return;
			openUserMentions(getActivity(), mAccountId, mUser.getScreenName());
		}

	}

}
