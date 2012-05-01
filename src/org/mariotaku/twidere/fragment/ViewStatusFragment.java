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

import twitter4j.GeoLocation;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ViewStatusFragment extends BaseFragment implements OnClickListener {

	private long mAccountId, mStatusId;

	public ServiceInterface mServiceInterface;
	private ContentResolver mResolver;
	private TextView mName, mScreenName, mText, mTimeAndSource;
	private ImageView mProfileImage;
	private Button mFollowButton;
	private View mProfileView, mFollowIndicator, mMapView;
	private ProgressBar mProgress;
	private long mStatusUserId;
	private String mStatusScreenName;

	private FollowInfoTask mFollowInfoTask;
	private boolean mIsFavorite, mIsRetweetByMe;

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

		mServiceInterface = ((TwidereApplication) getSherlockActivity().getApplication()).getServiceInterface();
		mResolver = getSherlockActivity().getContentResolver();
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			mAccountId = bundle.getLong(Statuses.ACCOUNT_ID);
			mStatusId = bundle.getLong(Statuses.STATUS_ID);
		}
		setHasOptionsMenu(true);
		View view = getView();
		mName = (TextView) view.findViewById(R.id.name);
		mScreenName = (TextView) view.findViewById(R.id.screen_name);
		mText = (TextView) view.findViewById(R.id.text);
		mProfileImage = (ImageView) view.findViewById(R.id.profile_image);
		mTimeAndSource = (TextView) view.findViewById(R.id.time_source);
		mFollowButton = (Button) view.findViewById(R.id.follow);
		mFollowIndicator = view.findViewById(R.id.follow_indicator);
		mProfileView = view.findViewById(R.id.profile);
		// mMapView = (FrameLayout) view.findViewById(R.id.map);
		mProgress = (ProgressBar) view.findViewById(R.id.progress);
		mProfileView.setOnClickListener(this);
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
			case MENU_SHARE:
				break;
			case MENU_REPLY:
				Bundle bundle = new Bundle();
				bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, mStatusId);
				bundle.putStringArray(INTENT_KEY_MENTIONS,
						CommonUtils.getMentionedNames(mStatusScreenName, mText.getText(), false, true));
				startActivity(new Intent(INTENT_ACTION_COMPOSE).putExtras(bundle));
				break;
			case MENU_RETWEET:
				if (mIsRetweetByMe) {
					mServiceInterface.retweetStatus(new long[] { mAccountId }, mStatusId);
				} else {
					// There is no way to "undo" retweet yet.
				}
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
		Cursor accounts_cur = mResolver.query(Accounts.CONTENT_URI, accounts_cols, null, null, null);
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

		for (Uri uri : TweetStore.STATUSES_URIS) {
			Cursor cur = mResolver.query(uri, cols, where, null, null);
			if (cur != null && cur.getCount() > 0) {
				cur.moveToFirst();
				mStatusScreenName = cur.getString(cur.getColumnIndexOrThrow(Statuses.SCREEN_NAME));
				CommonUtils.setMenuForStatus(getSherlockActivity(), menu, mStatusId, uri);
				cur.close();
				super.onPrepareOptionsMenu(menu);
				return;
			}
			if (cur != null) {
				cur.close();
			}
		}

		super.onPrepareOptionsMenu(menu);
		if (getSherlockActivity() instanceof ViewStatusActivity) {
			getSherlockActivity().finish();
		} else {
			// Do what? I will make a decision after I have a tablet.
			// getFragmentManager().beginTransaction().remove(this);
		}

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
			if (text != null) {
				mText.setText(Html.fromHtml(text));
			}
			mText.setMovementMethod(LinkMovementMethod.getInstance());
			String source = cur.getString(cur.getColumnIndexOrThrow(Statuses.SOURCE));
			long timestamp = cur.getLong(cur.getColumnIndexOrThrow(Statuses.STATUS_TIMESTAMP));
			String time = CommonUtils.formatToLongTimeString(getSherlockActivity(), timestamp);
			mTimeAndSource.setText(Html.fromHtml(getString(R.string.time_source, time, source)));
			mTimeAndSource.setMovementMethod(LinkMovementMethod.getInstance());
			mStatusUserId = cur.getLong(cur.getColumnIndexOrThrow(Statuses.USER_ID));
			mIsFavorite = cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_FAVORITE)) == 1;
			mIsRetweetByMe = cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_RETWEET)) < 0;
			String location_string = cur.getString(cur.getColumnIndexOrThrow(Statuses.LOCATION));
			GeoLocation location = CommonUtils.getGeoLocationFromString(location_string);
			// mMapView.setVisibility(location == null ? View.GONE :
			// View.VISIBLE);
			// if (location != null) {
			// FragmentTransaction ft = getFragmentManager().beginTransaction();
			// Fragment fragment = new GoogleMapFragment(location.getLatitude(),
			// location.getLongitude(), true);
			// ft.replace(R.id.map, fragment);
			// ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			// ft.commit();
			// }

			LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
					.getListProfileImageLoader();
			String profile_image_url = cur.getString(cur.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL));
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

		private boolean isAllFollowing() {
			long[] ids = CommonUtils.getActivatedAccounts(getSherlockActivity());
			for (long id : ids) {
				if (id == mStatusUserId) {
					continue;
				}
				Twitter twitter = CommonUtils.getTwitterInstance(getSherlockActivity(), id);
				try {
					Relationship result = twitter.showFriendship(id, mStatusUserId);
					if (!result.isSourceFollowingTarget()) return false;
				} catch (TwitterException e) {
					e.printStackTrace();
				}
			}
			return true;
		}
	}

}
