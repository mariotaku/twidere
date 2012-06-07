package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.BaseActivity;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageView;
import android.widget.ListView;

public class AccountsFragment extends BaseListFragment implements LoaderCallbacks<Cursor>, OnBackStackChangedListener {

	private ListView mListView;

	private int mSelectedColor;

	private long mSelectedUserId;

	private String mSelectedScreenName;
	private ContentResolver mResolver;

	private Fragment mDetailFragment;
	private boolean mActivityFirstCreated;

	private static final long INVALID_ID = -1;

	private Cursor mCursor;
	private AccountsAdapter mAdapter;
	private DeleteConfirmFragment mFragment = new DeleteConfirmFragment();

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_ACCOUNT_LIST_DATABASE_UPDATED.equals(action)) {
				getLoaderManager().restartLoader(0, null, AccountsFragment.this);
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getFragmentManager().addOnBackStackChangedListener(this);
		LazyImageLoader imageloader = ((TwidereApplication) getActivity().getApplication()).getProfileImageLoader();
		mResolver = getActivity().getContentResolver();
		mAdapter = new AccountsAdapter(getActivity(), imageloader);
		getLoaderManager().initLoader(0, null, this);
		mListView = getListView();
		mListView.setOnCreateContextMenuListener(this);
		setListAdapter(mAdapter);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SET_COLOR:
				if (resultCode == BaseActivity.RESULT_OK) if (data != null && data.getExtras() != null) {
					int color = data.getIntExtra(Accounts.USER_COLOR, Color.WHITE);
					ContentValues values = new ContentValues();
					values.put(Accounts.USER_COLOR, color);
					String where = Accounts.USER_ID + "=" + mSelectedUserId;
					mResolver.update(Accounts.CONTENT_URI, values, where, null);
					getLoaderManager().restartLoader(0, null, this);
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackStackChanged() {
		if (getActivity() instanceof HomeActivity) {
			boolean is_displaying_details = mDetailFragment != null && mDetailFragment.isAdded();
			((HomeActivity) getActivity()).setPagingEnabled(!is_displaying_details);
		}

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case MENU_VIEW:
				openUserProfile(mSelectedUserId);
				break;
			case MENU_SET_COLOR:
				if (mSelectedUserId != INVALID_ID) {
					intent = new Intent(INTENT_ACTION_SET_COLOR);
					Bundle bundle = new Bundle();
					bundle.putInt(Accounts.USER_COLOR, mSelectedColor);
					intent.putExtras(bundle);
					startActivityForResult(intent, REQUEST_SET_COLOR);
				}
				break;
			case MENU_DELETE:
				confirmDelection();
				break;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		mActivityFirstCreated = true;
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		new MenuInflater(getActivity()).inflate(R.menu.context_account, menu);

		AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo) menuInfo;

		if (mCursor != null && adapterinfo.position >= 0 && adapterinfo.position < mCursor.getCount()) {
			mCursor.moveToPosition(adapterinfo.position);
			mSelectedColor = mCursor.getInt(mCursor.getColumnIndexOrThrow(Accounts.USER_COLOR));
			mSelectedUserId = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
			mSelectedScreenName = mCursor.getString(mCursor.getColumnIndexOrThrow(Accounts.USERNAME));
			menu.setHeaderTitle(mSelectedScreenName);
		}

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = Accounts.COLUMNS;
		String where = Accounts.IS_ACTIVATED + " = 1";
		return new CursorLoader(getActivity(), uri, cols, where, null, null);
	}

	@Override
	public void onDestroy() {
		mActivityFirstCreated = true;
		super.onDestroy();
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (mCursor != null && position >= 0 && position < mCursor.getCount()) {
			mCursor.moveToPosition(position);
			long user_id = mCursor.getLong(mCursor.getColumnIndexOrThrow(Accounts.USER_ID));
			openUserProfile(user_id);
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
		mActivityFirstCreated = false;
		super.onStop();
	}

	private void confirmDelection() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		mFragment.show(ft, "delete_confirm");
	}

	private void openUserProfile(long account_id) {
		if (mDetailFragment == null) {
			mDetailFragment = Fragment.instantiate(getActivity(), UserProfileFragment.class.getName(), null);
		}
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Bundle args = new Bundle();
		args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
		args.putLong(INTENT_KEY_USER_ID, account_id);
		mDetailFragment.setArguments(args);
		FragmentActivity activity = getActivity();
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			HomeActivity home_activity = (HomeActivity) activity;
			home_activity.showAtPane(home_activity.getCurrentPane(), mDetailFragment);
		} else {
			ft.replace(R.id.dashboard, mDetailFragment);
			ft.addToBackStack(null);
			ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft.commit();
		}
	}

	public static class ViewHolder {

		public ImageView profile_image;
		public View content;

		public ViewHolder(View view) {
			profile_image = (ImageView) view.findViewById(android.R.id.icon);
			content = view;
		}

		public void setAccountColor(int color) {
			content.getBackground().mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		}
	}

	private static class AccountsAdapter extends SimpleCursorAdapter {

		private LazyImageLoader mImageLoader;

		private int mUserColorIdx, mProfileImageIdx;

		public AccountsAdapter(Context context, LazyImageLoader loader) {
			super(context, R.layout.account_list_item, null, new String[] { Accounts.USERNAME },
					new int[] { android.R.id.text1 }, 0);
			mImageLoader = loader;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			int color = cursor.getInt(mUserColorIdx);
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.setAccountColor(color);
			URL url = null;
			try {
				url = new URL(cursor.getString(mProfileImageIdx));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			mImageLoader.displayImage(url, holder.profile_image);
			super.bindView(view, context, cursor);
		}

		@Override
		public void changeCursor(Cursor cursor) {
			super.changeCursor(cursor);
			if (cursor != null) {
				mUserColorIdx = cursor.getColumnIndexOrThrow(Accounts.USER_COLOR);
				mProfileImageIdx = cursor.getColumnIndexOrThrow(Accounts.PROFILE_IMAGE_URL);
			}
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = super.newView(context, cursor, parent);
			ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
		}
	}

	private class DeleteConfirmFragment extends BaseDialogFragment implements OnClickListener {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mResolver.delete(Accounts.CONTENT_URI, Accounts.USER_ID + "=" + mSelectedUserId, null);
					// Also delete tweets related to the account we
					// previously deleted.
					mResolver.delete(Statuses.CONTENT_URI, Statuses.ACCOUNT_ID + "=" + mSelectedUserId, null);
					mResolver.delete(Mentions.CONTENT_URI, Mentions.ACCOUNT_ID + "=" + mSelectedUserId, null);
					if (getActivatedAccountIds(getActivity()).length > 0) {
						AccountsFragment.this.getLoaderManager().restartLoader(0, null, AccountsFragment.this);
					} else {
						getActivity().finish();
					}
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.delete_account);
			builder.setMessage(getString(R.string.delete_account_desc, mSelectedScreenName));
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}
	}
}
