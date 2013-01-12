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
import static org.mariotaku.twidere.model.ParcelableLocation.isValidLocation;
import static org.mariotaku.twidere.util.Utils.formatSameDayTime;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getAllAvailableImage;
import static org.mariotaku.twidere.util.Utils.getInlineImagePreviewDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.getNameDisplayOptionInt;
import static org.mariotaku.twidere.util.Utils.getStatusBackground;
import static org.mariotaku.twidere.util.Utils.getStatusTypeIconRes;
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getUserTypeIconRes;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ImageSpec;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;

public class ParcelableStatusesAdapter extends ArrayAdapter<ParcelableStatus> implements IStatusesAdapter,
		OnClickListener {

	private final Context mContext;
	private final Resources mResources;
	private final LazyImageLoader mProfileImageLoader, mPreviewImageLoader;
	private final MultiSelectManager mMultiSelectManager;

	private boolean mDisplayProfileImage, mShowAccountColor, mShowAbsoluteTime, mGapDisallowed, mMultiSelectEnabled,
			mMentionsHighlightDisabled, mDisplaySensitiveContents, mIndicateMyStatusDisabled, mLinkHighlightingEnabled,
			mFastTimelineProcessingEnabled;
	private float mTextSize;
	private int mNameDisplayOption, mInlineImagePreviewDisplayOption;

	public ParcelableStatusesAdapter(final Context context) {
		super(context, R.layout.status_list_item);
		mContext = context;
		mResources = context.getResources();
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mMultiSelectManager = application.getMultiSelectManager();
		mProfileImageLoader = application.getProfileImageLoader();
		mPreviewImageLoader = application.getPreviewImageLoader();
	}

	public long findItemIdByPosition(final int position) {
		if (position >= 0 && position < getCount()) return getItem(position).status_id;
		return -1;
	}

	public int findItemPositionByStatusId(final long status_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItem(i).status_id == status_id) return i;
		}
		return -1;
	}

	@Override
	public long getItemId(final int position) {
		final ParcelableStatus item = getItem(position);
		return item != null ? item.status_id : -1;
	}

	@Override
	public ParcelableStatus getStatus(final int position) {
		return getItem(position);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Object tag = view.getTag();
		final StatusViewHolder holder;

		if (tag instanceof StatusViewHolder) {
			holder = (StatusViewHolder) tag;
		} else {
			holder = new StatusViewHolder(view);
			view.setTag(holder);
			holder.profile_image.setOnClickListener(mMultiSelectEnabled ? null : this);
			holder.image_preview_frame.setOnClickListener(mMultiSelectEnabled ? null : this);
		}

		final ParcelableStatus status = getItem(position);

		final boolean show_gap = status.is_gap && !mGapDisallowed && position != getCount() - 1;

		holder.setShowAsGap(show_gap);

		if (!show_gap) {

			holder.setAccountColorEnabled(mShowAccountColor);

			if (mFastTimelineProcessingEnabled) {
				holder.text.setText(status.text_plain);
			} else if (mLinkHighlightingEnabled) {
				holder.text.setText(Html.fromHtml(status.text_html));
				final TwidereLinkify linkify = new TwidereLinkify(holder.text);
				linkify.setOnLinkClickListener(new OnLinkClickHandler(mContext, status.account_id));
				linkify.addAllLinks();
			} else {
				holder.text.setText(status.text_unescaped);
			}
			holder.text.setMovementMethod(null);

			if (mShowAccountColor) {
				holder.setAccountColor(getAccountColor(mContext, status.account_id));
			}
			final String retweeted_by_name = status.retweeted_by_name;
			final String retweeted_by_screen_name = status.retweeted_by_screen_name;

			if (mMultiSelectEnabled) {
				holder.setSelected(mMultiSelectManager.isStatusSelected(status.status_id));
			} else {
				holder.setSelected(false);
			}
			final String account_screen_name = getAccountScreenName(mContext, status.account_id);
			final boolean is_mention = mFastTimelineProcessingEnabled ? false : status.text_plain.toLowerCase()
					.contains('@' + account_screen_name.toLowerCase());
			final boolean is_my_status = status.account_id == status.user_id;
			holder.setUserColor(getUserColor(mContext, status.user_id));
			holder.setHighlightColor(mFastTimelineProcessingEnabled ? 0 : getStatusBackground(
					mMentionsHighlightDisabled ? false : is_mention, status.is_favorite, status.is_retweet));
			holder.setTextSize(mTextSize);

			holder.setIsMyStatus(is_my_status && !mIndicateMyStatusDisabled);

			holder.name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					getUserTypeIconRes(status.is_verified, status.is_protected), 0);
			switch (mNameDisplayOption) {
				case NAME_DISPLAY_OPTION_CODE_NAME: {
					holder.name.setText(status.name);
					holder.screen_name.setText(null);
					holder.screen_name.setVisibility(View.GONE);
					break;
				}
				case NAME_DISPLAY_OPTION_CODE_SCREEN_NAME: {
					holder.name.setText("@" + status.screen_name);
					holder.screen_name.setText(null);
					holder.screen_name.setVisibility(View.GONE);
					break;
				}
				default: {
					holder.name.setText(status.name);
					holder.screen_name.setText("@" + status.screen_name);
					holder.screen_name.setVisibility(View.VISIBLE);
					break;
				}
			}
			if (mShowAbsoluteTime) {
				holder.time.setText(formatSameDayTime(mContext, status.status_timestamp));
			} else {
				holder.time.setText(getRelativeTimeSpanString(status.status_timestamp));
			}
			holder.time.setCompoundDrawablesWithIntrinsicBounds(0, 0, mFastTimelineProcessingEnabled ? 0
					: getStatusTypeIconRes(status.is_favorite, isValidLocation(status.location), status.has_media), 0);
			holder.reply_retweet_status
					.setVisibility(status.in_reply_to_status_id != -1 || status.is_retweet ? View.VISIBLE : View.GONE);
			if (status.is_retweet && !TextUtils.isEmpty(retweeted_by_name)
					&& !TextUtils.isEmpty(retweeted_by_screen_name)) {
				if (mNameDisplayOption == NAME_DISPLAY_OPTION_CODE_SCREEN_NAME) {
					holder.reply_retweet_status.setText(status.retweet_count > 1 ? mContext.getString(
							R.string.retweeted_by_with_count, retweeted_by_screen_name, status.retweet_count - 1)
							: mContext.getString(R.string.retweeted_by, retweeted_by_screen_name));
				} else {
					holder.reply_retweet_status.setText(status.retweet_count > 1 ? mContext.getString(
							R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : mContext
							.getString(R.string.retweeted_by, retweeted_by_name));
				}
				holder.reply_retweet_status.setText(status.retweet_count > 1 ? mContext.getString(
						R.string.retweeted_by_with_count, retweeted_by_name, status.retweet_count - 1) : mContext
						.getString(R.string.retweeted_by, retweeted_by_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_retweet, 0,
						0, 0);
			} else if (status.in_reply_to_status_id > 0 && !TextUtils.isEmpty(status.in_reply_to_screen_name)) {
				holder.reply_retweet_status.setText(mContext.getString(R.string.in_reply_to,
						status.in_reply_to_screen_name));
				holder.reply_retweet_status.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_indicator_reply, 0,
						0, 0);
			}
			if (mDisplayProfileImage) {
				mProfileImageLoader.displayImage(holder.my_profile_image, status.profile_image_url);
				mProfileImageLoader.displayImage(holder.profile_image, status.profile_image_url);
				holder.profile_image.setTag(position);
				holder.my_profile_image.setTag(position);
			} else {
				holder.profile_image.setVisibility(View.GONE);
				holder.my_profile_image.setVisibility(View.GONE);
			}
			final boolean has_preview = mFastTimelineProcessingEnabled ? false
					: mInlineImagePreviewDisplayOption != INLINE_IMAGE_PREVIEW_DISPLAY_OPTION_CODE_NONE
							&& status.has_media && status.image_preview_url != null;
			holder.image_preview_container.setVisibility(!mFastTimelineProcessingEnabled && has_preview ? View.VISIBLE
					: View.GONE);
			if (has_preview) {
				final MarginLayoutParams lp = (MarginLayoutParams) holder.image_preview_frame.getLayoutParams();
				if (mInlineImagePreviewDisplayOption == INLINE_IMAGE_PREVIEW_DISPLAY_OPTION_CODE_LARGE) {
					lp.width = LayoutParams.MATCH_PARENT;
					lp.leftMargin = 0;
					holder.image_preview_frame.setLayoutParams(lp);
				} else if (mInlineImagePreviewDisplayOption == INLINE_IMAGE_PREVIEW_DISPLAY_OPTION_CODE_SMALL) {
					lp.width = mResources.getDimensionPixelSize(R.dimen.image_preview_width);
					lp.leftMargin = (int) (mResources.getDisplayMetrics().density * 16);
					holder.image_preview_frame.setLayoutParams(lp);
				}
				if (status.is_possibly_sensitive && !mDisplaySensitiveContents) {
					holder.image_preview.setImageResource(R.drawable.image_preview_nsfw);
				} else {
					mPreviewImageLoader.displayImage(holder.image_preview, status.image_preview_url);
				}
				holder.image_preview_frame.setTag(position);
			}
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
			case R.id.image_preview_frame: {
				final ImageSpec spec = getAllAvailableImage(status.image_orig_url);
				if (spec != null) {
					openImage(mContext, Uri.parse(spec.full_image_link), status.is_possibly_sensitive);
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

	public void setData(final List<ParcelableStatus> data) {
		clear();
		if (data == null) return;
		addAll(data);
		notifyDataSetChanged();
	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setDisplaySensitiveContents(final boolean display) {
		if (display != mDisplaySensitiveContents) {
			mDisplaySensitiveContents = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setFastTimelineProcessingEnabled(final boolean enabled) {
		if (mFastTimelineProcessingEnabled != enabled) {
			mFastTimelineProcessingEnabled = enabled;
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
	public void setIndicateMyStatusDisabled(final boolean disable) {
		if (mIndicateMyStatusDisabled != disable) {
			mIndicateMyStatusDisabled = disable;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setInlineImagePreviewDisplayOption(final String option) {
		if (option != null && !option.equals(mInlineImagePreviewDisplayOption)) {
			mInlineImagePreviewDisplayOption = getInlineImagePreviewDisplayOptionInt(option);
			notifyDataSetChanged();
		}
	}

	@Override
	public void setLinkHightlightingEnabled(final boolean enable) {
		if (mLinkHighlightingEnabled != enable) {
			mLinkHighlightingEnabled = enable;
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
		mNameDisplayOption = getNameDisplayOptionInt(option);
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
}
