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

import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.isMyActivatedAccount;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.parseURL;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.io.IOException;
import java.util.List;

import org.mariotaku.menubar.MenuBar;
import org.mariotaku.menubar.MenuBar.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ImagesPreviewFragment.ImageSpec;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.util.Utils;

import twitter4j.GeoLocation;
import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManagerTrojan;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.twitter.Extractor;

public class ViewStatusFragment extends BaseFragment implements OnClickListener, OnMenuItemClickListener {

	private long mAccountId, mStatusId;
	private ImagesPreviewFragment mImagesPreviewFragment = new ImagesPreviewFragment();
	private ServiceInterface mService;
	private SharedPreferences mPreferences;
	private TextView mNameView, mScreenNameView, mTextView, mTimeAndSourceView, mInReplyToView, mLocationView;
	private ImageView mProfileImageView;
	private Button mFollowButton;
	private View mProfileView, mFollowIndicator, mImagesPreviewContainer, mContentScroller;
	private MenuBar mMenuBar;
	private ProgressBar mProgress;
	private FollowInfoTask mFollowInfoTask;
	private GetStatusTask mGetStatusTask;
	private ParcelableStatus mStatus;
	private boolean mLoadMoreAutomatically;

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BROADCAST_FRIENDSHIP_CHANGED.equals(action)) {
				if (mStatus != null && mStatus.user_id == intent.getLongExtra(INTENT_KEY_USER_ID, -1)
						&& intent.getBooleanExtra(INTENT_KEY_SUCCEED, false)) {
					showFollowInfo(true);
				}
			} else if (BROADCAST_FAVORITE_CHANGED.equals(action)) {
				final long status_id = intent.getLongExtra(INTENT_KEY_STATUS_ID, -1);
				if (status_id > 0 && status_id == mStatusId) {
					getStatus(true);
				}
			}
		}
	};

	private boolean mFollowInfoDisplayed, mLocationInfoDisplayed;

	private LocationInfoTask mLocationInfoTask;

	public void displayStatus(ParcelableStatus status) {
		if (status == null || getActivity() == null) return;
		mStatus = status;

		mMenuBar.inflate(R.menu.menu_status);
		setMenuForStatus(getActivity(), mMenuBar.getMenu(), status);
		mMenuBar.show();

		final boolean is_multiple_account_enabled = getActivatedAccountIds(getActivity()).length > 1;

		mContentScroller.setBackgroundResource(is_multiple_account_enabled ? R.drawable.ic_label_color : 0);
		if (is_multiple_account_enabled) {
			final Drawable d = mContentScroller.getBackground();
			if (d != null) {
				d.mutate().setColorFilter(getAccountColor(getActivity(), status.account_id), PorterDuff.Mode.MULTIPLY);
			}
		}

		mNameView.setText(status.name);
		mScreenNameView.setText(status.screen_name);
		mScreenNameView.setCompoundDrawablesWithIntrinsicBounds(
				status.is_protected ? R.drawable.ic_indicator_is_protected : 0, 0, 0, 0);
		mTextView.setText(status.text);
		final TwidereLinkify linkify = new TwidereLinkify(mTextView);
		linkify.setOnLinkClickListener(new OnLinkClickHandler(getActivity(), mAccountId));
		linkify.addAllLinks();
		final boolean is_reply = status.in_reply_to_status_id > 0;
		final String time = formatToLongTimeString(getActivity(), status.status_timestamp);
		final String source_html = status.source;
		if (!isNullOrEmpty(time) && !isNullOrEmpty(source_html)) {
			mTimeAndSourceView.setText(Html.fromHtml(getString(R.string.time_source, time, source_html)));
		} else if (isNullOrEmpty(time) && !isNullOrEmpty(source_html)) {
			mTimeAndSourceView.setText(Html.fromHtml(getString(R.string.source, source_html)));
		} else if (!isNullOrEmpty(time) && isNullOrEmpty(source_html)) {
			mTimeAndSourceView.setText(time);
		}
		mTimeAndSourceView.setMovementMethod(LinkMovementMethod.getInstance());
		mInReplyToView.setVisibility(is_reply ? View.VISIBLE : View.GONE);
		if (is_reply) {
			mInReplyToView.setText(getString(R.string.in_reply_to, status.in_reply_to_screen_name));
		}

		final LazyImageLoader imageloader = getApplication().getProfileImageLoader();
		final boolean hires_profile_image = mPreferences.getBoolean(PREFERENCE_KEY_HIRES_PROFILE_IMAGE, false);
		imageloader.displayImage(
				hires_profile_image ? parseURL(getBiggerTwitterProfileImage(status.profile_image_url_string))
						: status.profile_image_url, mProfileImageView);
		final List<ImageSpec> images = Utils.getImagesInStatus(status.text_html);
		mImagesPreviewContainer.setVisibility(images.size() > 0 ? View.VISIBLE : View.GONE);
		if (images.size() > 0) {
			mImagesPreviewFragment.clear();
			for (final ImageSpec spec : images) {
				mImagesPreviewFragment.add(spec);
			}
			final FragmentManager fm = getFragmentManager();
			if (!FragmentManagerTrojan.isStateSaved(fm)) {
				final FragmentTransaction ft = fm.beginTransaction();
				ft.replace(R.id.images_preview, mImagesPreviewFragment);
				ft.commit();
			}
		}
		mLocationView.setVisibility(status.location != null ? View.VISIBLE : View.GONE);

		if (mLoadMoreAutomatically) {
			showFollowInfo(true);
			showLocationInfo(true);
		} else {
			mFollowIndicator.setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mService = getApplication().getServiceInterface();
		super.onActivityCreated(savedInstanceState);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
		final Bundle bundle = getArguments();
		if (bundle != null) {
			mAccountId = bundle.getLong(INTENT_KEY_ACCOUNT_ID);
			mStatusId = bundle.getLong(INTENT_KEY_STATUS_ID);
		}
		mInReplyToView.setOnClickListener(this);
		mFollowButton.setOnClickListener(this);
		mProfileView.setOnClickListener(this);
		mLocationView.setOnClickListener(this);
		mMenuBar.setOnMenuItemClickListener(this);
		getStatus(false);
	}

	@Override
	public void onClick(View view) {
		if (mStatus == null) return;
		switch (view.getId()) {
			case R.id.profile: {
				Utils.openUserProfile(getActivity(), mStatus.account_id, mStatus.user_id, null);
				break;
			}
			case R.id.follow: {
				mService.createFriendship(mAccountId, mStatus.user_id);
				break;
			}
			case R.id.in_reply_to: {
				Utils.openConversation(getActivity(), mStatus.account_id, mStatus.status_id);
				break;
			}
			case R.id.location_view: {
				if (mStatus.location == null) return;
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_MAP);
				builder.appendQueryParameter(QUERY_PARAM_LAT, String.valueOf(mStatus.location.getLatitude()));
				builder.appendQueryParameter(QUERY_PARAM_LNG, String.valueOf(mStatus.location.getLongitude()));
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				break;
			}
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.view_status, container, false);
		mContentScroller = view.findViewById(R.id.content_scroller);
		mImagesPreviewContainer = view.findViewById(R.id.images_preview);
		mLocationView = (TextView) view.findViewById(R.id.location_view);
		mNameView = (TextView) view.findViewById(R.id.name);
		mScreenNameView = (TextView) view.findViewById(R.id.screen_name);
		mTextView = (TextView) view.findViewById(R.id.text);
		mProfileImageView = (ImageView) view.findViewById(R.id.profile_image);
		mTimeAndSourceView = (TextView) view.findViewById(R.id.time_source);
		mInReplyToView = (TextView) view.findViewById(R.id.in_reply_to);
		mFollowButton = (Button) view.findViewById(R.id.follow);
		mFollowIndicator = view.findViewById(R.id.follow_indicator);
		mProgress = (ProgressBar) view.findViewById(R.id.progress);
		mProfileView = view.findViewById(R.id.profile);
		mMenuBar = (MenuBar) view.findViewById(R.id.menu_bar);
		return view;
	}

	@Override
	public void onDestroyView() {
		if (mGetStatusTask != null) {
			mGetStatusTask.cancel(true);
		}
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		super.onDestroyView();
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (mStatus == null) return false;
		final String text_plain = mStatus.text_plain;
		final String screen_name = mStatus.screen_name;
		final String name = mStatus.name;
		switch (item.getItemId()) {
			case MENU_SHARE: {
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, "@" + mStatus.screen_name + ": " + text_plain);
				startActivity(Intent.createChooser(intent, getString(R.string.share)));
				break;
			}
			case MENU_RETWEET: {
				if (isMyRetweet(getActivity(), mAccountId, mStatusId)) {
					mService.cancelRetweet(mAccountId, mStatusId);
				} else {
					final long id_to_retweet = mStatus.is_retweet && mStatus.retweet_id > 0 ? mStatus.retweet_id
							: mStatus.status_id;
					mService.retweetStatus(mAccountId, id_to_retweet);
				}
				break;
			}
			case MENU_QUOTE: {
				final Intent intent = new Intent(INTENT_ACTION_COMPOSE);
				final Bundle bundle = new Bundle();
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
				bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, mStatusId);
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
				mentions.remove(screen_name);
				mentions.add(0, screen_name);
				bundle.putStringArray(INTENT_KEY_MENTIONS, mentions.toArray(new String[mentions.size()]));
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
					mService.destroyFavorite(mAccountId, mStatusId);
				} else {
					mService.createFavorite(mAccountId, mStatusId);
				}
				break;
			}
			case MENU_DELETE: {
				mService.destroyStatus(mAccountId, mStatusId);
				break;
			}
			default:
				return false;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStart() {
		super.onStart();
		final IntentFilter filter = new IntentFilter();
		filter.addAction(BROADCAST_FRIENDSHIP_CHANGED);
		filter.addAction(BROADCAST_FAVORITE_CHANGED);
		registerReceiver(mStatusReceiver, filter);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	private void getStatus(boolean omit_intent_extra) {
		if (mGetStatusTask != null) {
			mGetStatusTask.cancel(true);
		}
		mGetStatusTask = new GetStatusTask(omit_intent_extra);
		mGetStatusTask.execute();

	}

	private void showFollowInfo(boolean force) {
		if (mFollowInfoDisplayed && !force) return;
		if (mFollowInfoTask != null) {
			mFollowInfoTask.cancel(true);
		}
		mFollowInfoTask = new FollowInfoTask();
		mFollowInfoTask.execute();
	}

	private void showLocationInfo(boolean force) {
		if (mLocationInfoDisplayed && !force) return;
		if (mLocationInfoTask != null) {
			mLocationInfoTask.cancel(true);
		}
		mLocationInfoTask = new LocationInfoTask();
		mLocationInfoTask.execute();
	}

	private class FollowInfoTask extends AsyncTask<Void, Void, Response<Boolean>> {

		@Override
		protected Response<Boolean> doInBackground(Void... params) {
			return isAllFollowing();
		}

		@Override
		protected void onPostExecute(Response<Boolean> result) {
			if (getActivity() == null) return;
			if (result.exception == null) {
				mFollowIndicator.setVisibility(result.value == null || result.value ? View.GONE : View.VISIBLE);
				if (result.value != null) {
					mFollowButton.setVisibility(result.value ? View.GONE : View.VISIBLE);
					mFollowInfoDisplayed = true;
				}
			}
			mProgress.setVisibility(View.GONE);
			super.onPostExecute(result);
			mFollowInfoTask = null;
		}

		@Override
		protected void onPreExecute() {
			if (getActivity() == null) return;
			mFollowIndicator.setVisibility(View.VISIBLE);
			mFollowButton.setVisibility(View.GONE);
			mProgress.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}

		private Response<Boolean> isAllFollowing() {
			if (mStatus == null) return new Response<Boolean>(null, null);
			if (isMyActivatedAccount(getActivity(), mStatus.user_id)) return new Response<Boolean>(true, null);
			final Twitter twitter = getTwitterInstance(getActivity(), mStatus.account_id, false);
			if (twitter == null) return new Response<Boolean>(null, null);
			try {
				final Relationship result = twitter.showFriendship(mStatus.account_id, mStatus.user_id);
				if (!result.isSourceFollowingTarget()) return new Response<Boolean>(false, null);
			} catch (final TwitterException e) {
				return new Response<Boolean>(null, e);
			}
			return new Response<Boolean>(null, null);
		}
	}

	private class GetStatusTask extends AsyncTask<Void, Void, Response<ParcelableStatus>> {

		private final boolean omit_intent_extra;

		public GetStatusTask(boolean omit_intent_extra) {
			this.omit_intent_extra = omit_intent_extra;
		}

		@Override
		protected Response<ParcelableStatus> doInBackground(Void... params) {
			ParcelableStatus status = null;
			if (!omit_intent_extra) {
				status = getArguments().getParcelable(INTENT_KEY_STATUS);
				if (status != null) return new Response<ParcelableStatus>(status, null);
			}
			status = Utils.findStatusInDatabases(getActivity(), mAccountId, mStatusId);
			if (status != null) return new Response<ParcelableStatus>(status, null);

			final Twitter twitter = getTwitterInstance(getActivity(), mAccountId, false);
			try {
				return new Response<ParcelableStatus>(new ParcelableStatus(twitter.showStatus(mStatusId), mAccountId,
						false), null);
			} catch (final TwitterException e) {
				return new Response<ParcelableStatus>(null, e);
			}
		}

		@Override
		protected void onPostExecute(Response<ParcelableStatus> result) {
			if (getActivity() == null) return;
			if (result.value == null) {
				showErrorToast(getActivity(), result.exception, true);
			} else {
				displayStatus(result.value);
			}
			setProgressBarIndeterminateVisibility(false);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			if (getActivity() == null) return;
			setProgressBarIndeterminateVisibility(true);
			super.onPreExecute();
		}

	}

	private class LocationInfoTask extends AsyncTask<Void, Void, String> {

		@Override
		protected String doInBackground(Void... params) {
			if (mStatus == null || mStatus.location == null) return null;
			final Twitter twitter = getTwitterInstance(getActivity(), mStatus.account_id, false);
			if (twitter == null) return null;
			final GeoLocation location = mStatus.location;
			try {
				final Geocoder coder = new Geocoder(getActivity());
				final List<Address> addresses = coder.getFromLocation(location.getLatitude(), location.getLongitude(),
						1);
				if (addresses.size() == 1) {
					final Address address = addresses.get(0);
					final StringBuilder builder = new StringBuilder();
					final int max_idx = address.getMaxAddressLineIndex();
					for (int i = 0; i < max_idx; i++) {
						builder.append(address.getAddressLine(i));
						if (i != address.getMaxAddressLineIndex() - 1) {
							builder.append(", ");
						}
					}
					return builder.toString();

				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (result != null) {
				mLocationView.setText(result);
				mLocationInfoDisplayed = true;
			} else {
				mLocationView.setText(R.string.view_map);
				mLocationInfoDisplayed = false;
			}
			super.onPostExecute(result);
		}

	}

	private class Response<T> {
		public final T value;
		public final TwitterException exception;

		public Response(T value, TwitterException exception) {
			this.value = value;
			this.exception = exception;
		}
	}

}
