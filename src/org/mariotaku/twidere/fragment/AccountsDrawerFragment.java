package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.CroutonStyle;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ColorSelectorActivity;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.activity.SignInActivity;
import org.mariotaku.twidere.activity.UserProfileEditorActivity;
import org.mariotaku.twidere.adapter.AccountsDrawerAdapter;
import org.mariotaku.twidere.adapter.AccountsDrawerAdapter.AccountAction;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Inbox;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.Outbox;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.view.iface.IExtendedView;
import org.mariotaku.twidere.view.iface.IExtendedView.OnSizeChangedListener;

public class AccountsDrawerFragment extends BaseSupportFragment implements LoaderCallbacks<Cursor>,
		OnSizeChangedListener, OnGroupExpandListener, OnChildClickListener, OnClickListener,
		OnSharedPreferenceChangeListener, OnItemLongClickListener {

	private static final String FRAGMENT_TAG_ACCOUNT_DELETION = "account_deletion";
	private ContentResolver mResolver;
	private SharedPreferences mPreferences;

	private ExpandableListView mListView;
	private AccountsDrawerAdapter mAdapter;
	private Button mAddAccountButton;

	private long mSelectedAccountId;

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
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = getContentResolver();
		mAdapter = new AccountsDrawerAdapter(getActivity());
		mListView.setAdapter(mAdapter);
		mListView.setOnItemLongClickListener(this);
		mListView.setOnGroupExpandListener(this);
		mListView.setOnChildClickListener(this);
		mAddAccountButton.setOnClickListener(this);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
		if (savedInstanceState != null) {
			mSelectedAccountId = savedInstanceState.getLong(EXTRA_ACCOUNT_ID);
		}
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
					final String where = Accounts.ACCOUNT_ID + " = " + mSelectedAccountId;
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
		final Account account = mAdapter.getGroup(groupPosition);
		if (account == null) return false;
		mSelectedAccountId = account.account_id;
		final AccountAction action = mAdapter.getChild(groupPosition, childPosition);
		switch (action.id) {
			case MENU_VIEW_PROFILE: {
				openUserProfile(getActivity(), account.account_id, account.account_id, account.screen_name);
				closeAccountsDrawer();
				break;
			}
			case MENU_STATUSES: {
				openUserTimeline(getActivity(), account.account_id, account.account_id, account.screen_name);
				closeAccountsDrawer();
				break;
			}
			case MENU_FAVORITES: {
				openUserFavorites(getActivity(), account.account_id, account.account_id, account.screen_name);
				closeAccountsDrawer();
				break;
			}
			case MENU_LISTS: {
				openUserLists(getActivity(), account.account_id, account.account_id, account.screen_name);
				closeAccountsDrawer();
				break;
			}
			case MENU_LIST_MEMBERSHIPS: {
				openUserListMemberships(getActivity(), account.account_id, account.account_id, account.screen_name);
				closeAccountsDrawer();
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
		return true;
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.add_account: {
				final Intent intent = new Intent(INTENT_ACTION_TWITTER_LOGIN);
				intent.setClass(getActivity(), SignInActivity.class);
				startActivity(intent);
				closeAccountsDrawer();
				break;
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
		return new CursorLoader(getActivity(), Accounts.CONTENT_URI, Accounts.COLUMNS, null, null, null);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.accounts_drawer, container, false);
		((IExtendedView) view).setOnSizeChangedListener(this);
		mListView = (ExpandableListView) view.findViewById(android.R.id.list);
		mAddAccountButton = (Button) view.findViewById(R.id.add_account);
		return view;
	}

	@Override
	public void onGroupExpand(final int groupPosition) {
		for (int i = 0, count = mAdapter.getGroupCount(); i < count; i++) {
			if (i != groupPosition) {
				mListView.collapseGroup(i);
			}
		}
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> view, final View child, final int position, final long id) {
		final long packedPos = mListView.getExpandableListPosition(position);
		if (ExpandableListView.getPackedPositionType(packedPos) != ExpandableListView.PACKED_POSITION_TYPE_GROUP)
			return false;
		final int groupPosition = ExpandableListView.getPackedPositionGroup(packedPos);
		if (groupPosition == -1) return false;
		final Account account = mAdapter.getGroup(groupPosition);
		if (account == null) return false;
		if (account.is_activated && getActivatedAccountIds(getActivity()).length == 1) {
			Crouton.showText(getActivity(), R.string.no_account_selected, CroutonStyle.WARN);
			return true;
		}
		final ContentValues values = new ContentValues();
		values.put(Accounts.IS_ACTIVATED, account.is_activated ? 0 : 1);
		final String where = Accounts.ACCOUNT_ID + " = " + account.account_id;
		mResolver.update(Accounts.CONTENT_URI, values, where, null);
		return true;
	}

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {
		mAdapter.setAccountsCursor(null);
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
		mAdapter.setAccountsCursor(data);
	}

	@Override
	public void onResume() {
		super.onResume();
		mAdapter.setDefaultAccountId(mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1));
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(EXTRA_ACCOUNT_ID, mSelectedAccountId);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (PREFERENCE_KEY_DEFAULT_ACCOUNT_ID.equals(key)) {
			mAdapter.setDefaultAccountId(mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1));
		}
	}

	@Override
	public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
		if (mAdapter == null) return;
		mAdapter.setBannerWidth(w);
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
