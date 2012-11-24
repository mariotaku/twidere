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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.cancelRetweet;
import static org.mariotaku.twidere.util.Utils.clearUserColor;
import static org.mariotaku.twidere.util.Utils.findStatus;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getImagesInStatus;
import static org.mariotaku.twidere.util.Utils.getQuoteStatus;
import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.isMyActivatedAccount;
import static org.mariotaku.twidere.util.Utils.isMyRetweet;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.setMenuForStatus;
import static org.mariotaku.twidere.util.Utils.setUserColor;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mariotaku.menubar.MenuBar;
import org.mariotaku.menubar.MenuBar.OnMenuItemClickListener;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.SetColorActivity;
import org.mariotaku.twidere.adapter.ParcelableStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.loader.DummyParcelableStatusesLoader;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.model.ParcelableLocation;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.ClipboardUtils;
import org.mariotaku.twidere.util.HtmlEscapeHelper;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.ServiceInterface;
import org.mariotaku.twidere.util.SynchronizedStateSavedList;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.ColorLabelRelativeLayout;
import org.mariotaku.twidere.view.ExtendedFrameLayout;

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
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.twitter.Extractor;

import edu.ucdavis.earlybird.ProfilingUtil;

public class StatusFragment extends ParcelableStatusesListFragment implements OnClickListener, Panes.Right,
		OnItemClickListener, OnTouchListener {

	private static final int LOADER_ID_STATUS = 1;
	private static final int LOADER_ID_FOLLOW = 2;
	private static final int LOADER_ID_LOCATION = 3;
	private static final int LOADER_ID_CONVERSATION = 4;

	private static final long TICKER_DURATION = 5000L;

	private final List<ImageSpec> mData = new ArrayList<ImageSpec>();
	private Handler mHandler;
	private Runnable mTicker;

	private long mAccountId, mStatusId;
	private boolean mLoadMoreAutomatically;
	private boolean mFollowInfoDisplayed, mLocationInfoDisplayed;
	private boolean mStatusLoaderInitialized, mLocationLoaderInitialized, mConversationLoaderInitialized;;
	private boolean mBusy, mTickerStopped;
	private boolean mFollowInfoLoaderInitialized;
	private boolean mShouldScroll;

	private SharedPreferences mPreferences;
	private ServiceInterface mService;
	private LazyImageLoader mProfileImageLoader;

	private ImagesAdapter mImagePreviewAdapter;
	private ParcelableStatusesAdapter mAdapter;

	private TextView mNameView, mScreenNameView, mTextView, mTimeAndSourceView, mInReplyToView, mLocationView,
			mRetweetedStatusView;
	private ImageView mProfileImageView;
	private Button mFollowButton;
	private View mMainContent, mFollowIndicator, mImagesPreviewContainer;
	private ColorLabelRelativeLayout mProfileView;
	private MenuBar mMenuBar;
	private ProgressBar mStatusLoadProgress, mFollowInfoProgress;
	private Gallery mGallery;
	private View mStatusView;
	private View mLoadImagesIndicator;
	private ExtendedFrameLayout mStatusContainer;
	private ListView mListView;

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

	private final LoaderCallbacks<Response<ParcelableStatus>> mConversationLoaderCallbacks = new LoaderCallbacks<Response<ParcelableStatus>>() {

		@Override
		public Loader<Response<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
			setProgressBarIndeterminateVisibility(true);
			final int count = mAdapter.getCount();
			final long status_id;
			if (count == 0) {
				mShouldScroll = !mLoadMoreAutomatically;
				status_id = mStatus != null ? mStatus.in_reply_to_status_id : -1;
			} else {
				status_id = mAdapter.getItem(0).in_reply_to_status_id;
			}
			mListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
			mInReplyToView.setClickable(false);
			setPullToRefreshEnabled(false);	
			return new StatusLoader(getActivity(), true, null, mAccountId, status_id);
		}

		@Override
		public void onLoaderReset(final Loader<Response<ParcelableStatus>> loader) {

		}

		@Override
		public void onLoadFinished(final Loader<Response<ParcelableStatus>> loader,
				final Response<ParcelableStatus> data) {
			updatePullRefresh();
			if (data == null) return;
			if (data.value != null) {
				mAdapter.add(data.value);
				mAdapter.sort(ParcelableStatus.REVERSE_ID_COMPARATOR);
				if (!mLoadMoreAutomatically && mShouldScroll) {
					mListView.setSelection(0 + mListView.getHeaderViewsCount());
				}
				if (data.value.in_reply_to_status_id > 0) {
					getLoaderManager().restartLoader(LOADER_ID_CONVERSATION, null, this);
				} else {
					setProgressBarIndeterminateVisibility(false);
				}
			} else {
				setProgressBarIndeterminateVisibility(false);
				showErrorToast(getActivity(), getString(R.string.getting_status), data.exception, true);
			}
			updatePullRefresh();
		}

	};

	private final LoaderCallbacks<Response<ParcelableStatus>> mStatusLoaderCallbacks = new LoaderCallbacks<Response<ParcelableStatus>>() {

		@Override
		public Loader<Response<ParcelableStatus>> onCreateLoader(final int id, final Bundle args) {
			mStatusLoadProgress.setVisibility(View.VISIBLE);
			mMainContent.setVisibility(View.INVISIBLE);
			mMainContent.setEnabled(false);
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
				showErrorToast(getActivity(), getString(R.string.getting_status), data.exception, true);
			} else {
				displayStatus(data.value);
				mStatusLoadProgress.setVisibility(View.GONE);
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

	private final LoaderCallbacks<Response<Boolean>> mFollowInfoLoaderCallbacks = new LoaderCallbacks<Response<Boolean>>() {

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

	private final OnMenuItemClickListener mMenuItemClickListener = new OnMenuItemClickListener() {

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
				case MENU_COPY: {
					final CharSequence text = Html.fromHtml(mStatus.text_html);
					ClipboardUtils.setText(getActivity(), text);
					Toast.makeText(getActivity(), R.string.text_copied, Toast.LENGTH_SHORT).show();
					break;
				}
				case MENU_RETWEET: {
					if (isMyRetweet(mStatus)) {
						cancelRetweet(mService, mStatus);
					} else {
						final long id_to_retweet = mStatus.is_retweet && mStatus.retweet_id > 0 ? mStatus.retweet_id
								: mStatus.status_id;
						mService.retweetStatus(mStatus.account_id, id_to_retweet);
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
					final List<String> mentions = new Extractor().extractMentionedScreennames(mStatus.text_plain);
					mentions.remove(mStatus.screen_name);
					mentions.add(0, mStatus.screen_name);
					bundle.putStringArray(INTENT_KEY_MENTIONS, mentions.toArray(new String[mentions.size()]));
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, mStatus.account_id);
					bundle.putLong(INTENT_KEY_IN_REPLY_TO_ID, mStatus.status_id);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_SCREEN_NAME, mStatus.screen_name);
					bundle.putString(INTENT_KEY_IN_REPLY_TO_NAME, mStatus.name);
					intent.putExtras(bundle);
					startActivity(intent);
					break;
				}
				case MENU_FAVORITE: {
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
					final String source = HtmlEscapeHelper.toPlainText(mStatus.source);
					if (source == null) return false;
					final Uri uri = Filters.Sources.CONTENT_URI;
					final ContentValues values = new ContentValues();
					final ContentResolver resolver = getContentResolver();
					values.put(Filters.TEXT, source);
					resolver.delete(uri, Filters.TEXT + " = '" + source + "'", null);
					resolver.insert(uri, values);
					Toast.makeText(getActivity(), getString(R.string.source_muted, source), Toast.LENGTH_SHORT).show();
					break;
				}
				case MENU_SET_COLOR: {
					final Intent intent = new Intent(getActivity(), SetColorActivity.class);
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
			return true;
		}
	};

	private final ExtendedFrameLayout.OnSizeChangedListener mOnSizeChangedListener = new ExtendedFrameLayout.OnSizeChangedListener() {

		@Override
		public void onSizeChanged(final View view, final int w, final int h, final int oldw, final int oldh) {
			if (getActivity() == null) return;
			final float density = getResources().getDisplayMetrics().density;
			mStatusView.setMinimumHeight(h - (int) (density * 2));
		}

	};

	public void displayStatus(final ParcelableStatus status) {
		onRefreshComplete();
		updatePullRefresh();
		if (status == null || !status.equals(mStatus)) {
			mAdapter.clear();
		}
		mListView.setSelection(0);
		// UCD
		if (mStatus != null && status != null && mStatus.status_id != status.status_id) {
			ProfilingUtil.profiling(getActivity(), mAccountId, "End, " + mStatus.status_id);
		}
		mStatusId = -1;
		mAccountId = -1;
		mStatus = status;
		if (mStatus != null) {
			// UCD
			ProfilingUtil.profiling(getActivity(), mAccountId, "Start, " + mStatus.status_id);
			mAccountId = mStatus.account_id;
			mStatusId = mStatus.status_id;
		}
		clearPreviewImages();
		if (status == null || getActivity() == null) return;
		final Bundle args = getArguments();
		args.putLong(INTENT_KEY_ACCOUNT_ID, mAccountId);
		args.putLong(INTENT_KEY_STATUS_ID, mStatusId);
		args.putParcelable(INTENT_KEY_STATUS, status);
		mMenuBar.inflate(R.menu.menu_status);
		setMenuForStatus(getActivity(), mMenuBar.getMenu(), status);
		mMenuBar.show();

		updateUserColor();
		mProfileView.drawRight(getAccountColor(getActivity(), status.account_id));

		mNameView.setText(status.name);
		mNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0,
				getUserTypeIconRes(status.is_verified, status.is_protected), 0);
		mScreenNameView.setText("@" + status.screen_name);
		mTextView.setText(Html.fromHtml(status.text_html));
		final TwidereLinkify linkify = new TwidereLinkify(mTextView);
		linkify.setOnLinkClickListener(new OnLinkClickHandler(getActivity(), mAccountId));
		linkify.addAllLinks();
		final boolean is_reply = status.in_reply_to_status_id > 0;
		final String time = formatToLongTimeString(getActivity(), status.status_timestamp);
		final String source_html = status.source;
		setPullToRefreshEnabled(!mLoadMoreAutomatically && is_reply);
		if (!isEmpty(time) && !isEmpty(source_html)) {
			mTimeAndSourceView.setText(Html.fromHtml(getString(R.string.time_source, time, source_html)));
		} else if (isEmpty(time) && !isEmpty(source_html)) {
			mTimeAndSourceView.setText(Html.fromHtml(getString(R.string.source, source_html)));
		} else if (!isEmpty(time) && isEmpty(source_html)) {
			mTimeAndSourceView.setText(time);
		}
		mTimeAndSourceView.setMovementMethod(LinkMovementMethod.getInstance());
		final boolean display_screen_name = NAME_DISPLAY_OPTION_SCREEN_NAME.equals(mPreferences.getString(
				PREFERENCE_KEY_NAME_DISPLAY_OPTION, NAME_DISPLAY_OPTION_BOTH));
		mInReplyToView.setVisibility(is_reply ? View.VISIBLE : View.GONE);
		if (is_reply) {
			if (mLoadMoreAutomatically) {
				mInReplyToView.setText(getString(R.string.in_reply_to, "@" + status.in_reply_to_screen_name));
			} else {
				mInReplyToView.setText(getString(R.string.in_reply_to_pull_to_load_conversation, "@"
						+ status.in_reply_to_screen_name));
			}
		}

		if (mPreferences.getBoolean(PREFERENCE_KEY_DISPLAY_PROFILE_IMAGE, true)) {
			final boolean hires_profile_image = getResources().getBoolean(R.bool.hires_profile_image);
			mProfileImageLoader.displayImage(
					hires_profile_image ? getBiggerTwitterProfileImage(status.profile_image_url_string)
							: status.profile_image_url_string, mProfileImageView);
		} else {
			mProfileImageView.setImageResource(R.drawable.ic_profile_image_default);
		}
		final List<ImageSpec> images = getImagesInStatus(status.text_html);
		mImagesPreviewContainer.setVisibility(images.size() > 0 ? View.VISIBLE : View.GONE);
		loadPreviewImages(images);
		updatePreviewImages();
		if (mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false)) {
			showPreviewImages();
		}
		mRetweetedStatusView.setVisibility(status.retweet_id > 0 ? View.VISIBLE : View.GONE);
		if (status.retweet_id > 0) {
			if (display_screen_name) {
				mRetweetedStatusView.setText(status.retweet_count > 1 ? getString(R.string.retweeted_by_with_count,
						status.retweeted_by_screen_name, status.retweet_count - 1) : getString(R.string.retweeted_by,
						status.retweeted_by_screen_name));
			} else {
				mRetweetedStatusView.setText(status.retweet_count > 1 ? getString(R.string.retweeted_by_with_count,
						status.retweeted_by_name, status.retweet_count - 1) : getString(R.string.retweeted_by,
						status.retweeted_by_name));
			}
		}
		mLocationView.setVisibility(ParcelableLocation.isValidLocation(status.location) ? View.VISIBLE : View.GONE);
		if (mLoadMoreAutomatically) {
			showFollowInfo(true);
			showLocationInfo(true);
			showConversation();
		} else {
			mFollowIndicator.setVisibility(View.GONE);
		}

	}

	@Override
	public Loader<SynchronizedStateSavedList<ParcelableStatus, Long>> newLoaderInstance(final Bundle args) {
		final long account_id = args != null ? args.getLong(INTENT_KEY_ACCOUNT_ID, -1) : -1;
		return new DummyParcelableStatusesLoader(getActivity(), account_id, getData());
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setListShownNoAnimation(true);
		mListView = getListView();
		mListView.setStackFromBottom(true);
		mAdapter = getListAdapter();
		mAdapter.setGapDisallowed(true);
		final TwidereApplication application = getApplication();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mProfileImageLoader = application.getProfileImageLoader();
		mService = getServiceInterface();
		setRetainInstance(true);
		mLoadMoreAutomatically = mPreferences.getBoolean(PREFERENCE_KEY_LOAD_MORE_AUTOMATICALLY, false);
		if (mLoadMoreAutomatically) {
			setPullToRefreshEnabled(false);
		} else {
			setMode(Mode.PULL_DOWN_TO_REFRESH);
			setPullLabel(getString(R.string.pull_to_load_conversation_label), Mode.BOTH);
			setReleaseLabel(getString(R.string.pull_to_load_conversation_release_label), Mode.BOTH);
		}
		final Bundle bundle = getArguments();
		if (bundle != null) {
			mAccountId = bundle.getLong(INTENT_KEY_ACCOUNT_ID);
			mStatusId = bundle.getLong(INTENT_KEY_STATUS_ID);
		}
		mImagePreviewAdapter = new ImagesAdapter(getActivity());
		mLoadImagesIndicator.setOnClickListener(this);
		mInReplyToView.setOnClickListener(this);
		mFollowButton.setOnClickListener(this);
		mProfileView.setOnClickListener(this);
		mLocationView.setOnClickListener(this);
		mRetweetedStatusView.setOnClickListener(this);
		mMenuBar.setOnMenuItemClickListener(mMenuItemClickListener);
		getStatus(false);
		mGallery.setAdapter(mImagePreviewAdapter);
		mGallery.setOnItemClickListener(this);
		mGallery.setOnTouchListener(this);
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
				showConversation();
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
			case R.id.load_images: {
				showPreviewImages();
				// UCD
				ProfilingUtil.profiling(getActivity(), mAccountId, "Thumbnail click, " + mStatusId);
				break;
			}
		}

	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.status, null, false);
		mMainContent = view.findViewById(R.id.content);
		mStatusLoadProgress = (ProgressBar) view.findViewById(R.id.status_load_progress);
		mMenuBar = (MenuBar) view.findViewById(R.id.menu_bar);
		mStatusContainer = (ExtendedFrameLayout) view.findViewById(R.id.status_container);
		mStatusContainer.addView(super.onCreateView(inflater, container, savedInstanceState));
		mStatusContainer.setOnSizeChangedListener(mOnSizeChangedListener);
		mStatusView = inflater.inflate(R.layout.status_content, null, false);
		mImagesPreviewContainer = mStatusView.findViewById(R.id.images_preview);
		mLocationView = (TextView) mStatusView.findViewById(R.id.location_view);
		mRetweetedStatusView = (TextView) mStatusView.findViewById(R.id.retweet_view);
		mNameView = (TextView) mStatusView.findViewById(R.id.name);
		mScreenNameView = (TextView) mStatusView.findViewById(R.id.screen_name);
		mTextView = (TextView) mStatusView.findViewById(R.id.text);
		mProfileImageView = (ImageView) mStatusView.findViewById(R.id.profile_image);
		mTimeAndSourceView = (TextView) mStatusView.findViewById(R.id.time_source);
		mInReplyToView = (TextView) mStatusView.findViewById(R.id.in_reply_to);
		mFollowButton = (Button) mStatusView.findViewById(R.id.follow);
		mFollowIndicator = mStatusView.findViewById(R.id.follow_indicator);
		mFollowInfoProgress = (ProgressBar) mStatusView.findViewById(R.id.follow_info_progress);
		mProfileView = (ColorLabelRelativeLayout) mStatusView.findViewById(R.id.profile);
		mGallery = (Gallery) mStatusView.findViewById(R.id.preview_gallery);
		mLoadImagesIndicator = mStatusView.findViewById(R.id.load_images);
		return view;
	}

	@Override
	public void onDestroyView() {
		// UCD
		if (mStatus != null) {
			ProfilingUtil.profiling(getActivity(), mAccountId, "End, " + mStatus.status_id);
		}
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
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final ImageSpec spec = mImagePreviewAdapter.getItem(position);
		if (spec == null) return;
		// UCD
		ProfilingUtil.profiling(getActivity(), mAccountId, "Large image click, " + mStatusId + ", " + spec.image_link);
		final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, Uri.parse(spec.image_link));
		intent.setPackage(getActivity().getPackageName());
		startActivity(intent);
	}

	@Override
	public void onPullDownToRefresh() {
		onRefreshComplete();
		showConversation();
	}

	@Override
	public void onScrollStateChanged(final AbsListView view, final int scrollState) {
		super.onScrollStateChanged(view, scrollState);
		mShouldScroll = false;
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
		final int text_size = mPreferences.getInt(PREFERENCE_KEY_TEXT_SIZE, PREFERENCE_DEFAULT_TEXT_SIZE);
		mNameView.setTextSize(text_size * 1.25f);
		mTextView.setTextSize(text_size * 1.25f);
		mScreenNameView.setTextSize(text_size * 0.85f);
		mTimeAndSourceView.setTextSize(text_size * 0.85f);
		mInReplyToView.setTextSize(text_size * 0.85f);
		mLocationView.setTextSize(text_size * 0.85f);
		mRetweetedStatusView.setTextSize(text_size * 0.85f);
		mTickerStopped = false;
		mHandler = new Handler();

		mTicker = new Runnable() {

			@Override
			public void run() {
				if (mTickerStopped) return;
				if (mGallery != null && !mBusy) {
					mImagePreviewAdapter.notifyDataSetChanged();
				}
				final long now = SystemClock.uptimeMillis();
				final long next = now + TICKER_DURATION - now % TICKER_DURATION;
				mHandler.postAtTime(mTicker, next);
			}
		};
		mTicker.run();
	}

	@Override
	public void onStop() {
		mTickerStopped = true;
		unregisterReceiver(mStatusReceiver);
		super.onStop();
	}

	@Override
	public boolean onTouch(final View view, final MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mBusy = true;
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				mBusy = false;
				break;
		}
		return false;
	}

	private void clearPreviewImages() {
		mData.clear();
		updatePreviewImages();
		if (mLoadImagesIndicator != null) {
			mLoadImagesIndicator.setVisibility(View.VISIBLE);
		}
		if (mGallery != null) {
			mGallery.setVisibility(View.GONE);
		}
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

	private boolean loadPreviewImages(final Collection<? extends ImageSpec> images) {
		mData.clear();
		return images != null && mData.addAll(images);
	}

	private void showConversation() {
		final LoaderManager lm = getLoaderManager();
		lm.destroyLoader(LOADER_ID_CONVERSATION);
		if (mStatus == null || mStatus.in_reply_to_status_id <= 0) return;
		if (!mConversationLoaderInitialized) {
			lm.initLoader(LOADER_ID_CONVERSATION, null, mConversationLoaderCallbacks);
			mConversationLoaderInitialized = true;
		} else {
			lm.restartLoader(LOADER_ID_CONVERSATION, null, mConversationLoaderCallbacks);
		}
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

	private void showPreviewImages() {
		if (mImagePreviewAdapter == null) return;
		updatePreviewImages();
		mLoadImagesIndicator.setVisibility(View.GONE);
		mGallery.setVisibility(View.VISIBLE);
	}

	private void updatePreviewImages() {
		if (mImagePreviewAdapter == null) return;
		mImagePreviewAdapter.clear();
		mImagePreviewAdapter.addAll(mData);
	}
	
	private void updatePullRefresh() {
		final boolean has_converstion = mStatus != null && mStatus.in_reply_to_status_id > 0;
		final boolean load_not_finished = mAdapter.getCount() > 0 && mAdapter.getItem(0).in_reply_to_status_id > 0;
		final boolean should_enable = has_converstion && load_not_finished;
		mListView.setTranscriptMode(should_enable ? ListView.TRANSCRIPT_MODE_NORMAL : ListView.TRANSCRIPT_MODE_DISABLED);
		mInReplyToView.setClickable(should_enable);
		setPullToRefreshEnabled(should_enable);
	}

	private void updateUserColor() {
		if (mStatus == null) return;
		mProfileView.drawLeft(getUserColor(getActivity(), mStatus.user_id));
	}

	@Override
	void setListHeaderFooters(final ListView list) {
		list.addFooterView(mStatusView);
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

		private final Context context;
		private final boolean omit_intent_extra;
		private final Bundle extras;
		private final long account_id, status_id;

		public StatusLoader(final Context context, final boolean omit_intent_extra, final Bundle extras,
				final long account_id, final long status_id) {
			super(context);
			this.context = context;
			this.omit_intent_extra = omit_intent_extra;
			this.extras = extras;
			this.account_id = account_id;
			this.status_id = status_id;
		}

		@Override
		public Response<ParcelableStatus> loadInBackground() {
			if (!omit_intent_extra && extras != null) {
				final ParcelableStatus status = extras.getParcelable(INTENT_KEY_STATUS);
				if (status != null) return new Response<ParcelableStatus>(status, null);
			}
			try {
				return new Response<ParcelableStatus>(findStatus(context, account_id, status_id), null);
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

	static class ImagesAdapter extends BaseAdapter {

		private final List<ImageSpec> mImages = new ArrayList<ImageSpec>();
		private final LazyImageLoader mImageLoader;
		private final LayoutInflater mInflater;

		public ImagesAdapter(final Context context) {
			mImageLoader = TwidereApplication.getInstance(context).getPreviewImageLoader();
			mInflater = LayoutInflater.from(context);
		}

		public boolean addAll(final Collection<? extends ImageSpec> images) {
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
		public ImageSpec getItem(final int position) {
			return mImages.get(position);
		}

		@Override
		public long getItemId(final int position) {
			final ImageSpec spec = getItem(position);
			return spec != null ? spec.hashCode() : 0;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(R.layout.images_preview_item, null);
			final ImageView image = (ImageView) view.findViewById(R.id.image);
			final ImageSpec spec = getItem(position);
			mImageLoader.displayImage(spec != null ? spec.thumbnail_link : null, image);
			return view;
		}

	}

}
