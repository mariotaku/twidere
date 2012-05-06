package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getTypeIcon;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusViewHolder;

import twitter4j.Query;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
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

public class TweetSearchFragment extends BaseListFragment implements LoaderCallbacks<List<Tweet>> {

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
	public Loader<List<Tweet>> onCreateLoader(int id, Bundle args) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		if (args != null) {
			long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			String query = args.getString(INTENT_KEY_QUERY);
			return new UserTimelineLoader(getSherlockActivity(), account_id, new Query(query));
		}
		return null;
	}

	@Override
	public void onLoaderReset(Loader<List<Tweet>> loader) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void onLoadFinished(Loader<List<Tweet>> loader, List<Tweet> data) {
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
		if (mDisplayProfileImage != display_profile_image || mDisplayName != display_name) {
			mDisplayProfileImage = display_profile_image;
			mDisplayName = display_name;
			mListView.invalidateViews();
		}
	}

	public static class UserTimelineLoader extends AsyncTaskLoader<List<Tweet>> {

		private final Twitter mTwitter;
		private final Query mQuery;

		public UserTimelineLoader(Context context, long account_id, Query query) {
			super(context);
			mTwitter = getTwitterInstance(context, account_id, true);
			mQuery = query;
		}

		@Override
		public void deliverResult(List<Tweet> data) {
			super.deliverResult(data);
		}

		@Override
		public List<Tweet> loadInBackground() {
			try {
				return mTwitter.search(mQuery).getTweets();
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

	private static class UserTimelineAdapter extends ArrayAdapter<Tweet> {

		private LazyImageLoader image_loader;

		private boolean mDisplayProfileImage;

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
			Tweet tweet = getItem(position);
			boolean has_media = tweet.getMediaEntities() != null && tweet.getMediaEntities().length > 0;
			boolean has_location = tweet.getGeoLocation() != null;
			holder.name_view.setText("@" + tweet.getFromUser());
			holder.text_view.setText(tweet.getText());
			holder.tweet_time_view.setText(formatToShortTimeString(getContext(), tweet.getCreatedAt().getTime()));
			holder.tweet_time_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getTypeIcon(false, has_location, has_media), 0);
			holder.profile_image_view.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				image_loader.displayImage(parseURL(tweet.getProfileImageUrl()), holder.profile_image_view);
			}
			return view;
		}

		public URL parseURL(String url_string) {
			try {
				return new URL(url_string);
			} catch (MalformedURLException e) {
				// Do nothing.
			}
			return null;
		}

		public void setDisplayProfileImage(boolean display) {
			mDisplayProfileImage = display;
		}

	}

}
