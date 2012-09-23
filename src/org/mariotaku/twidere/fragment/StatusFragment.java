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

import static org.mariotaku.twidere.util.Utils.clearUserColor;
import static org.mariotaku.twidere.util.Utils.findStatusInDatabases;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getImagesInStatus;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.isMyActivatedAccount;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.openConversation;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.openUserRetweetedStatus;
import static org.mariotaku.twidere.util.Utils.parseURL;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;
import static org.mariotaku.twidere.util.Utils.setUserColor;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.io.IOException;
import java.util.List;

import org.mariotaku.menubar.MenuBar;
import org.mariotaku.menubar.MenuBar.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.TwidereLinkify;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import android.widget.Toast;

import com.twitter.Extractor;

public class StatusFragment extends BaseFragment implements OnClickListener, OnMenuItemClickListener, Panes.Right {

	private long mAccountId, mStatusId;
	private boolean mLoadMoreAutomatically;
	private boolean mFollowInfoDisplayed, mLocationInfoDisplayed;

	private ServiceInterface mService;
	private SharedPreferences mPreferences;
	private LazyImageLoader mProfileImageLoader;

	private TextView mNameView, mScreenNameView, mTextView, mTimeAndSourceView, mInReplyToView, mLocationView,
			mRetweetedStatusView;
	private ImageView mProfileImageView;
	private Button mFollowButton;
	private View mStatusContent, mProfileView, mFollowIndicator, mImagesPreviewContainer, mContentScroller,
			mUserColorLabel;
	private MenuBar mMenuBar;
	private ProgressBar mStatusLoadProgress, mFollowInfoProgress;

	private ImagesPreviewFragment mImagesPreviewFragment;

