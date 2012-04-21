package org.mariotaku.twidere.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.BaseActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.LazyImageLoader;

import com.actionbarsherlock.view.Menu;

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
	private Cursor mCursor;

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Intent intent;
		Bundle bundle;
		switch (item.getItemId()) {
			case MENU_VIEW:
				break;
			case MENU_SET_COLOR:
				if (mSelectedPosition >= 0) {
					intent = new Intent(INTENT_ACTION_SET_COLOR);
					bundle = new Bundle();
					bundle.putInt(Accounts.USER_COLOR, mSelectedColor);
					intent.putExtras(bundle);
					startActivityForResult(intent, REQUEST_SET_COLOR);
				}
				break;
			case MENU_EDIT_API:
				break;
			case MENU_DELETE:
				confirmDelection();
				break;
		}
		return super.onContextItemSelected(item);
	}

	private int mSelectedPosition, mSelectedColor;
	private long mSelectedUserId;
	private String mSelectedScreenName;

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

	private ContentResolver mResolver;

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		new MenuInflater(getSherlockActivity()).inflate(R.menu.context_account, menu);
		if (menuInfo instanceof AdapterContextMenuInfo) {
			mSelectedPosition = ((AdapterContextMenuInfo) menuInfo).position - 1;
		}
		if (mSelectedPosition >= 0 && mCursor != null) {
			mCursor.moveToPosition(mSelectedPosition);
			mSelectedColor = mCursor.getInt(mUserColorIdx);
			mSelectedUserId = mCursor.getLong(mUserIdIdx);
			mSelectedScreenName = mCursor.getString(mUsernameIdx);
		}
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	private AccountsAdapter mAdapter;

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Fragment fragment = new MeFragment();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.dashboard, fragment);
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
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
	public void onCreateOptionsMenu(Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		inflater.inflate(R.menu.menu_accounts, menu);
		super.onCreateOptionsMenu(menu, inflater);
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

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Accounts.CONTENT_URI;
		String[] cols = Accounts.COLUMNS;
		StringBuilder where = new StringBuilder();
		where.append(Accounts.IS_ACTIVATED + "=1");
		return new CursorLoader(getSherlockActivity(), uri, cols, null, null, null);
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

	private int mUserColorIdx, mUserIdIdx, mProfileImageIdx, mUsernameIdx;

	private class AccountsAdapter extends SimpleCursorAdapter {

		private LazyImageLoader mImageLoader;

		private class ViewHolder {

			private ImageView profile_image, color_indicator;
			private int user_color;
			private String username;
			private long user_id;

			public ViewHolder(View view) {
				profile_image = (ImageView) view.findViewById(R.id.profile_image);
				color_indicator = (ImageView) view.findViewById(R.id.color);
			}

			public void setAccountColor(int color) {
				color_indicator.getDrawable().mutate()
						.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
			}
		}

		public AccountsAdapter(Context context, LazyImageLoader loader) {
			super(context, R.layout.account_list_item, null, new String[] { Accounts.USERNAME },
					new int[] { android.R.id.text1 }, 0);
			mImageLoader = loader;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = super.newView(context, cursor, parent);
			ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
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
				mUserIdIdx = cursor.getColumnIndexOrThrow(Accounts.USER_ID);
				mUsernameIdx = cursor.getColumnIndexOrThrow(Accounts.USERNAME);
			}
		}
	}

	private void confirmDelection() {
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		mFragment.show(ft, "delete_confirm");
	}

	DeleteConfirmFragment mFragment = new DeleteConfirmFragment();

	private class DeleteConfirmFragment extends BaseDialogFragment implements OnClickListener {

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

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {

			}

		}

	}
}
