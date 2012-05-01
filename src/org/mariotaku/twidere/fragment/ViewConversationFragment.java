package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusItemHolder;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.content.Context;
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

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class ViewConversationFragment extends BaseListFragment implements OnScrollListener {

	private static final int ADD_STATUS = 1;
	private static final long INVALID_ID = -1;

	private ConversationAdapter mAdapter;
	private ShowConversationTask mShowConversationTask;
	private StatusHandler mStatusHandler;
	private ListView mListView;

	private Handler mHandler;
	private Runnable mTicker;
	private boolean mBusy, mTickerStopped;

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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle == null) return;
		long account_id = bundle.getLong(INTENT_KEY_ACCOUNT_ID, INVALID_ID);
		long status_id = bundle.getLong(INTENT_KEY_STATUS_ID, INVALID_ID);
		if (account_id == INVALID_ID || status_id == INVALID_ID) return;

		LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) mShowConversationTask.cancel(true);
		mAdapter = new ConversationAdapter(getSherlockActivity(), imageloader);
		mStatusHandler = new StatusHandler(mAdapter);
		mShowConversationTask = new ShowConversationTask(getSherlockActivity(), mStatusHandler, account_id, status_id);
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnScrollListener(this);
		mShowConversationTask.execute();
	}

	@Override
	public void onDestroy() {
		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) mShowConversationTask.cancel(true);
		super.onDestroy();
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
			Twitter twitter = CommonUtils.getTwitterInstance(mActivity, mAccountId, true);
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

	private static class ConversationAdapter extends ArrayAdapter<Status> {

		private LazyImageLoader image_loader;

		public ConversationAdapter(Context context, LazyImageLoader image_loader) {
			super(context, R.layout.status_list_item, R.id.user_name);
			this.image_loader = image_loader;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = super.getView(position, convertView, parent);
			Object tag = view.getTag();
			StatusItemHolder holder = null;
			if (tag instanceof StatusItemHolder) {
				holder = (StatusItemHolder) tag;
			} else {
				holder = new StatusItemHolder(view);
				view.setTag(holder);
			}
			Status status = getItem(position);
			User user = status.getUser();
			boolean is_retweet = status.isRetweet();
			boolean is_favorite = status.isFavorited();
			boolean has_media = status.getMediaEntities() != null && status.getMediaEntities().length > 0;
			boolean has_location = status.getGeoLocation() != null;
			boolean is_protected = user.isProtected();
			holder.user_name.setText(user.getName());
			holder.user_name.setCompoundDrawablesWithIntrinsicBounds(
					is_protected ? R.drawable.ic_tweet_stat_is_protected : 0, 0, 0, 0);
			holder.screen_name.setText(user.getScreenName());
			holder.text.setText(status.getText());
			holder.tweet_time.setText(CommonUtils
					.formatToShortTimeString(getContext(), status.getCreatedAt().getTime()));
			holder.tweet_time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					CommonUtils.getTypeIcon(is_retweet, is_favorite, has_location, has_media), 0);
			image_loader.displayImage(user.getProfileImageURL(), holder.profile_image);
			return view;
		}

	}
}
