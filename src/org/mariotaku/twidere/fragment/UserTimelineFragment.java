package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getTypeIcon;

import java.util.Collections;
import java.util.Comparator;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusViewHolder;

import twitter4j.ResponseList;
import twitter4j.Status;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UserTimelineFragment extends BaseListFragment implements LoaderCallbacks<ResponseList<Status>> {

	private UserTimelineAdapter mAdapter;
	private SharedPreferences mPreferences;
	private boolean mDisplayProfileImage;
	private boolean mDisplayName;
	private ListView mListView;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSherlockActivity().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		mAdapter = new UserTimelineAdapter(getSherlockActivity(), imageloader);
		mListView = getListView();
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(0, getArguments(), this);
	}

	@Override
	public Loader<ResponseList<Status>> onCreateLoader(int id, Bundle args) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		if (args != null) {
			long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			if (user_id == -1) {
				String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
				return new UserTimelineLoader(getSherlockActivity(), account_id, screen_name);
			}
			return new UserTimelineLoader(getSherlockActivity(), account_id, user_id);
		}
		return null;
	}

	@Override
	public void onLoaderReset(Loader<ResponseList<Status>> loader) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);

	}

	@Override
	public void onLoadFinished(Loader<ResponseList<Status>> loader, ResponseList<Status> data) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
		if (data != null) {
			mAdapter.addAll(data);
		}

	}

	@Override
	public void onResume() {
		super.onResume();
		boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayName(display_name);
		if (mDisplayProfileImage != display_profile_image || mDisplayName != display_name) {
			mDisplayProfileImage = display_profile_image;
			mDisplayName = display_name;
			mListView.invalidateViews();
		}
	}

	public static class UserTimelineLoader extends AsyncTaskLoader<ResponseList<Status>> {

		private final Twitter mTwitter;
		private final long mUserId;
		private final String mUserScreenName;

		/**
		 * Perform alphabetical comparison of application entry objects.
		 */
		public static final Comparator<Status> TIMESTAMP_COMPARATOR = new Comparator<Status>() {

			@Override
			public int compare(Status object1, Status object2) {
				long diff = object2.getCreatedAt().getTime() - object1.getCreatedAt().getTime();
				if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
				if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
				return (int) diff;
			}
		};

		public UserTimelineLoader(Context context, long account_id, long user_id) {
			super(context);
			mTwitter = getTwitterInstance(context, account_id, true);
			mUserId = user_id;
			mUserScreenName = null;
		}

		public UserTimelineLoader(Context context, long account_id, String user_screenname) {
			super(context);
			mTwitter = getTwitterInstance(context, account_id, true);
			mUserId = -1;
			mUserScreenName = user_screenname;
		}

		@Override
		public void deliverResult(ResponseList<Status> data) {
			if (data != null) {
				Collections.sort(data, TIMESTAMP_COMPARATOR);
			}
			super.deliverResult(data);
		}

		@Override
		public ResponseList<Status> loadInBackground() {
			try {
				if (mUserId != -1)
					return mTwitter.getUserTimeline(mUserId);
				else if (mUserScreenName != null) return mTwitter.getUserTimeline(mUserScreenName);
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

	private static class UserTimelineAdapter extends ArrayAdapter<Status> {

		private LazyImageLoader image_loader;

		private boolean mDisplayProfileImage, mDisplayName;

		public UserTimelineAdapter(Context context, LazyImageLoader image_loader) {
			super(context, R.layout.status_list_item, R.id.text);
			this.image_loader = image_loader;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			Object tag = view.getTag();
			StatusViewHolder holder = null;
			if (tag instanceof StatusViewHolder) {
				holder = (StatusViewHolder) tag;
			} else {
				holder = new StatusViewHolder(view);
				view.setTag(holder);
			}
			Status status = getItem(position);
			User user = status.getUser();
			boolean is_favorite = status.isFavorited();
			boolean has_media = status.getMediaEntities() != null && status.getMediaEntities().length > 0;
			boolean has_location = status.getGeoLocation() != null;
			boolean is_protected = user.isProtected();
			holder.name_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					is_protected ? R.drawable.ic_tweet_stat_is_protected : 0, 0);
			holder.name_view.setText(mDisplayName ? user.getName() : "@" + user.getScreenName());
			holder.text_view.setText(status.getText());
			holder.tweet_time_view.setText(formatToShortTimeString(getContext(), status.getCreatedAt().getTime()));
			holder.tweet_time_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getTypeIcon(is_favorite, has_location, has_media), 0);
			holder.profile_image_view.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				image_loader.displayImage(user.getProfileImageURL(), holder.profile_image_view);
			}
			return view;
		}

		public void setDisplayName(boolean display) {
			mDisplayName = display;
		}

		public void setDisplayProfileImage(boolean display) {
			mDisplayProfileImage = display;
		}

	}

}
