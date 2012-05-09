package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getTypeIcon;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusViewHolder;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class ViewConversationFragment extends BaseListFragment implements OnScrollListener {

	private static final int ADD_STATUS = 1;
	private static final long INVALID_ID = -1;

	private ConversationAdapter mAdapter;
	private ShowConversationTask mShowConversationTask;
	private StatusHandler mStatusHandler;
	private ListView mListView;
	private SharedPreferences mPreferences;
	private Handler mHandler;
	private Runnable mTicker;
	private boolean mBusy, mTickerStopped, mDisplayProfileImage, mDisplayName;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSherlockActivity().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		Bundle bundle = getArguments();
		if (bundle == null) bundle = new Bundle();
		long account_id = bundle.getLong(INTENT_KEY_ACCOUNT_ID, INVALID_ID);
		long status_id = bundle.getLong(INTENT_KEY_STATUS_ID, INVALID_ID);

		LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) {
			mShowConversationTask.cancel(true);
		}
		mAdapter = new ConversationAdapter(getSherlockActivity(), imageloader);
		mStatusHandler = new StatusHandler(mAdapter);
		mShowConversationTask = new ShowConversationTask(getSherlockActivity(), mStatusHandler, account_id, status_id);
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnScrollListener(this);

		if (account_id != INVALID_ID && status_id != INVALID_ID) {
			mShowConversationTask.execute();
		}
	}

	@Override
	public void onDestroy() {
		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) {
			mShowConversationTask.cancel(true);
		}
		super.onDestroy();
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

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		switch (scrollState) {
			case SCROLL_STATE_FLING:
			case SCROLL_STATE_TOUCH_SCROLL:
				mBusy = true;
				break;
			case SCROLL_STATE_IDLE:
				mBusy = false;
				break;
		}

	}

	@Override
	public void onStart() {
		super.onStart();
		mTickerStopped = false;
		mHandler = new Handler();

		mTicker = new Runnable() {

			@Override
			public void run() {
				if (mTickerStopped) return;
				if (mListView != null && !mBusy) {
					mListView.invalidateViews();
				}
				final long now = SystemClock.uptimeMillis();
				final long next = now + 1000 - now % 1000;
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();

	}

	@Override
	public void onStop() {
		mTickerStopped = true;
		super.onStop();
	}

	private static class ConversationAdapter extends ArrayAdapter<Status> {

		private LazyImageLoader image_loader;

		private boolean mDisplayProfileImage, mDisplayName;

		public ConversationAdapter(Context context, LazyImageLoader image_loader) {
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
			boolean is_protected = user != null ? user.isProtected() : false;
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

	private static class ShowConversationTask extends AsyncTask<Void, Void, TwitterException> {

		private SherlockFragmentActivity mActivity;
		private long mAccountId, mStatusId;
		private StatusHandler mHandler;

		public ShowConversationTask(SherlockFragmentActivity context, StatusHandler handler, long account_id,
				long status_id) {
			mActivity = context;
			mHandler = handler;
			mAccountId = account_id;
			mStatusId = status_id;
		}

		@Override
		protected TwitterException doInBackground(Void... params) {
			Twitter twitter = getTwitterInstance(mActivity, mAccountId, true);
			try {
				twitter4j.Status status = twitter.showStatus(mStatusId);
				mHandler.sendMessage(mHandler.obtainMessage(ADD_STATUS, status));
				long in_reply_to_id = status.getInReplyToStatusId();
				while (in_reply_to_id != -1) {
					status = twitter.showStatus(in_reply_to_id);
					mHandler.sendMessage(mHandler.obtainMessage(ADD_STATUS, status));
					in_reply_to_id = status.getInReplyToStatusId();
				}
			} catch (TwitterException e) {
				return e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(TwitterException result) {
			if (result != null) {

			}
			mActivity.setSupportProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			mActivity.setSupportProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

	}

	private static class StatusHandler extends Handler {

		private ConversationAdapter mAdapter;

		public StatusHandler(ConversationAdapter adapter) {
			mAdapter = adapter;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ADD_STATUS:
					Object obj = msg.obj;
					if (obj instanceof Status) {
						mAdapter.add((Status) obj);
					}
					break;
			}
			super.handleMessage(msg);
		}
	}
}
