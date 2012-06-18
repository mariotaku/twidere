package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.parseURL;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ProfileImageLoader;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;

public class AccountsFragment extends BaseListFragment implements LoaderCallbacks<Cursor>, OnItemLongClickListener,
		OnMenuItemClickListener {

	private ListView mListView;

	private int mSelectedColor;

	private long mSelectedUserId;

	private ContentResolver mResolver;

	private PopupMenu mPopupMenu;

	private boolean mActivityFirstCreated;

	private static final long INVALID_ID = -1;

	private Cursor mCursor;
	private AccountsAdapter mAdapter;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, AccountsFragment.this);
			}
		}
	};

	private SharedPreferences mPreferences;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		ProfileImageLoader imageloader = ((TwidereApplication) getActivity().getApplication()).getProfileImageLoader();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = getContentResolver();
		mAdapter = new AccountsAdapter(getActivity(), imageloader);
		getLoaderManager().initLoader(0, null, this);
		mListView = getListView();
		mListView.setOnItemLongClickListener(this);
		setListAdapter(mAdapter);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SET_COLOR: {
				if (resultCode == Activity.RESULT_OK) if (data != null && data.getExtras() != null) {
					int color = data.getIntExtra(Accounts.USER_COLOR, Color.WHITE);
					ContentValues values = new ContentValues();
					values.put(Accounts.USER_COLOR, color);
					String where = Accounts.USER_ID + "=" + mSelectedUserId;
					mResolver.update(Accounts.CONTENT_URI, values, where, null);
					getLoaderManager().restartLoader(0, null, this);
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mActivityFirstCreated = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = Accounts.COLUMNS;
		String where = Accounts.IS_ACTIVATED + " = 1";
		return new CursorLoader(getActivity(), uri, cols, where, null, null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.accounts, null);
	}

	@Override
	public void onDestroy() {
		mActivityFirstCreated = true;
		super.onDestroy();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		if (mCursor != null && position >= 0 && position < mCursor.getCount()) {
			mCursor.moveToPosition(position);
			mSelectedColor = mCursor.getInt(mCursor.getColumnIndexOrThrow(Accounts.USER_COLOR));
			mSelectedUserId = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
			mPopupMenu = new PopupMenu(getActivity(), view);
			mPopupMenu.inflate(R.menu.context_account);
			mPopupMenu.setOnMenuItemClickListener(this);
			mPopupMenu.show();
			return true;
		}
		return false;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mCursor != null && position >= 0 && position < mCursor.getCount()) {
			mCursor.moveToPosition(position);
			long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
			openUserProfile(getActivity(), user_id, user_id, null);
		}
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursor = null;
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mCursor = data;
		mAdapter.changeCursor(data);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_VIEW: {
				openUserProfile(getActivity(), mSelectedUserId, mSelectedUserId, null);
				break;
			}
			case MENU_SET_COLOR: {
				if (mSelectedUserId != INVALID_ID) {
					final Intent intent = new Intent(INTENT_ACTION_SET_COLOR);
					Bundle bundle = new Bundle();
					bundle.putInt(Accounts.USER_COLOR, mSelectedColor);
					intent.putExtras(bundle);
					startActivityForResult(intent, REQUEST_SET_COLOR);
				}
				break;
			}
			case MENU_SET_AS_DEFAULT: {
				if (mSelectedUserId != INVALID_ID) {
					mPreferences.edit().putLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, mSelectedUserId).commit();
					mAdapter.notifyDataSetChanged();
				}
				break;
			}
			case MENU_DELETE: {
				mResolver.delete(Accounts.CONTENT_URI, Accounts.USER_ID + " = " + mSelectedUserId, null);
				// Also delete tweets related to the account we previously
				// deleted.
				mResolver.delete(Statuses.CONTENT_URI, Statuses.ACCOUNT_ID + " = " + mSelectedUserId, null);
				mResolver.delete(Mentions.CONTENT_URI, Mentions.ACCOUNT_ID + " = " + mSelectedUserId, null);
				if (getActivatedAccountIds(getActivity()).length > 0) {
					AccountsFragment.this.getLoaderManager().restartLoader(0, null, AccountsFragment.this);
				} else {
					getActivity().finish();
				}
				break;
			}
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD_ACCOUNT:
				startActivity(new Intent(INTENT_ACTION_TWITTER_LOGIN));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED);
		if (getActivity() != null) {
			getActivity().registerReceiver(mStatusReceiver, filter);
		}
		if (!mActivityFirstCreated) {
			getLoaderManager().restartLoader(0, null, this);
		}
	}

	@Override
	public void onStop() {
		if (getActivity() != null) {
			getActivity().unregisterReceiver(mStatusReceiver);
		}
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		mActivityFirstCreated = false;
		super.onStop();
	}

	public static class ViewHolder {

		public final ImageView profile_image;
		private final View content, default_indicator;

		public ViewHolder(View view) {
			content = view;
			profile_image = (ImageView) view.findViewById(android.R.id.icon);
			default_indicator = view.findViewById(android.R.id.text2);
		}

		public void setAccountColor(int color) {
			content.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		}

		public void setIsDefault(boolean is_default) {
			default_indicator.setVisibility(is_default ? View.VISIBLE : View.GONE);
		}
	}

	private static class AccountsAdapter extends SimpleCursorAdapter {

		private final ProfileImageLoader mImageLoader;
		private final SharedPreferences mPreferences;
		private int mUserColorIdx, mProfileImageIdx, mUserIdIdx;
		private long mDefaultAccountId;

		public AccountsAdapter(Context context, ProfileImageLoader loader) {
			super(context, R.layout.account_list_item, null, new String[] { Accounts.USERNAME },
					new int[] { android.R.id.text1 }, 0);
			mImageLoader = loader;
			mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			int color = cursor.getInt(mUserColorIdx);
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.setAccountColor(color);
			holder.setIsDefault(mDefaultAccountId != -1 && mDefaultAccountId == cursor.getLong(mUserIdIdx));
			mImageLoader.displayImage(parseURL(cursor.getString(mProfileImageIdx)), holder.profile_image);
			super.bindView(view, context, cursor);
		}

		@Override
		public void changeCursor(Cursor cursor) {
			super.changeCursor(cursor);
			if (cursor != null) {
				mUserColorIdx = cursor.getColumnIndex(Accounts.USER_COLOR);
				mProfileImageIdx = cursor.getColumnIndex(Accounts.PROFILE_IMAGE_URL);
				mUserIdIdx = cursor.getColumnIndex(Accounts.USER_ID);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			final View view = super.newView(context, cursor, parent);
			final ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
		}

		@Override
		public void notifyDataSetChanged() {
			mDefaultAccountId = mPreferences.getLong(PREFERENCE_KEY_DEFAULT_ACCOUNT_ID, -1);
			super.notifyDataSetChanged();
		}
	}
}
