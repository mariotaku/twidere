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

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.findStatusInDatabases;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getAllAvailableImage;
import static org.mariotaku.twidere.util.Utils.getPreviewImage;
import static org.mariotaku.twidere.util.Utils.getStatusBackground;
import static org.mariotaku.twidere.util.Utils.getStatusTypeIconRes;
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import java.util.ArrayList;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.PreviewImage;
import org.mariotaku.twidere.model.StatusCursorIndices;
import org.mariotaku.twidere.model.StatusViewHolder;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.StatusesAdapterInterface;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

public class CursorStatusesAdapter extends SimpleCursorAdapter implements StatusesAdapterInterface, OnClickListener {

	private boolean mDisplayProfileImage, mDisplayImagePreview, mShowAccountColor, mShowAbsoluteTime, mGapDisallowed,
			mMultiSelectEnabled, mMentionsHighlightDisabled;
	private final LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private float mTextSize;
	private final Context mContext;
	private StatusCursorIndices mIndices;
	private final ArrayList<Long> mSelectedStatusIds;
	private int mNameDisplayOption;

	public CursorStatusesAdapter(final Context context) {
		super(context, R.layout.status_list_item, null, new String[0], new int[0], 0);
		mContext = context;
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mSelectedStatusIds = application.getSelectedStatusIds();
		mProfileImageLoader = application.getProfileImageLoader();
		mPreviewImageLoader = application.getPreviewImageLoader();
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int position = cursor.getPosition();
		final StatusViewHolder holder = (StatusViewHolder) view.getTag();

		final boolean is_gap = cursor.getShort(mIndices.is_gap) == 1;

		final boolean show_gap = is_gap && !mGapDisallowed && position != getCount() - 1;

		holder.setShowAsGap(show_gap);

		if (!show_gap) {

			final long account_id = cursor.getLong(mIndices.account_id);
			final long user_id = cursor.getLong(mIndices.user_id);
			final long status_id = cursor.getLong(mIndices.status_id);
			final long status_timestamp = cursor.getLong(mIndices.status_timestamp);
			final long retweet_count = cursor.getLong(mIndices.retweet_count);

			final String retweeted_by_name = cursor.getString(mIndices.retweeted_by_name);
			final String retweeted_by_screen_name = cursor.getString(mIndices.retweeted_by_screen_name);
			final String text = cursor.getString(mIndices.text);
			final String screen_name = cursor.getString(mIndices.screen_name);
			final String name = cursor.getString(mIndices.name);
			final String in_reply_to_screen_name = cursor.getString(mIndices.in_reply_to_screen_name);
			final String account_screen_name = getAccountScreenName(mContext, account_id);

			final boolean is_favorite = cursor.getShort(mIndices.is_favorite) == 1;
			final boolean is_protected = cursor.getShort(mIndices.is_protected) == 1;
			final boolean is_verified = cursor.getShort(mIndices.is_verified) == 1;

			final boolean has_location = !TextUtils.isEmpty(cursor.getString(mIndices.location));
			final boolean is_retweet = !TextUtils.isEmpty(retweeted_by_name)
					&& cursor.getShort(mIndices.is_retweet) == 1;
			final boolean is_reply = !TextUtils.isEmpty(in_reply_to_screen_name)
					&& cursor.getLong(mIndices.in_reply_to_status_id) > 0;
			final boolean is_mention = text.toLowerCase().contains('@' + account_screen_name.toLowerCase());

			if (mMultiSelectEnabled) {
				holder.setSelected(mSelectedStatusIds.contains(status_id));
			} else {
				holder.setSelected(false);
			}

			holder.setUserColor(getUserColor(mContext, user_id));
			if (text != null) {
				holder.setHighlightColor(getStatusBackground(mMentionsHighlightDisabled ? false : is_mention,
						is_favorite, is_retweet));
			}

			holder.setAccountColorEnabled(mShowAccountColor);

			if (mShowAccountColor) {
				holder.setAccountColor(getAccountColor(mContext, account_id));
			}

			final PreviewImage preview = getPreviewImage(text, mDisplayImagePreview);
			final boolean has_media = preview != null ? preview.has_image : false;

			holder.setTextSize(mTextSize);

			holder.text.setText(toPlainText(text));
			holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, getUserTypeIconRes(is_verified, is_protected), 0);
			switch (mNameDisplayOption) {
				case NAME_DISPLAY_OPTION_CODE_NAME: {
					holder.name.setText(name);
					holder.screen_name.setText(null);
					holder.screen_name.setVisibility(View.GONE);
					break;
				}
				case NAME_DISPLAY_OPTION_CODE_SCREEN_NAME: {
					holder.name.setText(screen_name);
					holder.screen_name.setText(null);
					holder.screen_name.setVisibility(View.GONE);
					break;
				}
				default: {
					holder.name.setText(name);
					holder.screen_name.setText("@" + screen_name);
					holder.screen_name.setVisibility(View.VISIBLE);
					break;
				}
			}
			if (mShowAbsoluteTime) {
				holder.time.setText(formatSameDayTime(context, status_timestamp));
			} else {
				holder.time.setText(getRelativeTimeSpanString(status_timestamp));
			}
			holder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getStatusTypeIconRes(is_favorite, has_location, has_media), 0);

