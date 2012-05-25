package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.Utils;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UserProfileFragment extends BaseListFragment implements OnClickListener, OnItemClickListener {

	private LazyImageLoader mProfileImageLoader;
	private ImageView mProfileImageView;
	private FollowInfoTask mFollowInfoTask;
	private View mFollowIndicator;
	private TextView mNameView, mScreenNameView, mBioView, mLocationView, mWebView;
	private ProgressBar mProgress;
	private Button mFollowButton;

	private UserProfileActionAdapter mAdapter;
	private ListView mListView;
	private UserInfoTask mUserInfoTask;
	private View mHeaderView;
	private long mAccountId;
	private boolean mIsFollowing;

	private User mUser = null;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_FRIENDSHIP_CHANGED.equals(action)) {
				getFollowInfo();
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null) {
			mAccountId = getArguments().getLong(INTENT_KEY_ACCOUNT_ID);
		}
		mProfileImageLoader = ((TwidereApplication) getSherlockActivity().getApplication()).getListProfileImageLoader();
		mAdapter = new UserProfileActionAdapter(getSherlockActivity());
		mAdapter.add(new ViewStatusesAction());
		mAdapter.add(new ViewFollowersAction());
		mAdapter.add(new ViewFollowingAction());
		setListAdapter(null);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.setOnItemClickListener(this);
		setListAdapter(mAdapter);
		getUserInfo();

	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.follow: {
				if (mUser != null) {
					ServiceInterface service = ServiceInterface.getInstance(getSherlockActivity());
					if (mIsFollowing) {
						service.destroyFriendship(mAccountId, mUser.getId());
					} else {
						service.createFriendship(mAccountId, mUser.getId());
					}
				}
				break;
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mHeaderView = inflater.inflate(R.layout.user_profile_header, null, false);
		mNameView = (TextView) mHeaderView.findViewById(R.id.name);
		mScreenNameView = (TextView) mHeaderView.findViewById(R.id.screen_name);
		mProfileImageView = (ImageView) mHeaderView.findViewById(R.id.profile_image);
		mBioView = (TextView) mHeaderView.findViewById(R.id.bio);
		mLocationView = (TextView) mHeaderView.findViewById(R.id.location);
		mWebView = (TextView) mHeaderView.findViewById(R.id.web);
		mFollowButton = (Button) mHeaderView.findViewById(R.id.follow);
		mFollowButton.setOnClickListener(this);
		mFollowIndicator = mHeaderView.findViewById(R.id.follow_indicator);
		mProgress = (ProgressBar) mHeaderView.findViewById(R.id.progress);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onDestroyView() {
		mUser = null;
		super.onDestroyView();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		UserAction action = mAdapter.findItem(id);
		if (action != null) {
			action.execute();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_FRIENDSHIP_CHANGED);
		if (getSherlockActivity() != null) {
			getSherlockActivity().registerReceiver(mStatusReceiver, filter);
		}
	}

	@Override
	public void onStop() {
		if (getSherlockActivity() != null) {
			getSherlockActivity().unregisterReceiver(mStatusReceiver);
		}
		super.onStop();
	}

	private void getFollowInfo() {
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		mFollowInfoTask = new FollowInfoTask();
		mFollowInfoTask.execute();
	}

	private void getUserInfo() {
		if (mUserInfoTask != null && mUserInfoTask.getStatus() == AsyncTask.Status.RUNNING) {
			mUserInfoTask.cancel(true);
		}
		mUserInfoTask = null;
		Bundle args = getArguments();
		if (args != null) {
			long account_id = args.getLong(INTENT_KEY_ACCOUNT_ID);
			long user_id = args.getLong(INTENT_KEY_USER_ID, -1);
			String screen_name = args.getString(INTENT_KEY_SCREEN_NAME);
			if (user_id == -1 && screen_name != null) {
				mUserInfoTask = new UserInfoTask(getSherlockActivity(), account_id, screen_name);
			} else {
				mUserInfoTask = new UserInfoTask(getSherlockActivity(), account_id, user_id);
			}
		}
		if (mUserInfoTask != null) {
			mUserInfoTask.execute();
		}
	}

	private class FollowInfoTask extends AsyncTask<Void, Void, Response<Boolean>> {

		@Override
		protected Response<Boolean> doInBackground(Void... params) {
			return isFollowing();
		}

		@Override
		protected void onPostExecute(Response<Boolean> result) {
			mIsFollowing = false;
			if (result.exception == null) {
				mFollowIndicator.setVisibility(result.value == null ? View.GONE : View.VISIBLE);
				if (result.value != null) {
					mIsFollowing = result.value;
					mFollowButton.setVisibility(View.VISIBLE);
					mFollowButton.setText(result.value ? R.string.unfollow : R.string.follow);
				}
			} else {
				Utils.showErrorToast(getSherlockActivity(), result.exception, true);
			}
			mProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
			mFollowInfoTask = null;
		}

		@Override
		protected void onPreExecute() {
			mFollowIndicator.setVisibility(View.VISIBLE);
			mFollowButton.setVisibility(View.GONE);
			mProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		private Response<Boolean> isFollowing() {
			if (mUser == null) return new Response<Boolean>(null, null);
			if (mAccountId == mUser.getId()) return new Response<Boolean>(null, null);
			Twitter twitter = getTwitterInstance(getSherlockActivity(), mAccountId, false);
			try {
				Relationship result = twitter.showFriendship(mAccountId, mUser.getId());
				return new Response<Boolean>(result.isSourceFollowingTarget(), null);
			} catch (TwitterException e) {
				return new Response<Boolean>(null, e);
			}
		}
	}

	private class Response<T> {
		public final T value;
		public final TwitterException exception;

		public Response(T value, TwitterException exception) {
			this.value = value;
			this.exception = exception;
		}
	}

	private abstract class UserAction {
		public abstract void execute();

		public abstract String getName();

		public abstract String getSummary();

		@Override
		public String toString() {
			return getName();
		}
	}

	private class UserInfoTask extends AsyncTask<Void, Void, Response<User>> {

		private final Twitter twitter;
		private final long user_id;
		private final String screen_name;

		private UserInfoTask(Context context, long account_id, long user_id, String screen_name) {
			twitter = Utils.getTwitterInstance(context, account_id, true);
			this.user_id = user_id;
			this.screen_name = screen_name;
		}

		UserInfoTask(Context context, long account_id, long user_id) {
			this(context, account_id, user_id, null);
		}

		UserInfoTask(Context context, long account_id, String screen_name) {
			this(context, account_id, -1, screen_name);
		}

		@Override
		protected Response<User> doInBackground(Void... args) {
			try {
				if (user_id != -1)
					return new Response<User>(twitter.showUser(user_id), null);
				else if (screen_name != null) return new Response<User>(twitter.showUser(screen_name), null);
			} catch (TwitterException e) {
				return new Response<User>(null, e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Response<User> result) {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
			if (result.value != null) {
				mUser = result.value;
				getListView().invalidateViews();
				mNameView.setText(mUser.getName());
				mScreenNameView.setText(mUser.getScreenName());
				mProfileImageLoader.displayImage(mUser.getProfileImageURL(), mProfileImageView);
				mBioView.setText(mUser.getDescription());
				mLocationView.setText(mUser.getLocation());
				mWebView.setText(String.valueOf(mUser.getURL()));
			} else {
				Utils.showErrorToast(getSherlockActivity(), result.exception, true);
			}
			getFollowInfo();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

	}

	private class UserProfileActionAdapter extends ArrayAdapter<UserAction> {

		public UserProfileActionAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_2, android.R.id.text1);
		}

		public UserAction findItem(long id) {
			for (int i = 0; i < getCount(); i++) {
				if (id == getItemId(i)) return getItem(i);
			}
			return null;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			TextView summary_view = (TextView) view.findViewById(android.R.id.text2);
			String summary = getItem(position).getSummary();
			summary_view.setText(summary != null ? summary : "");
			return view;
		}

	}

	private class ViewFollowersAction extends UserAction {

		@Override
		public void execute() {

		}

		@Override
		public String getName() {
			return getString(R.string.followers);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return String.valueOf(mUser.getFollowersCount());
		}

	}

	private class ViewFollowingAction extends UserAction {

		@Override
		public void execute() {

		}

		@Override
		public String getName() {
			return getString(R.string.following);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return String.valueOf(mUser.getFriendsCount());
		}

	}

	private class ViewStatusesAction extends UserAction {

		@Override
		public void execute() {

		}

		@Override
		public String getName() {
			return getString(R.string.tweets);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return String.valueOf(mUser.getStatusesCount());
		}

	}
	
}