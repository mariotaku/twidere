package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getActivatedAccounts;
import static org.mariotaku.twidere.util.Utils.getMentionedNames;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.LinkHandlerActivity;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.StatusesCursorIndices;
import org.mariotaku.twidere.util.Utils;

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
	private FollowInfoTask mFollowInfoTask;
	private ParcelableStatus mStatus;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (BROADCAST_DATABASE_UPDATED.equals(action)) {
				getStatus();
				getSherlockActivity().invalidateOptionsMenu();
			} else if (BROADCAST_FRIENDSHIP_CHANGED.equals(action)) {
				showFollowInfo();
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
		getStatus();
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
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, mStatus.screen_name);
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				break;
			}
			case R.id.follow: {
				ServiceInterface.getInstance(getSherlockActivity()).createFriendship(mAccountId, mStatus.user_id);
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
				if (mStatus.location != null) {
					Bundle bundle = new Bundle();
					bundle.putDouble(INTENT_KEY_LATITUDE, mStatus.location.getLatitude());
					bundle.putDouble(INTENT_KEY_LONGITUDE, mStatus.location.getLongitude());
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
		String text_plain = mStatus.text_plain;
		String screen_name = mStatus.screen_name;
		String name = mStatus.name;
		switch (item.getItemId()) {
			case MENU_SHARE: {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, "@" + mStatus.screen_name + ": " + text_plain);
				startActivity(Intent.createChooser(intent, getString(R.string.share)));
				break;
			}
			case MENU_RETWEET: {
				if (isMyRetweet(getSherlockActivity(), mAccountId, mStatusId)) {
					mServiceInterface.cancelRetweet(mAccountId, mStatusId);
				} else {
					long id_to_retweet = mStatus.is_retweet && mStatus.retweet_id > 0 ? mStatus.retweet_id : mStatus.status_id;
					mServiceInterface.retweetStatus(new long[] { mAccountId }, id_to_retweet);
				}
				break;
			}
			case MENU_QUOTE: {
				Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				Bundle bundle = new Bundle();
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
				bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, mStatusId);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
				bundle.putBoolean(INTENT_KEY_IS_QUOTE, true);
				bundle.putString(INTENT_KEY_TEXT, "RT @" + screen_name + ": " + text_plain);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_REPLY: {
				Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				Bundle bundle = new Bundle();
				bundle.putStringArray(INTENT_KEY_MENTIONS, getMentionedNames(screen_name, text_plain, false, true));
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
				bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, mStatusId);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, screen_name);
				bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, name);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_FAV: {
				if (mStatus.is_favorite) {
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
		setMenuForStatus(getSherlockActivity(), menu, mStatus);
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(BROADCAST_DATABASE_UPDATED);
		filter.addAction(BROADCAST_FRIENDSHIP_CHANGED);
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
		if (mStatus == null) return;

		mNameView.setText(mStatus.name != null ? mStatus.name : "");
		mScreenNameView.setText(mStatus.screen_name != null ? "@" + mStatus.screen_name : "");
		if (mStatus.text != null) {
			mTextView.setText(mStatus.text);
		}
		mTextView.setMovementMethod(LinkMovementMethod.getInstance());
		boolean is_reply = mStatus.in_reply_to_status_id != -1;
		String time = formatToLongTimeString(getSherlockActivity(), mStatus.status_timestamp);
		mTimeAndSourceView.setText(Html.fromHtml(getString(R.string.time_source, time, mStatus.source)));
		mTimeAndSourceView.setMovementMethod(LinkMovementMethod.getInstance());
		mInReplyToView.setVisibility(is_reply ? View.VISIBLE : View.GONE);
		if (is_reply) {
			mInReplyToView.setText(getString(R.string.in_reply_to, mStatus.in_reply_to_screen_name));
		}
		mViewMapButton.setVisibility(mStatus.location != null ? View.VISIBLE : View.GONE);
		mViewMediaButton.setVisibility(mStatus.has_media ? View.VISIBLE : View.GONE);

		LazyImageLoader imageloader = ((TwidereApplication) getSherlockActivity().getApplication())
				.getListProfileImageLoader();
		imageloader.displayImage(mStatus.profile_image_url, mProfileImageView);
	}

	private void getStatus() {
		ParcelableStatus status = getArguments().getParcelable(INTENT_KEY_STATUS);
		if (status != null) {
			mStatus = status;
			return;
		}
		String[] cols = Statuses.COLUMNS;
		String where = Statuses.STATUS_ID + "=" + mStatusId;

		for (Uri uri : TweetStore.STATUSES_URIS) {
			Cursor cur = mResolver.query(uri, cols, where, null, null);
			if (cur != null && cur.getCount() > 0) {
				cur.moveToFirst();
				mStatus = new ParcelableStatus(cur, new StatusesCursorIndices(cur));
				break;
			}
			if (cur != null) {
				cur.close();
			}
		}
		if (mStatus == null) {
			if (getSherlockActivity() instanceof LinkHandlerActivity) {
				// Finish the activity
				getSherlockActivity().finish();
			} else {
				// Maybe I'll remove this fragment here?
			}
		}
	}

	private void showFollowInfo() {
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		mFollowInfoTask = new FollowInfoTask();
		mFollowInfoTask.execute();
	}

	private class FollowInfoTask extends AsyncTask<Void, Void, FollowInfoTask.Response> {

		@Override
		protected Response doInBackground(Void... params) {
			return isAllFollowing();
		}

		@Override
		protected void onPostExecute(Response result) {
			if (result.exception == null) {
				mFollowIndicator.setVisibility(result.value == null || result.value ? View.GONE : View.VISIBLE);
				if (result.value != null) {
					mFollowButton.setVisibility(result.value ? View.GONE : View.VISIBLE);
				}
			} else {
				Utils.showErrorToast(getSherlockActivity(), result.exception, true);
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

		private Response isAllFollowing() {
			if (mStatus == null) return new Response(null, null);
			if (Utils.isMyAccount(getSherlockActivity(), mStatus.user_id)) return new Response(true, null);
			long[] ids = getActivatedAccounts(getSherlockActivity());
			for (long id : ids) {
				Twitter twitter = getTwitterInstance(getSherlockActivity(), id, false);
				try {
					Relationship result = twitter.showFriendship(id, mStatus.user_id);
					if (!result.isSourceFollowingTarget()) return new Response(false, null);
				} catch (TwitterException e) {
					return new Response(null, e);
				}
			}
			return new Response(null, null);
		}

		private class Response {
			public final Boolean value;
			public final TwitterException exception;

			public Response(Boolean value, TwitterException exception) {
				this.value = value;
				this.exception = exception;
			}
		}
	}

}
