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

import static org.mariotaku.twidere.util.HtmlUnescapeHelper.unescapeHTML;
import static org.mariotaku.twidere.util.Utils.findStatusInDatabases;
import static org.mariotaku.twidere.util.Utils.formatToShortTimeString;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getNormalTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getPreviewImage;
import static org.mariotaku.twidere.util.Utils.getTypeIcon;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.openUserProfile;
import static org.mariotaku.twidere.util.Utils.parseURL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.PreviewImage;
import org.mariotaku.twidere.model.StatusCursorIndices;
import org.mariotaku.twidere.model.StatusViewHolder;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusesAdapterInterface;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class CursorStatusesAdapter extends SimpleCursorAdapter implements StatusesAdapterInterface, OnClickListener {

	private boolean mDisplayProfileImage, mDisplayHiResProfileImage, mDisplayImagePreview, mDisplayName,
			mShowAccountColor, mShowLastItemAsGap, mForceSSLConnection, mGapDisallowed;
	private final LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private float mTextSize;
	private final Context mContext;
	private StatusCursorIndices mIndices;

	public CursorStatusesAdapter(Context context, LazyImageLoader profile_image_loader, LazyImageLoader preview_loader) {
		super(context, R.layout.status_list_item, null, new String[0], new int[0], 0);
		mContext = context;
		mProfileImageLoader = profile_image_loader;
		mPreviewImageLoader = preview_loader;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final int position = cursor.getPosition();
		final StatusViewHolder holder = (StatusViewHolder) view.getTag();

		final String retweeted_by = mDisplayName ? cursor.getString(mIndices.retweeted_by_name) : cursor
				.getString(mIndices.retweeted_by_screen_name);
		final String text = cursor.getString(mIndices.text);
		final String screen_name = cursor.getString(mIndices.screen_name);
		final String name = mDisplayName ? cursor.getString(mIndices.name) : screen_name;
		final String in_reply_to_screen_name = cursor.getString(mIndices.in_reply_to_screen_name);

		final PreviewImage preview = getPreviewImage(text, mDisplayImagePreview, mForceSSLConnection);

		final long account_id = cursor.getLong(mIndices.account_id);
		final long status_timestamp = cursor.getLong(mIndices.status_timestamp);
		final long retweet_count = cursor.getLong(mIndices.retweet_count);

		final boolean is_gap = cursor.getShort(mIndices.is_gap) == 1;
		final boolean is_favorite = cursor.getShort(mIndices.is_favorite) == 1;
		final boolean is_protected = cursor.getShort(mIndices.is_protected) == 1;

		final boolean has_media = preview.has_image;
		final boolean has_location = !isNullOrEmpty(cursor.getString(mIndices.location));
		final boolean is_retweet = !isNullOrEmpty(retweeted_by) && cursor.getShort(mIndices.is_retweet) == 1;
		final boolean is_reply = !isNullOrEmpty(in_reply_to_screen_name)
				&& cursor.getLong(mIndices.in_reply_to_status_id) > 0;

		final boolean is_last = position == getCount() - 1;
		final boolean show_gap = (is_gap && !is_last || mShowLastItemAsGap && is_last && getCount() > 1)
				&& !mGapDisallowed;

		holder.setShowAsGap(show_gap);
		holder.setAccountColorEnabled(mShowAccountColor);

		if (mShowAccountColor) {
			holder.setAccountColor(getAccountColor(mContext, account_id));
		}

		if (!show_gap) {

			holder.setTextSize(mTextSize);

			holder.text.setText(unescapeHTML(text));
			holder.name.setCompoundDrawablesWithIntrinsicBounds(
					is_protected ? R.drawable.ic_indicator_is_protected : 0, 0, 0, 0);
			holder.name.setText(name);
			holder.time.setText(formatToShortTimeString(mContext, status_timestamp));
			holder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getTypeIcon(is_favorite, has_location, has_media), 0);

			holder.reply_retweet_status.setVisibility(is_retweet || is_reply ? View.VISIBLE : View.GONE);
			if (is_retweet) {
				holder.reply_retweet_status.setText(retweet_count > 1 ? mContext.getString(
						R.string.retweeted_by_with_count, retweeted_by, retweet_count - 1) : mContext.getString(
						R.string.retweeted_by, retweeted_by));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0,
						0, 0);
			} else if (is_reply) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.in_reply_to, in_reply_to_screen_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0,
						0, 0);
			}
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				final String profile_image_url_string = cursor.getString(mIndices.profile_image_url);
				if (mDisplayHiResProfileImage) {
					mProfileImageLoader.displayImage(
							parseURL(getBiggerTwitterProfileImage(profile_image_url_string, mForceSSLConnection)),
							holder.profile_image);
				} else {
					mProfileImageLoader.displayImage(
							parseURL(getNormalTwitterProfileImage(profile_image_url_string, mForceSSLConnection)),
							holder.profile_image);
				}
				holder.profile_image.setTag(position);
			}
			holder.image_preview
					.setVisibility(mDisplayImagePreview && preview.has_image && preview.matched_url != null ? View.VISIBLE
							: View.GONE);
			if (mDisplayImagePreview && preview.has_image && preview.matched_url != null) {
				mPreviewImageLoader.displayImage(parseURL(preview.matched_url), holder.image_preview);

			}
		}

		super.bindView(view, context, cursor);
	}

	public long findItemIdByPosition(int position) {
		if (position >= 0 && position < getCount()) return getItem(position).getLong(mIndices.status_id);
		return -1;
	}

	public int findItemPositionByStatusId(long status_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItem(i).getLong(mIndices.status_id) == status_id) return i;
		}
		return -1;
	}

	@Override
	public ParcelableStatus findStatus(long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) {
				final Cursor cur = getItem(i);
				final long account_id = cur.getLong(mIndices.account_id);
				final long status_id = cur.getLong(mIndices.status_id);
				return findStatusInDatabases(mContext, account_id, status_id);
			}
		}
		return null;
	}

	@Override
	public Cursor getItem(int position) {
		return (Cursor) super.getItem(position);
	}

	@Override
	public ParcelableStatus getStatus(int position) {
		final Cursor cur = getItem(position);
		final long account_id = cur.getLong(mIndices.account_id);
		final long status_id = cur.getLong(mIndices.status_id);
		return findStatusInDatabases(mContext, account_id, status_id);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof StatusViewHolder)) {
			final StatusViewHolder holder = new StatusViewHolder(view, context);
			view.setTag(holder);
			holder.profile_image.setOnClickListener(this);
		}
		return view;
	}

	@Override
	public void onClick(View view) {
		final Object tag = view.getTag();
		if (tag instanceof Integer && mContext instanceof Activity) {
			final ParcelableStatus status = getStatus((Integer) tag);
			if (status == null) return;
			openUserProfile((Activity) mContext, status.account_id, status.user_id, status.screen_name);
		}
	}

	@Override
	public void setDisplayHiResProfileImage(boolean display) {
		if (display != mDisplayHiResProfileImage) {
			mDisplayHiResProfileImage = display;
			notifyDataSetChanged();
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
	public void setForceSSLConnection(boolean force_ssl) {
		mForceSSLConnection = force_ssl;
	}

	@Override
	public void setGapDisallowed(boolean disallowed) {
		if (mGapDisallowed != disallowed) {
			mGapDisallowed = disallowed;
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
	public Cursor swapCursor(Cursor cursor) {
		if (cursor != null) {
			mIndices = new StatusCursorIndices(cursor);
		} else {
			mIndices = null;
		}
		return super.swapCursor(cursor);
	}
}
