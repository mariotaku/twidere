package org.mariotaku.twidere.widget;

import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getTypeIcon;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.ParcelableStatus;
import org.mariotaku.twidere.util.StatusViewHolder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class StatusesAdapter extends BaseAdapter {

	private boolean mDisplayProfileImage, mDisplayName, mMultipleAccountsActivated, mShowLastItemAsGap;
	private final LazyImageLoader mImageLoader;
	private float mTextSize;
	private final Context mContext;
	private final LayoutInflater mInflater;

	private List<ParcelableStatus> mData = new ArrayList<ParcelableStatus>();

	public StatusesAdapter(Context context, LazyImageLoader loader) {
		super();
		mContext = context;
		mInflater = LayoutInflater.from(mContext);
		mImageLoader = loader;
	}

	public void changeData(List<ParcelableStatus> data) {
		if (data == null) {
			mData = new ArrayList<ParcelableStatus>();

		} else {
			mData = data;
		}
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		if (mData == null) return 0;
		return mData.size();
	}

	@Override
	public ParcelableStatus getItem(int position) {
		return mData != null ? mData.get(position) : null;
	}

	@Override
	public long getItemId(int position) {
		return mData != null ? mData.get(position).status_id : -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View view = convertView != null ? convertView : mInflater.inflate(R.layout.status_list_item, parent, false);

		Object tag = view.getTag();
		StatusViewHolder holder = null;

		if (view.getTag() instanceof StatusViewHolder) {
			holder = (StatusViewHolder) tag;
		} else {
			holder = new StatusViewHolder(view);
			view.setTag(holder);
		}

		ParcelableStatus status = getItem(position);

		final CharSequence retweeted_by = mDisplayName ? status.retweeted_by_name
				: !isNullOrEmpty(status.retweeted_by_screen_name) ? "@" + status.retweeted_by_screen_name : null;
		final boolean is_last = position == getCount() - 1;
		final boolean show_gap = status.is_gap && !is_last || mShowLastItemAsGap && is_last;

		holder.status_id = status.status_id;
		holder.account_id = status.account_id;
		holder.setShowAsGap(show_gap);
		holder.setAccountColorEnabled(mMultipleAccountsActivated);

		if (mMultipleAccountsActivated) {
			holder.setAccountColor(getAccountColor(mContext, status.account_id));
		}

		if (!show_gap) {

			holder.name_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					status.is_protected ? R.drawable.ic_tweet_stat_is_protected : 0, 0);
			holder.name_view.setText(status.name);
			holder.tweet_time_view.setText(formatToShortTimeString(mContext, status.status_timestamp));
			holder.tweet_time_view.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getTypeIcon(status.is_favorite, status.location != null, status.has_media), 0);
			holder.text_view.setText(status.text_plain);
			holder.text_view.setTextSize(mTextSize);
			holder.reply_retweet_status_view
					.setVisibility(status.in_reply_to_status_id != -1 || status.is_retweet ? View.VISIBLE : View.GONE);
			if (status.is_retweet && !isNullOrEmpty(retweeted_by)) {
				holder.reply_retweet_status_view.setText(mContext.getString(R.string.retweeted_by, retweeted_by));
				holder.reply_retweet_status_view.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.ic_tweet_stat_retweet, 0, 0, 0);
			} else if (status.in_reply_to_status_id != -1 && !isNullOrEmpty(status.in_reply_to_screen_name)) {
				holder.reply_retweet_status_view.setText(mContext.getString(R.string.in_reply_to,
						status.in_reply_to_screen_name));
				holder.reply_retweet_status_view.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.ic_tweet_stat_reply, 0, 0, 0);
			}
			holder.profile_image_view.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				mImageLoader.displayImage(status.profile_image_url, holder.profile_image_view);
			}
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

	public void setStatusesTextSize(float text_size) {
		mTextSize = text_size;
	}

}
