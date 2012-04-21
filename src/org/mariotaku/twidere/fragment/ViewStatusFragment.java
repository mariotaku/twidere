package org.mariotaku.twidere.fragment;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;

import roboguice.inject.InjectExtra;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewStatusFragment extends BaseFragment implements OnClickListener {

	@InjectExtra(Statuses.ACCOUNT_ID) private long mAccountId;
	@InjectExtra(Statuses.STATUS_ID) private long mStatusId;
	@InjectExtra(TweetStore.KEY_TYPE) private int mType;
	private ContentResolver mResolver;
	private TextView mName, mScreenName, mText, mSource;
	private ImageView mProfileImage;
	private Button mFollowButton;
	private FrameLayout mFollowIndicator;
	private ProgressBar mProgress;
	private long mTweetUserId;

	private FollowInfoTask mFollowInfoTask;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		View view = getView();
		mName = (TextView) view.findViewById(R.id.name);
		mScreenName = (TextView) view.findViewById(R.id.screen_name);
		mText = (TextView) view.findViewById(R.id.text);
		mProfileImage = (ImageView) view.findViewById(R.id.profile_image);
		mSource = (TextView) view.findViewById(R.id.source);
		mResolver = getSherlockActivity().getContentResolver();
		mFollowButton = (Button) view.findViewById(R.id.follow);
		mFollowIndicator = (FrameLayout) view.findViewById(R.id.follow_indicator);
		mProgress = (ProgressBar) view.findViewById(R.id.progress);
		displayStatus();
		showFollowInfo();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.follow:
				break;
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.view_status, container, false);
	}

	@Override
	public void onDestroy() {
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		super.onDestroy();
	}

	private void displayStatus() {
		Uri uri;
		String[] cols;
		String where;
		switch (mType) {
			case TweetStore.VALUE_TYPE_MENTION:
				uri = Mentions.CONTENT_URI;
				cols = Mentions.COLUMNS;
				where = Mentions.STATUS_ID + "=" + mStatusId;
				break;
			case TweetStore.VALUE_TYPE_STATUS:
			default:
				uri = Statuses.CONTENT_URI;
				cols = Statuses.COLUMNS;
				where = Statuses.STATUS_ID + "=" + mStatusId;
				break;
		}

		Cursor cur = mResolver.query(uri, cols, where, null, null);
		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.NAME));
			mName.setText(name != null ? name : "");
			String screen_name = cur.getString(cur.getColumnIndexOrThrow(Statuses.SCREEN_NAME));
			mScreenName.setText(screen_name != null ? "@" + screen_name : "");
			String text = cur.getString(cur.getColumnIndexOrThrow(Statuses.TEXT));
			mText.setText(text != null ? text : "");
			String source = cur.getString(cur.getColumnIndexOrThrow(Statuses.SOURCE));
			mSource.setText(Html.fromHtml(getString(R.string.sent_from, source)));
			mSource.setMovementMethod(LinkMovementMethod.getInstance());
			mTweetUserId = cur.getLong(cur.getColumnIndexOrThrow(Statuses.USER_ID));

			LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity()
					.getApplication()).getListProfileImageLoader();
			String profile_image_url = cur.getString(cur
					.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL));
			URL url = null;
			try {
				url = new URL(profile_image_url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			imageloader.displayImage(url, mProfileImage);
		}
		if (cur != null) {
			cur.close();
		}
	}

	private void showFollowInfo() {
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		mFollowInfoTask = new FollowInfoTask();
		mFollowInfoTask.execute();
	}

	private class FollowInfoTask extends AsyncTask<Void, Void, Boolean> {

		private boolean isAllFollowing() {
			long[] ids = CommonUtils.getActivatedAccounts(getSherlockActivity());
			for (long id : ids) {
				Twitter twitter = CommonUtils.getTwitterInstance(getSherlockActivity(), id);
				try {
					Relationship result = twitter.showFriendship(id, mTweetUserId);
					if (!result.isSourceFollowingTarget()) return false;
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}
			return true;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return isAllFollowing();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			mFollowIndicator.setVisibility(result ? View.GONE : View.VISIBLE);
			mFollowButton.setVisibility(result ? View.GONE : View.VISIBLE);
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
	}

}
