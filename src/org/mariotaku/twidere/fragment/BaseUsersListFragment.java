package org.mariotaku.twidere.fragment;

import java.util.List;

import org.mariotaku.twidere.adapter.UsersAdapter;

import twitter4j.User;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public abstract class BaseUsersListFragment extends BaseListFragment implements LoaderCallbacks<List<User>>,
		OnItemClickListener {

	private UsersAdapter mAdapter;
	private SharedPreferences mPreferences;
	private boolean mDisplayProfileImage;
	private ListView mListView;
	private long mAccountId;

	public abstract Loader<List<User>> newLoaderInstance();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);

		Bundle args = getArguments() != null ? getArguments() : new Bundle();
		mAccountId = args.getLong(INTENT_KEY_ACCOUNT_ID);
		mAdapter = new UsersAdapter(getActivity());
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public Loader<List<User>> onCreateLoader(int id, Bundle args) {
		setProgressBarIndeterminateVisibility(true);
		return newLoaderInstance();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		User user = mAdapter.getItem(position);
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME_TWIDERE);
		builder.authority(AUTHORITY_USER);
		builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(mAccountId));
		builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, user.getScreenName());
		startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
	}

	@Override
	public void onLoaderReset(Loader<List<User>> loader) {
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
		setProgressBarIndeterminateVisibility(false);
		mAdapter.clear();
		if (data != null) {
			for (User user : data) {
				mAdapter.add(user);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mAdapter.setDisplayProfileImage(display_profile_image);
		if (mDisplayProfileImage != display_profile_image) {
			mDisplayProfileImage = display_profile_image;
			mListView.invalidateViews();
		}
	}

}
