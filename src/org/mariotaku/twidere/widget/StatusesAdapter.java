package org.mariotaku.twidere.widget;

import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.CommonUtils;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusItemHolder;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class StatusesAdapter extends SimpleCursorAdapter {

	private final static String[] mFrom = new String[] { Statuses.NAME, Statuses.TEXT };
	private final static int[] mTo = new int[] { R.id.user_name, R.id.tweet_content };
	private boolean mDisplayProfileImage;
	private LazyImageLoader mImageLoader;
	private int mAccountIdIdx, mStatusIdIdx, mStatusTimestampIdx, mScreenNameIdx,
			mProfileImageUrlIdx, mIsRetweetIdx, mIsFavoriteIdx, mIsGapIdx, mHasLocationIdx,
			mHasMediaIdx, mInReplyToStatusIdIdx, mInReplyToScreennameIdx;

	public StatusesAdapter(Context context, LazyImageLoader loader) {
		super(context, R.layout.status_list_item, null, mFrom, mTo, 0);
		mImageLoader = loader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {

		StatusItemHolder holder = (StatusItemHolder) view.getTag();

		if (holder == null) return;

		boolean is_gap = cursor.getInt(mIsGapIdx) == 1
				&& cursor.getPosition() != cursor.getCount() - 1;

		holder.setIsGap(is_gap);
		holder.status_id = cursor.getLong(mStatusIdIdx);
		holder.account_id = cursor.getLong(mAccountIdIdx);

		if (!is_gap) {

			String screen_name = cursor.getString(mScreenNameIdx);
			String profile_image_url = cursor.getString(mProfileImageUrlIdx);
			boolean is_retweet = cursor.getInt(mIsRetweetIdx) == 1;
			boolean is_favorite = cursor.getInt(mIsFavoriteIdx) == 1;
			boolean has_media = cursor.getInt(mHasMediaIdx) == 1;
			boolean has_location = cursor.getInt(mHasLocationIdx) == 1;
			boolean is_reply = cursor.getInt(mInReplyToStatusIdIdx) != -1;

			holder.screen_name.setText("@" + screen_name);
			holder.tweet_time.setText(CommonUtils.formatToShortTimeString(context,
					cursor.getLong(mStatusTimestampIdx)));
			holder.tweet_time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					CommonUtils.getTypeIcon(is_retweet, is_favorite, has_location, has_media), 0);
			holder.in_reply_to.setVisibility(is_reply ? View.VISIBLE : View.GONE);
			if (is_reply) {
				holder.in_reply_to.setText(context.getString(R.string.in_reply_to,
						cursor.getString(mInReplyToScreennameIdx)));
			}
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				URL url = null;
				try {
					url = new URL(profile_image_url);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				mImageLoader.displayImage(url, holder.profile_image);
			}
		}
		super.bindView(view, context, cursor);

	}

	@Override
	public void changeCursor(Cursor cursor) {
		super.changeCursor(cursor);
		if (cursor != null) {
			mAccountIdIdx = cursor.getColumnIndexOrThrow(Statuses.ACCOUNT_ID);
			mStatusIdIdx = cursor.getColumnIndexOrThrow(Statuses.STATUS_ID);
			mStatusTimestampIdx = cursor.getColumnIndexOrThrow(Statuses.STATUS_TIMESTAMP);
			mScreenNameIdx = cursor.getColumnIndexOrThrow(Statuses.SCREEN_NAME);
			mProfileImageUrlIdx = cursor.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
			mIsRetweetIdx = cursor.getColumnIndexOrThrow(Statuses.IS_RETWEET);
			mIsFavoriteIdx = cursor.getColumnIndexOrThrow(Statuses.IS_FAVORITE);
			mIsGapIdx = cursor.getColumnIndexOrThrow(Statuses.IS_GAP);
			mHasLocationIdx = cursor.getColumnIndexOrThrow(Statuses.HAS_LOCATION);
			mHasMediaIdx = cursor.getColumnIndexOrThrow(Statuses.HAS_MEDIA);
			mInReplyToStatusIdIdx = cursor.getColumnIndexOrThrow(Mentions.IN_REPLY_TO_STATUS_ID);
			mInReplyToScreennameIdx = cursor
					.getColumnIndexOrThrow(Mentions.IN_REPLY_TO_SCREEN_NAME);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		View view = super.newView(context, cursor, parent);
		StatusItemHolder viewholder = new StatusItemHolder(view);
		view.setTag(viewholder);
		return view;
	}

	public void setDisplayProfileImage(boolean display) {
		mDisplayProfileImage = display;
	}

}