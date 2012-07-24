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

package org.mariotaku.twidere.adapter;

import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getTypeIcon;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;

import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.StatusViewHolder;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusesAdapterInterface;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ParcelableStatusesAdapter extends ArrayAdapter<ParcelableStatus> implements StatusesAdapterInterface, OnClickListener {

	private boolean mDisplayProfileImage, mDisplayImagePreview, mDisplayName, mShowAccountColor, mShowLastItemAsGap;
	private final LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private float mTextSize;
	private final Context mContext;

	public ParcelableStatusesAdapter(Context context, LazyImageLoader profile_image_loader,
			LazyImageLoader preview_loader) {
		super(context, R.layout.status_list_item, R.id.text);
		mContext = context;
		mProfileImageLoader = profile_image_loader;
		mPreviewImageLoader = preview_loader;
	}

	@Override
	public ParcelableStatus findStatus(long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return getItem(i);
		}
		return null;
	}

	public ParcelableStatus findItemByStatusId(long status_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			final ParcelableStatus status = getItem(i);
			if (status.status_id == status_id) return status;
		}
		return null;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		final View view = super.getView(position, convertView, parent);

		final Object tag = view.getTag();
		StatusViewHolder holder = null;

		if (tag instanceof StatusViewHolder) {
			holder = (StatusViewHolder) tag;
		} else {
			holder = new StatusViewHolder(view, mContext);
			view.setTag(holder);
			holder.profile_image.setOnClickListener(this);
		}

		final ParcelableStatus status = getItem(position);

		final CharSequence retweeted_by = mDisplayName ? status.retweeted_by_name : status.retweeted_by_screen_name;
		final boolean is_last = position == getCount() - 1;
		final boolean show_gap = status.is_gap && !is_last || mShowLastItemAsGap && is_last && getCount() > 1;

		holder.setShowAsGap(show_gap);
		holder.setAccountColorEnabled(mShowAccountColor);

		if (mShowAccountColor) {
			holder.setAccountColor(getAccountColor(mContext, status.account_id));
		}

		if (!show_gap) {

			holder.setTextSize(mTextSize);
			holder.name.setCompoundDrawablesWithIntrinsicBounds(
					status.is_protected ? R.drawable.ic_indicator_is_protected : 0, 0, 0, 0);
			holder.name.setText(mDisplayName ? status.name : status.screen_name);
			holder.time.setText(formatToShortTimeString(mContext, status.status_timestamp));
			holder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getTypeIcon(status.is_favorite, status.location != null, status.has_media), 0);
			holder.reply_retweet_status
					.setVisibility(status.in_reply_to_status_id != -1 || status.is_retweet ? View.VISIBLE : View.GONE);
			if (status.is_retweet && !isNullOrEmpty(retweeted_by)) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.retweeted_by, retweeted_by
						+ (status.retweet_count > 1 ? " + " + (status.retweet_count - 1) : "")));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0,
						0, 0);
			} else if (status.in_reply_to_status_id > 0 && !isNullOrEmpty(status.in_reply_to_screen_name)) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.in_reply_to,
						status.in_reply_to_screen_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0,
						0, 0);
			}
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				mProfileImageLoader.displayImage(status.profile_image_url, holder.profile_image);
				holder.profile_image.setOnClickListener(this);
				holder.profile_image.setTag(position);
			}
			holder.image_preview.setVisibility(mDisplayImagePreview && status.has_media
					&& status.image_preview_url != null ? View.VISIBLE : View.GONE);
			if (mDisplayImagePreview && status.has_media && status.image_preview_url != null) {
				mPreviewImageLoader.displayImage(status.image_preview_url, holder.image_preview);
			}
		}

		return view;
	}

	public void setData(List<ParcelableStatus> data) {
		setData(data, false);
	}

	public void setData(List<ParcelableStatus> data, boolean clear_old) {
		if (clear_old) {
			clear();
		}
		if (data == null) return;
		for (final ParcelableStatus status : data) {
			if (clear_old || findItemByStatusId(status.status_id) == null) {
				add(status);
			}
		}
	}

	@Override
	public void setDisplayImagePreview(boolean preview) {
		if (preview != mDisplayImagePreview) {
			mDisplayImagePreview = preview;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setDisplayName(boolean display) {
		if (display != mDisplayName) {
			mDisplayName = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setDisplayProfileImage(boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setShowAccountColor(boolean show) {
		if (show != mShowAccountColor) {
			mShowAccountColor = show;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setShowLastItemAsGap(boolean gap) {
		if (gap != mShowLastItemAsGap) {
			mShowLastItemAsGap = gap;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setTextSize(float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View view) {
		Object tag = view.getTag();
		if (tag instanceof Integer && mContext instanceof Activity) {
			ParcelableStatus status = getStatus((Integer)tag);
			if (status == null) return;
			openUserProfile((Activity) mContext, status.account_id, status.user_id, status.screen_name);
		}
	}

	@Override
	public ParcelableStatus getStatus(int position) {
		return getItem(position);
	}
}
