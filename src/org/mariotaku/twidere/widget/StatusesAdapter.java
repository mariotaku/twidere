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

	private final static String[] mFrom = new String[] { Statuses.NAME };
	private final static int[] mTo = new int[] { R.id.user_name };
	private boolean mDisplayProfileImage, mMultipleAccountsActivated;
	private LazyImageLoader mImageLoader;
	private int mAccountIdIdx, mStatusIdIdx, mStatusTimestampIdx, mScreenNameIdx, mTextIdx,
			mProfileImageUrlIdx, mRetweetCountIdx, mIsFavoriteIdx, mIsGapIdx, mLocationIdx,
			mHasMediaIdx, mInReplyToStatusIdIdx, mInReplyToScreennameIdx;

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

		boolean is_gap = cursor.getInt(mIsGapIdx) == 1
				&& cursor.getPosition() != cursor.getCount() - 1;

		holder.setIsGap(is_gap);
		holder.status_id = cursor.getLong(mStatusIdIdx);
		holder.account_id = cursor.getLong(mAccountIdIdx);

		holder.setAccountColorEnabled(mMultipleAccountsActivated);
		if (mMultipleAccountsActivated) {
			holder.setAccountColor(CommonUtils.getAccountColor(context, holder.account_id));
		}

		if (!is_gap) {

			String screen_name = cursor.getString(mScreenNameIdx);
			String text = cursor.getString(mTextIdx);
			String profile_image_url = cursor.getString(mProfileImageUrlIdx);
			boolean is_retweet = cursor.getLong(mRetweetCountIdx) != 0;
			boolean is_favorite = cursor.getInt(mIsFavoriteIdx) != 0;
			boolean has_media = cursor.getInt(mHasMediaIdx) == 1;
			boolean has_location = cursor.getString(mLocationIdx) != null;
			boolean is_reply = cursor.getLong(mInReplyToStatusIdIdx) != -1;
			holder.screen_name.setText("@" + screen_name);
			holder.text.setText(Html.fromHtml(text).toString());
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
				try {
					mImageLoader.displayImage(new URL(profile_image_url), holder.profile_image);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
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
			mScreenNameIdx = cursor.getColumnIndexOrThrow(Statuses.SCREEN_NAME);
			mTextIdx = cursor.getColumnIndexOrThrow(Statuses.TEXT);
			mProfileImageUrlIdx = cursor.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
			mRetweetCountIdx = cursor.getColumnIndexOrThrow(Statuses.RETWEET_COUNT);
			mIsFavoriteIdx = cursor.getColumnIndexOrThrow(Statuses.IS_FAVORITE);
			mIsGapIdx = cursor.getColumnIndexOrThrow(Statuses.IS_GAP);
			mLocationIdx = cursor.getColumnIndexOrThrow(Statuses.LOCATION);
			mHasMediaIdx = cursor.getColumnIndexOrThrow(Statuses.HAS_MEDIA);
			mInReplyToStatusIdIdx = cursor.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_STATUS_ID);
			mInReplyToScreennameIdx = cursor
					.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_SCREEN_NAME);
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
