package org.mariotaku.twidere.adapter;

import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getGeoLocationFromString;
import static org.mariotaku.twidere.util.Utils.getTypeIcon;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.StatusViewHolder;
import org.mariotaku.twidere.util.StatusesCursorIndices;

import twitter4j.GeoLocation;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.ViewGroup;

public class StatusesCursorAdapter extends SimpleCursorAdapter {

	private boolean mDisplayProfileImage, mDisplayName, mShowAccountColor, mShowLastItemAsGap;
	private final LazyImageLoader mImageLoader;
	private float mTextSize;
	private final Context mContext;
	private StatusesCursorIndices mIndices;

	public StatusesCursorAdapter(Context context, LazyImageLoader loader) {
		super(context, R.layout.status_list_item, null, new String[0], new int[0], 0);
		mContext = context;
		mImageLoader = loader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final StatusViewHolder holder = (StatusViewHolder) view.getTag();

		final long account_id = cursor.getLong(mIndices.account_id);
		final long status_timestamp = cursor.getLong(mIndices.status_timestamp);
		final long in_reply_to_status_id = cursor.getLong(mIndices.in_reply_to_status_id);
		final long retweet_count = cursor.getLong(mIndices.retweet_count);
		final boolean is_gap = cursor.getShort(mIndices.is_gap) == 1;
		final boolean is_retweet = cursor.getShort(mIndices.is_retweet) == 1;
		final boolean is_favorite = cursor.getShort(mIndices.is_favorite) == 1;
		final boolean is_protected = cursor.getShort(mIndices.is_protected) == 1;
		final boolean has_media = cursor.getShort(mIndices.has_media) == 1;
		final String retweeted_by = mDisplayName ? cursor.getString(mIndices.retweeted_by_name) : cursor
				.getString(mIndices.retweeted_by_screen_name);
		final String text_plain = cursor.getString(mIndices.text_plain);
		final String name = mDisplayName ? cursor.getString(mIndices.name) : cursor.getString(mIndices.screen_name);
		final String in_reply_to_screen_name = cursor.getString(mIndices.in_reply_to_screen_name);
		final GeoLocation location = getGeoLocationFromString(cursor.getString(mIndices.location));
		final URL profile_image_url = parseURL(cursor.getString(mIndices.profile_image_url));
		final boolean is_last = cursor.getPosition() == getCount() - 1;
		final boolean show_gap = is_gap && !is_last || mShowLastItemAsGap && is_last;

		holder.setShowAsGap(show_gap);
		holder.setAccountColorEnabled(mShowAccountColor);

		if (mShowAccountColor) {
			holder.setAccountColor(getAccountColor(mContext, account_id));
		}

		if (!show_gap) {

			holder.text.setTextSize(mTextSize);
			holder.name.setTextSize(mTextSize * 1.05f);
			holder.tweet_time.setTextSize(mTextSize * 0.65f);
			holder.reply_retweet_status.setTextSize(mTextSize * 0.65f);

			holder.text.setText(text_plain);
			holder.name.setCompoundDrawablesWithIntrinsicBounds(is_protected ? R.drawable.ic_tweet_stat_is_protected
					: 0, 0, 0, 0);
			holder.name.setText(name);
			holder.tweet_time.setText(formatToShortTimeString(mContext, status_timestamp));
			holder.tweet_time.setCompoundDrawables(null, null,
					getTypeIcon(context, is_favorite, location != null, has_media), null);
			holder.reply_retweet_status.setVisibility(in_reply_to_status_id != -1 || is_retweet ? View.VISIBLE
					: View.GONE);
			if (is_retweet && !isNullOrEmpty(retweeted_by)) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.retweeted_by, retweeted_by
						+ (retweet_count > 1 ? " + " + (retweet_count - 1) : "")));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tweet_stat_retweet,
						0, 0, 0);
			} else if (in_reply_to_status_id != -1 && !isNullOrEmpty(in_reply_to_screen_name)) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.in_reply_to, in_reply_to_screen_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_tweet_stat_reply, 0,
						0, 0);
			}
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				mImageLoader.displayImage(profile_image_url, holder.profile_image);
			}
		}

		super.bindView(view, context, cursor);
	}

	@Override
	public void changeCursor(Cursor cursor) {
		if (cursor != null) {
			mIndices = new StatusesCursorIndices(cursor);
		} else {
			mIndices = null;
		}
		super.changeCursor(cursor);
	}

	public ParcelableStatus findItem(long id) {
		for (int i = 0; i < getCount(); i++) {
			if (getItemId(i) == id) return new ParcelableStatus(getItem(i), mIndices);
		}
		return null;
	}

	@Override
	public Cursor getItem(int position) {
		return (Cursor) super.getItem(position);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		Object tag = view.getTag();
		if (!(tag instanceof StatusViewHolder)) {
			view.setTag(new StatusViewHolder(view, context));
		}
		return view;
	}

	public void setDisplayName(boolean display) {
		mDisplayName = display;
	}

	public void setDisplayProfileImage(boolean display) {
		mDisplayProfileImage = display;
	}

	public void setShowAccountColor(boolean show) {
		mShowAccountColor = show;
	}

	public void setShowLastItemAsGap(boolean gap) {
		mShowLastItemAsGap = gap;
	}

	public void setStatusesTextSize(float text_size) {
		mTextSize = text_size;
	}

}
