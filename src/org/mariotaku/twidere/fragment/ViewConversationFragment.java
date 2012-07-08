/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.HomeActivity;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.StatusViewHolder;
import org.mariotaku.twidere.util.ProfileImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import com.twitter.Extractor;

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

	private Fragment mDetailFragment;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mServiceInterface = getApplication().getServiceInterface();
		mDisplayProfileImage = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		mDisplayName = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		Bundle bundle = getArguments();
		if (bundle == null) {
			bundle = new Bundle();
		}
		final long account_id = bundle.getLong(INTENT_KEY_ACCOUNT_ID, INVALID_ID);
		final long status_id = bundle.getLong(INTENT_KEY_STATUS_ID, INVALID_ID);

		final ProfileImageLoader imageloader = getApplication().getProfileImageLoader();
		if (mShowConversationTask != null && !mShowConversationTask.isCancelled()) {
			mShowConversationTask.cancel(true);
		}
		mAdapter = new ParcelableStatusesAdapter(getActivity(), imageloader);
		mStatusHandler = new StatusHandler(mAdapter, account_id);
		mShowConversationTask = new ShowConversationTask(mStatusHandler, account_id, status_id);
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
		final ParcelableStatus status = mAdapter.findItem(id);
		openStatus(status);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		final Object tag = view.getTag();
		if (tag instanceof StatusViewHolder) {
			final StatusViewHolder holder = (StatusViewHolder) tag;
			if (holder.show_as_gap) return false;
			mSelectedStatus = mAdapter.findItem(id);
			mPopupMenu = PopupMenu.getInstance(getActivity(), view);
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
			final long status_id = mSelectedStatus.status_id;
			final String text_plain = mSelectedStatus.text_plain;
			final String screen_name = mSelectedStatus.screen_name;
			final String name = mSelectedStatus.name;
			final long account_id = mSelectedStatus.account_id;
			switch (item.getItemId()) {
				case MENU_SHARE: {
					final Intent intent = new Intent(Intent.ACTION_SEND);
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
					final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					final Bundle bundle = new Bundle();
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
					final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
					final Bundle bundle = new Bundle();
					final List<String> mentions = new Extractor().extractMentionedScreennames(text_plain);
					mentions.add(0, screen_name);
					bundle.putStringArray(INTENT_KEY_MENTIONS, mentions.toArray(new String[mentions.size()]));
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
		final boolean display_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true);
		final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
		final float text_size = mPreferences.getFloat(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mAdapter.setDisplayProfileImage(display_profile_image);
		mAdapter.setDisplayName(display_name);
		mAdapter.setTextSize(text_size);
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

	private void openStatus(ParcelableStatus status) {
		final long account_id = status.account_id, status_id = status.status_id;
		final FragmentActivity activity = getActivity();
		final Bundle bundle = new Bundle();
		bundle.putParcelable(INTENT_KEY_STATUS, status);
		if (activity instanceof HomeActivity && ((HomeActivity) activity).isDualPaneMode()) {
			final HomeActivity home_activity = (HomeActivity) activity;
			if (mDetailFragment instanceof ViewStatusFragment && mDetailFragment.isAdded()) {
				((ViewStatusFragment) mDetailFragment).displayStatus(status);
			} else {
				mDetailFragment = new ViewStatusFragment();
				final Bundle args = new Bundle(bundle);
				args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
				args.putLong(INTENT_KEY_STATUS_ID, status_id);
				mDetailFragment.setArguments(args);
				home_activity.showAtPane(HomeActivity.PANE_RIGHT, mDetailFragment, true);
			}
		} else {
			final Uri.Builder builder = new Uri.Builder();
			builder.scheme(SCHEME_TWIDERE);
			builder.authority(AUTHORITY_STATUS);
			builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(account_id));
			builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(status_id));
			final Intent intent = new Intent(Intent.ACTION_VIEW, builder.build());

			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	private class ShowConversationTask extends AsyncTask<Void, Void, TwitterException> {

		private final long mAccountId, mStatusId;
		private final StatusHandler mHandler;

		public ShowConversationTask(StatusHandler handler, long account_id, long status_id) {
			mHandler = handler;
			mAccountId = account_id;
			mStatusId = status_id;
		}

		@Override
		protected TwitterException doInBackground(Void... params) {
			final Twitter twitter = getTwitterInstance(getActivity(), mAccountId, true);
			try {
				twitter4j.Status status = twitter.showStatus(mStatusId);
				mHandler.sendMessage(mHandler.obtainMessage(ADD_STATUS, status));
				long in_reply_to_id = status.getInReplyToStatusId();
				while (in_reply_to_id != -1) {
					status = twitter.showStatus(in_reply_to_id);
					if (status.getId() <= 0) {
						break;
					}
					mHandler.sendMessage(mHandler.obtainMessage(ADD_STATUS, status));
					in_reply_to_id = status.getInReplyToStatusId();
				}
			} catch (final TwitterException e) {
				return e;
			}
			return null;
		}

		@Override
		protected void onPostExecute(TwitterException result) {
			if (result != null) {

			}
			setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			setProgressBarIndeterminateVisibility(true);
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
					final Object obj = msg.obj;
					if (obj instanceof Status) {
						mAdapter.add(new ParcelableStatus((Status) obj, mAccountId, false));
					}
					break;
			}
			super.handleMessage(msg);
		}
	}
}
