package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getMentionedNames;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import org.mariotaku.actionbarcompat.app.ActionBarFragmentActivity;
import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusViewHolder;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class ViewConversationFragment extends BaseListFragment implements OnScrollListener, OnItemClickListener,
		OnItemLongClickListener, OnMenuItemClickListener {

	private static final int ADD_STATUS = 1;
	private static final long INVALID_ID = -1;

	private ParcelableStatusesAdapter mAdapter;
	private ShowConversationTask mShowConversationTask;
	private StatusHandler mStatusHandler;
	private ListView mListView;
	private SharedPreferences mPreferences;
	private Handler mHandler;
	private Runnable mTicker;
	private PopupMenu mPopupMenu;
	private boolean mBusy, mTickerStopped, mDisplayProfileImage, mDisplayName;
	private ParcelableStatus mSelectedStatus;
	private float mTextSize;
	private ServiceInterface mServiceInterface;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getActivity().getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mServiceInterface = ((TwidereApplication) getActivity().getApplication()).getServiceInterface();
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		Bundle bundle = getArguments();
		if (bundle == null) {
			bundle = new Bundle();
		}
		long account_id = bundle.getLong(INTENT_KEY_ACCOUNT_ID, INVALID_ID);
		long status_id = bundle.getLong(INTENT_KEY_STATUS_ID, INVALID_ID);

		LazyImageLoader imageloader = ((TwidereApplication) getActivity().getApplication()).getProfileImageLoader();
		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) {
			mShowConversationTask.cancel(true);
		}
		mAdapter = new ParcelableStatusesAdapter(getActivity(), imageloader);
		mStatusHandler = new StatusHandler(mAdapter, account_id);
		mShowConversationTask = new ShowConversationTask(getActivity(), mStatusHandler, account_id, status_id);
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnScrollListener(this);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		if (account_id != INVALID_ID && status_id != INVALID_ID) {
			mShowConversationTask.execute();
		}
	}

	@Override
	public void onDestroyView() {
		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) {
			mShowConversationTask.cancel(true);
		}
		super.onDestroyView();
	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		ParcelableStatus status = mAdapter.findItem(id);
		Uri.Builder builder = new Uri.Builder();
		builder.scheme(SCHEME_TWIDERE);
		builder.authority(AUTHORITY_STATUS);
		builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(status.account_id));
		builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status.status_id));
		Intent intent = new Intent(Intent.ACTION_DEFAULT, builder.build());
		Bundle bundle = new Bundle();
		bundle.putParcelable(INTENT_KEY_STATUS, status);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) return false;
			mSelectedStatus = mAdapter.findItem(id);
			mPopupMenu = new PopupMenu(getActivity(), view);
			mPopupMenu.inflate(R.menu.action_status);
			setMenuForStatus(getActivity(), mPopupMenu.getMenu(), mSelectedStatus);
			mPopupMenu.setOnMenuItemClickListener(this);
			mPopupMenu.show();
			return true;
		}
		return false;
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (mSelectedStatus != null) {
			long status_id = mSelectedStatus.status_id;
			String text_plain = mSelectedStatus.text_plain;
			String screen_name = mSelectedStatus.screen_name;
			String name = mSelectedStatus.name;
			long account_id = mSelectedStatus.account_id;
			switch (item.getItemId()) {
				case MENU_SHARE: {
					Intent intent = new Intent(Intent.ACTION_SEND);
					intent.setType("text/plain");
					intent.putExtra(Intent.EXTRA_TEXT, "@" + screen_name + ": " + text_plain);
					startActivity(Intent.createChooser(intent, getString(R.string.share)));
					break;
				}
				case MENU_RETWEET: {
					mServiceInterface.retweetStatus(account_id, status_id);
					break;
				}
				case MENU_QUOTE: {
					Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					Bundle bundle = new Bundle();
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
					bundle.putBoolean(INTENT_KEY_IS_QUOTE, true);
					bundle.putString(INTENT_KEY_TEXT, getQuoteStatus(getActivity(), screen_name, text_plain));
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case MENU_REPLY: {
					Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					Bundle bundle = new Bundle();
					bundle.putStringArray(INTENT_KEY_MENTIONS, getMentionedNames(screen_name, text_plain, false, true));
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case MENU_FAV: {
					if (mSelectedStatus.is_favorite) {
						mServiceInterface.destroyFavorite(account_id, status_id);
					} else {
						mServiceInterface.createFavorite(account_id, status_id);
					}
					break;
				}
				case MENU_DELETE: {
					mServiceInterface.destroyStatus(account_id, status_id);
					break;
				}
				default:
					return false;
			}
		}
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayName(display_name);
		mAdapter.setStatusesTextSize(text_size);
		if (mDisplayProfileImage != display_profile_image || mDisplayName != display_name || mTextSize != text_size) {
			mDisplayProfileImage = display_profile_image;
			mDisplayName = display_name;
			mTextSize = text_size;
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
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	private static class ShowConversationTask extends AsyncTask<Void, Void, TwitterException> {

		private FragmentActivity mActivity;
		private long mAccountId, mStatusId;
		private StatusHandler mHandler;

		public ShowConversationTask(FragmentActivity context, StatusHandler handler, long account_id, long status_id) {
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
			if (mActivity instanceof ActionBarFragmentActivity) {
				((ActionBarFragmentActivity) mActivity).setSupportProgressBarIndeterminateVisibility(false);
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			if (mActivity instanceof ActionBarFragmentActivity) {
				((ActionBarFragmentActivity) mActivity).setSupportProgressBarIndeterminateVisibility(true);
			}
			super.onPreExecute();
		}

	}

	private static class StatusHandler extends Handler {

		private final ParcelableStatusesAdapter mAdapter;
		private final long mAccountId;

		public StatusHandler(ParcelableStatusesAdapter adapter, long account_id) {
			mAdapter = adapter;
			mAccountId = account_id;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case ADD_STATUS:
					Object obj = msg.obj;
					if (obj instanceof Status) {
						mAdapter.add(new ParcelableStatus((Status) obj, mAccountId, false));
					}
					break;
			}
			super.handleMessage(msg);
		}
	}
}
