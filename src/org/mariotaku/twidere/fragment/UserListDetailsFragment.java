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

import static org.mariotaku.twidere.util.Utils.findUserList;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyActivatedAccount;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.openTweetSearch;
import static org.mariotaku.twidere.util.Utils.openUserBlocks;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.*;


import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.TwidereLinkify.OnLinkClickListener;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.UserList;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UserListDetailsFragment extends BaseListFragment implements OnClickListener, OnLongClickListener,
		OnItemClickListener, OnItemLongClickListener, OnLinkClickListener {

	private LazyImageLoader mProfileImageLoader;
	private ImageView mProfileImageView;
	private GetFriendshipTask mFollowInfoTask;
	private View mFollowContainer;
	private TextView mListNameView, mUserNameView, mDescriptionView;
	private View mNameContainer, mProfileImageContainer, mDescriptionContainer;
	private ProgressBar mFollowProgress, mListProgress;
	private Button mFollowButton, mRetryButton;

	private UserProfileActionAdapter mAdapter;
	private ListView mListView;
	private UserInfoTask mUserInfoTask;
	private View mHeaderView;
	private long mAccountId, mUserId;
	private Relationship mFriendship;
	private final DialogFragment mDialogFragment = new EditTextDialogFragment();

	private UserList mUser = null;

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
					reloadUserListInfo();
				}
			}
		}
	};

	private static final int TYPE_NAME = 1;

	private static final int TYPE_URL = 2;

	private static final int TYPE_LOCATION = 3;

	private static final int TYPE_DESCRIPTION = 4;

	private int mUserListId;

	private String mUserName, mUserScreenName, mListName;

	private ServiceInterface mService;

	private boolean mFollowInfoDisplayed = false;
	private SharedPreferences mPreferences;
	private boolean mDisplayName;

	public void changeUser(long account_id, UserList user_list) {
		if (user_list == null || getActivity() == null || !isMyActivatedAccount(getActivity(), account_id)) return;
		if (mUserInfoTask != null && mUserInfoTask.getStatus() == AsyncTask.Status.RUNNING) {
			mUserInfoTask.cancel(true);
		}
		final User user = user_list.getUser();
		if (user == null) return;
		final boolean is_my_activated_account = isMyActivatedAccount(getActivity(), user_list.getId());
		mUserInfoTask = null;
		mRetryButton.setVisibility(View.GONE);
		mAccountId = account_id;
		mUserListId = user_list.getId();
		mUserName = user.getName();
		mUserId = user.getId();
		mUserScreenName = user.getScreenName();
		mListName = user_list.getName();

		final boolean is_multiple_account_enabled = getActivatedAccountIds(getActivity()).length > 1;

		mListView.setBackgroundResource(is_multiple_account_enabled ? R.drawable.ic_label_color : 0);
		if (is_multiple_account_enabled) {
			final Drawable d = mListView.getBackground();
			if (d != null) {
				d.mutate().setColorFilter(getAccountColor(getActivity(), account_id), PorterDuff.Mode.MULTIPLY);
			}
		}

		mListNameView.setText(mListName);
		mUserNameView.setText(mDisplayName ? mUserName : mUserScreenName);
		final String description = user_list.getDescription();
		mDescriptionContainer.setVisibility(is_my_activated_account || !isNullOrEmpty(description) ? View.VISIBLE
				: View.GONE);
		mDescriptionContainer.setOnLongClickListener(this);
		mDescriptionView.setText(description);
		final TwidereLinkify linkify = new TwidereLinkify(mDescriptionView);
		linkify.setOnLinkClickListener(this);
		linkify.addAllLinks();
		mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
		mProfileImageLoader.displayImage(user.getProfileImageURL(), mProfileImageView);
		mUser = user_list;
		mAdapter.notifyDataSetChanged();
		showFollowInfo(false);
	}

	public void getUserInfo(long account_id, int list_id, String list_name, long user_id, String screen_name) {
		mAccountId = account_id;
		mUserListId = list_id;
		mUserName = screen_name;
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
		
		if (list_id > 0 || (list_name != null && (user_id > 0 || screen_name != null))) {
			mUserInfoTask = new UserInfoTask(getActivity(), account_id, list_id, list_name, user_id, screen_name);
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
		mService = getApplication().getServiceInterface();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		super.onActivityCreated(savedInstanceState);
		final Bundle args = getArguments();
		long account_id = -1, user_id = -1;
		int list_id = -1;
		String screen_name = null, list_name = null;
		if (args != null) {
			account_id = args.getLong(INTENT_KEY_ACCOUNT_ID, -1);
			user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			list_id = args.getInt(INTENT_KEY_LIST_ID, -1);
			list_name = args.getString(INTENT_KEY_LIST_NAME);
			screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
		}
		mProfileImageLoader = getApplication().getProfileImageLoader();
		mAdapter = new UserProfileActionAdapter(getActivity());
		mAdapter.add(new ListTimelineAction());
		mAdapter.add(new ListMembersAction());
		mAdapter.add(new ListSubscribersAction());
		mProfileImageContainer.setOnClickListener(this);
		mProfileImageContainer.setOnLongClickListener(this);
		mNameContainer.setOnClickListener(this);
		mNameContainer.setOnLongClickListener(this);
		mFollowButton.setOnClickListener(this);
		mRetryButton.setOnClickListener(this);
		setListAdapter(null);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		setListAdapter(mAdapter);
		getUserInfo(account_id, list_id, list_name, user_id, screen_name);

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.follow: {
				if (mUser != null && mAccountId != mUser.getId()) {
					if (mFriendship.isSourceFollowingTarget()) {
						mService.destroyFriendship(mAccountId, mUser.getId());
					} else {
						mService.createFriendship(mAccountId, mUser.getId());
					}
				}
				break;
			}
			case R.id.retry: {
				reloadUserListInfo();
				break;
			}
			case R.id.name_container: {
				if (mUser != null) {
				}
				break;
			}
			case R.id.profile_image_container: {
				//TODO
				break;
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mHeaderView = inflater.inflate(R.layout.user_profile_header, null, false);
		mNameContainer = mHeaderView.findViewById(R.id.name_container);
		mListNameView = (TextView) mHeaderView.findViewById(R.id.list_name);
		mUserNameView = (TextView) mHeaderView.findViewById(R.id.user_name);
		mDescriptionView = (TextView) mHeaderView.findViewById(R.id.description);
		mProfileImageView = (ImageView) mHeaderView.findViewById(R.id.profile_image);
		mProfileImageContainer = mHeaderView.findViewById(R.id.profile_image_container);
		mDescriptionContainer = mHeaderView.findViewById(R.id.description_container);
		mFollowContainer = mHeaderView.findViewById(R.id.follow_container);
		mFollowButton = (Button) mHeaderView.findViewById(R.id.follow);
		mFollowProgress = (ProgressBar) mHeaderView.findViewById(R.id.follow_progress);
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
	public void onLinkClick(String link, int type) {
		if (mUser == null) return;
		switch (type) {
			case TwidereLinkify.LINK_TYPE_MENTION: {
				openUserProfile(getActivity(), mAccountId, -1, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_HASHTAG: {
				openTweetSearch(getActivity(), mAccountId, link);
				break;
			}
			case TwidereLinkify.LINK_TYPE_IMAGE: {
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
	public boolean onLongClick(View view) {
		if (mUser == null) return false;
		final boolean is_my_activated_account = isMyActivatedAccount(getActivity(), mUser.getId());
		if (!is_my_activated_account) return false;
		switch (view.getId()) {
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
		}
		return false;
	}

	@Override
	public void onStart() {
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
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


	private void reloadUserListInfo() {
		getUserInfo(mAccountId, mUserListId, mUserName, mUserId, mUserName);
	}

	private void showFollowInfo(boolean force) {
		if (mFollowInfoDisplayed && !force) return;
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		mFollowInfoTask = new GetFriendshipTask();
		mFollowInfoTask.execute();
	}

	public static class EditTextDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener {
		private EditText mEditText;
		private String mText;
		private int mType;
		private String mTitle;
		private long mAccountId;
		private ServiceInterface mService;

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

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			mService = getApplication().getServiceInterface();
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mAccountId = bundle != null ? bundle.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
			mText = bundle != null ? bundle.getString(INTENT_KEY_TEXT) : null;
			mType = bundle != null ? bundle.getInt(INTENT_KEY_TYPE, -1) : -1;
			mTitle = bundle != null ? bundle.getString(INTENT_KEY_TITLE) : null;
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
			builder.setTitle(mTitle);
			builder.setView(mEditText);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(Bundle outState) {
			outState.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
			outState.putString(INTENT_KEY_TEXT, mText);
			outState.putInt(INTENT_KEY_TYPE, mType);
			outState.putString(INTENT_KEY_TITLE, mTitle);
			super.onSaveInstanceState(outState);
		}

	}

	private class ListSubscribersAction extends UserAction {

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

	private class ListTimelineAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.list_timeline);
		}

		@Override
		public String getSummary() {
			//if (mUser == null) return null;
			//return String.valueOf(mUser.getFavouritesCount());
			return null;
		}

		@Override
		public void onClick() {
			if (mUser == null) return;
			openListTimeline(getActivity(), mAccountId, mUserListId, mUserId, mUserName, mListName);
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
				if (result.value != null) {
					mFriendship = result.value;
					mFollowButton.setVisibility(View.VISIBLE);
					mFollowButton.setText(mFriendship.isSourceFollowingTarget() ? R.string.unfollow : R.string.follow);
					if (!is_my_activated_account) {
					}
					mFollowInfoDisplayed = true;
				}
			}
			mFollowProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
			mFollowInfoTask = null;
		}

		@Override
		protected void onPreExecute() {
			mFollowContainer.setVisibility(is_my_activated_account ? View.GONE : View.VISIBLE);
			mFollowButton.setVisibility(View.GONE);
			mFollowProgress.setVisibility(View.VISIBLE);
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

	private class ListMembersAction extends UserAction {

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

	private class UserInfoTask extends AsyncTask<Void, Void, Response<UserList>> {

		private final Twitter twitter;
		private final long user_id;
		private final int list_id;
		private final String screen_name, list_name;

		private UserInfoTask(Context context, long account_id, int list_id, String list_name, long user_id, String screen_name) {
			twitter = getTwitterInstance(context, account_id, true);
			this.user_id = user_id;
			this.list_id = list_id;
			this.screen_name = screen_name;
			this.list_name = list_name;
		}

		@Override
		protected Response<UserList> doInBackground(Void... args) {
			try {
				if (list_id > 0)
					return new Response<UserList>(twitter.showUserList(list_id), null);
				else if (user_id > 0) {
					final UserList list = findUserList(twitter, user_id, list_name);
					if (list != null && list.getId() > 0) return new Response<UserList>(twitter.showUserList(list.getId()), null);
				} else if (screen_name != null && list_name != null) {
					final UserList list = findUserList(twitter, screen_name, list_name);
					if (list != null && list.getId() > 0) return new Response<UserList>(twitter.showUserList(list.getId()), null);
				}
				return new Response<UserList>(null, null);
			} catch (final TwitterException e) {
				return new Response<UserList>(null, e);
			}
		}

		@Override
		protected void onCancelled() {
			setProgressBarIndeterminateVisibility(false);
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Response<UserList> result) {
			if (result == null) return;
			if (getActivity() == null) return;
			if (result.value != null) {
				final UserList user = result.value;
				setListShown(true);
				changeUser(mAccountId, user);
				mRetryButton.setVisibility(View.GONE);
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
			final int count = getCount();
			for (int i = 0; i < count; i++) {
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
