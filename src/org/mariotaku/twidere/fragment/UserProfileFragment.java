package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.Utils;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class UserProfileFragment extends BaseListFragment implements OnClickListener, OnLongClickListener,
		OnItemClickListener, OnItemLongClickListener {

	private LazyImageLoader mProfileImageLoader;
	private ImageView mProfileImageView;
	private FollowInfoTask mFollowInfoTask;
	private View mFollowIndicator;
	private TextView mName, mScreenName;
	private View mNameLayout;
	private ProgressBar mProgress, mListProgress;
	private Button mFollowButton, mRetryButton;

	private UserProfileActionAdapter mAdapter;
	private ListView mListView;
	private UserInfoTask mUserInfoTask;
	private View mHeaderView;
	private long mAccountId;
	private boolean mIsFollowing;
	private EditTextDialogFragment mDialogFragment;

	private User mUser = null;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_FRIENDSHIP_CHANGED.equals(action)) {
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mAccountId
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					getFollowInfo();
				}
			}
			if (BROADCAST_PROFILE_UPDATED.equals(action)) {
				if (intent.getLongExtra(INTENT_KEY_USER_ID, -1) == mAccountId
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					getUserInfo();
				}
			}
		}
	};

	private static final int TYPE_NAME = 1;

	private static final int TYPE_URL = 2;

	private static final int TYPE_LOCATION = 3;

	private static final int TYPE_DESCRIPTION = 4;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments() != null) {
			mAccountId = getArguments().getLong(INTENT_KEY_ACCOUNT_ID);
		}
		mProfileImageLoader = ((TwidereApplication) getSherlockActivity().getApplication()).getListProfileImageLoader();
		mAdapter = new UserProfileActionAdapter(getSherlockActivity());
		mAdapter.add(new DescriptionAction());
		mAdapter.add(new LocationAction());
		mAdapter.add(new URLAction());
		mAdapter.add(new StatusesAction());
		mAdapter.add(new FollowersAction());
		mAdapter.add(new FollowingAction());
		mAdapter.add(new FavoritesAction());
		mRetryButton.setOnClickListener(this);
		setListAdapter(null);
		mListView = getListView();
		mListView.addHeaderView(mHeaderView, null, false);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
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
			case R.id.retry: {
				getUserInfo();
				break;
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mHeaderView = inflater.inflate(R.layout.user_profile_header, null, false);
		mNameLayout = mHeaderView.findViewById(R.id.name_view);
		mName = (TextView) mHeaderView.findViewById(R.id.name);
		mScreenName = (TextView) mHeaderView.findViewById(R.id.screen_name);
		mProfileImageView = (ImageView) mHeaderView.findViewById(R.id.profile_image);
		mFollowButton = (Button) mHeaderView.findViewById(R.id.follow);
		mFollowIndicator = mHeaderView.findViewById(R.id.follow_indicator);
		mProgress = (ProgressBar) mHeaderView.findViewById(R.id.progress);
		View view = inflater.inflate(R.layout.user_profile, null, false);
		mRetryButton = (Button) view.findViewById(R.id.retry);
		mListProgress = (ProgressBar) view.findViewById(R.id.list_progress);
		return view;
	}

	@Override
	public void onDestroyView() {
		mUser = null;
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		if (mUserInfoTask != null) {
			mUserInfoTask.cancel(true);
		}
		super.onDestroyView();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		UserAction action = mAdapter.findItem(id);
		if (action != null) {
			action.onClick();
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		UserAction action = mAdapter.findItem(id);
		if (action != null) return action.onLongClick();
		return false;
	}

	@Override
	public boolean onLongClick(View view) {
		switch (view.getId()) {
			case R.id.name_view: {
				if (mUser != null && mUser.getId() == mAccountId) {
					mDialogFragment = new EditTextDialogFragment(mUser.getName(), getString(R.string.name), TYPE_NAME);
					mDialogFragment.show(getFragmentManager(), "edit_name");
					return true;
				}
				break;
			}
			case R.id.profile_image: {
				break;
			}
		}
		return false;
	}


	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_FRIENDSHIP_CHANGED);
		filter.addAction(BROADCAST_PROFILE_UPDATED);
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

	private class DescriptionAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.description);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return mUser.getDescription();
		}

		@Override
		public void onClick() {

		}

		@Override
		public boolean onLongClick() {
			if (mUser != null && mUser.getId() == mAccountId) {
				mDialogFragment = new EditTextDialogFragment(getSummary(), getName(), TYPE_DESCRIPTION);
				mDialogFragment.show(getFragmentManager(), "edit_description");
				return true;
			}
			return false;
		}

	}

	private class EditTextDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener {
		private EditText mEditText;
		private String mText;
		private final int mType;
		private final String mTitle;

		public EditTextDialogFragment(String text, String title, int type) {
			mText = text;
			mType = type;
			mTitle = title;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mText = mEditText.getText().toString();
					ServiceInterface service = ServiceInterface.getInstance(getSherlockActivity());
					switch (mType) {
						case TYPE_NAME: {
							service.updateProfile(mAccountId, mText, null, null, null);
							break;
						}
						case TYPE_URL: {
							service.updateProfile(mAccountId, null, mText, null, null);
							break;
						}
						case TYPE_LOCATION: {
							service.updateProfile(mAccountId, null, null, mText, null);
							break;
						}
						case TYPE_DESCRIPTION: {
							service.updateProfile(mAccountId, null, null, null, mText);
							break;
						}
					}
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
			FrameLayout layout = new FrameLayout(getSherlockActivity());
			mEditText = new EditText(getSherlockActivity());
			if (mText != null) {
				mEditText.setText(mText);
			}
			int limit = 140;
			switch (mType) {
				case TYPE_NAME: {
					limit = 20;
					break;
				}
				case TYPE_URL: {
					limit = 100;
					break;
				}
				case TYPE_LOCATION: {
					limit = 30;
					break;
				}
				case TYPE_DESCRIPTION: {
					limit = 160;
					break;
				}
			}
			mEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(limit) });
			layout.addView(mEditText, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) {

				{
					int margin = (int) (getResources().getDisplayMetrics().density * 16);
					bottomMargin = margin;
					leftMargin = margin;
					rightMargin = margin;
					topMargin = margin;
				}
			});
			builder.setTitle(mTitle);
			builder.setView(layout);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

	}

	private class FavoritesAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.favorites);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return String.valueOf(mUser.getFavouritesCount());
		}

		@Override
		public void onClick() {

		}

	}

	private class FollowersAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.followers);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return String.valueOf(mUser.getFollowersCount());
		}

		@Override
		public void onClick() {

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

	private class FollowingAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.following);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return String.valueOf(mUser.getFriendsCount());
		}

		@Override
		public void onClick() {

		}

	}

	private class LocationAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.location);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return mUser.getLocation();
		}

		@Override
		public void onClick() {

		}

		@Override
		public boolean onLongClick() {
			if (mUser != null && mUser.getId() == mAccountId) {
				mDialogFragment = new EditTextDialogFragment(getSummary(), getName(), TYPE_LOCATION);
				mDialogFragment.show(getFragmentManager(), "edit_location");
				return true;
			}
			return false;
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

	private class StatusesAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.tweets);
		}

		@Override
		public String getSummary() {
			if (mUser == null) return null;
			return String.valueOf(mUser.getStatusesCount());
		}

		@Override
		public void onClick() {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			Fragment fragment = Fragment.instantiate(getSherlockActivity(), UserTimelineFragment.class.getName());
			fragment.setArguments(getArguments());
			int viewId = android.R.id.content;
			if (getSherlockActivity() instanceof HomeActivity) {
				viewId = R.id.dashboard;
			}
			ft.replace(viewId, fragment);
			ft.addToBackStack(null);
			ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			ft.commit();
		}

	}

	private class URLAction extends UserAction {

		@Override
		public String getName() {
			return getString(R.string.url);
		}

		@Override
		public String getSummary() {
			if (mUser == null || mUser.getURL() == null) return null;
			return String.valueOf(mUser.getURL());
		}

		@Override
		public void onClick() {
			if (mUser == null) return;
			URL url = mUser.getURL();
			if (url != null) {
				Uri uri = Uri.parse(String.valueOf(url));
				startActivity(new Intent(Intent.ACTION_VIEW, uri));
			}
		}

		@Override
		public boolean onLongClick() {
			if (mUser != null && mUser.getId() == mAccountId) {
				mDialogFragment = new EditTextDialogFragment(getSummary(), getName(), TYPE_URL);
				mDialogFragment.show(getFragmentManager(), "edit_url");
				return true;
			}
			return false;
		}

	}

	private abstract class UserAction {
		public abstract String getName();

		public abstract String getSummary();

		public void onClick() {

		}

		public boolean onLongClick() {
			return false;
		}

		@Override
		public final String toString() {
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
		protected void onCancelled() {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(Response<User> result) {
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
			if (result.value != null) {
				setListShown(true);
				mUser = result.value;
				getListView().invalidateViews();
				mName.setText(mUser.getName());
				mScreenName.setText(mUser.getScreenName());
				mProfileImageLoader.displayImage(mUser.getProfileImageURL(), mProfileImageView);
				mRetryButton.setVisibility(View.GONE);
			} else {
				mListProgress.setVisibility(View.GONE);
				mListView.setVisibility(View.GONE);
				mRetryButton.setVisibility(View.VISIBLE);
			}
			mFollowButton.setOnClickListener(mUser != null ? UserProfileFragment.this : null);
			if (mUser != null && mUser.getId() == mAccountId) {
				mProfileImageView.setOnClickListener(UserProfileFragment.this);
				mProfileImageView.setOnLongClickListener(UserProfileFragment.this);
				mNameLayout.setOnClickListener(UserProfileFragment.this);
				mNameLayout.setOnLongClickListener(UserProfileFragment.this);
			} else {
				mProfileImageView.setOnClickListener(null);
				mProfileImageView.setOnLongClickListener(null);
				mNameLayout.setOnClickListener(null);
				mNameLayout.setOnLongClickListener(null);
			}
			mFollowButton.setOnClickListener(mUser != null ? UserProfileFragment.this : null);
			if (mUser != null && mUser.getId() == mAccountId) {
				mProfileImageView.setOnClickListener(UserProfileFragment.this);
				mProfileImageView.setOnLongClickListener(UserProfileFragment.this);
				mNameLayout.setOnClickListener(UserProfileFragment.this);
				mNameLayout.setOnLongClickListener(UserProfileFragment.this);
			} else {
				mProfileImageView.setOnClickListener(null);
				mProfileImageView.setOnLongClickListener(null);
				mNameLayout.setOnClickListener(null);
				mNameLayout.setOnLongClickListener(null);
			}
			getFollowInfo();
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			setListShown(false);
			mRetryButton.setVisibility(View.GONE);
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

	}

	public void setListShown(boolean shown) {
		int fade_in = android.R.anim.fade_in;
		int fade_out = android.R.anim.fade_out;
		mListProgress.setVisibility(shown ? View.GONE : View.VISIBLE);
		mListProgress.startAnimation(AnimationUtils.loadAnimation(getSherlockActivity(), shown ? fade_out : fade_in));
		mListView.setVisibility(shown ? View.VISIBLE : View.GONE);
		mListView.startAnimation(AnimationUtils.loadAnimation(getSherlockActivity(), shown ? fade_in : fade_out));
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
}