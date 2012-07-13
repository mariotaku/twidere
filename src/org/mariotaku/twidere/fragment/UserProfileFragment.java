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
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getImagePathFromUri;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.isMyActivatedAccount;
import static org.mariotaku.twidere.util.Utils.isMyActivatedUserName;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.makeCachedUserContentValues;
import static org.mariotaku.twidere.util.Utils.openUserBlocks;
import static org.mariotaku.twidere.util.Utils.openUserFavorites;
import static org.mariotaku.twidere.util.Utils.openUserFollowers;
import static org.mariotaku.twidere.util.Utils.openUserFollowing;
import static org.mariotaku.twidere.util.Utils.openUserTimeline;

import java.io.File;
import java.net.URL;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.GetExternalCacheDirAccessor;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.conf.Configuration;
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
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class UserProfileFragment extends BaseListFragment implements OnClickListener, OnLongClickListener,
		OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener {

	private LazyImageLoader mProfileImageLoader;
	private ImageView mProfileImageView;
	private GetFriendshipTask mFollowInfoTask;
	private View mFollowContainer, mMoreOptionsContainer;
	private TextView mNameView, mScreenNameView, mDescriptionView, mLocationView, mURLView, mCreatedAtView,
			mTweetCount, mFollowersCount, mFriendsCount;
	private View mNameContainer, mProfileImageContainer, mDescriptionContainer, mLocationContainer, mURLContainer,
			mTweetsContainer, mFollowersContainer, mFriendsContainer;
	private ProgressBar mFollowProgress, mMoreOptionsProgress, mListProgress;
	private Button mFollowButton, mMoreOptionsButton, mRetryButton;

	private UserProfileActionAdapter mAdapter;
	private ListView mListView;
	private UserInfoTask mUserInfoTask;
	private View mHeaderView;
	private long mAccountId;
	private Relationship mFriendship;
	private final DialogFragment mDialogFragment = new EditTextDialogFragment();
	private Uri mImageUri;

	private User mUser = null;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_FRIENDSHIP_CHANGED.equals(action)) {
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mAccountId
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					showFollowInfo(true);
				}
			}
			if (BROADCAST_BLOCKSTATE_CHANGED.equals(action)) {
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mAccountId
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					showFollowInfo(true);
				}
			}
			if (BROADCAST_PROFILE_UPDATED.equals(action)) {
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mAccountId
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					reloadUserInfo();
				}
			}
		}
	};

	private static final int TYPE_NAME = 1;

	private static final int TYPE_URL = 2;

	private static final int TYPE_LOCATION = 3;

	private static final int TYPE_DESCRIPTION = 4;

	private long mUserId;

	private String mScreenName;

	private ServiceInterface mService;

	private PopupMenu mPopupMenu;

	private boolean mFollowInfoDisplayed = false;

	public void changeUser(long account_id, User user) {
		if (user == null || getActivity() == null || !isMyActivatedAccount(getActivity(), account_id)) return;
		if (mUserInfoTask != null && mUserInfoTask.getStatus() == AsyncTask.Status.RUNNING) {
			mUserInfoTask.cancel(true);
		}
		final boolean is_my_activated_account = isMyActivatedAccount(getActivity(), user.getId());
		mUserInfoTask = null;
		mRetryButton.setVisibility(View.GONE);
		mAccountId = account_id;
		mUserId = user.getId();
		mScreenName = user.getScreenName();
		mNameView.setText(user.getName());
		mScreenNameView.setText(user.getScreenName());
		final String description = user.getDescription();
		mDescriptionContainer.setVisibility(is_my_activated_account || !isNullOrEmpty(description) ? View.VISIBLE
				: View.GONE);
		mDescriptionContainer.setOnLongClickListener(this);
		mDescriptionView.setText(description);
		final String location = user.getLocation();
		mLocationContainer
				.setVisibility(is_my_activated_account || !isNullOrEmpty(location) ? View.VISIBLE : View.GONE);
		mLocationContainer.setOnLongClickListener(this);
		mLocationView.setText(location);
		final String url = user.getURL() != null ? user.getURL().toString() : null;
		mURLContainer.setVisibility(is_my_activated_account || !isNullOrEmpty(url) ? View.VISIBLE : View.GONE);
		mURLContainer.setOnLongClickListener(this);
		mURLView.setText(url);
		mCreatedAtView.setText(formatToLongTimeString(getActivity(), user.getCreatedAt().getTime()));
		mTweetCount.setText(String.valueOf(user.getStatusesCount()));
		mFollowersCount.setText(String.valueOf(user.getFollowersCount()));
		mFriendsCount.setText(String.valueOf(user.getFriendsCount()));
		mProfileImageLoader.displayImage(user.getProfileImageURL(), mProfileImageView);
		mUser = user;
		mAdapter.notifyDataSetChanged();
		showFollowInfo(false);
	}

	public void getUserInfo(long account_id, long user_id, String screen_name) {
		mAccountId = account_id;
		mUserId = user_id;
		mScreenName = screen_name;
		if (mUserInfoTask != null && mUserInfoTask.getStatus() == AsyncTask.Status.RUNNING) {
			mUserInfoTask.cancel(true);
		}
		mUserInfoTask = null;
		if (!isMyActivatedAccount(getActivity(), mAccountId)) {
			mListProgress.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mRetryButton.setVisibility(View.GONE);
			return;
		}

		if (user_id != -1) {
			mUserInfoTask = new UserInfoTask(getActivity(), account_id, user_id);
		} else if (screen_name != null) {
			mUserInfoTask = new UserInfoTask(getActivity(), account_id, screen_name);
		} else {
			mListProgress.setVisibility(View.GONE);
			mListView.setVisibility(View.GONE);
			mRetryButton.setVisibility(View.GONE);
			return;
		}

		if (mUserInfoTask != null) {
			mUserInfoTask.execute();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mService = ServiceInterface.getInstance(getActivity());
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
		mAdapter = new UserProfileActionAdapter(getActivity());
		mAdapter.add(new FavoritesAction());
		if (isMyActivatedAccount(getActivity(), user_id) || isMyActivatedUserName(getActivity(), screen_name)) {
			mAdapter.add(new UserBlocksAction());
			mAdapter.add(new DirectMessagesAction());
		}
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
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode) {
			case REQUEST_TAKE_PHOTO: {
				if (resultCode == Activity.RESULT_OK) {
					final File file = new File(mImageUri.getPath());
					if (file.exists()) {
						mService.updateProfileImage(mUser.getId(), mImageUri, true);
					}
				}
				break;
			}
			case REQUEST_PICK_IMAGE: {
				if (resultCode == Activity.RESULT_OK) {
					final Uri uri = intent.getData();
					final File file = uri == null ? null : new File(getImagePathFromUri(getActivity(), uri));
					if (file != null && file.exists()) {
						mService.updateProfileImage(mUser.getId(), Uri.fromFile(file), false);
					}
				}
				break;
			}
		}

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.follow: {
				if (mUser != null && mAccountId != mUser.getId()) {
					final ServiceInterface service = ServiceInterface.getInstance(getActivity());
					if (mFriendship.isSourceFollowingTarget()) {
						service.destroyFriendship(mAccountId, mUser.getId());
					} else {
						service.createFriendship(mAccountId, mUser.getId());
					}
				}
				break;
			}
			case R.id.retry: {
				reloadUserInfo();
				break;
			}
			case R.id.name_container: {
				if (mUser != null) {
				}
				break;
			}
			case R.id.profile_image_container: {
				final Twitter twitter = getTwitterInstance(getActivity(), mAccountId, false);
				if (twitter != null) {
					final Configuration conf = twitter.getConfiguration();
					final Uri uri = Uri.parse(conf.getRestBaseURL() + "/users/profile_image?screen_name="
							+ mUser.getScreenName() + "&size=original");
					final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, uri);
					intent.setPackage(getActivity().getPackageName());
					startActivity(intent);
				}
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
				openUserFollowing(getActivity(), mAccountId, mUser.getId(), mUser.getScreenName());
				break;
			}
			case R.id.more_options: {
				if (mUser == null || mFriendship == null) return;
				if (!isMyActivatedAccount(getActivity(), mUser.getId())) {
					mPopupMenu = PopupMenu.getInstance(getActivity(), view);
					mPopupMenu.inflate(R.menu.action_user_profile);
					final MenuItem blockItem = mPopupMenu.getMenu().findItem(MENU_BLOCK);
					if (blockItem == null) return;
					final Drawable blockIcon = blockItem.getIcon();
					if (mFriendship.isSourceBlockingTarget()) {
						blockItem.setTitle(R.string.unblock);
						blockIcon.setColorFilter(getResources().getColor(R.color.holo_blue_bright),
								PorterDuff.Mode.MULTIPLY);
					} else {
						blockItem.setTitle(R.string.block);
						blockIcon.clearColorFilter();
					}
					mPopupMenu.setOnMenuItemClickListener(this);
					mPopupMenu.show();
				}
				break;
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
		mProfileImageView = (ImageView) mHeaderView.findViewById(R.id.profile_image);
		mProfileImageContainer = mHeaderView.findViewById(R.id.profile_image_container);
		mDescriptionContainer = mHeaderView.findViewById(R.id.description_container);
		mLocationContainer = mHeaderView.findViewById(R.id.location_container);
		mURLContainer = mHeaderView.findViewById(R.id.url_container);
		mFollowContainer = mHeaderView.findViewById(R.id.follow_container);
		mFollowButton = (Button) mHeaderView.findViewById(R.id.follow);
		mFollowProgress = (ProgressBar) mHeaderView.findViewById(R.id.follow_progress);
		mMoreOptionsContainer = mHeaderView.findViewById(R.id.more_options_container);
		mMoreOptionsButton = (Button) mHeaderView.findViewById(R.id.more_options);
		mMoreOptionsProgress = (ProgressBar) mHeaderView.findViewById(R.id.more_options_progress);
		final View view = inflater.inflate(R.layout.user_profile, null, false);
		mRetryButton = (Button) view.findViewById(R.id.retry);
		mListProgress = (ProgressBar) view.findViewById(R.id.list_progress);
		return view;
	}

	@Override
	public void onDestroyView() {
		mUser = null;
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		if (mUserInfoTask != null) {
			mUserInfoTask.cancel(true);
		}
		super.onDestroyView();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		final UserAction action = mAdapter.findItem(id);
		if (action != null) {
			action.onClick();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		final UserAction action = mAdapter.findItem(id);
		if (action != null) return action.onLongClick();
		return false;
	}

	@Override
	public boolean onLongClick(View view) {
		if (mUser == null) return false;
		final boolean is_my_activated_account = isMyActivatedAccount(getActivity(), mUser.getId());
		if (!is_my_activated_account) return false;
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
	public boolean onMenuItemClick(MenuItem item) {
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
				if (mService == null || mUser == null || mFriendship == null) {
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
				if (mService == null || mUser == null) {
					break;
				}
				mService.reportSpam(mAccountId, mUser.getId());
				break;
			}
			case MENU_MUTE: {
				if (mUser == null) {
					break;
				}
				final String screen_name = mUser.getScreenName();
				final Uri uri = Filters.Users.CONTENT_URI;
				final ContentValues values = new ContentValues();
				final SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFERENCES_NAME,
						Context.MODE_PRIVATE).edit();
				final ContentResolver resolver = getContentResolver();
				values.put(Filters.Users.TEXT, screen_name);
				resolver.delete(uri, Filters.Users.TEXT + " = '" + screen_name + "'", null);
				resolver.insert(uri, values);
				editor.putBoolean(PREFERENCE_KEY_ENABLE_FILTER, true).commit();
				Toast.makeText(getActivity(), R.string.user_muted, Toast.LENGTH_SHORT).show();
				break;
			}
		}
		return true;
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_FRIENDSHIP_CHANGED);
		filter.addAction(BROADCAST_PROFILE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	public void setListShown(boolean shown) {
		final int fade_in = android.R.anim.fade_in;
		final int fade_out = android.R.anim.fade_out;
		mListProgress.setVisibility(shown ? View.GONE : View.VISIBLE);
		mListProgress.startAnimation(AnimationUtils.loadAnimation(getActivity(), shown ? fade_out : fade_in));
		mListView.setVisibility(shown ? View.VISIBLE : View.GONE);
		mListView.startAnimation(AnimationUtils.loadAnimation(getActivity(), shown ? fade_in : fade_out));
	}

	private void pickImage() {
		final Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_PICK_IMAGE);
	}

	private void reloadUserInfo() {
		getUserInfo(mAccountId, mUserId, mScreenName);
	}

	private void showFollowInfo(boolean force) {
		if (mFollowInfoDisplayed && !force) return;
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		mFollowInfoTask = new GetFriendshipTask();
		mFollowInfoTask.execute();
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

	private class DirectMessagesAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.direct_messages);
		}

		@Override
		public void onClick() {
			if (mUser == null || !isMyActivatedAccount(getActivity(), mUser.getId())) return;
			final Bundle bundle = new Bundle();
			bundle.putLong(INTENT_KEY_ACCOUNT_ID, mUser.getId());
			final Intent intent = new Intent(INTENT_ACTION_DIRECT_MESSAGES);
			intent.putExtras(bundle);
			startActivity(intent);
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
		public void onSaveInstanceState(Bundle outState) {
			outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
			outState.putString(INTENT_KEY_TEXT, mText);
			outState.putInt(INTENT_KEY_TYPE, mType);
			outState.putString(INTENT_KEY_TITLE, mTitle);
			super.onSaveInstanceState(outState);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
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

		@SuppressWarnings("deprecation")
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			mService = ServiceInterface.getInstance(getActivity());
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mAccountId = bundle != null ? bundle.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
			mText = bundle != null ? bundle.getString(INTENT_KEY_TEXT) : null;
			mType = bundle != null ? bundle.getInt(INTENT_KEY_TYPE, -1) : -1;
			mTitle = bundle != null ? bundle.getString(INTENT_KEY_TITLE) : null;
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final FrameLayout layout = new FrameLayout(getActivity());
			mEditText = new EditText(getActivity());
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
			layout.addView(mEditText, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT) {

				{
					final int margin = (int) (getResources().getDisplayMetrics().density * 16);
					bottomMargin = margin;
					leftMargin = margin;
					rightMargin = margin;
					topMargin = margin;
				}
			});
			builder.setTitle(mTitle);
			builder.setView(layout);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

	}

	private class FavoritesAction extends UserAction {

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

	private class GetFriendshipTask extends AsyncTask<Void, Void, Response<Relationship>> {

		private final boolean is_my_activated_account;

		GetFriendshipTask() {
			is_my_activated_account = isMyActivatedAccount(getActivity(), mUser.getId());
		}

		@Override
		protected Response<Relationship> doInBackground(Void... params) {
			return getFriendship();
		}

		@Override
		protected void onPostExecute(Response<Relationship> result) {
			mFriendship = null;
			if (result.exception == null) {
				mFollowContainer.setVisibility(result.value == null ? View.GONE : View.VISIBLE);
				if (!is_my_activated_account) {
					mMoreOptionsContainer.setVisibility(result.value == null ? View.GONE : View.VISIBLE);
				}
				if (result.value != null) {
					mFriendship = result.value;
					mFollowButton.setVisibility(View.VISIBLE);
					mFollowButton.setText(mFriendship.isSourceFollowingTarget() ? R.string.unfollow : R.string.follow);
					if (!is_my_activated_account) {
						mMoreOptionsButton.setVisibility(View.VISIBLE);
					}
					mFollowInfoDisplayed = true;
				}
			}
			mFollowProgress.setVisibility(View.GONE);
			mMoreOptionsProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
			mFollowInfoTask = null;
		}

		@Override
		protected void onPreExecute() {
			mFollowContainer.setVisibility(is_my_activated_account ? View.GONE : View.VISIBLE);
			mFollowButton.setVisibility(View.GONE);
			mFollowProgress.setVisibility(View.VISIBLE);
			mMoreOptionsContainer.setVisibility(is_my_activated_account ? View.GONE : View.VISIBLE);
			mMoreOptionsButton.setVisibility(View.GONE);
			mMoreOptionsProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		private Response<Relationship> getFriendship() {
			if (mUser == null) return new Response<Relationship>(null, null);
			if (mAccountId == mUser.getId()) return new Response<Relationship>(null, null);
			final Twitter twitter = getTwitterInstance(getActivity(), mAccountId, false);
			try {
				final Relationship result = twitter.showFriendship(mAccountId, mUser.getId());
				return new Response<Relationship>(result, null);
			} catch (final TwitterException e) {
				return new Response<Relationship>(null, e);
			}
		}
	}

	private class Response<T> {
		public final T value;
		public final TwitterException exception;

		public Response(T value, TwitterException exception) {
			this.value = value;
			this.exception = exception;
		}
	}

	private abstract class UserAction {
		public abstract String getName();

		public String getSummary() {
			return null;
		}

		public void onClick() {

		}

		public boolean onLongClick() {
			return false;
		}

		@Override
		public final String toString() {
			return getName();
		}
	}

	private class UserBlocksAction extends UserAction {

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

	private class UserInfoTask extends AsyncTask<Void, Void, Response<User>> {

		private final Twitter twitter;
		private final long user_id;
		private final String screen_name;

		private UserInfoTask(Context context, long account_id, long user_id, String screen_name) {
			twitter = getTwitterInstance(context, account_id, true);
			this.user_id = user_id;
			this.screen_name = screen_name;
		}

		UserInfoTask(Context context, long account_id, long user_id) {
			this(context, account_id, user_id, null);
		}

		UserInfoTask(Context context, long account_id, String screen_name) {
			this(context, account_id, -1, screen_name);
		}

		@Override
		protected Response<User> doInBackground(Void... args) {
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
		protected void onCancelled() {
			setProgressBarIndeterminateVisibility(false);
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Response<User> result) {
			if (result == null) return;
			if (getActivity() == null) return;
			if (result.value != null) {
				final User user = result.value;
				final ContentResolver resolver = getContentResolver();
				resolver.delete(CachedUsers.CONTENT_URI, CachedUsers.USER_ID + "=" + result.value.getId(), null);
				resolver.insert(CachedUsers.CONTENT_URI, makeCachedUserContentValues(result.value));
				setListShown(true);
				changeUser(mAccountId, user);
				mRetryButton.setVisibility(View.GONE);
				if (isMyAccount(getActivity(), mUser.getId())) {
					final ContentValues values = new ContentValues();
					final URL profile_image_url = user.getProfileImageURL();
					if (profile_image_url != null) {
						values.put(Accounts.PROFILE_IMAGE_URL, profile_image_url.toString());
					}
					values.put(Accounts.USERNAME, mUser.getScreenName());
					final String where = Accounts.USER_ID + " = " + mUser.getId();
					getContentResolver().update(Accounts.CONTENT_URI, values, where, null);
				}
			} else {
				mListProgress.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
				mRetryButton.setVisibility(View.VISIBLE);
			}
			setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			setListShown(false);
			mRetryButton.setVisibility(View.GONE);
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

	}

	private class UserProfileActionAdapter extends ArrayAdapter<UserAction> {

		public UserProfileActionAdapter(Context context) {
			super(context, R.layout.user_action_list_item, android.R.id.text1);
		}

		public UserAction findItem(long id) {
			for (int i = 0; i < getCount(); i++) {
				if (id == getItemId(i)) return getItem(i);
			}
			return null;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final TextView summary_view = (TextView) view.findViewById(android.R.id.text2);
			final String summary = getItem(position).getSummary();
			summary_view.setText(summary);
			summary_view.setVisibility(!isNullOrEmpty(summary) ? View.VISIBLE : View.GONE);
			return view;
		}

	}
}
