package org.mariotaku.twidere.fragment.support;

import static org.mariotaku.twidere.util.Utils.openUserFavorites;
import static org.mariotaku.twidere.util.Utils.openUserListMemberships;
import static org.mariotaku.twidere.util.Utils.openUserLists;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.openUserTimeline;

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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.FiltersActivity;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.activity.SettingsActivity;
import org.mariotaku.twidere.activity.support.ColorSelectorActivity;
import org.mariotaku.twidere.activity.support.SignInActivity;
import org.mariotaku.twidere.activity.support.UserProfileEditorActivity;
import org.mariotaku.twidere.adapter.AccountsDrawerAdapter;
import org.mariotaku.twidere.adapter.AccountsDrawerAdapter.GroupItem;
import org.mariotaku.twidere.adapter.AccountsDrawerAdapter.OnAccountActivateStateChangeListener;
import org.mariotaku.twidere.adapter.AccountsDrawerAdapter.OptionItem;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Outbox;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

public class AccountsDrawerFragment extends BaseSupportFragment implements LoaderCallbacks<Cursor>,
		OnChildClickListener, OnSharedPreferenceChangeListener, OnAccountActivateStateChangeListener {

	private static final String FRAGMENT_TAG_ACCOUNT_DELETION = "account_deletion";
	private ContentResolver mResolver;
	private SharedPreferences mPreferences;

	private ExpandableListView mListView;
	private AccountsDrawerAdapter mAdapter;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, AccountsDrawerFragment.this);
			}
		}
	};

	@Override
	public void onAccountActivateStateChanged(final Account account, final boolean activated) {
		final ContentValues values = new ContentValues();
		values.put(Accounts.IS_ACTIVATED, activated);
		final String where = Accounts.ACCOUNT_ID + " = " + account.account_id;
		mResolver.update(Accounts.CONTENT_URI, values, where, null);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = getContentResolver();
		mAdapter = new AccountsDrawerAdapter(getView().getContext());
		mAdapter.setOnAccountActivateStateChangeListener(this);
		mListView.setAdapter(mAdapter);
		mListView.setOnChildClickListener(this);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_SET_COLOR: {
				if (resultCode == Activity.RESULT_OK) if (data != null && data.getExtras() != null) {
					final int color = data.getIntExtra(Accounts.USER_COLOR, Color.WHITE);
					final ContentValues values = new ContentValues();
					values.put(Accounts.USER_COLOR, color);
					final String where = Accounts.ACCOUNT_ID + " = " + mAdapter.getSelectedAccountId();
					mResolver.update(Accounts.CONTENT_URI, values, where, null);
					getLoaderManager().restartLoader(0, null, this);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onChildClick(final ExpandableListView parent, final View v, final int groupPosition,
			final int childPosition, final long id) {
		final GroupItem groupItem = mAdapter.getGroup(groupPosition);
		switch (groupItem.getId()) {
			case AccountsDrawerAdapter.GROUP_ID_ACCOUNTS: {
				final Object child = mAdapter.getChild(groupPosition, childPosition);
				if (!(child instanceof Account)) return false;
				final Account account = (Account) child;
				mAdapter.setSelectedAccountId(account.account_id);
				break;
			}
			case AccountsDrawerAdapter.GROUP_ID_ACCOUNT_OPTIONS: {
				final Account account = Account.getAccount(getActivity(), mAdapter.getSelectedAccountId());
				if (account == null) return false;
				final OptionItem option = (OptionItem) mAdapter.getChild(groupPosition, childPosition);
				switch (option.getId()) {
					case MENU_VIEW_PROFILE: {
						openUserProfile(getActivity(), account.account_id, account.account_id, account.screen_name);
						break;
					}
					case MENU_STATUSES: {
						openUserTimeline(getActivity(), account.account_id, account.account_id, account.screen_name);
						break;
					}
					case MENU_FAVORITES: {
						openUserFavorites(getActivity(), account.account_id, account.account_id, account.screen_name);
						break;
					}
					case MENU_LISTS: {
						openUserLists(getActivity(), account.account_id, account.account_id, account.screen_name);
						break;
					}
					case MENU_LIST_MEMBERSHIPS: {
						openUserListMemberships(getActivity(), account.account_id, account.account_id,
								account.screen_name);
						break;
					}
					case MENU_EDIT: {
						final Bundle bundle = new Bundle();
						bundle.putLong(EXTRA_ACCOUNT_ID, account.account_id);
						final Intent intent = new Intent(INTENT_ACTION_EDIT_USER_PROFILE);
						intent.setClass(getActivity(), UserProfileEditorActivity.class);
						intent.putExtras(bundle);
						startActivity(intent);
						break;
					}
					case MENU_SET_COLOR: {
						final Intent intent = new Intent(getActivity(), ColorSelectorActivity.class);
						final Bundle bundle = new Bundle();
						bundle.putInt(Accounts.USER_COLOR, account.user_color);
						intent.putExtras(bundle);
						startActivityForResult(intent, REQUEST_SET_COLOR);
						break;
					}
					case MENU_SET_AS_DEFAULT: {
						mPreferences.edit().putLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, account.account_id).commit();
						break;
					}
					case MENU_DELETE: {
						final AccountDeletionDialogFragment f = new AccountDeletionDialogFragment();
						final Bundle args = new Bundle();
						args.putLong(EXTRA_ACCOUNT_ID, account.account_id);
						f.setArguments(args);
						f.show(getChildFragmentManager(), FRAGMENT_TAG_ACCOUNT_DELETION);
						break;
					}
				}
				break;
			}
			case AccountsDrawerAdapter.GROUP_ID_MENU: {
				final OptionItem option = (OptionItem) mAdapter.getChild(groupPosition, childPosition);
				switch (option.getId()) {
					case MENU_SEARCH: {
						getActivity().onSearchRequested();
						break;
					}
					case MENU_ADD_ACCOUNT: {
						final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
						intent.setClass(getActivity(), SignInActivity.class);
						startActivity(intent);
						break;
					}
					case MENU_FILTERS: {
						final Intent intent = new Intent(getActivity(), FiltersActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(intent);
						break;
					}
					case MENU_SETTINGS: {
						final Intent intent = new Intent(getActivity(), SettingsActivity.class);
						intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
						startActivity(intent);
						break;
					}
				}
				closeAccountsDrawer();
				break;
			}
		}
		return true;
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(getActivity(), Accounts.CONTENT_URI, Accounts.COLUMNS, null, null, null);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final Context theme = new ContextThemeWrapper(getActivity(), R.style.Theme_Twidere_Drawer_Dark);
		final View view = LayoutInflater.from(theme).inflate(R.layout.accounts_drawer, container, false);
		mListView = (ExpandableListView) view.findViewById(android.R.id.list);
		return view;
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.setAccountsCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		if (data != null && data.getCount() > 0 && mAdapter.getSelectedAccountId() <= 0) {
			data.moveToFirst();
			mAdapter.setSelectedAccountId(data.getLong(data.getColumnIndex(Accounts.ACCOUNT_ID)));
		}
		mAdapter.setAccountsCursor(data);
		for (int i = 0, count = mAdapter.getGroupCount(); i < count; i++) {
			mListView.expandGroup(i);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdapter.setDefaultAccountId(mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1));
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (PREFERENCE_KEY_DEFAULT_ACCOUNT_ID.equals(key)) {
			mAdapter.setDefaultAccountId(mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1));
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		registerReceiver(mStatusReceiver, filter);
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	private void closeAccountsDrawer() {
		final Activity activity = getActivity();
		if (activity instanceof HomeActivity) {
			((HomeActivity) activity).closeAccountsDrawer();
		}
	}

	public static final class AccountDeletionDialogFragment extends BaseSupportDialogFragment implements
			DialogInterface.OnClickListener {

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final Bundle args = getArguments();
			final long account_id = args != null ? args.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
			if (account_id < 0) return;
			final ContentResolver resolver = getContentResolver();
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					resolver.delete(Accounts.CONTENT_URI, Accounts.ACCOUNT_ID + " = " + account_id, null);
					// Also delete tweets related to the account we previously
					// deleted.
					resolver.delete(Statuses.CONTENT_URI, Statuses.ACCOUNT_ID + " = " + account_id, null);
					resolver.delete(Mentions.CONTENT_URI, Mentions.ACCOUNT_ID + " = " + account_id, null);
					resolver.delete(Inbox.CONTENT_URI, DirectMessages.ACCOUNT_ID + " = " + account_id, null);
					resolver.delete(Outbox.CONTENT_URI, DirectMessages.ACCOUNT_ID + " = " + account_id, null);
					break;
				}
			}
		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setNegativeButton(android.R.string.cancel, null);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setTitle(R.string.account_delete_confirm_title);
			builder.setMessage(R.string.account_delete_confirm_message);
			return builder.create();
		}

	}

}