			holder.reply_retweet_status.setVisibility(is_retweet || is_reply ? View.VISIBLE : View.GONE);
			if (is_retweet) {
				if (mNameDisplayOption == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME) {
					holder.reply_retweet_status.setText(retweet_count > 1 ? mContext.getString(
							R.string.retweeted_by_with_count, retweeted_by_screen_name, retweet_count - 1) : mContext
							.getString(R.string.retweeted_by, retweeted_by_screen_name));
				} else {
					holder.reply_retweet_status.setText(retweet_count > 1 ? mContext.getString(
							R.string.retweeted_by_with_count, retweeted_by_name, retweet_count - 1) : mContext
							.getString(R.string.retweeted_by, retweeted_by_name));
				}
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0,
						0, 0);
			} else if (is_reply) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.in_reply_to, in_reply_to_screen_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0,
						0, 0);
			}
			holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
			if (mDisplayProfileImage) {
				final String profile_image_url = cursor.getString(mIndices.profile_image_url);
				mProfileImageLoader.displayImage(profile_image_url, holder.profile_image);
				holder.profile_image.setTag(position);
			}
			final boolean has_preview = mDisplayImagePreview && has_media && preview.matched_url != null;
			holder.image_preview.setVisibility(has_preview ? View.VISIBLE : View.GONE);
			if (has_preview) {
				mPreviewImageLoader.displayImage(preview.matched_url, holder.image_preview);
				holder.image_preview.setTag(position);
			}
		}

		super.bindView(view, context, cursor);
	}

	public long findItemIdByPosition(final int position) {
		if (position >= 0 && position < getCount()) return getItem(position).getLong(mIndices.status_id);
		return -1;
	}

	public int findItemPositionByStatusId(final long status_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItem(i).getLong(mIndices.status_id) == status_id) return i;
		}
		return -1;
	}

	@Override
	public Cursor getItem(final int position) {
		return (Cursor) super.getItem(position);
	}

	@Override
	public ParcelableStatus getStatus(final int position) {
		final Cursor cur = getItem(position);
		final long account_id = cur.getLong(mIndices.account_id);
		final long status_id = cur.getLong(mIndices.status_id);
		return findStatusInDatabases(mContext, account_id, status_id);
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof StatusViewHolder)) {
			final StatusViewHolder holder = new StatusViewHolder(view);
			view.setTag(holder);
			holder.profile_image.setOnClickListener(mMultiSelectEnabled ? null : this);
			holder.image_preview.setOnClickListener(mMultiSelectEnabled ? null : this);
			// holder.image_preview.setClickable(!mMultiSelectEnabled);
			// holder.profile_image.setClickable(!mMultiSelectEnabled);
		}
		return view;
	}

	@Override
	public void onClick(final View view) {
		if (mMultiSelectEnabled) return;
		final Object tag = view.getTag();
		final ParcelableStatus status = tag instanceof Integer ? getStatus((Integer) tag) : null;
		if (status == null) return;
		switch (view.getId()) {
			case R.id.image_preview: {
				final ImageSpec spec = getAllAvailableImage(status.image_orig_url_string);
				if (spec != null) {
					final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE, Uri.parse(spec.image_link));
					intent.setPackage(mContext.getPackageName());
					mContext.startActivity(intent);
				}
				break;
			}
			case R.id.profile_image: {
				if (mContext instanceof Activity) {
					openUserProfile((Activity) mContext, status.account_id, status.user_id, status.screen_name);
				}
				break;
			}
		}
	}

	@Override
	public void setDisplayImagePreview(final boolean preview) {
		if (preview != mDisplayImagePreview) {
			mDisplayImagePreview = preview;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setGapDisallowed(final boolean disallowed) {
		if (mGapDisallowed != disallowed) {
			mGapDisallowed = disallowed;
			notifyDataSetChanged();
		}

	}

	@Override
	public void setMentionsHightlightDisabled(final boolean disable) {
		if (disable != mMentionsHighlightDisabled) {
			mMentionsHighlightDisabled = disable;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setMultiSelectEnabled(final boolean multi) {
		if (mMultiSelectEnabled != multi) {
			mMultiSelectEnabled = multi;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setNameDisplayOption(final String option) {
		if (NAME_DISPLAY_OPTION_NAME.equals(option)) {
			mNameDisplayOption = NAME_DISPLAY_OPTION_CODE_NAME;
		} else if (NAME_DISPLAY_OPTION_SCREEN_NAME.equals(option)) {
			mNameDisplayOption = NAME_DISPLAY_OPTION_CODE_SCREEN_NAME;
		} else {
			mNameDisplayOption = 0;
		}
	}

	@Override
	public void setShowAbsoluteTime(final boolean show) {
		if (show != mShowAbsoluteTime) {
			mShowAbsoluteTime = show;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setShowAccountColor(final boolean show) {
		if (show != mShowAccountColor) {
			mShowAccountColor = show;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setTextSize(final float text_size) {
		if (text_size != mTextSize) {
			mTextSize = text_size;
			notifyDataSetChanged();
		}
	}

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		if (cursor != null) {
			mIndices = new StatusCursorIndices(cursor);
		} else {
			mIndices = null;
		}
		return super.swapCursor(cursor);
	}

	static class TimeSpec {

		long time;

		TimeSpec(final long time) {
			this.time = time;
		}

		@Override
		public String toString() {
			return getRelativeTimeSpanString(time).toString();
		}
	}

}
