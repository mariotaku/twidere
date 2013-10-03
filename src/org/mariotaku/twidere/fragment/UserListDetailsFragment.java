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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.addIntentToMenu;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getLocalizedNumber;
import static org.mariotaku.twidere.util.Utils.getNameDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.openUserListMembers;
import static org.mariotaku.twidere.util.Utils.openUserListSubscribers;
import static org.mariotaku.twidere.util.Utils.openUserListTimeline;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.InputFilter;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ListActionAdapter;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.model.ListAction;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;

import java.util.Locale;

public class UserListDetailsFragment extends BaseSupportListFragment implements OnClickListener, OnLongClickListener,
		OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener,
		LoaderCallbacks<SingleResponse<ParcelableUserList>>, Panes.Right {

	private ImageLoaderWrapper mProfileImageLoader;
	private AsyncTwitterWrapper mTwitterWrapper;

	private ImageView mProfileImageView;
	private TextView mListNameView, mCreatedByView, mDescriptionView, mErrorMessageView;
	private View mListContainer, mErrorRetryContainer;
	private ColorLabelRelativeLayout mProfileContainer;
	private View mNameContainer, mDescriptionContainer;
	private Button mSubscribeMoreButton, mRetryButton;
	private ListView mListView;
	private View mHeaderView;

	private ListActionAdapter mAdapter;

	private PopupMenu mPopupMenu;

	private ParcelableUserList mUserList;
	private Locale mLocale;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			final ParcelableUserList user_list = intent.getParcelableExtra(EXTRA_USER_LIST);
			if (user_list == null || mUserList == null || !intent.getBooleanExtra(EXTRA_SUCCEED, false)) return;
			if (BROADCAST_USER_LIST_DETAILS_UPDATED.equals(action)) {
				if (user_list.id == mUserList.id) {
					reloadUserListInfo();
				}
			} else if (BROADCAST_USER_LIST_SUBSCRIBED.equals(action) || BROADCAST_USER_LIST_UNSUBSCRIBED.equals(action)) {
				if (user_list.id == mUserList.id) {
					reloadUserListInfo();
				}
			}
		}
	};

	public void changeUserList(final ParcelableUserList list) {
		if (list == null || getActivity() == null) return;
		getLoaderManager().destroyLoader(0);
		final boolean is_myself = list.account_id == list.user_id;
		mErrorRetryContainer.setVisibility(View.GONE);
		mUserList = list;
		mProfileContainer.drawEnd(getAccountColor(getActivity(), list.account_id));
		mListNameView.setText(list.name);
		final boolean display_screen_name = getNameDisplayOptionInt(getActivity()) == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME;
		final String name = display_screen_name ? "@" + list.user_screen_name : list.user_name;
		mCreatedByView.setText(getString(R.string.created_by, name));
		final String description = list.description;
		mDescriptionContainer.setVisibility(is_myself || !isEmpty(description) ? View.VISIBLE : View.GONE);
		mDescriptionContainer.setOnLongClickListener(this);
		mDescriptionView.setText(description);
		final TwidereLinkify linkify = new TwidereLinkify(new OnLinkClickHandler(getActivity()));
		linkify.applyAllLinks(mDescriptionView, list.account_id, false);
		mDescriptionView.setMovementMethod(LinkMovementMethod.getInstance());
		mProfileImageLoader.displayProfileImage(mProfileImageView, list.user_profile_image_url);
		if (list.user_id == list.account_id) {
			mSubscribeMoreButton.setText(R.string.more);
			mSubscribeMoreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.expander_open_holo, 0);
		} else {
			mSubscribeMoreButton.setText(list.is_following ? R.string.unsubscribe : R.string.subscribe);
			mSubscribeMoreButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
		}
		mAdapter.notifyDataSetChanged();
	}

	public void getUserListInfo(final boolean init, final long account_id, final int list_id, final String list_name,
			final long user_id, final String screen_name) {
		getLoaderManager().destroyLoader(0);
		if (!isMyAccount(getActivity(), account_id)) {
			mListContainer.setVisibility(View.GONE);
			mErrorRetryContainer.setVisibility(View.GONE);
			return;
		}

		if (list_id > 0 || list_name != null && (user_id > 0 || screen_name != null)) {
		} else {
			mListContainer.setVisibility(View.GONE);
			mErrorRetryContainer.setVisibility(View.GONE);
			return;
		}
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ACCOUNT_ID, account_id);
		args.putLong(EXTRA_USER_ID, user_id);
		args.putInt(EXTRA_LIST_ID, list_id);
		args.putString(EXTRA_LIST_NAME, list_name);
		args.putString(EXTRA_SCREEN_NAME, screen_name);
		if (init) {
			getLoaderManager().initLoader(0, args, this);
		} else {
			getLoaderManager().restartLoader(0, args, this);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mTwitterWrapper = getApplication().getTwitterWrapper();
		mLocale = getResources().getConfiguration().locale;
		super.onActivityCreated(savedInstanceState);
		mProfileImageLoader = getApplication().getImageLoaderWrapper();
		mAdapter = new ListActionAdapter(getActivity());
		mAdapter.add(new ListTimelineAction(1));
		mAdapter.add(new ListMembersAction(2));
		mAdapter.add(new ListSubscribersAction(3));
		mProfileImageView.setOnClickListener(this);
		mProfileImageView.setOnLongClickListener(this);
		mNameContainer.setOnClickListener(this);
		mNameContainer.setOnLongClickListener(this);
		mSubscribeMoreButton.setOnClickListener(this);
		mRetryButton.setOnClickListener(this);
		setListAdapter(null);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		setListAdapter(mAdapter);
		final Bundle args = getArguments();
		final long account_id = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		final long user_id = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
		final int list_id = args != null ? args.getInt(EXTRA_LIST_ID, -1) : -1;
		final String list_name = args != null ? args.getString(EXTRA_LIST_NAME) : null;
		final String screen_name = args != null ? args.getString(EXTRA_SCREEN_NAME) : null;
		getUserListInfo(true, account_id, list_id, list_name, user_id, screen_name);
	}

	@Override
	public void onClick(final View view) {
		switch (view.getId()) {
			case R.id.subscribe_more: {
				if (mUserList == null) return;
				if (mUserList.account_id != mUserList.user_id) {
					mSubscribeMoreButton.setVisibility(View.GONE);
					if (mUserList.is_following) {
						mTwitterWrapper.destroyUserListSubscription(mUserList.account_id, mUserList.id);
					} else {
						mTwitterWrapper.createUserListSubscription(mUserList.account_id, mUserList.id);
					}
				} else {
					mPopupMenu = PopupMenu.getInstance(getActivity(), view);
					mPopupMenu.inflate(R.menu.action_user_list_details);
					final Menu menu = mPopupMenu.getMenu();
					final Intent extensions_intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_USER_LIST);
					final Bundle extensions_extras = new Bundle();
					extensions_extras.putParcelable(EXTRA_USER_LIST, mUserList);
					extensions_intent.putExtras(extensions_extras);
					addIntentToMenu(getActivity(), menu, extensions_intent);
					mPopupMenu.setOnMenuItemClickListener(this);
					mPopupMenu.show();
				}
				break;
			}
			case R.id.retry: {
				reloadUserListInfo();
				break;
			}
			case R.id.profile_image: {
				if (mUserList == null) return;
				openUserProfile(getActivity(), mUserList.account_id, mUserList.user_id, mUserList.user_screen_name);
				break;
			}
		}

	}

	@Override
	public Loader<SingleResponse<ParcelableUserList>> onCreateLoader(final int id, final Bundle args) {
		mListContainer.setVisibility(View.VISIBLE);
		mErrorMessageView.setText(null);
		mErrorMessageView.setVisibility(View.GONE);
		mErrorRetryContainer.setVisibility(View.GONE);
		setListShown(false);
		setProgressBarIndeterminateVisibility(true);
		final long account_id = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		final long user_id = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
		final int list_id = args != null ? args.getInt(EXTRA_LIST_ID, -1) : -1;
		final String list_name = args != null ? args.getString(EXTRA_LIST_NAME) : null;
		final String screen_name = args != null ? args.getString(EXTRA_SCREEN_NAME) : null;
		return new ListInfoLoader(getActivity(), account_id, list_id, list_name, user_id, screen_name);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mHeaderView = inflater.inflate(R.layout.user_list_details_header, null);
		mProfileContainer = (ColorLabelRelativeLayout) mHeaderView.findViewById(R.id.profile_name_container);
		mNameContainer = mHeaderView.findViewById(R.id.name_container);
		mListNameView = (TextView) mHeaderView.findViewById(R.id.list_name);
		mCreatedByView = (TextView) mHeaderView.findViewById(R.id.created_by);
		mDescriptionView = (TextView) mHeaderView.findViewById(R.id.description);
		mProfileImageView = (ImageView) mHeaderView.findViewById(R.id.profile_image);
		mDescriptionContainer = mHeaderView.findViewById(R.id.description_container);
		mSubscribeMoreButton = (Button) mHeaderView.findViewById(R.id.subscribe_more);
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
		mUserList = null;
		getLoaderManager().destroyLoader(0);
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
	public void onLoaderReset(final Loader<SingleResponse<ParcelableUserList>> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<SingleResponse<ParcelableUserList>> loader,
			final SingleResponse<ParcelableUserList> data) {
		if (data == null) return;
		if (getActivity() == null) return;
		if (data.data != null) {
			final ParcelableUserList list = data.data;
			setListShown(true);
			changeUserList(list);
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

	@Override
	public boolean onLongClick(final View view) {
		if (mUserList == null) return false;
		switch (view.getId()) {
			case R.id.name_container:
			case R.id.description_container:
				final Bundle args = new Bundle();
				args.putLong(EXTRA_ACCOUNT_ID, mUserList.account_id);
				args.putString(EXTRA_LIST_NAME, mUserList.name);
				args.putString(EXTRA_DESCRIPTION, mUserList.description);
				args.putBoolean(EXTRA_IS_PUBLIC, mUserList.is_public);
				args.putInt(EXTRA_LIST_ID, mUserList.id);
				final DialogFragment f = new EditUserListDialogFragment();
				f.setArguments(args);
				f.show(getFragmentManager(), "edit_user_list_details");
				return true;
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD: {
				final Bundle args = new Bundle();
				args.putLong(EXTRA_ACCOUNT_ID, mUserList.account_id);
				args.putString(EXTRA_TEXT, "");
				args.putInt(EXTRA_LIST_ID, mUserList.id);
				final DialogFragment f = new AddMemberDialogFragment();
				f.setArguments(args);
				f.show(getFragmentManager(), "add_member");
				break;
			}
			case MENU_DELETE: {
				if (mUserList.user_id != mUserList.account_id) return false;
				mTwitterWrapper.destroyUserList(mUserList.account_id, mUserList.id);
				break;
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

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_USER_LIST_DETAILS_UPDATED);
		filter.addAction(BROADCAST_USER_LIST_SUBSCRIBED);
		filter.addAction(BROADCAST_USER_LIST_UNSUBSCRIBED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	private void reloadUserListInfo() {
		final Bundle args = getArguments();
		final long account_id = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		final long user_id = args != null ? args.getLong(EXTRA_USER_ID, -1) : -1;
		final int list_id = args != null ? args.getInt(EXTRA_LIST_ID, -1) : -1;
		final String list_name = args != null ? args.getString(EXTRA_LIST_NAME) : null;
		final String screen_name = args != null ? args.getString(EXTRA_SCREEN_NAME) : null;
		getUserListInfo(false, account_id, list_id, list_name, user_id, screen_name);
	}

	public static class AddMemberDialogFragment extends BaseSupportDialogFragment implements
			DialogInterface.OnClickListener {

		private AutoCompleteTextView mEditText;
		private String mText;
		private long mAccountId;
		private AsyncTwitterWrapper mTwitterWrapper;
		private int mListId;
		private UserHashtagAutoCompleteAdapter mUserAutoCompleteAdapter;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			if (mListId <= 0 || mAccountId <= 0) return;
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mText = ParseUtils.parseString(mEditText.getText());
					if (mText == null || mText.length() <= 0) return;
					mTwitterWrapper.addUserListMembers(mAccountId, mListId, mText);
					break;
				}
			}
		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			mTwitterWrapper = getApplication().getTwitterWrapper();
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mAccountId = bundle != null ? bundle.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			mListId = bundle != null ? bundle.getInt(EXTRA_LIST_ID, -1) : -1;
			mText = bundle != null ? bundle.getString(EXTRA_TEXT) : null;
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final View view = LayoutInflater.from(getActivity()).inflate(R.layout.auto_complete_textview, null);
			builder.setView(view);
			mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
			if (mText != null) {
				mEditText.setText(mText);
			}
			mUserAutoCompleteAdapter = new UserHashtagAutoCompleteAdapter(getActivity());
			mEditText.setAdapter(mUserAutoCompleteAdapter);
			mEditText.setThreshold(1);
			mEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
			builder.setTitle(R.string.screen_name);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(final Bundle outState) {
			outState.putLong(EXTRA_ACCOUNT_ID, mAccountId);
			outState.putInt(EXTRA_LIST_ID, mListId);
			outState.putString(EXTRA_TEXT, mText);
			super.onSaveInstanceState(outState);
		}

	}

	public static class EditUserListDialogFragment extends BaseSupportDialogFragment implements
			DialogInterface.OnClickListener {

		private EditText mEditName, mEditDescription;
		private CheckBox mPublicCheckBox;
		private String mName, mDescription;
		private long mAccountId;
		private int mListId;
		private boolean mIsPublic;
		private AsyncTwitterWrapper mTwitterWrapper;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			if (mAccountId <= 0) return;
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mName = ParseUtils.parseString(mEditName.getText());
					mDescription = ParseUtils.parseString(mEditDescription.getText());
					mIsPublic = mPublicCheckBox.isChecked();
					if (mName == null || mName.length() <= 0) return;
					mTwitterWrapper.updateUserListDetails(mAccountId, mListId, mIsPublic, mName, mDescription);
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			mTwitterWrapper = getApplication().getTwitterWrapper();
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mAccountId = bundle != null ? bundle.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			mListId = bundle != null ? bundle.getInt(EXTRA_LIST_ID, -1) : -1;
			mName = bundle != null ? bundle.getString(EXTRA_LIST_NAME) : null;
			mDescription = bundle != null ? bundle.getString(EXTRA_DESCRIPTION) : null;
			mIsPublic = bundle != null ? bundle.getBoolean(EXTRA_IS_PUBLIC, true) : true;
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_user_list_detail, null);
			builder.setView(view);
			mEditName = (EditText) view.findViewById(R.id.name);
			mEditDescription = (EditText) view.findViewById(R.id.description);
			mPublicCheckBox = (CheckBox) view.findViewById(R.id.is_public);
			if (mName != null) {
				mEditName.setText(mName);
			}
			if (mDescription != null) {
				mEditDescription.setText(mDescription);
			}
			mPublicCheckBox.setChecked(mIsPublic);
			builder.setTitle(R.string.user_list);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(final Bundle outState) {
			outState.putLong(EXTRA_ACCOUNT_ID, mAccountId);
			outState.putInt(EXTRA_LIST_ID, mListId);
			outState.putString(EXTRA_LIST_NAME, mName);
			outState.putString(EXTRA_DESCRIPTION, mDescription);
			outState.putBoolean(EXTRA_IS_PUBLIC, mIsPublic);
			super.onSaveInstanceState(outState);
		}

	}

	public static class ListInfoLoader extends AsyncTaskLoader<SingleResponse<ParcelableUserList>> {

		private final long account_id, user_id;
		private final int list_id;
		private final String screen_name, list_name;
		private final boolean hires_profile_image;

		private ListInfoLoader(final Context context, final long account_id, final int list_id, final String list_name,
				final long user_id, final String screen_name) {
			super(context);
			this.account_id = account_id;
			this.user_id = user_id;
			this.list_id = list_id;
			this.screen_name = screen_name;
			this.list_name = list_name;
			hires_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
		}

		@Override
		public SingleResponse<ParcelableUserList> loadInBackground() {
			final Twitter twitter = getTwitterInstance(getContext(), account_id, true);
			if (twitter == null) return SingleResponse.nullInstance();
			try {
				final UserList list;
				if (list_id > 0) {
					list = twitter.showUserList(list_id);
				} else if (user_id > 0) {
					list = twitter.showUserList(list_name, user_id);
				} else if (screen_name != null) {
					list = twitter.showUserList(list_name, screen_name);
				} else
					return SingleResponse.nullInstance();
				return new SingleResponse<ParcelableUserList>(new ParcelableUserList(list, account_id,
						hires_profile_image), null);
			} catch (final TwitterException e) {
				return new SingleResponse<ParcelableUserList>(null, e);
			}
		}

		@Override
		public void onStartLoading() {
			forceLoad();
		}

	}

	class ListMembersAction extends ListAction {

		public ListMembersAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.list_members);
		}

		@Override
		public String getSummary() {
			if (mUserList == null) return null;
			return getLocalizedNumber(mLocale, mUserList.members_count);
		}

		@Override
		public void onClick() {
			openUserListMembers(getActivity(), mUserList);
		}

	}

	class ListSubscribersAction extends ListAction {

		public ListSubscribersAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.list_subscribers);
		}

		@Override
		public String getSummary() {
			if (mUserList == null) return null;
			return getLocalizedNumber(mLocale, mUserList.subscribers_count);
		}

		@Override
		public void onClick() {
			openUserListSubscribers(getActivity(), mUserList);
		}

	}

	class ListTimelineAction extends ListAction {

		public ListTimelineAction(final int order) {
			super(order);
		}

		@Override
		public String getName() {
			return getString(R.string.list_timeline);
		}

		@Override
		public void onClick() {
			if (mUserList == null) return;
			openUserListTimeline(getActivity(), mUserList);
		}

	}

}
