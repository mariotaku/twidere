/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.support;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.clearUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserColor;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.getUserNickname;
import static org.mariotaku.twidere.util.UserColorNicknameUtils.setUserColor;
import static org.mariotaku.twidere.util.Utils.cancelRetweet;
import static org.mariotaku.twidere.util.Utils.findStatus;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getDefaultTextSize;
import static org.mariotaku.twidere.util.Utils.getDisplayName;
import static org.mariotaku.twidere.util.Utils.getMapStaticImageUri;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.isSameAccount;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openMap;
import static org.mariotaku.twidere.util.Utils.openStatus;
import static org.mariotaku.twidere.util.Utils.openStatusReplies;
import static org.mariotaku.twidere.util.Utils.openStatusRetweeters;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.scrollListToPosition;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;
import static org.mariotaku.twidere.util.Utils.showErrorMessage;
import static org.mariotaku.twidere.util.Utils.showOkMessage;
import static org.mariotaku.twidere.util.Utils.startStatusShareChooser;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.ucdavis.earlybird.ProfilingUtil;

import org.mariotaku.menucomponent.widget.MenuBar;
import org.mariotaku.refreshnow.widget.RefreshMode;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.support.AccountSelectorActivity;
import org.mariotaku.twidere.activity.support.ColorPickerDialogActivity;
import org.mariotaku.twidere.activity.support.LinkHandlerActivity;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.Account.AccountWithCredentials;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableMedia;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.PreviewMedia;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.task.AsyncTask;
import org.mariotaku.twidere.text.method.StatusContentMovementMethod;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.MediaPreviewUtils.OnMediaClickListener;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.SmartBarUtils;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout;