	private ParcelableStatus mStatus;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
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
			} else if (BROADCAST_RETWEET_CHANGED.equals(action)) {
				final long status_id = intent.getLongExtra(INTENT_KEY_STATUS_ID, -1);
				if (status_id > 0 && status_id == mStatusId) {
					getStatus(true);
				}
			}
		}
	};

	private static final String INTENT_KEY_OMIT_INTENT_EXTRA = "omit_intent_extra";

	private boolean mStatusLoaderInitialized, mLocationLoaderInitialized;

	final LoaderCallbacks<Response<ParcelableStatus>> mStatusLoaderCallbacks = new LoaderCallbacks<Response<ParcelableStatus>>() {

		@Override
		public Loader<Response<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
			mStatusLoadProgress.setVisibility(View.VISIBLE);
			mStatusContent.setVisibility(View.INVISIBLE);
			mStatusContent.setEnabled(false);
			setProgressBarIndeterminateVisibility(true);
			final boolean omit_intent_extra = args != null ? args.getBoolean(INTENT_KEY_OMIT_INTENT_EXTRA, true) : true;
			return new StatusLoader(getActivity(), omit_intent_extra, getArguments(), mAccountId, mStatusId);
		}

		@Override
		public void onLoaderReset(final Loader<Response<ParcelableStatus>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<Response<ParcelableStatus>> loader,
				final Response<ParcelableStatus> data) {
			if (data.value == null) {
				showErrorToast(getActivity(), data.exception, true);
			} else {
				displayStatus(data.value);
				mStatusLoadProgress.setVisibility(View.GONE);
				mStatusContent.setVisibility(View.VISIBLE);
				mStatusContent.setEnabled(true);
			}
			setProgressBarIndeterminateVisibility(false);
		}

	};

	final LoaderCallbacks<String> mLocationLoaderCallbacks = new LoaderCallbacks<String>() {

		@Override
		public Loader<String> onCreateLoader(final int id, final Bundle args) {
			return new LocationInfoLoader(getActivity(), mStatus != null ? mStatus.location : null);
		}

		@Override
		public void onLoaderReset(final Loader<String> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<String> loader, final String data) {
			if (data != null) {
				mLocationView.setText(data);
				mLocationInfoDisplayed = true;
			} else {
				mLocationView.setText(R.string.view_map);
				mLocationInfoDisplayed = false;
			}
		}

	};

	final LoaderCallbacks<Response<Boolean>> mFollowInfoLoaderCallbacks = new LoaderCallbacks<Response<Boolean>>() {

		@Override
		public Loader<Response<Boolean>> onCreateLoader(final int id, final Bundle args) {
			mFollowIndicator.setVisibility(View.VISIBLE);
			mFollowButton.setVisibility(View.GONE);
			mFollowInfoProgress.setVisibility(View.VISIBLE);
			return new FollowInfoLoader(getActivity(), mStatus);
		}

		@Override
		public void onLoaderReset(final Loader<Response<Boolean>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<Response<Boolean>> loader, final Response<Boolean> data) {
			if (data.exception == null) {
				mFollowIndicator.setVisibility(data.value == null || data.value ? View.GONE : View.VISIBLE);
				if (data.value != null) {
					mFollowButton.setVisibility(data.value ? View.GONE : View.VISIBLE);
					mFollowInfoDisplayed = true;
				}
			}
			mFollowInfoProgress.setVisibility(View.GONE);
		}

	};

	private static final int LOADER_ID_STATUS = 1;

	private static final int LOADER_ID_FOLLOW = 2;

	private static final int LOADER_ID_LOCATION = 3;

	private boolean mFollowInfoLoaderInitialized;

	public void displayStatus(final ParcelableStatus status) {
		mStatus = null;
		mImagesPreviewFragment.clear();
		if (status == null || getActivity() == null) return;
		mStatus = status;

		mMenuBar.inflate(R.menu.menu_status);
		setMenuForStatus(getActivity(), mMenuBar.getMenu(), status);
		mMenuBar.show();

		final boolean is_multiple_account_enabled = getActivatedAccountIds(getActivity()).length > 1;

		updateUserColor();

		mContentScroller.setBackgroundResource(is_multiple_account_enabled ? R.drawable.ic_label_account_nopadding : 0);
		if (is_multiple_account_enabled) {
			final Drawable d = mContentScroller.getBackground();
			if (d != null) {
				d.mutate().setColorFilter(getAccountColor(getActivity(), status.account_id), PorterDuff.Mode.MULTIPLY);
				mContentScroller.invalidate();
			}
		}

		mNameView.setText(status.name);
		mScreenNameView.setText(status.screen_name);
		mScreenNameView.setCompoundDrawablesWithIntrinsicBounds(
				getUserTypeIconRes(status.is_verified, status.is_protected), 0, 0, 0);
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

		final boolean hires_profile_image = getResources().getBoolean(R.bool.hires_profile_image);

		mProfileImageLoader.displayImage(
				parseURL(hires_profile_image ? getBiggerTwitterProfileImage(status.profile_image_url_string)
						: status.profile_image_url_string), mProfileImageView);
		final List<ImageSpec> images = getImagesInStatus(status.text_html);
		mImagesPreviewContainer.setVisibility(images.size() > 0 ? View.VISIBLE : View.GONE);
		mImagesPreviewFragment.addAll(images);
		mImagesPreviewFragment.update();
		if (mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false)) {
			mImagesPreviewFragment.show();
		}
		mRetweetedStatusView.setVisibility(status.is_protected ? View.GONE : View.VISIBLE);
		if (status.retweet_id > 0) {
			final boolean display_name = mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_NAME, true);
			final String retweeted_by = display_name ? status.retweeted_by_name : status.retweeted_by_screen_name;
			mRetweetedStatusView.setText(status.retweet_count > 1 ? getString(R.string.retweeted_by_with_count,
					retweeted_by, status.retweet_count - 1) : getString(R.string.retweeted_by, retweeted_by));
		} else {
			mRetweetedStatusView.setText(R.string.users_retweeted_this);
		}
		mLocationView.setVisibility(ParcelableLocation.isValidLocation(status.location) ? View.VISIBLE : View.GONE);

		if (mLoadMoreAutomatically) {
			showFollowInfo(true);
			showLocationInfo(true);
		} else {
			mFollowIndicator.setVisibility(View.GONE);
		}
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final TwidereApplication application = getApplication();
		mService = application.getServiceInterface();
		mProfileImageLoader = application.getProfileImageLoader();
		mImagesPreviewFragment = (ImagesPreviewFragment) Fragment.instantiate(getActivity(),
				ImagesPreviewFragment.class.getName());
		super.onActivityCreated(savedInstanceState);
		setRetainInstance(true);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
		final Bundle bundle = getArguments();
		if (bundle != null) {
			mAccountId = bundle.getLong(INTENT_KEY_ACCOUNT_ID);
			mStatusId = bundle.getLong(INTENT_KEY_STATUS_ID);
			mStatus = bundle.getParcelable(INTENT_KEY_STATUS);
		}
		mInReplyToView.setOnClickListener(this);
		mFollowButton.setOnClickListener(this);
		mProfileView.setOnClickListener(this);
		mLocationView.setOnClickListener(this);
		mRetweetedStatusView.setOnClickListener(this);
		mMenuBar.setOnMenuItemClickListener(this);
		final FragmentManager fm = getFragmentManager();
		final FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.images_preview, mImagesPreviewFragment);
		ft.commit();
		getStatus(false);

	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		if (intent == null || mStatus == null) return;
		switch (requestCode) {
			case REQUEST_SET_COLOR: {
				if (resultCode == Activity.RESULT_OK) if (intent != null && intent.getExtras() != null) {
					final int color = intent.getIntExtra(Accounts.USER_COLOR, Color.TRANSPARENT);
					setUserColor(getActivity(), mStatus.user_id, color);
					updateUserColor();
				}
				break;
			}
		}

	}

	@Override
	public void onClick(final View view) {
		if (mStatus == null) return;
		switch (view.getId()) {
			case R.id.profile: {
				openUserProfile(getActivity(), mStatus.account_id, mStatus.user_id, null);
				break;
			}
			case R.id.follow: {
				mService.createFriendship(mAccountId, mStatus.user_id);
				break;
			}
			case R.id.in_reply_to: {
				openConversation(getActivity(), mStatus.account_id, mStatus.status_id);
				break;
			}
			case R.id.location_view: {
				if (mStatus.location == null) return;
				final ParcelableLocation location = mStatus.location;
				if (location == null || !location.isValid()) return;
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_MAP);
				builder.appendQueryParameter(QUERY_PARAM_LAT, String.valueOf(location.latitude));
				builder.appendQueryParameter(QUERY_PARAM_LNG, String.valueOf(location.longitude));
				startActivity(new Intent(Intent.ACTION_VIEW, builder.build()));
				break;
			}
			case R.id.retweet_view: {
				openUserRetweetedStatus(getActivity(), mStatus.account_id, mStatus.retweet_id > 0 ? mStatus.retweet_id
						: mStatus.status_id);
				break;
			}
		}

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.view_status, container, false);
		mStatusContent = view.findViewById(R.id.content);
		mStatusLoadProgress = (ProgressBar) view.findViewById(R.id.status_load_progress);
		mUserColorLabel = view.findViewById(R.id.user_color_label);
		mContentScroller = view.findViewById(R.id.content_scroller);
		mImagesPreviewContainer = view.findViewById(R.id.images_preview);
		mLocationView = (TextView) view.findViewById(R.id.location_view);
		mRetweetedStatusView = (TextView) view.findViewById(R.id.retweet_view);
		mNameView = (TextView) view.findViewById(R.id.name);
		mScreenNameView = (TextView) view.findViewById(R.id.screen_name);
		mTextView = (TextView) view.findViewById(R.id.text);
		mProfileImageView = (ImageView) view.findViewById(R.id.profile_image);
		mTimeAndSourceView = (TextView) view.findViewById(R.id.time_source);
		mInReplyToView = (TextView) view.findViewById(R.id.in_reply_to);
		mFollowButton = (Button) view.findViewById(R.id.follow);
		mFollowIndicator = view.findViewById(R.id.follow_indicator);
		mFollowInfoProgress = (ProgressBar) view.findViewById(R.id.follow_info_progress);
		mProfileView = view.findViewById(R.id.profile);
		mMenuBar = (MenuBar) view.findViewById(R.id.menu_bar);
		return view;
	}

	@Override
	public void onDestroyView() {
		mStatus = null;
		mAccountId = -1;
		mStatusId = -1;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_STATUS);
		lm.destroyLoader(LOADER_ID_LOCATION);
		lm.destroyLoader(LOADER_ID_FOLLOW);
		super.onDestroyView();
	}

	@Override
	public boolean onMenuItemClick(final MenuItem item) {
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
				if (isMyRetweet(mStatus)) {
					mService.cancelRetweet(mAccountId, mStatus.retweet_id);
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
			case MENU_EXTENSIONS: {
				final Intent intent = new Intent(INTENT_ACTION_EXTENSION_OPEN_STATUS);
				final Bundle extras = new Bundle();
				extras.putParcelable(INTENT_KEY_STATUS, mStatus);
				intent.putExtras(extras);
				startActivity(Intent.createChooser(intent, getString(R.string.open_with_extensions)));
				break;
			}
			case MENU_MUTE_SOURCE: {
				final String source = HtmlEscapeHelper.unescape(mStatus.source);
				if (source == null) return false;
				final Uri uri = Filters.Sources.CONTENT_URI;
				final ContentValues values = new ContentValues();
				final SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFERENCES_NAME,
						Context.MODE_PRIVATE).edit();
				final ContentResolver resolver = getContentResolver();
				values.put(Filters.TEXT, source);
				resolver.delete(uri, Filters.TEXT + " = '" + source + "'", null);
				resolver.insert(uri, values);
				editor.putBoolean(PREFERENCE_KEY_ENABLE_FILTER, true).commit();
				Toast.makeText(getActivity(), getString(R.string.source_muted, source), Toast.LENGTH_SHORT).show();
				break;
			}
			case MENU_SET_COLOR: {
				final Intent intent = new Intent(INTENT_ACTION_SET_COLOR);
				startActivityForResult(intent, REQUEST_SET_COLOR);
				break;
			}
			case MENU_CLEAR_COLOR: {
				clearUserColor(getActivity(), mStatus.user_id);
				updateUserColor();
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
		filter.addAction(BROADCAST_RETWEET_CHANGED);
		registerReceiver(mStatusReceiver, filter);
		updateUserColor();
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	private void getStatus(final boolean omit_intent_extra) {
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_STATUS);
		final Bundle args = new Bundle();
		args.putBoolean(INTENT_KEY_OMIT_INTENT_EXTRA, omit_intent_extra);
		if (!mStatusLoaderInitialized) {
			lm.initLoader(LOADER_ID_STATUS, args, mStatusLoaderCallbacks);
			mStatusLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_STATUS, args, mStatusLoaderCallbacks);
		}
	}

	private void showFollowInfo(final boolean force) {
		if (mFollowInfoDisplayed && !force) return;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_FOLLOW);
		if (!mFollowInfoLoaderInitialized) {
			lm.initLoader(LOADER_ID_FOLLOW, null, mLocationLoaderCallbacks);
			mFollowInfoLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_FOLLOW, null, mLocationLoaderCallbacks);
		}
	}

	private void showLocationInfo(final boolean force) {
		if (mLocationInfoDisplayed && !force) return;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_LOCATION);
		if (!mLocationLoaderInitialized) {
			lm.initLoader(LOADER_ID_LOCATION, null, mLocationLoaderCallbacks);
			mLocationLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_LOCATION, null, mLocationLoaderCallbacks);
		}
	}

	private void updateUserColor() {
		if (mUserColorLabel != null && mStatus != null) {
			final Drawable d = mUserColorLabel.getBackground();
			if (d != null) {
				d.mutate().setColorFilter(getUserColor(getActivity(), mStatus.user_id), Mode.MULTIPLY);
				mUserColorLabel.invalidate();
			}
		}
	}

	public static class LocationInfoLoader extends AsyncTaskLoader<String> {

		private final Context context;
		private final ParcelableLocation location;

		public LocationInfoLoader(final Context context, final ParcelableLocation location) {
			super(context);
			this.context = context;
			this.location = location;
		}

		@Override
		public String loadInBackground() {
			if (location == null) return null;
			try {
				final Geocoder coder = new Geocoder(context);
				final List<Address> addresses = coder.getFromLocation(location.latitude, location.longitude, 1);
				if (addresses.size() == 1) {
					final Address address = addresses.get(0);
					final StringBuilder builder = new StringBuilder();
					final int max_idx = address.getMaxAddressLineIndex();
					for (int i = 0; i < max_idx; i++) {
						builder.append(address.getAddressLine(i));
						if (i != max_idx - 1) {
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
		protected void onStartLoading() {
			forceLoad();
		}

	}

	public static class Response<T> {
		public final T value;
		public final TwitterException exception;

		public Response(final T value, final TwitterException exception) {
			this.value = value;
			this.exception = exception;
		}
	}

	public static class StatusLoader extends AsyncTaskLoader<Response<ParcelableStatus>> {

		private final boolean omit_intent_extra;
		private final Context context;
		private final Bundle intent_args;
		private final long account_id, status_id;

		public StatusLoader(final Context context, final boolean omit_intent_extra, final Bundle intent_args,
				final long account_id, final long status_id) {
			super(context);
			this.context = context;
			this.intent_args = intent_args;
			this.account_id = account_id;
			this.status_id = status_id;
			this.omit_intent_extra = omit_intent_extra;
		}

		@Override
		public Response<ParcelableStatus> loadInBackground() {
			ParcelableStatus status = null;
			if (!omit_intent_extra) {
				status = intent_args.getParcelable(INTENT_KEY_STATUS);
				if (status != null) return new Response<ParcelableStatus>(status, null);
			}
			status = findStatusInDatabases(context, account_id, status_id);
			if (status != null) return new Response<ParcelableStatus>(status, null);

			final Twitter twitter = getTwitterInstance(context, account_id, false);
			try {
				return new Response<ParcelableStatus>(new ParcelableStatus(twitter.showStatus(status_id), account_id,
						false), null);
			} catch (final TwitterException e) {
				return new Response<ParcelableStatus>(null, e);
			}
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

	}

	static class FollowInfoLoader extends AsyncTaskLoader<Response<Boolean>> {

		private final ParcelableStatus status;
		private final Context context;

		public FollowInfoLoader(final Context context, final ParcelableStatus status) {
			super(context);
			this.context = context;
			this.status = status;
		}

		@Override
		public Response<Boolean> loadInBackground() {
			return isAllFollowing();
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		private Response<Boolean> isAllFollowing() {
			if (status == null) return new Response<Boolean>(null, null);
			if (isMyActivatedAccount(context, status.user_id)) return new Response<Boolean>(true, null);
			final Twitter twitter = getTwitterInstance(context, status.account_id, false);
			if (twitter == null) return new Response<Boolean>(null, null);
			try {
				final Relationship result = twitter.showFriendship(status.account_id, status.user_id);
				if (!result.isSourceFollowingTarget()) return new Response<Boolean>(false, null);
			} catch (final TwitterException e) {
				return new Response<Boolean>(null, e);
			}
			return new Response<Boolean>(null, null);
		}
	}

}
