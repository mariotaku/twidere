package org.mariotaku.twidere.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ComposeActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.ServiceInterface.StateListener;
import org.mariotaku.twidere.util.TopScrollable;
import org.mariotaku.twidere.widget.RefreshableListView;
import org.mariotaku.twidere.widget.RefreshableListView.OnRefreshListener;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ConnectTabFragment extends SherlockListFragment implements Constants, TopScrollable,
		OnRefreshListener, LoaderCallbacks<Cursor>, StateListener {

	private MentionsAdapter mAdapter;
	private LazyImageLoader mListProfileImageLoader;
	private CommonUtils mCommonUtils;
	private ServiceInterface mServiceInterface;
	private RefreshableListView mListView;
	private int mAccountIdIdx, mStatusIdIdx, mUserIdIdx, mStatusTimestampIdx, mTextIdx, mNameIdx,
			mScreenNameIdx, mProfileImageUrlIdx, mIsRetweetIdx, mIsFavoriteIdx;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListProfileImageLoader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		mCommonUtils = ((TwidereApplication) getSherlockActivity().getApplication())
				.getCommonUtils();
		mServiceInterface = ((TwidereApplication) getSherlockActivity().getApplication())
				.getServiceInterface();
		mServiceInterface.addMediaStateListener(this);
		setHasOptionsMenu(true);
		mAdapter = new MentionsAdapter(getSherlockActivity(), R.layout.tweet_list_item);
		setListAdapter(mAdapter);
		mListView = (RefreshableListView) getListView();
		mListView.setOnRefreshListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] cols = Mentions.COLUMNS;
		Uri uri = Mentions.CONTENT_URI;
		return new CursorLoader(getSherlockActivity(), uri, cols, null, null,
				Mentions.DEFAULT_SORT_ORDER);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.connect, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.timeline, container, false);
	}

	@Override
	public void onHomeTimelineRefreshed() {

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.changeCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.changeCursor(data);
		mAccountIdIdx = data.getColumnIndexOrThrow(Mentions.ACCOUNT_ID);
		mStatusIdIdx = data.getColumnIndexOrThrow(Mentions.STATUS_ID);
		mUserIdIdx = data.getColumnIndexOrThrow(Mentions.USER_ID);
		mStatusTimestampIdx = data.getColumnIndexOrThrow(Mentions.STATUS_TIMESTAMP);
		mTextIdx = data.getColumnIndexOrThrow(Mentions.TEXT);
		mNameIdx = data.getColumnIndexOrThrow(Mentions.NAME);
		mScreenNameIdx = data.getColumnIndexOrThrow(Mentions.SCREEN_NAME);
		mProfileImageUrlIdx = data.getColumnIndexOrThrow(Mentions.PROFILE_IMAGE_URL);
		mIsRetweetIdx = data.getColumnIndexOrThrow(Mentions.IS_RETWEET);
		mIsFavoriteIdx = data.getColumnIndexOrThrow(Mentions.IS_FAVORITE);

	}

	@Override
	public void onMentionsRefreshed() {
		mListView.completeRefreshing();
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.compose:
				startActivity(new Intent(getSherlockActivity(), ComposeActivity.class));
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onRefresh() {
		String[] cols = new String[] { Accounts.USER_ID };
		Cursor cur = getSherlockActivity().getContentResolver().query(Accounts.CONTENT_URI, cols,
				null, null, null);

		if (cur != null) {
			int idx = cur.getColumnIndexOrThrow(Accounts.USER_ID);
			long[] ids = new long[cur.getCount()];
			for (int i = 0; i < cur.getCount(); i++) {
				cur.moveToPosition(i);
				ids[i] = cur.getLong(idx);
			}
			mServiceInterface.refreshMentions(ids, 20);
			cur.close();
		}

	}

	@Override
	public void scrolltoTop() {
		if (getView() != null) {
			getListView().smoothScrollToPosition(0);
		}

	}

	private class MentionsAdapter extends SimpleCursorAdapter {

		public MentionsAdapter(Context context, int layout) {
			super(context, layout, null, new String[] {}, new int[] {}, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ViewHolder viewholder = (ViewHolder) view.getTag();

			if (viewholder == null) return;

			String user_name = cursor.getString(mNameIdx);
			String screen_name = cursor.getString(mScreenNameIdx);
			String text = cursor.getString(mTextIdx);
			String profile_image_url = cursor.getString(mProfileImageUrlIdx);
			boolean is_retweet = cursor.getInt(mIsRetweetIdx) == 1;
			boolean is_favorite = cursor.getInt(mIsFavoriteIdx) == 1;

			viewholder.user_name.setText(user_name);
			viewholder.screen_name.setText("@" + screen_name);
			viewholder.tweet_content.setText(text);
			viewholder.tweet_time.setText(mCommonUtils.formatTimeStampString(cursor
					.getLong(mStatusTimestampIdx)));
			if (is_retweet && is_favorite) {
				viewholder.retweet_fav_indicator
						.setImageResource(R.drawable.ic_indicator_retweet_fav);
			} else if (is_retweet && !is_favorite) {
				viewholder.retweet_fav_indicator.setImageResource(R.drawable.ic_indicator_retweet);
			} else if (!is_retweet && is_favorite) {
				viewholder.retweet_fav_indicator.setImageResource(R.drawable.ic_indicator_fav);
			} else {
				viewholder.retweet_fav_indicator.setImageResource(R.drawable.ic_indicator_none);
			}
			URL url = null;
			try {
				url = new URL(profile_image_url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			if (url != null) {
				mListProfileImageLoader.displayImage(url, viewholder.profile_image);
			}

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {

			View view = super.newView(context, cursor, parent);
			ViewHolder viewholder = new ViewHolder(view);
			view.setTag(viewholder);
			return view;
		}

		private class ViewHolder {

			TextView user_name;
			TextView screen_name;
			TextView tweet_content;
			TextView tweet_time;
			ImageView profile_image;
			ImageView retweet_fav_indicator;

			public ViewHolder(View view) {
				user_name = (TextView) view.findViewById(R.id.user_name);
				screen_name = (TextView) view.findViewById(R.id.screen_name);
				tweet_content = (TextView) view.findViewById(R.id.tweet_content);
				tweet_time = (TextView) view.findViewById(R.id.tweet_time);
				profile_image = (ImageView) view.findViewById(R.id.profile_image);
				retweet_fav_indicator = (ImageView) view.findViewById(R.id.retweet_fav_indicator);
			}

		}

	}
}
