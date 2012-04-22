package org.mariotaku.twidere.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.BaseActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.LazyImageLoader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

public class AccountsFragment extends BaseListFragment implements LoaderCallbacks<Cursor> {

	private ListView mListView;

	private int mSelectedColor;

	private long mSelectedUserId;
	private String mSelectedScreenName;
	private ContentResolver mResolver;

	private static final long INVALID_ID = -1;

	private AccountsAdapter mAdapter;

	private int mUserColorIdx, mUserIdIdx, mProfileImageIdx, mUsernameIdx;
	private DeleteConfirmFragment mFragment = new DeleteConfirmFragment();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		mResolver = getSherlockActivity().getContentResolver();
		mAdapter = new AccountsAdapter(getSherlockActivity(), imageloader);
		getLoaderManager().initLoader(0, null, this);
		mListView = getListView();
		mListView.setOnCreateContextMenuListener(this);
		setListAdapter(mAdapter);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_SET_COLOR:
				if (resultCode == BaseActivity.RESULT_OK) {
					if (data != null && data.getExtras() != null) {
						int color = data.getIntExtra(Accounts.USER_COLOR, Color.WHITE);
						ContentValues values = new ContentValues();
						values.put(Accounts.USER_COLOR, color);
						String where = Accounts.USER_ID + "=" + mSelectedUserId;
						mResolver.update(Accounts.CONTENT_URI, values, where, null);
						getLoaderManager().restartLoader(0, null, this);
					}
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
			case MENU_VIEW:
				showDetails(mSelectedUserId);
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
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		new MenuInflater(getSherlockActivity()).inflate(R.menu.context_account, menu);

		AdapterContextMenuInfo adapterinfo = (AdapterContextMenuInfo) menuInfo;

		Object tag = adapterinfo.targetView.getTag();
		if (tag instanceof ViewHolder) {
			ViewHolder holder = (ViewHolder) tag;
			mSelectedColor = holder.user_color;
			mSelectedUserId = holder.user_id;
			mSelectedScreenName = holder.username;
			menu.setHeaderTitle(holder.username);
		} else {
			mSelectedUserId = INVALID_ID;
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = Accounts.COLUMNS;
		StringBuilder where = new StringBuilder();
		where.append(Accounts.IS_ACTIVATED + "=1");
		return new CursorLoader(getSherlockActivity(), uri, cols, null, null, null);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Object tag = v.getTag();
		if (tag instanceof ViewHolder) {
			ViewHolder holder = (ViewHolder) tag;
			showDetails(holder.user_id);
		}
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD_ACCOUNT:
				startActivity(new Intent(INTENT_ACTION_TWITTER_LOGIN));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void confirmDelection() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		mFragment.show(ft, "delete_confirm");
	}

	private void showDetails(long user_id) {
		Fragment fragment = new MeFragment();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.dashboard, fragment);
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}

	private class AccountsAdapter extends SimpleCursorAdapter {

		private LazyImageLoader mImageLoader;

		public AccountsAdapter(Context context, LazyImageLoader loader) {
			super(context, R.layout.account_list_item, null, new String[] { Accounts.USERNAME },
					new int[] { android.R.id.text1 }, 0);
			mImageLoader = loader;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			int color = cursor.getInt(mUserColorIdx);
			ViewHolder holder = (ViewHolder) view.getTag();
			holder.user_color = color;
			holder.user_id = cursor.getLong(mUserIdIdx);
			holder.username = cursor.getString(mUsernameIdx);
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
				mUserIdIdx = cursor.getColumnIndexOrThrow(Accounts.USER_ID);
				mUsernameIdx = cursor.getColumnIndexOrThrow(Accounts.USERNAME);
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
				case DialogInterface.BUTTON_POSITIVE:
					Cursor cur = mResolver.query(Accounts.CONTENT_URI, new String[0],
							Accounts.IS_ACTIVATED + "=1", null, null);
					if (cur == null) {
						break;
					}
					// Have more than one accounts? Then delete the account we
					// selected.
					if (cur.getCount() > 1) {
						mResolver.delete(Accounts.CONTENT_URI, Accounts.USER_ID + "="
								+ mSelectedUserId, null);
						// Also delete tweets related to the account we
						// previously deleted.
						mResolver.delete(Statuses.CONTENT_URI, Statuses.ACCOUNT_ID + "="
								+ mSelectedUserId, null);
						mResolver.delete(Mentions.CONTENT_URI, Mentions.ACCOUNT_ID + "="
								+ mSelectedUserId, null);
						AccountsFragment.this.getLoaderManager().restartLoader(0, null,
								AccountsFragment.this);
					} else {
						// Do something else if we only have one account.
					}
					cur.close();
					break;
			}

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
			builder.setIcon(android.R.drawable.ic_dialog_alert);
			builder.setTitle(R.string.delete_account);
			builder.setMessage(getString(R.string.delete_account_desc, mSelectedScreenName));
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

	}

	private class ViewHolder {

		public ImageView profile_image, color_indicator;
		public int user_color;
		public String username;
		public long user_id;

		public ViewHolder(View view) {
			profile_image = (ImageView) view.findViewById(R.id.profile_image);
			color_indicator = (ImageView) view.findViewById(R.id.color);
		}

		public void setAccountColor(int color) {
			color_indicator.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
		}
	}
}
