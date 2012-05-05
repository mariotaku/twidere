package org.mariotaku.twidere.widget;

import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getActivatedAccounts;
import static org.mariotaku.twidere.util.Utils.getTypeIcon;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusViewHolder;

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
	private final LazyImageLoader mImageLoader;
	private int mAccountIdIdx, mStatusIdIdx, mStatusTimestampIdx, mNameIdx, mScreenNameIdx, mTextIdx,
			mProfileImageUrlIdx, mIsRetweetIdx, mIsFavoriteIdx, mIsGapIdx, mLocationIdx, mHasMediaIdx, mIsProtectedIdx,
			mInReplyToStatusIdIdx, mInReplyToScreennameIdx, mRetweetedByNameIdx, mRetweetedByScreenNameIdx;

	private final Context mContext;

	public StatusesAdapter(Context context, LazyImageLoader loader) {
		super(context, R.layout.status_list_item, null, mFrom, mTo, 0);
		mImageLoader = loader;
		mContext = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		final StatusViewHolder holder = (StatusViewHolder) view.getTag();

		if (holder == null) return;

		final long status_id = cursor.getLong(mStatusIdIdx);
		final long account_id = cursor.getLong(mAccountIdIdx);
		final long status_timestamp = cursor.getLong(mStatusTimestampIdx);
		final CharSequence text = Html.fromHtml(cursor.getString(mTextIdx)).toString();
		final CharSequence name = mDisplayName ? cursor.getString(mNameIdx) : "@" + cursor.getString(mScreenNameIdx);
		final CharSequence in_reply_to = cursor.getString(mInReplyToScreennameIdx);
		final CharSequence retweeted_by_screen_name = cursor.getString(mRetweetedByScreenNameIdx);
		final CharSequence retweeted_by = mDisplayName ? cursor.getString(mRetweetedByNameIdx)
				: retweeted_by_screen_name != null ? "@" + retweeted_by_screen_name : null;
		final URL profile_image_url = parseURL(cursor.getString(mProfileImageUrlIdx));
		final boolean is_retweet = cursor.getInt(mIsRetweetIdx) == 1;
		final boolean is_reply = cursor.getLong(mInReplyToStatusIdIdx) != -1;
		final boolean is_favorite = cursor.getInt(mIsFavoriteIdx) == 1;
		final boolean is_protected = cursor.getInt(mIsProtectedIdx) == 1;
		final boolean has_media = cursor.getInt(mHasMediaIdx) == 1;
		final boolean has_location = cursor.getString(mLocationIdx) != null;
		final boolean is_gap = cursor.getInt(mIsGapIdx) == 1;

		final boolean is_last = cursor.getPosition() == getCount() - 1;
		final boolean show_gap = is_gap && !is_last || mShowLastItemAsGap && is_last;

		holder.status_id = status_id;
		holder.account_id = account_id;
		holder.setShowAsGap(show_gap);
		holder.setAccountColorEnabled(mMultipleAccountsActivated);

		if (mMultipleAccountsActivated) {
			holder.setAccountColor(getAccountColor(context, account_id));
		}

		if (!show_gap) {

			holder.name_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					is_protected ? R.drawable.ic_tweet_stat_is_protected : 0, 0);
			holder.name_view.setText(name);
			holder.tweet_time_view.setText(formatToShortTimeString(context, status_timestamp));
			holder.tweet_time_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getTypeIcon(is_favorite, has_location, has_media), 0);
			holder.text_view.setText(text);
			holder.reply_retweet_status_view.setVisibility(is_reply || is_retweet ? View.VISIBLE : View.GONE);
			if (is_retweet && retweeted_by != null) {
				holder.reply_retweet_status_view.setText(context.getString(R.string.retweeted_by, retweeted_by));
				holder.reply_retweet_status_view.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.ic_tweet_stat_retweet, 0, 0, 0);
			} else if (is_reply && in_reply_to != null) {
				holder.reply_retweet_status_view.setText(context.getString(R.string.in_reply_to, in_reply_to));
				holder.reply_retweet_status_view.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.ic_tweet_stat_reply, 0, 0, 0);
			}
			holder.profile_image_view.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				mImageLoader.displayImage(profile_image_url, holder.profile_image_view);
			}
		}
		super.bindView(view, context, cursor);

	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		final long[] account_ids = getActivatedAccounts(mContext);
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
			mRetweetedByNameIdx = cursor.getColumnIndexOrThrow(Statuses.RETWEETED_BY_NAME);
			mRetweetedByScreenNameIdx = cursor.getColumnIndexOrThrow(Statuses.RETWEETED_BY_SCREEN_NAME);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View view = super.newView(context, cursor, parent);

		if (!(view.getTag() instanceof StatusViewHolder)) {
			StatusViewHolder viewholder = new StatusViewHolder(view);
			view.setTag(viewholder);
		}

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