import twitter4j.Relationship;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StatusFragment extends ParcelableStatusesListFragment implements OnClickListener, Panes.Right,
		OnMediaClickListener, OnSharedPreferenceChangeListener {

	private static final int LOADER_ID_STATUS = 1;
	private static final int LOADER_ID_FOLLOW = 2;
	private static final int LOADER_ID_LOCATION = 3;

	private ParcelableStatus mStatus;

	private boolean mLoadMoreAutomatically;
	private boolean mFollowInfoDisplayed, mLocationInfoDisplayed;
	private boolean mStatusLoaderInitialized, mLocationLoaderInitialized;
	private boolean mFollowInfoLoaderInitialized;;
	private boolean mShouldScroll;
	private SharedPreferences mPreferences;
	private AsyncTwitterWrapper mTwitterWrapper;
	private ImageLoaderWrapper mImageLoader;
	private Handler mHandler;
	private TextView mNameView, mScreenNameView, mTextView, mTimeSourceView, mInReplyToView, mLocationView,
			mRetweetView, mRepliesView;

	private ImageView mProfileImageView, mMapView;
	private Button mFollowButton;
	private Button mRetryButton;
	private View mMainContent, mFollowIndicator, mImagePreviewContainer, mLocationContainer, mLocationBackgroundView;
	private ColorLabelRelativeLayout mProfileView;
	private MenuBar mMenuBar;
	private ProgressBar mDetailsLoadProgress, mFollowInfoProgress;
	private LinearLayout mImagePreviewGrid;
	private View mHeaderView;
	private View mLoadImagesIndicator;
	private ExtendedFrameLayout mDetailsContainer;
	private ListView mListView;

	private LoadConversationTask mConversationTask;

	private final BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (getActivity() == null || !isAdded() || isDetached()) return;
			final String action = intent.getAction();
			if (BROADCAST_FRIENDSHIP_CHANGED.equals(action)) {
				if (mStatus != null && mStatus.user_id == intent.getLongExtra(EXTRA_USER_ID, -1)
						&& intent.getBooleanExtra(EXTRA_SUCCEED, false)) {
					showFollowInfo(true);
				}
			} else if (BROADCAST_FAVORITE_CHANGED.equals(action)) {
				final ParcelableStatus status = intent.getParcelableExtra(EXTRA_STATUS);
				if (mStatus != null && status != null && isSameAccount(context, status.account_id, mStatus.account_id)
						&& status.id == getStatusId()) {
					getStatus(true);
				}
			} else if (BROADCAST_RETWEET_CHANGED.equals(action)) {
				final long status_id = intent.getLongExtra(EXTRA_STATUS_ID, -1);
				if (status_id > 0 && status_id == getStatusId()) {
					getStatus(true);
				}
			}
		}
	};

	private final LoaderCallbacks<SingleResponse<ParcelableStatus>> mStatusLoaderCallbacks = new LoaderCallbacks<SingleResponse<ParcelableStatus>>() {

		@Override
		public Loader<SingleResponse<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
			mDetailsLoadProgress.setVisibility(View.VISIBLE);
			mMainContent.setVisibility(View.INVISIBLE);
			mRetryButton.setVisibility(View.GONE);
			mMainContent.setEnabled(false);
			setProgressBarIndeterminateVisibility(true);
			final boolean omitIntentExtra = args.getBoolean(EXTRA_OMIT_INTENT_EXTRA, true);
			final long accountId = args.getLong(EXTRA_ACCOUNT_ID, -1);
			final long statusId = args.getLong(EXTRA_STATUS_ID, -1);
			return new ParcelableStatusLoader(getActivity(), omitIntentExtra, getArguments(), accountId, statusId);
		}

		@Override
		public void onLoaderReset(final Loader<SingleResponse<ParcelableStatus>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<SingleResponse<ParcelableStatus>> loader,
				final SingleResponse<ParcelableStatus> data) {
			if (data.data == null) {
				// TODO
				mRetryButton.setVisibility(View.VISIBLE);
				showErrorMessage(getActivity(), getString(R.string.action_getting_status), data.exception, true);
			} else {
				mRetryButton.setVisibility(View.GONE);
				displayStatus(data.data);
				mDetailsLoadProgress.setVisibility(View.GONE);
				mMainContent.setVisibility(View.VISIBLE);
				mMainContent.setEnabled(true);
			}
			setProgressBarIndeterminateVisibility(false);
		}

	};

	private final LoaderCallbacks<String> mLocationLoaderCallbacks = new LoaderCallbacks<String>() {

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

	private final LoaderCallbacks<SingleResponse<Boolean>> mFollowInfoLoaderCallbacks = new LoaderCallbacks<SingleResponse<Boolean>>() {

		@Override
		public Loader<SingleResponse<Boolean>> onCreateLoader(final int id, final Bundle args) {
			mFollowIndicator.setVisibility(View.VISIBLE);
			mFollowButton.setVisibility(View.GONE);
			mFollowInfoProgress.setVisibility(View.VISIBLE);
			return new FollowInfoLoader(getActivity(), mStatus);
		}

		@Override
		public void onLoaderReset(final Loader<SingleResponse<Boolean>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<SingleResponse<Boolean>> loader, final SingleResponse<Boolean> data) {
			if (data.exception == null) {
				mFollowIndicator.setVisibility(data.data == null || data.data ? View.GONE : View.VISIBLE);
				if (data.data != null) {
					mFollowButton.setVisibility(data.data ? View.GONE : View.VISIBLE);
					mFollowInfoDisplayed = true;
				}
			}
			mFollowInfoProgress.setVisibility(View.GONE);
		}

	};

	private final OnMenuItemClickListener mMenuItemClickListener = new OnMenuItemClickListener() {

		@Override
		public boolean onMenuItemClick(final MenuItem item) {
			return handleMenuItemClick(item);
		}
	};

	public void displayStatus(final ParcelableStatus status) {
		final boolean status_unchanged = mStatus != null && status != null && status.equals(mStatus);
		if (!status_unchanged) {
			getListAdapter().setData(null);
			if (mStatus != null) {
				// UCD
				ProfilingUtil.profile(getActivity(), mStatus.account_id, "End, " + mStatus.id);
			}
		} else {
			setSelection(0);
		}
		if (mConversationTask != null && mConversationTask.getStatus() == AsyncTask.Status.RUNNING) {
			mConversationTask.cancel(true);
		}
		mStatus = status;
		if (mStatus != null) {
			// UCD
			ProfilingUtil.profile(getActivity(), mStatus.account_id, "Start, " + mStatus.id);
		}
		if (!status_unchanged) {
			clearPreviewImages();
			hidePreviewImages();
		}
		if (status == null || getActivity() == null) return;
		final Bundle args = getArguments();
		args.putLong(EXTRA_ACCOUNT_ID, status.account_id);
		args.putLong(EXTRA_STATUS_ID, status.id);
		args.putParcelable(EXTRA_STATUS, status);
		if (shouldUseNativeMenu()) {
			getActivity().supportInvalidateOptionsMenu();
		} else {
			setMenuForStatus(getActivity(), mMenuBar.getMenu(), status);
			mMenuBar.show();
		}

		updateUserColor();
		mProfileView.drawEnd(getAccountColor(getActivity(), status.account_id));
		final boolean nickname_only = mPreferences.getBoolean(KEY_NICKNAME_ONLY, false);
		final boolean name_first = mPreferences.getBoolean(KEY_NAME_FIRST, true);
		final boolean display_image_preview = mPreferences.getBoolean(KEY_DISPLAY_IMAGE_PREVIEW, false);
		final String nick = getUserNickname(getActivity(), status.user_id, true);
		mNameView.setText(TextUtils.isEmpty(nick) ? status.user_name : nickname_only ? nick : getString(
				R.string.name_with_nickname, status.user_name, nick));
		mNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
				getUserTypeIconRes(status.user_is_verified, status.user_is_protected), 0);
		mScreenNameView.setText("@" + status.user_screen_name);
		mTextView.setText(Html.fromHtml(status.text_html));
		final TwidereLinkify linkify = new TwidereLinkify(
				new OnLinkClickHandler(getActivity(), getMultiSelectManager()));
		linkify.setLinkTextColor(ThemeUtils.getUserLinkTextColor(getActivity()));
		linkify.applyAllLinks(mTextView, status.account_id, status.is_possibly_sensitive);
		mTextView.setMovementMethod(StatusContentMovementMethod.getInstance());
		final String timeString = formatToLongTimeString(getActivity(), status.timestamp);
		final String source_html = status.source;
		if (!isEmpty(timeString) && !isEmpty(source_html)) {
			mTimeSourceView.setText(Html.fromHtml(getString(R.string.time_source, timeString, source_html)));
		} else if (isEmpty(timeString) && !isEmpty(source_html)) {
			mTimeSourceView.setText(Html.fromHtml(getString(R.string.source, source_html)));
		} else if (!isEmpty(timeString) && isEmpty(source_html)) {
			mTimeSourceView.setText(timeString);
		}
		mTimeSourceView.setMovementMethod(LinkMovementMethod.getInstance());

		final String in_reply_to = getDisplayName(getActivity(), status.in_reply_to_user_id, status.in_reply_to_name,
				status.in_reply_to_screen_name, name_first, nickname_only, true);
		mInReplyToView.setText(getString(R.string.in_reply_to, in_reply_to));

		if (mPreferences.getBoolean(KEY_DISPLAY_PROFILE_IMAGE, true)) {
			mImageLoader.displayProfileImage(mProfileImageView, status.user_profile_image_url);
		} else {
			mProfileImageView.setImageResource(R.drawable.ic_profile_image_default);
		}
		final List<String> images = MediaPreviewUtils.getSupportedLinksInStatus(status.text_html);
		mImagePreviewContainer.setVisibility(images.isEmpty() ? View.GONE : View.VISIBLE);
		if (display_image_preview) {
			loadPreviewImages();
		}
		mRetweetView.setVisibility(!status.user_is_protected ? View.VISIBLE : View.GONE);
		if (status.is_retweet && status.retweet_id > 0) {
			final String retweeted_by = getDisplayName(getActivity(), status.retweeted_by_id, status.retweeted_by_name,
					status.retweeted_by_screen_name, name_first, nickname_only, true);
			if (status.retweet_count > 1) {
				mRetweetView
						.setText(getString(R.string.retweeted_by_with_count, retweeted_by, status.retweet_count - 1));
			} else {
				mRetweetView.setText(getString(R.string.retweeted_by, retweeted_by));
			}
		} else {
			if (status.retweet_count > 0) {
				mRetweetView.setText(getString(R.string.retweeted_by_count, status.retweet_count));
			} else {
				mRetweetView.setText(R.string.users_retweeted_this);
			}
		}
		final ParcelableLocation location = status.location;
		final boolean is_valid_location = ParcelableLocation.isValidLocation(location);
		mLocationContainer.setVisibility(is_valid_location ? View.VISIBLE : View.GONE);
		// mMapView.setVisibility(View.VISIBLE);
		// mLocationView.setVisibility(View.VISIBLE);
		if (display_image_preview) {
			mMapView.setVisibility(is_valid_location ? View.VISIBLE : View.GONE);
			mLocationBackgroundView.setVisibility(is_valid_location ? View.VISIBLE : View.GONE);
			mLocationView.setVisibility(View.VISIBLE);
			if (is_valid_location) {
				mHandler.post(new DisplayMapRunnable(location, mImageLoader, mMapView));
			} else {
				mMapView.setImageDrawable(null);
			}
		} else {
			mMapView.setVisibility(View.GONE);
			mLocationBackgroundView.setVisibility(View.GONE);
			mMapView.setImageDrawable(null);
			mLocationView.setVisibility(View.VISIBLE);
		}
		if (mLoadMoreAutomatically) {
			showFollowInfo(true);
			showLocationInfo(true);
			showConversation();
		} else {
			mFollowIndicator.setVisibility(View.GONE);
		}
		updateConversationInfo();
		scrollToStart();
	}

	@Override
	public Loader<List<ParcelableStatus>> newLoaderInstance(final Context context, final Bundle args) {
		return null;
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setRefreshMode(shouldEnablePullToRefresh() ? RefreshMode.BOTH : RefreshMode.NONE);
		setHasOptionsMenu(shouldUseNativeMenu());
		setListShownNoAnimation(true);
		mHandler = new Handler();
		mListView = getListView();
		getListAdapter().setGapDisallowed(true);
		final TwidereApplication application = getApplication();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		getSharedPreferences(USER_COLOR_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(this);
		getSharedPreferences(USER_NICKNAME_PREFERENCES_NAME, Context.MODE_PRIVATE)
				.registerOnSharedPreferenceChangeListener(this);
		mImageLoader = application.getImageLoaderWrapper();
		mTwitterWrapper = getTwitterWrapper();
		mLoadMoreAutomatically = mPreferences.getBoolean(KEY_LOAD_MORE_AUTOMATICALLY, false);
		mLoadImagesIndicator.setOnClickListener(this);
		mInReplyToView.setOnClickListener(this);
		mRepliesView.setOnClickListener(this);
		mFollowButton.setOnClickListener(this);
		mProfileView.setOnClickListener(this);
		mLocationContainer.setOnClickListener(this);
		mRetweetView.setOnClickListener(this);
		mMenuBar.setVisibility(shouldUseNativeMenu() ? View.GONE : View.VISIBLE);
		mMenuBar.inflate(R.menu.menu_status);
		mMenuBar.setIsBottomBar(true);
		mMenuBar.setOnMenuItemClickListener(mMenuItemClickListener);
		getStatus(false);
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_SET_COLOR: {
				if (mStatus == null) return;
				if (resultCode == Activity.RESULT_OK) {
					if (data == null) return;
					final int color = data.getIntExtra(EXTRA_COLOR, Color.TRANSPARENT);
					setUserColor(getActivity(), mStatus.user_id, color);
				} else if (resultCode == ColorPickerDialogActivity.RESULT_CLEARED) {
					clearUserColor(getActivity(), mStatus.user_id);
				}
				break;
			}
			case REQUEST_SELECT_ACCOUNT: {
				if (mStatus == null) return;
				if (resultCode == Activity.RESULT_OK) {
					if (data == null || !data.hasExtra(EXTRA_ID)) return;
					final long accountId = data.getLongExtra(EXTRA_ID, -1);
					openStatus(getActivity(), accountId, mStatus.id);
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
				mTwitterWrapper.createFriendshipAsync(mStatus.account_id, mStatus.user_id);
				break;
			}
			case R.id.in_reply_to: {
				showConversation();
				break;
			}
			case R.id.replies_view: {
				openStatusReplies(getActivity(), mStatus.account_id, mStatus.id, mStatus.user_screen_name);
				break;
			}
			case R.id.location_container: {
				final ParcelableLocation location = mStatus.location;
				if (!ParcelableLocation.isValidLocation(location)) return;
				openMap(getActivity(), location.latitude, location.longitude);
				break;
			}
			case R.id.load_images: {
				loadPreviewImages();
				// UCD
				ProfilingUtil.profile(getActivity(), mStatus.account_id, "Thumbnail click, " + mStatus.id);
				break;
			}
			case R.id.retweet_view: {
				openStatusRetweeters(getActivity(), mStatus.account_id, mStatus.retweet_id > 0 ? mStatus.retweet_id
						: mStatus.id);
				break;
			}
			// case R.id.prev_image: {
			// final int count = mImagePreviewAdapter.getCount(), pos =
			// mImagePreviewGallery.getSelectedItemPosition();
			// if (count == 0 || pos == 0) return;
			// mImagePreviewGallery.setSelection(pos - 1, true);
			// break;
			// }
			// case R.id.next_image: {
			// final int count = mImagePreviewAdapter.getCount(), pos =
			// mImagePreviewGallery.getSelectedItemPosition();
			// if (count == 0 || pos == count - 1) return;
			// mImagePreviewGallery.setSelection(pos + 1, true);
			// break;
			// }
		}

	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		if (!shouldUseNativeMenu()) return;
		inflater.inflate(R.menu.menu_status, menu);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.fragment_details_page, null, false);
		mMainContent = view.findViewById(R.id.content);
		mDetailsLoadProgress = (ProgressBar) view.findViewById(R.id.details_load_progress);
		mMenuBar = (MenuBar) view.findViewById(R.id.menu_bar);
		mDetailsContainer = (ExtendedFrameLayout) view.findViewById(R.id.details_container);
		mDetailsContainer.addView(super.onCreateView(inflater, container, savedInstanceState));
		mHeaderView = inflater.inflate(R.layout.header_status, null, false);
		mImagePreviewContainer = mHeaderView.findViewById(R.id.image_preview);
		mLocationContainer = mHeaderView.findViewById(R.id.location_container);
		mLocationView = (TextView) mHeaderView.findViewById(R.id.location_view);
		mLocationBackgroundView = mHeaderView.findViewById(R.id.location_background_view);
		mMapView = (ImageView) mHeaderView.findViewById(R.id.map_view);
		mRetweetView = (TextView) mHeaderView.findViewById(R.id.retweet_view);
		mNameView = (TextView) mHeaderView.findViewById(R.id.name);
		mScreenNameView = (TextView) mHeaderView.findViewById(R.id.screen_name);
		mTextView = (TextView) mHeaderView.findViewById(R.id.text);
		mProfileImageView = (ImageView) mHeaderView.findViewById(R.id.profile_image);
		mTimeSourceView = (TextView) mHeaderView.findViewById(R.id.time_source);
		mInReplyToView = (TextView) mHeaderView.findViewById(R.id.in_reply_to);
		mRepliesView = (TextView) mHeaderView.findViewById(R.id.replies_view);
		mFollowButton = (Button) mHeaderView.findViewById(R.id.follow);
		mFollowIndicator = mHeaderView.findViewById(R.id.follow_indicator);
		mFollowInfoProgress = (ProgressBar) mHeaderView.findViewById(R.id.follow_info_progress);
		mProfileView = (ColorLabelRelativeLayout) mHeaderView.findViewById(R.id.profile);
		mImagePreviewGrid = (LinearLayout) mHeaderView.findViewById(R.id.image_grid);
		mLoadImagesIndicator = mHeaderView.findViewById(R.id.load_images);
		mRetryButton = (Button) view.findViewById(R.id.retry);
		final View cardView = mHeaderView.findViewById(R.id.card);
		ThemeUtils.applyThemeAlphaToDrawable(cardView.getContext(), cardView.getBackground());
		return view;
	}

	@Override
	public void onDestroyView() {
		// UCD
		if (mStatus != null) {
			ProfilingUtil.profile(getActivity(), mStatus.account_id, "End, " + mStatus.id);
		}
		mStatus = null;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_STATUS);
		lm.destroyLoader(LOADER_ID_LOCATION);
		lm.destroyLoader(LOADER_ID_FOLLOW);
		if (mConversationTask != null && mConversationTask.getStatus() == AsyncTask.Status.RUNNING) {
			mConversationTask.cancel(true);
		}
		super.onDestroyView();
	}

	@Override
	public void onItemsCleared() {

	}

	@Override
	public void onMediaClick(final View view, final ParcelableMedia media) {
		final ParcelableStatus status = mStatus;
		if (status == null) return;
		// UCD
		ProfilingUtil.profile(getActivity(), mStatus.account_id, "Large image click, " + mStatus.id + ", " + media.url);
		openImage(getActivity(), media.url, mStatus.is_possibly_sensitive);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (!shouldUseNativeMenu() || mStatus == null) return false;
		return handleMenuItemClick(item);
	}

	@Override
	public void onPrepareOptionsMenu(final Menu menu) {
		if (!shouldUseNativeMenu() || mStatus == null) return;
		setMenuForStatus(getActivity(), menu, mStatus);
	}

	@Override
	public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
			final int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		mShouldScroll = false;
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, final String key) {
		if (mStatus == null || !ParseUtils.parseString(mStatus.user_id).equals(key)) return;
		displayStatus(mStatus);
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
		final int text_size = mPreferences.getInt(KEY_TEXT_SIZE, getDefaultTextSize(getActivity()));
		mTextView.setTextSize(text_size * 1.25f);
		mNameView.setTextSize(text_size * 1.25f);
		mScreenNameView.setTextSize(text_size * 0.85f);
		mTimeSourceView.setTextSize(text_size * 0.85f);
		mInReplyToView.setTextSize(text_size * 0.85f);
		mLocationView.setTextSize(text_size * 0.85f);
		mRetweetView.setTextSize(text_size * 0.85f);
		mRepliesView.setTextSize(text_size * 0.85f);
	}

	@Override
	public void onStop() {
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	public boolean scrollToStart() {
		if (mListView == null) return false;
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		scrollListToPosition(mListView, adapter.getCount() + mListView.getFooterViewsCount() - 1, 0);
		return true;
	}

	@Override
	protected String[] getSavedStatusesFileArgs() {
		return null;
	}

	protected boolean handleMenuItemClick(final MenuItem item) {
		if (mStatus == null) return false;
		switch (item.getItemId()) {
			case MENU_SHARE: {
				startStatusShareChooser(getActivity(), mStatus);
				break;
			}
			case MENU_COPY: {
				if (ClipboardUtils.setText(getActivity(), mStatus.text_plain)) {
					showOkMessage(getActivity(), R.string.text_copied, false);
				}
				break;
			}
			case MENU_RETWEET: {
				if (isMyRetweet(mStatus)) {
					cancelRetweet(mTwitterWrapper, mStatus);
				} else {
					final long id_to_retweet = mStatus.is_retweet && mStatus.retweet_id > 0 ? mStatus.retweet_id
							: mStatus.id;
					mTwitterWrapper.retweetStatus(mStatus.account_id, id_to_retweet);
				}
				break;
			}
			case MENU_QUOTE: {
				final Intent intent = new Intent(INTENT_ACTION_QUOTE);
				final Bundle bundle = new Bundle();
				bundle.putParcelable(EXTRA_STATUS, mStatus);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_REPLY: {
				final Intent intent = new Intent(INTENT_ACTION_REPLY);
				final Bundle bundle = new Bundle();
				bundle.putParcelable(EXTRA_STATUS, mStatus);
				intent.putExtras(bundle);
				startActivity(intent);
				break;
			}
			case MENU_FAVORITE: {
				if (mStatus.is_favorite) {
					mTwitterWrapper.destroyFavoriteAsync(mStatus.account_id, mStatus.id);
				} else {
					mTwitterWrapper.createFavoriteAsync(mStatus.account_id, mStatus.id);
				}
				break;
			}
			case MENU_DELETE: {
				DestroyStatusDialogFragment.show(getFragmentManager(), mStatus);
				break;
			}
			case MENU_ADD_TO_FILTER: {
				AddStatusFilterDialogFragment.show(getFragmentManager(), mStatus);
				break;
			}
			case MENU_SET_COLOR: {
				final Intent intent = new Intent(getActivity(), ColorPickerDialogActivity.class);
				final int color = getUserColor(getActivity(), mStatus.user_id, true);
				if (color != 0) {
					intent.putExtra(EXTRA_COLOR, color);
				}
				intent.putExtra(EXTRA_CLEAR_BUTTON, color != 0);
				intent.putExtra(EXTRA_ALPHA_SLIDER, false);
				startActivityForResult(intent, REQUEST_SET_COLOR);
				break;
			}
			case MENU_CLEAR_NICKNAME: {
				clearUserNickname(getActivity(), mStatus.user_id);
				displayStatus(mStatus);
				break;
			}
			case MENU_SET_NICKNAME: {
				final String nick = getUserNickname(getActivity(), mStatus.user_id, true);
				SetUserNicknameDialogFragment.show(getFragmentManager(), mStatus.user_id, nick);
				break;
			}
			case MENU_TRANSLATE: {
				final AccountWithCredentials account = Account.getAccountWithCredentials(getActivity(),
						mStatus.account_id);
				if (AccountWithCredentials.isOfficialCredentials(getActivity(), account)) {
					StatusTranslateDialogFragment.show(getFragmentManager(), mStatus);
				} else {

				}
				break;
			}
			case MENU_OPEN_WITH_ACCOUNT: {
				final Intent intent = new Intent(INTENT_ACTION_SELECT_ACCOUNT);
				intent.setClass(getActivity(), AccountSelectorActivity.class);
				intent.putExtra(EXTRA_SINGLE_SELECTION, true);
				startActivityForResult(intent, REQUEST_SELECT_ACCOUNT);
				break;
			}
			default: {
				if (item.getIntent() != null) {
					try {
						startActivity(item.getIntent());
					} catch (final ActivityNotFoundException e) {
						Log.w(LOGTAG, e);
						return false;
					}
				}
				break;
			}
		}
		return true;
	}

	@Override
	protected void onReachedBottom() {

	}

	// @Override
	// protected void setItemSelected(final ParcelableStatus status, final int
	// position, final boolean selected) {
	// final MultiSelectManager manager = getMultiSelectManager();
	// final Object only_item = manager.getCount() == 1 ?
	// manager.getSelectedItems().get(0) : null;
	// final boolean only_item_selected = only_item != null &&
	// !only_item.equals(mStatus);
	// mListView.setItemChecked(0, only_item_selected);
	// if (mStatus != null) {
	// if (only_item_selected) {
	// manager.selectItem(mStatus);
	// } else {
	// manager.unselectItem(mStatus);
	// }
	// }
	// super.setItemSelected(status, position, selected);
	// }

	@Override
	protected void setItemSelected(final ParcelableStatus status, final int position, final boolean selected) {
	}

	@Override
	protected void setListHeaderFooters(final ListView list) {
		if (getActivity() == null || isDetached()) return;
		list.addHeaderView(mHeaderView, null, true);
	}

	@Override
	protected boolean shouldEnablePullToRefresh() {
		return false;
	}

	@Override
	protected boolean shouldShowAccountColor() {
		return false;
	}

	private void addConversationStatus(final ParcelableStatus status) {
		if (getActivity() == null || isDetached()) return;
		final List<ParcelableStatus> data = getData();
		if (data == null) return;
		data.add(status);
		final ParcelableStatusesAdapter adapter = (ParcelableStatusesAdapter) getListAdapter();
		adapter.setData(data);
		if (!mLoadMoreAutomatically && mShouldScroll) {
			setSelection(0);
		}
	}

	private void clearPreviewImages() {
		mImagePreviewGrid.removeAllViews();
	}

	private void getStatus(final boolean omit_intent_extra) {
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_STATUS);
		final Bundle args = new Bundle(getArguments());
		args.putBoolean(EXTRA_OMIT_INTENT_EXTRA, omit_intent_extra);
		if (!mStatusLoaderInitialized) {
			lm.initLoader(LOADER_ID_STATUS, args, mStatusLoaderCallbacks);
			mStatusLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_STATUS, args, mStatusLoaderCallbacks);
		}
	}

	private long getStatusId() {
		return mStatus != null ? mStatus.id : -1;
	}

	private void hidePreviewImages() {
		mLoadImagesIndicator.setVisibility(View.VISIBLE);
		mImagePreviewGrid.setVisibility(View.GONE);
	}

	private void loadPreviewImages() {
		if (mStatus == null) return;
		mLoadImagesIndicator.setVisibility(View.GONE);
		mImagePreviewGrid.setVisibility(View.VISIBLE);
		mImagePreviewGrid.removeAllViews();
		if (mStatus.medias != null) {
			final int maxColumns = getResources().getInteger(R.integer.grid_column_image_preview);
			MediaPreviewUtils.addToLinearLayout(mImagePreviewGrid, mImageLoader, mStatus.medias, maxColumns, this);
		}
	}

	private boolean shouldUseNativeMenu() {
		final boolean isInLinkHandler = getActivity() instanceof LinkHandlerActivity;
		return isInLinkHandler && SmartBarUtils.hasSmartBar();
	}

	private void showConversation() {
		if (mConversationTask != null && mConversationTask.getStatus() == AsyncTask.Status.RUNNING) {
			mConversationTask.cancel(true);
			return;
		}
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		final int count = adapter.getCount();
		final ParcelableStatus status;
		if (count == 0) {
			mShouldScroll = !mLoadMoreAutomatically;
			status = mStatus;
		} else {
			status = adapter.getStatus(adapter.getCount() - 1);
		}
		if (status == null || status.in_reply_to_status_id <= 0) return;
		mConversationTask = new LoadConversationTask(this);
		mConversationTask.execute(status);
	}

	private void showFollowInfo(final boolean force) {
		if (mFollowInfoDisplayed && !force) return;
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_FOLLOW);
		if (!mFollowInfoLoaderInitialized) {
			lm.initLoader(LOADER_ID_FOLLOW, null, mFollowInfoLoaderCallbacks);
			mFollowInfoLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_FOLLOW, null, mFollowInfoLoaderCallbacks);
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

	private void updateConversationInfo() {
		final boolean has_converstion = mStatus != null && mStatus.in_reply_to_status_id > 0;
		final IStatusesAdapter<List<ParcelableStatus>> adapter = getListAdapter();
		final boolean load_not_finished = adapter.isEmpty()
				|| adapter.getStatus(adapter.getCount() - 1).in_reply_to_status_id > 0;
		final boolean enable = has_converstion && load_not_finished;
		mInReplyToView.setVisibility(enable ? View.VISIBLE : View.GONE);
		mInReplyToView.setClickable(enable);
	}

	private void updateUserColor() {
		if (mStatus == null) return;
		mProfileView.drawStart(getUserColor(getActivity(), mStatus.user_id, true));
	}

	private static class DisplayMapRunnable implements Runnable {
		private final ParcelableLocation mLocation;
		private final ImageLoaderWrapper mLoader;
		private final ImageView mView;

		DisplayMapRunnable(final ParcelableLocation location, final ImageLoaderWrapper loader, final ImageView view) {
			mLocation = location;
			mLoader = loader;
			mView = view;
		}

		@Override
		public void run() {
			final String uri = getMapStaticImageUri(mLocation.latitude, mLocation.longitude, mView);
			mLoader.displayPreviewImage(mView, uri);
		}
	}

	static class FollowInfoLoader extends AsyncTaskLoader<SingleResponse<Boolean>> {

		private final ParcelableStatus status;
		private final Context context;

		public FollowInfoLoader(final Context context, final ParcelableStatus status) {
			super(context);
			this.context = context;
			this.status = status;
		}

		@Override
		public SingleResponse<Boolean> loadInBackground() {
			return isAllFollowing();
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		private SingleResponse<Boolean> isAllFollowing() {
			if (status == null) return SingleResponse.nullInstance();
			if (status.user_id == status.account_id) return SingleResponse.withData(true);
			final Twitter twitter = getTwitterInstance(context, status.account_id, false);
			if (twitter == null) return SingleResponse.nullInstance();
			try {
				final Relationship result = twitter.showFriendship(status.account_id, status.user_id);
				if (!result.isSourceFollowingTarget()) {
					SingleResponse.withData(false);
				}
			} catch (final TwitterException e) {
				return SingleResponse.withException(e);
			}
			return SingleResponse.nullInstance();
		}
	}

	static class ImagesAdapter extends BaseAdapter {

		private final List<PreviewMedia> mImages = new ArrayList<PreviewMedia>();
		private final ImageLoaderWrapper mImageLoader;
		private final LayoutInflater mInflater;

		public ImagesAdapter(final Context context) {
			mImageLoader = TwidereApplication.getInstance(context).getImageLoaderWrapper();
			mInflater = LayoutInflater.from(context);
		}

		public boolean addAll(final Collection<? extends PreviewMedia> images) {
			final boolean ret = images != null && mImages.addAll(images);
			notifyDataSetChanged();
			return ret;
		}

		public void clear() {
			mImages.clear();
			notifyDataSetChanged();
		}

		@Override
		public int getCount() {
			return mImages.size();
		}

		@Override
		public PreviewMedia getItem(final int position) {
			return mImages.get(position);
		}

		@Override
		public long getItemId(final int position) {
			final PreviewMedia spec = getItem(position);
			return spec != null ? spec.hashCode() : 0;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(
					R.layout.gallery_item_image_preview, null);
			final ImageView image = (ImageView) view.findViewById(R.id.image);
			final PreviewMedia spec = getItem(position);
			mImageLoader.displayPreviewImage(image, spec != null ? spec.url : null);
			return view;
		}

	}

	static class LoadConversationTask extends AsyncTask<ParcelableStatus, Void, SingleResponse<Boolean>> {

		final Handler handler;
		final Context context;
		final StatusFragment fragment;

		LoadConversationTask(final StatusFragment fragment) {
			context = fragment.getActivity();
			this.fragment = fragment;
			handler = new Handler();
		}

		@Override
		protected SingleResponse<Boolean> doInBackground(final ParcelableStatus... params) {
			if (params == null || params.length != 1) return new SingleResponse<Boolean>(false, null);
			try {
				final long account_id = params[0].account_id;
				ParcelableStatus status = params[0];
				while (status != null && status.in_reply_to_status_id > 0 && !isCancelled()) {
					status = findStatus(context, account_id, status.in_reply_to_status_id);
					if (status == null) {
						break;
					}
					handler.post(new AddStatusRunnable(status));
				}
			} catch (final TwitterException e) {
				return new SingleResponse<Boolean>(false, e);
			}
			return new SingleResponse<Boolean>(true, null);
		}

		@Override
		protected void onCancelled() {
			fragment.setProgressBarIndeterminateVisibility(false);
			fragment.updateConversationInfo();
		}

		@Override
		protected void onPostExecute(final SingleResponse<Boolean> data) {
			fragment.setProgressBarIndeterminateVisibility(false);
			fragment.updateConversationInfo();
			if (data.data == null || !data.data) {
				showErrorMessage(context, context.getString(R.string.action_getting_status), data.exception, true);
			}
		}

		@Override
		protected void onPreExecute() {
			fragment.setProgressBarIndeterminateVisibility(true);
			fragment.updateConversationInfo();
		}

		class AddStatusRunnable implements Runnable {

			final ParcelableStatus status;

			AddStatusRunnable(final ParcelableStatus status) {
				this.status = status;
			}

			@Override
			public void run() {
				fragment.addConversationStatus(status);
			}
		}
	}

	static class LocationInfoLoader extends AsyncTaskLoader<String> {

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
					for (int i = 0, max_idx = address.getMaxAddressLineIndex(); i < max_idx; i++) {
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

	static class ParcelableStatusLoader extends AsyncTaskLoader<SingleResponse<ParcelableStatus>> {

		private final boolean mOmitIntentExtra;
		private final Bundle mExtras;
		private final long mAccountId, mStatusId;

		public ParcelableStatusLoader(final Context context, final boolean omitIntentExtra, final Bundle extras,
				final long accountId, final long statusId) {
			super(context);
			mOmitIntentExtra = omitIntentExtra;
			mExtras = extras;
			mAccountId = accountId;
			mStatusId = statusId;
		}

		@Override
		public SingleResponse<ParcelableStatus> loadInBackground() {
			if (!mOmitIntentExtra && mExtras != null) {
				final ParcelableStatus cache = mExtras.getParcelable(EXTRA_STATUS);
				if (cache != null) return SingleResponse.withData(cache);
			}
			try {
				return SingleResponse.withData(findStatus(getContext(), mAccountId, mStatusId));
			} catch (final TwitterException e) {
				return SingleResponse.withException(e);
			}
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

	}

}
