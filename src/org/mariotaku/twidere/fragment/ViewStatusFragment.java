package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getActivatedAccounts;
import static org.mariotaku.twidere.util.Utils.getGeoLocationFromString;
import static org.mariotaku.twidere.util.Utils.getMentionedNames;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.LinkHandlerActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
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
import android.widget.ImageButton;
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
	private TextView mNameView, mScreenNameView, mTextView, mTimeAndSourceView, mInReplyToView;
	private ImageView mProfileImageView;
	private Button mFollowButton;
	private ImageButton mViewMapButton, mViewMediaButton;
	private View mProfileView, mFollowIndicator;
	private ProgressBar mProgress;
	private long mStatusUserId;
	private String mStatusScreenName;
	private GeoLocation mGeoLocation;
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

		mServiceInterface = ((TwidereApplication) getSherlockActivity().getApplication()).getServiceInterface();
		mResolver = getSherlockActivity().getContentResolver();
		super.onActivityCreated(savedInstanceState);
		Bundle bundle = getArguments();
		if (bundle != null) {
			mAccountId = bundle.getLong(INTENT_KEY_ACCOUNT_ID);
			mStatusId = bundle.getLong(INTENT_KEY_STATUS_ID);
		}
		setHasOptionsMenu(true);
		View view = getView();
		mNameView = (TextView) view.findViewById(R.id.name);
		mScreenNameView = (TextView) view.findViewById(R.id.screen_name);
		mTextView = (TextView) view.findViewById(R.id.text);
		mProfileImageView = (ImageView) view.findViewById(R.id.profile_image);
		mTimeAndSourceView = (TextView) view.findViewById(R.id.time_source);
		mInReplyToView = (TextView) view.findViewById(R.id.in_reply_to);
		mInReplyToView.setOnClickListener(this);
		mFollowButton = (Button) view.findViewById(R.id.follow);
		mFollowButton.setOnClickListener(this);
		mFollowIndicator = view.findViewById(R.id.follow_indicator);
		mProfileView = view.findViewById(R.id.profile);
		mProfileView.setOnClickListener(this);
		mViewMapButton = (ImageButton) view.findViewById(R.id.view_map);
		mViewMapButton.setOnClickListener(this);
		mViewMediaButton = (ImageButton) view.findViewById(R.id.view_media);
		mViewMediaButton.setOnClickListener(this);
		mProgress = (ProgressBar) view.findViewById(R.id.progress);
		displayStatus();
		showFollowInfo();
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.profile: {
				Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER);
				builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(mAccountId));
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, mStatusScreenName);
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				break;
			}
			case R.id.follow: {
				ServiceInterface.getInstance(getSherlockActivity()).createFriendship(mAccountId, mStatusUserId);
				break;
			}
			case R.id.in_reply_to: {
				Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_CONVERSATION);
				builder.appendQueryParameter(QUERY_PARAM_ACCOUNT_ID, String.valueOf(mAccountId));
				builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, String.valueOf(mStatusId));
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				break;
			}
			case R.id.view_map: {
				if (mGeoLocation != null) {
					Bundle bundle = new Bundle();
					bundle.putDouble(INTENT_KEY_LATITUDE, mGeoLocation.getLatitude());
					bundle.putDouble(INTENT_KEY_LONGITUDE, mGeoLocation.getLongitude());
					startActivity(new Intent(INTENT_ACTION_VIEW_MAP).putExtras(bundle));
				}
				break;
			}
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
			case MENU_SHARE: {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, "@" + mStatusScreenName + ": " + mTextView.getText());
				startActivity(Intent.createChooser(intent, getString(R.string.share)));
				break;
			}
			case MENU_REPLY: {
				Bundle bundle = new Bundle();
				bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, mStatusId);
				bundle.putStringArray(INTENT_KEY_MENTIONS,
						getMentionedNames(mStatusScreenName, mTextView.getText(), false, true));
				startActivity(new Intent(INTENT_ACTION_COMPOSE).putExtras(bundle));
				break;
			}
			case MENU_RETWEET: {
				if (isMyRetweet(getSherlockActivity(), mAccountId, mStatusId)) {
					mServiceInterface.cancelRetweet(mAccountId, mStatusId);
				} else {
					mServiceInterface.retweetStatus(new long[] { mAccountId }, mStatusId);
				}
				break;
			}
			case MENU_QUOTE: {
				Bundle bundle = new Bundle();
				bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, mStatusId);
				bundle.putBoolean(INTENT_KEY_IS_QUOTE, true);
				bundle.putString(INTENT_KEY_TEXT, "RT @" + mStatusScreenName + ": " + mTextView.getText());
				startActivity(new Intent(INTENT_ACTION_COMPOSE).putExtras(bundle));
				break;
			}
			case MENU_FAV: {
				if (mIsFavorite) {
					mServiceInterface.destroyFavorite(new long[] { mAccountId }, mStatusId);
				} else {
					mServiceInterface.createFavorite(new long[] { mAccountId }, mStatusId);
				}
				break;
			}
			case MENU_DELETE: {
				mServiceInterface.destroyStatus(mAccountId, mStatusId);
				break;
			}
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
				setMenuForStatus(getSherlockActivity(), menu, mStatusId, uri);
				cur.close();
				super.onPrepareOptionsMenu(menu);
				return;
			}
			if (cur != null) {
				cur.close();
			}
		}

		super.onPrepareOptionsMenu(menu);
		if (getSherlockActivity() instanceof LinkHandlerActivity) {
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
			mNameView.setText(name != null ? name : "");
			String screen_name = cur.getString(cur.getColumnIndexOrThrow(Statuses.SCREEN_NAME));
			mScreenNameView.setText(screen_name != null ? "@" + screen_name : "");
			String text = cur.getString(cur.getColumnIndexOrThrow(Statuses.TEXT));
			if (text != null) {
				mTextView.setText(Html.fromHtml(text));
			}
			mTextView.setMovementMethod(LinkMovementMethod.getInstance());
			String source = cur.getString(cur.getColumnIndexOrThrow(Statuses.SOURCE));
			long timestamp = cur.getLong(cur.getColumnIndexOrThrow(Statuses.STATUS_TIMESTAMP));
			boolean is_reply = cur.getLong(cur.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_STATUS_ID)) != -1;
			String time = formatToLongTimeString(getSherlockActivity(), timestamp);
			mTimeAndSourceView.setText(Html.fromHtml(getString(R.string.time_source, time, source)));
			mTimeAndSourceView.setMovementMethod(LinkMovementMethod.getInstance());
			mInReplyToView.setVisibility(is_reply ? View.VISIBLE : View.GONE);
			if (is_reply) {
				mInReplyToView.setText(getString(R.string.in_reply_to,
						cur.getString(cur.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_SCREEN_NAME))));
			}
			mStatusUserId = cur.getLong(cur.getColumnIndexOrThrow(Statuses.USER_ID));
			mIsFavorite = cur.getInt(cur.getColumnIndexOrThrow(Statuses.IS_FAVORITE)) == 1;
			String location_string = cur.getString(cur.getColumnIndexOrThrow(Statuses.LOCATION));
			mGeoLocation = getGeoLocationFromString(location_string);
			mViewMapButton.setVisibility(mGeoLocation != null ? View.VISIBLE : View.GONE);
			boolean has_media = cur.getInt(cur.getColumnIndexOrThrow(Statuses.HAS_MEDIA)) == 1;
			mViewMediaButton.setVisibility(has_media ? View.VISIBLE : View.GONE);

			LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
					.getListProfileImageLoader();
			String profile_image_url = cur.getString(cur.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL));
			URL url = null;
			try {
				url = new URL(profile_image_url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			imageloader.displayImage(url, mProfileImageView);
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
			long[] ids = getActivatedAccounts(getSherlockActivity());
			for (long id : ids) {
				if (id == mStatusUserId) {
					continue;
				}
				Twitter twitter = getTwitterInstance(getSherlockActivity(), id, false);
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
