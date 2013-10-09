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

import static org.mariotaku.twidere.model.ParcelableLocation.isValidLocation;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getAccountScreenName;
import static org.mariotaku.twidere.util.Utils.getLinkHighlightOptionInt;
import static org.mariotaku.twidere.util.Utils.getStatusBackground;
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.isFiltered;
import static org.mariotaku.twidere.util.Utils.openImage;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import java.util.List;

public class ParcelableStatusesAdapter extends ArrayAdapter<ParcelableStatus> implements
		IStatusesAdapter<List<ParcelableStatus>>, OnClickListener {

	private final Context mContext;
	private final ImageLoaderWrapper mImageLoader;
	private final MultiSelectManager mMultiSelectManager;
	private final TwidereLinkify mLinkify;
	private final SQLiteDatabase mDatabase;
	private final ImageLoadingHandler mImageLoadingHandler;

	private MenuButtonClickListener mListener;
	private boolean mDisplayProfileImage, mDisplayImagePreview, mShowAccountColor, mGapDisallowed,
			mMentionsHighlightDisabled, mFavoritesHighlightDisabled, mDisplaySensitiveContents,
			mIndicateMyStatusDisabled, mIsLastItemFiltered, mFiltersEnabled, mAnimationEnabled;
	private float mTextSize;
	private int mLinkHighlightOption;
	private boolean mFilterIgnoreUser, mFilterIgnoreSource, mFilterIgnoreTextHtml, mFilterIgnoreTextPlain,
			mFilterRetweetedById, mNicknameOnly, mDisplayNameFirst;
	private int mMaxAnimationPosition;

	public ParcelableStatusesAdapter(final Context context) {
		super(context, R.layout.status_list_item);
		mContext = context;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mMultiSelectManager = app.getMultiSelectManager();
		mImageLoader = app.getImageLoaderWrapper();
		mDatabase = app.getSQLiteDatabase();
		mLinkify = new TwidereLinkify(new OnLinkClickHandler(mContext));
		mImageLoadingHandler = new ImageLoadingHandler();
		configBaseCardAdapter(context, this);
		setMaxAnimationPosition(-1);
	}

	@Override
	public long findItemIdByPosition(final int position) {
		if (position >= 0 && position < getCount()) return getItem(position).id;
		return -1;
	}

	@Override
	public int findItemPositionByStatusId(final long status_id) {
		for (int i = 0, count = getCount(); i < count; i++) {
			if (getItem(i).id == status_id) return i;
		}
		return -1;
	}

	@Override
	public int getCount() {
		final int count = super.getCount();
		return mFiltersEnabled && mIsLastItemFiltered && count > 0 ? count - 1 : count;
	}

	@Override
	public long getItemId(final int position) {
		final ParcelableStatus item = getItem(position);
		return item != null ? item.id : -1;
	}

	@Override
	public ParcelableStatus getLastStatus() {
		if (super.getCount() == 0) return null;
		return getItem(super.getCount() - 1);
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
			holder.profile_image.setOnClickListener(this);
			holder.my_profile_image.setOnClickListener(this);
			holder.image_preview.setOnClickListener(this);
			holder.item_menu.setOnClickListener(this);
			view.setTag(holder);
		}

		// Clear images in prder to prevent images in recycled view shown.
		// holder.profile_image.setImageDrawable(null);
		// holder.my_profile_image.setImageDrawable(null);
		// holder.image_preview.setImageDrawable(null);

		final ParcelableStatus status = getItem(position);

		final boolean show_gap = status.is_gap && !mGapDisallowed && position != getCount() - 1;

		holder.setShowAsGap(show_gap);

		if (!show_gap) {

			holder.setAccountColorEnabled(mShowAccountColor);

			if (mLinkHighlightOption != LINK_HIGHLIGHT_OPTION_CODE_NONE) {
				holder.text.setText(Html.fromHtml(status.text_html));
				mLinkify.applyAllLinks(holder.text, status.account_id, status.is_possibly_sensitive);
				holder.text.setMovementMethod(null);
			} else {
				holder.text.setText(status.text_unescaped);
			}

			if (mShowAccountColor) {
				holder.setAccountColor(getAccountColor(mContext, status.account_id));
			}

			final String account_screen_name = getAccountScreenName(mContext, status.account_id);
			final boolean is_mention = !TextUtils.isEmpty(status.text_plain)
					&& status.text_plain.toLowerCase().contains('@' + account_screen_name.toLowerCase());
			final boolean is_my_status = status.account_id == status.user_id;
			holder.setUserColor(getUserColor(mContext, status.user_id));
			holder.setHighlightColor(getStatusBackground(!mMentionsHighlightDisabled && is_mention,
					!mFavoritesHighlightDisabled && status.is_favorite, status.is_retweet));
			holder.setTextSize(mTextSize);

			holder.setIsMyStatus(is_my_status && !mIndicateMyStatusDisabled);

			holder.setUserType(status.user_is_verified, status.user_is_protected);
			holder.setDisplayNameFirst(mDisplayNameFirst);
			holder.setNicknameOnly(mNicknameOnly);
			final String nick = getUserNickname(mContext, status.user_id);
			holder.name.setText(TextUtils.isEmpty(nick) ? status.user_name : mNicknameOnly ? nick : mContext.getString(
					R.string.name_with_nickname, status.user_name, nick));
			holder.screen_name.setText("@" + status.user_screen_name);
			if (mLinkHighlightOption != LINK_HIGHLIGHT_OPTION_CODE_NONE) {
				mLinkify.applyUserProfileLink(holder.name, status.account_id, status.user_id, status.user_screen_name);
				mLinkify.applyUserProfileLink(holder.screen_name, status.account_id, status.user_id,
						status.user_screen_name);
				holder.name.setMovementMethod(null);
				holder.screen_name.setMovementMethod(null);
			}
			holder.time.setTime(status.timestamp);
			holder.setStatusType(!mFavoritesHighlightDisabled && status.is_favorite, isValidLocation(status.location),
					status.has_media, status.is_possibly_sensitive);
			holder.setIsReplyRetweet(status.in_reply_to_status_id > 0, status.is_retweet);
			if (status.is_retweet) {
				holder.setRetweetedBy(status.retweet_count, status.retweeted_by_id, status.retweeted_by_name,
						status.retweeted_by_screen_name);
			} else if (status.in_reply_to_status_id > 0) {
				holder.setReplyTo(status.in_reply_to_user_id, status.in_reply_to_name, status.in_reply_to_screen_name);
			}
			if (mDisplayProfileImage) {
				mImageLoader.displayProfileImage(holder.my_profile_image, status.user_profile_image_url);
				mImageLoader.displayProfileImage(holder.profile_image, status.user_profile_image_url);
				holder.profile_image.setTag(position);
				holder.my_profile_image.setTag(position);
			} else {
				holder.profile_image.setVisibility(View.GONE);
				holder.my_profile_image.setVisibility(View.GONE);
			}
			final boolean has_preview = mDisplayImagePreview && status.has_media && status.media_link != null;
			holder.image_preview_container.setVisibility(has_preview ? View.VISIBLE : View.GONE);
			if (has_preview) {
				if (status.is_possibly_sensitive && !mDisplaySensitiveContents) {
					holder.image_preview.setImageDrawable(null);
					holder.image_preview.setBackgroundResource(R.drawable.image_preview_nsfw);
					holder.image_preview_progress.setVisibility(View.GONE);
				} else if (!status.media_link.equals(mImageLoadingHandler.getLoadingUri(holder.image_preview))) {
					holder.image_preview.setBackgroundResource(0);
					mImageLoader.displayPreviewImage(holder.image_preview, status.media_link, mImageLoadingHandler);
				}
				holder.image_preview.setTag(position);
			}
			holder.item_menu.setTag(position);
		}
		if (position > mMaxAnimationPosition) {
			if (mAnimationEnabled) {
				view.startAnimation(holder.item_animation);
			}
			mMaxAnimationPosition = position;
		}
		return view;
	}

	@Override
	public boolean isLastItemFiltered() {
		return mIsLastItemFiltered;
	}

	@Override
	public void onClick(final View view) {
		if (mMultiSelectManager.isActive()) return;
		final Object tag = view.getTag();
		final int position = tag instanceof Integer ? (Integer) tag : -1;
		if (position == -1) return;
		switch (view.getId()) {
			case R.id.image_preview: {
				final ParcelableStatus status = getStatus(position);
				if (status == null || status.media_link == null) return;
				openImage(mContext, status.media_link, status.is_possibly_sensitive);
				break;
			}
			case R.id.my_profile_image:
			case R.id.profile_image: {
				final ParcelableStatus status = getStatus(position);
				if (status == null) return;
				if (mContext instanceof Activity) {
					openUserProfile((Activity) mContext, status.account_id, status.user_id, status.user_screen_name);
				}
				break;
			}
			case R.id.item_menu: {
				if (position == -1 || mListener == null) return;
				mListener.onMenuButtonClick(view, position, getItemId(position));
				break;
			}
		}
	}

	@Override
	public void setAnimationEnabled(final boolean anim) {
		if (mAnimationEnabled == anim) return;
		mAnimationEnabled = anim;
	}

	@Override
	public void setData(final List<ParcelableStatus> data) {
		clear();
		if (data != null && !data.isEmpty()) {
			addAll(data);
			notifyDataSetChanged();
		}
		rebuildFilterInfo();
	}

	@Override
	public void setDisplayImagePreview(final boolean display) {
		if (display == mDisplayImagePreview) return;
		mDisplayImagePreview = display;
		notifyDataSetChanged();
	}

	@Override
	public void setDisplayNameFirst(final boolean name_first) {
		if (mDisplayNameFirst == name_first) return;
		mDisplayNameFirst = name_first;
		notifyDataSetChanged();
	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display == mDisplayProfileImage) return;
		mDisplayProfileImage = display;
		notifyDataSetChanged();
	}

	@Override
	public void setDisplaySensitiveContents(final boolean display) {
		if (display == mDisplaySensitiveContents) return;
		mDisplaySensitiveContents = display;
		notifyDataSetChanged();
	}

	@Override
	public void setFavoritesHightlightDisabled(final boolean disable) {
		if (disable == mFavoritesHighlightDisabled) return;
		mFavoritesHighlightDisabled = disable;
		notifyDataSetChanged();
	}

	@Override
	public void setFiltersEnabled(final boolean enabled) {
		if (mFiltersEnabled == enabled) return;
		mFiltersEnabled = enabled;
		rebuildFilterInfo();
		notifyDataSetChanged();
	}

	@Override
	public void setGapDisallowed(final boolean disallowed) {
		if (mGapDisallowed == disallowed) return;
		mGapDisallowed = disallowed;
		notifyDataSetChanged();
	}

	@Override
	public void setIgnoredFilterFields(final boolean user, final boolean text_plain, final boolean text_html,
			final boolean source, final boolean retweeted_by_id) {
		mFilterIgnoreTextPlain = text_plain;
		mFilterIgnoreTextHtml = text_html;
		mFilterIgnoreUser = user;
		mFilterIgnoreSource = source;
		mFilterRetweetedById = retweeted_by_id;
		rebuildFilterInfo();
		notifyDataSetChanged();
	}

	@Override
	public void setIndicateMyStatusDisabled(final boolean disable) {
		if (mIndicateMyStatusDisabled == disable) return;
		mIndicateMyStatusDisabled = disable;
		notifyDataSetChanged();
	}

	@Override
	public void setLinkHighlightOption(final String option) {
		final int option_int = getLinkHighlightOptionInt(option);
		if (option_int == mLinkHighlightOption) return;
		mLinkHighlightOption = option_int;
		mLinkify.setHighlightOption(option_int);
		notifyDataSetChanged();
	}

	@Override
	public void setMaxAnimationPosition(final int position) {
		mMaxAnimationPosition = position;
	}

	@Override
	public void setMentionsHightlightDisabled(final boolean disable) {
		if (disable == mMentionsHighlightDisabled) return;
		mMentionsHighlightDisabled = disable;
		notifyDataSetChanged();
	}

	@Override
	public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
		mListener = listener;
	}

	@Override
	public void setNicknameOnly(final boolean nickname_only) {
		if (mNicknameOnly == nickname_only) return;
		mNicknameOnly = nickname_only;
		notifyDataSetChanged();
	}

	@Override
	public void setShowAccountColor(final boolean show) {
		if (show == mShowAccountColor) return;
		mShowAccountColor = show;
		notifyDataSetChanged();
	}

	@Override
	public void setTextSize(final float text_size) {
		if (text_size == mTextSize) return;
		mTextSize = text_size;
		notifyDataSetChanged();
	}

	private void rebuildFilterInfo() {
		if (!isEmpty()) {
			final ParcelableStatus last = getItem(super.getCount() - 1);
			final long user_id = mFilterIgnoreUser ? -1 : last.user_id;
			final String text_plain = mFilterIgnoreTextPlain ? null : last.text_plain;
			final String text_html = mFilterIgnoreTextHtml ? null : last.text_html;
			final String source = mFilterIgnoreSource ? null : last.source;
			final long retweeted_by_id = mFilterRetweetedById ? -1 : last.retweeted_by_id;
			mIsLastItemFiltered = isFiltered(mDatabase, user_id, text_plain, text_html, source, retweeted_by_id);
		} else {
			mIsLastItemFiltered = false;
		}
	}
}
