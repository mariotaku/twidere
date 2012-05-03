package org.mariotaku.twidere.widget;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusItemHolder;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

public class StatusesAdapter extends SimpleCursorAdapter {

	private final static String[] mFrom = new String[] {};
	private final static int[] mTo = new int[] {};
	private boolean mDisplayProfileImage, mDisplayName, mMultipleAccountsActivated, mShowLastItemAsGap;
	private LazyImageLoader mImageLoader;
	private int mAccountIdIdx, mStatusIdIdx, mStatusTimestampIdx, mNameIdx, mScreenNameIdx, mTextIdx,
			mProfileImageUrlIdx, mIsRetweetIdx, mIsFavoriteIdx, mIsGapIdx, mLocationIdx, mHasMediaIdx, mIsProtectedIdx,
			mInReplyToStatusIdIdx, mInReplyToScreennameIdx;

	private Context mContext;

	public StatusesAdapter(Context context, LazyImageLoader loader) {
		super(context, R.layout.status_list_item, null, mFrom, mTo, 0);
		mImageLoader = loader;
		mContext = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		StatusItemHolder holder = (StatusItemHolder) view.getTag();

		if (holder == null) return;

		boolean is_last = cursor.getPosition() == cursor.getCount() - 1;

		if (holder.is_gap == -1) {
			holder.is_gap = (byte) cursor.getInt(mIsGapIdx);
		}

		boolean is_gap = holder.is_gap == 1;
		boolean show_gap = is_gap && !is_last || mShowLastItemAsGap && is_last;

		holder.setShowAsGap(show_gap);
		if (holder.status_id == -1) {
			holder.status_id = cursor.getLong(mStatusIdIdx);
		}
		if (holder.account_id == -1) {
			holder.account_id = cursor.getLong(mAccountIdIdx);
		}

		holder.setAccountColorEnabled(mMultipleAccountsActivated);
		if (mMultipleAccountsActivated) {
			holder.setAccountColor(CommonUtils.getAccountColor(context, holder.account_id));
		}

		if (!show_gap) {

			// Use this to avoid get values from cursor too often, this may make
			// scroll faster.
			if (holder.text == null) {
				holder.text = Html.fromHtml(cursor.getString(mTextIdx)).toString();
			}
			if (holder.screen_name == null) {
				holder.screen_name = cursor.getString(mScreenNameIdx);
			}
			if (holder.name == null) {
				holder.name = cursor.getString(mNameIdx);
			}
			if (holder.status_timestamp == -1) {
				holder.status_timestamp = cursor.getLong(mStatusTimestampIdx);
			}
			if (holder.profile_image_url == null) {
				holder.profile_image_url = parseURL(cursor.getString(mProfileImageUrlIdx));
			}
			if (holder.is_retweet == -2) {
				holder.is_retweet = (byte) cursor.getInt(mIsRetweetIdx);
			}
			if (holder.is_reply == -1) {
				holder.is_reply = (byte) (cursor.getLong(mInReplyToStatusIdIdx) == -1 ? 0 : 1);
			}
			if (holder.is_favorite == -1) {
				holder.is_favorite = (byte) cursor.getInt(mIsFavoriteIdx);
			}
			if (holder.is_protected == -1) {
				holder.is_protected = (byte) cursor.getInt(mIsProtectedIdx);
			}
			if (holder.has_media == -1) {
				holder.has_media = (byte) cursor.getInt(mHasMediaIdx);
			}
			if (holder.has_location == -1) {
				holder.has_location = (byte) (cursor.getString(mLocationIdx) == null ? 0 : 1);
			}
			if (holder.is_reply == 1 && holder.in_reply_to == null) {
				holder.in_reply_to = cursor.getString(mInReplyToScreennameIdx);
			}

			boolean is_retweet = holder.is_retweet == 1;
			boolean is_favorite = holder.is_favorite == 1;
			boolean has_media = holder.has_media == 1;
			boolean has_location = holder.has_location == 1;
			boolean is_reply = holder.is_reply == 1;
			boolean is_protected = holder.is_protected == 1;

			holder.name_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					is_protected ? R.drawable.ic_tweet_stat_is_protected : 0, 0);
			holder.name_view.setText(mDisplayName ? holder.name : "@" + holder.screen_name);
			holder.text_view.setText(holder.text);
			holder.tweet_time_view.setText(CommonUtils.formatToShortTimeString(context, holder.status_timestamp));
			holder.tweet_time_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					CommonUtils.getTypeIcon(is_retweet, is_favorite, has_location, has_media), 0);
			holder.in_reply_to_view.setVisibility(is_reply ? View.VISIBLE : View.GONE);
			if (is_reply && holder.in_reply_to != null) {
				holder.in_reply_to_view.setText(context.getString(R.string.in_reply_to,
						holder.in_reply_to));
			}
			holder.profile_image_view.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				mImageLoader.displayImage(holder.profile_image_url, holder.profile_image_view);
			}
		}
		super.bindView(view, context, cursor);

	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		long[] account_ids = CommonUtils.getActivatedAccounts(mContext);
		mMultipleAccountsActivated = account_ids.length > 1;
		if (cursor != null) {
			mAccountIdIdx = cursor.getColumnIndexOrThrow(Statuses.ACCOUNT_ID);
			mStatusIdIdx = cursor.getColumnIndexOrThrow(Statuses.STATUS_ID);
			mStatusTimestampIdx = cursor.getColumnIndexOrThrow(Statuses.STATUS_TIMESTAMP);
			mNameIdx = cursor.getColumnIndexOrThrow(Statuses.NAME);
			mScreenNameIdx = cursor.getColumnIndexOrThrow(Statuses.SCREEN_NAME);
			mTextIdx = cursor.getColumnIndexOrThrow(Statuses.TEXT);
			mProfileImageUrlIdx = cursor.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
			mIsFavoriteIdx = cursor.getColumnIndexOrThrow(Statuses.IS_FAVORITE);
			mIsRetweetIdx = cursor.getColumnIndexOrThrow(Statuses.IS_RETWEET);
			mIsGapIdx = cursor.getColumnIndexOrThrow(Statuses.IS_GAP);
			mLocationIdx = cursor.getColumnIndexOrThrow(Statuses.LOCATION);
			mHasMediaIdx = cursor.getColumnIndexOrThrow(Statuses.HAS_MEDIA);
			mIsProtectedIdx = cursor.getColumnIndexOrThrow(Statuses.IS_PROTECTED);
			mInReplyToStatusIdIdx = cursor.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_STATUS_ID);
			mInReplyToScreennameIdx = cursor.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_SCREEN_NAME);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View view = super.newView(context, cursor, parent);
		StatusItemHolder viewholder = new StatusItemHolder(view);
		view.setTag(viewholder);
		return view;
	}

	public void setDisplayName(boolean display) {
		mDisplayName = display;
	}

	public void setDisplayProfileImage(boolean display) {
		mDisplayProfileImage = display;
	}

	public void setShowLastItemAsGap(boolean gap) {
		mShowLastItemAsGap = gap;
	}

	private URL parseURL(String url_string) {
		try {
			return new URL(url_string);
		} catch (MalformedURLException e) {
			// This should not happen.
		}
		return null;
	}

}
