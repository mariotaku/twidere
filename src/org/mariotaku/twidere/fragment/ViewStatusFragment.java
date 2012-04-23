package org.mariotaku.twidere.fragment;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ViewStatusActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ServiceInterface;

import roboguice.inject.InjectExtra;
import roboguice.inject.InjectResource;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff.Mode;
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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ViewStatusFragment extends BaseFragment implements OnClickListener {

	@InjectResource(R.color.holo_blue_bright) public int mActivedMenuColor;
	@InjectExtra(Statuses.ACCOUNT_ID) private long mAccountId;
	@InjectExtra(Statuses.STATUS_ID) private long mStatusId;

	public ServiceInterface mServiceInterface;
	private ContentResolver mResolver;
	private TextView mName, mScreenName, mText, mSource;
	private ImageView mProfileImage;
	private Button mFollowButton;
	private FrameLayout mFollowIndicator;
	private ProgressBar mProgress;
	private long mTweetUserId;

	private FollowInfoTask mFollowInfoTask;
	private boolean mIsFavorite;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_DATABASE_UPDATED.equals(action)) {
				getSherlockActivity().invalidateOptionsMenu();
			}
		}
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		mServiceInterface = ((TwidereApplication) getSherlockActivity().getApplication())
				.getServiceInterface();
		mResolver = getSherlockActivity().getContentResolver();
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		View view = getView();
		mName = (TextView) view.findViewById(R.id.name);
		mScreenName = (TextView) view.findViewById(R.id.screen_name);
		mText = (TextView) view.findViewById(R.id.text);
		mProfileImage = (ImageView) view.findViewById(R.id.profile_image);
		mSource = (TextView) view.findViewById(R.id.source);
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_status, menu);
		super.onCreateOptionsMenu(menu, inflater);
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_REPLY:
				break;
			case MENU_RETWEET:
				mServiceInterface.retweetStatus(new long[] { mAccountId }, mStatusId);
				break;
			case MENU_QUOTE:
				break;
			case MENU_FAV:
				if (mIsFavorite) {
					mServiceInterface.destroyFavorite(new long[] { mAccountId }, mStatusId);
				} else {
					mServiceInterface.createFavorite(new long[] { mAccountId }, mStatusId);
				}
				break;
			case MENU_DELETE:
				mServiceInterface.destroyStatus(mAccountId, mStatusId);
				break;
			default:
				return false;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		String[] accounts_cols = new String[] { Accounts.USER_ID };
		Cursor accounts_cur = mResolver
				.query(Accounts.CONTENT_URI, accounts_cols, null, null, null);
		ArrayList<Long> ids = new ArrayList<Long>();
		if (accounts_cur != null) {
			accounts_cur.moveToFirst();
			int idx = accounts_cur.getColumnIndexOrThrow(Accounts.USER_ID);
			while (!accounts_cur.isAfterLast()) {
				ids.add(accounts_cur.getLong(idx));
				accounts_cur.moveToNext();
			}
			accounts_cur.close();
		}
		String[] cols = Statuses.COLUMNS;
		String where = Statuses.STATUS_ID + "=" + mStatusId;

		Cursor cur = null;

		for (Uri uri : TweetStore.STATUSES_URIS) {
			cur = mResolver.query(uri, cols, where, null, null);
			if (cur != null && cur.getCount() > 0) {
				break;
			}
			if (cur != null) {
				cur.close();
			}
		}

		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			int idx = cur.getColumnIndexOrThrow(Statuses.USER_ID);
			long user_id = cur.getLong(idx);
			menu.findItem(MENU_DELETE).setVisible(ids.contains(user_id));
			MenuItem itemFav = menu.findItem(MENU_FAV);
			if (cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_FAVORITE)) == 1) {
				itemFav.getIcon().setColorFilter(mActivedMenuColor, Mode.MULTIPLY);
				itemFav.setTitle(R.string.unfav);
			} else {
				itemFav.getIcon().clearColorFilter();
				itemFav.setTitle(R.string.fav);
			}
		} else {
			if (getSherlockActivity() instanceof ViewStatusActivity) {
				getSherlockActivity().finish();
			} else {
				//Do what? I will make a decision after I have a tablet.
				//getFragmentManager().beginTransaction().remove(this);
			}
		}
		if (cur != null) {
			cur.close();
		}
		super.onPrepareOptionsMenu(menu);

	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_DATABASE_UPDATED);
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

	private void displayStatus() {
		String[] cols = Statuses.COLUMNS;
		String where = Statuses.STATUS_ID + "=" + mStatusId;

		Cursor cur = null;

		for (Uri uri : TweetStore.STATUSES_URIS) {
			cur = mResolver.query(uri, cols, where, null, null);
			if (cur != null && cur.getCount() > 0) {
				break;
			}
			if (cur != null) {
				cur.close();
			}
		}

		if (cur != null && cur.getCount() > 0) {
			cur.moveToFirst();
			String name = cur.getString(cur.getColumnIndexOrThrow(Statuses.NAME));
			mName.setText(name != null ? name : "");
			String screen_name = cur.getString(cur.getColumnIndexOrThrow(Statuses.SCREEN_NAME));
			mScreenName.setText(screen_name != null ? "@" + screen_name : "");
			String text = cur.getString(cur.getColumnIndexOrThrow(Statuses.TEXT));
			if (text != null) mText.setText(Html.fromHtml(text));
			mText.setMovementMethod(LinkMovementMethod.getInstance());
			String source = cur.getString(cur.getColumnIndexOrThrow(Statuses.SOURCE));
			mSource.setText(Html.fromHtml(getString(R.string.sent_from, source)));
			mSource.setMovementMethod(LinkMovementMethod.getInstance());
			mTweetUserId = cur.getLong(cur.getColumnIndexOrThrow(Statuses.USER_ID));
			mIsFavorite = cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_FAVORITE)) == 1;

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
				if (id == mTweetUserId) continue;
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
