package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.UserViewHolder;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SearchUsersFragment extends BaseListFragment implements LoaderCallbacks<ResponseList<User>> {

	private UsersAdapter mAdapter;
	private SharedPreferences mPreferences;
	private boolean mDisplayProfileImage;
	private ListView mListView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSherlockActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);

		Bundle args = getArguments() != null ? getArguments() : new Bundle();
		long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
		mAdapter = new UsersAdapter(getSherlockActivity(), account_id);
		mListView = getListView();
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public Loader<ResponseList<User>> onCreateLoader(int id, Bundle args) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		if (args != null) {
			long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			int page = args.getInt(INTENT_KEY_PAGE);
			String query = args.getString(INTENT_KEY_QUERY);
			return new UserSearchLoader(getSherlockActivity(), account_id, query, page);
		}
		return null;
	}

	@Override
	public void onLoaderReset(Loader<ResponseList<User>> loader) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(Loader<ResponseList<User>> loader, ResponseList<User> data) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
		if (data != null) {
			mAdapter.addAll(data);
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

	public static class UserSearchLoader extends AsyncTaskLoader<ResponseList<User>> {

		private final Twitter mTwitter;
		private final String mQuery;
		private final int mPage;

		public UserSearchLoader(Context context, long account_id, String query, int page) {
			super(context);
			mTwitter = getTwitterInstance(context, account_id, true);
			mQuery = query;
			mPage = page;
		}

		@Override
		public void deliverResult(ResponseList<User> data) {
			super.deliverResult(data);
		}

		@Override
		public ResponseList<User> loadInBackground() {
			try {
				return mTwitter.searchUsers(mQuery, mPage);
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void onStartLoading() {
			forceLoad();
		}

	}

	private static class UsersAdapter extends ArrayAdapter<User> {

		private final LazyImageLoader mImageLoader;
		private final ServiceInterface mServiceInterface;
		private final long mAccountId;

		private boolean mDisplayProfileImage;

		public UsersAdapter(Context context, long account_id) {
			super(context, R.layout.user_list_item, R.id.bio);
			TwidereApplication application = (TwidereApplication) context.getApplicationContext();
			mImageLoader = application.getListProfileImageLoader();
			mServiceInterface = application.getServiceInterface();
			mAccountId = account_id;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			Object tag = view.getTag();
			UserViewHolder holder = null;
			if (tag instanceof UserViewHolder) {
				holder = (UserViewHolder) tag;
			} else {
				holder = new UserViewHolder(view);
				view.setTag(holder);
			}
			final User user = getItem(position);
			holder.screen_name.setText('@' + user.getScreenName());
			holder.user_name.setText(user.getName());
			holder.bio.setText(user.getDescription());
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				mImageLoader.displayImage(user.getProfileImageURL(), holder.profile_image);
			}
			holder.follow.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					mServiceInterface.createFriendship(mAccountId, user.getId());
				}
			});
			return view;
		}

		public void setDisplayProfileImage(boolean display) {
			mDisplayProfileImage = display;
		}

	}

}
