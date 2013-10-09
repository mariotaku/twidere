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

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.findStatusInDatabases;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IStatusesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.StatusCursorIndices;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ImageLoadingHandler;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.StatusViewHolder;

import java.util.Locale;

public class CursorStatusesAdapter extends SimpleCursorAdapter implements IStatusesAdapter<Cursor>, OnClickListener {

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

	private StatusCursorIndices mIndices;

	public CursorStatusesAdapter(final Context context) {
		super(context, R.layout.status_list_item, null, new String[0], new int[0], 0);
		mContext = context;
		final TwidereApplication application = TwidereApplication.getInstance(context);
		mMultiSelectManager = application.getMultiSelectManager();
		mImageLoader = application.getImageLoaderWrapper();
		mDatabase = application.getSQLiteDatabase();
		mLinkify = new TwidereLinkify(new OnLinkClickHandler(mContext));
		mImageLoadingHandler = new ImageLoadingHandler();
		configBaseCardAdapter(context, this);
		setMaxAnimationPosition(-1);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int position = cursor.getPosition();
		final StatusViewHolder holder = (StatusViewHolder) view.getTag();

		// Clear images in prder to prevent images in recycled view shown.
		// holder.profile_image.setImageDrawable(null);
		// holder.my_profile_image.setImageDrawable(null);
		// holder.image_preview.setImageDrawable(null);

		final boolean is_gap = cursor.getShort(mIndices.is_gap) == 1;
		final boolean show_gap = is_gap && !mGapDisallowed && position != getCount() - 1;

		holder.setShowAsGap(show_gap);

		if (!show_gap) {

			final long account_id = cursor.getLong(mIndices.account_id);
			final long user_id = cursor.getLong(mIndices.user_id);
			final long status_timestamp = cursor.getLong(mIndices.status_timestamp);
			final long retweet_count = cursor.getLong(mIndices.retweet_count);
			final long retweeted_by_user_id = cursor.getLong(mIndices.retweeted_by_user_id);
			final long in_reply_to_user_id = cursor.getLong(mIndices.in_reply_to_user_id);

			final String retweeted_by_name = cursor.getString(mIndices.retweeted_by_user_name);
			final String retweeted_by_screen_name = cursor.getString(mIndices.retweeted_by_user_screen_name);
			final String text = mLinkHighlightOption != LINK_HIGHLIGHT_OPTION_CODE_NONE ? cursor
					.getString(mIndices.text_html) : cursor.getString(mIndices.text_unescaped);
			final String screen_name = cursor.getString(mIndices.user_screen_name);
			final String name = cursor.getString(mIndices.user_name);
			final String in_reply_to_name = cursor.getString(mIndices.in_reply_to_user_name);
			final String in_reply_to_screen_name = cursor.getString(mIndices.in_reply_to_user_screen_name);
			final String account_screen_name = getAccountScreenName(mContext, account_id);
			final String media_link = cursor.getString(mIndices.media_link);

			// Tweet type (favorite/location/media)
			final boolean is_favorite = cursor.getShort(mIndices.is_favorite) == 1;
			final boolean has_location = !TextUtils.isEmpty(cursor.getString(mIndices.location));
			final boolean is_possibly_sensitive = cursor.getInt(mIndices.is_possibly_sensitive) == 1;
			final boolean has_media = media_link != null;

			// User type (protected/verified)
			final boolean is_verified = cursor.getShort(mIndices.is_verified) == 1;
			final boolean is_protected = cursor.getShort(mIndices.is_protected) == 1;

			final boolean is_retweet = cursor.getShort(mIndices.is_retweet) == 1;
			final boolean is_reply = cursor.getLong(mIndices.in_reply_to_status_id) > 0;
			final boolean is_mention = TextUtils.isEmpty(text) || TextUtils.isEmpty(account_screen_name) ? false : text
					.toLowerCase(Locale.US).contains('@' + account_screen_name.toLowerCase(Locale.US));
			final boolean is_my_status = account_id == user_id;

			holder.setUserColor(getUserColor(mContext, user_id));
			holder.setHighlightColor(getStatusBackground(!mMentionsHighlightDisabled && is_mention,
					!mFavoritesHighlightDisabled && is_favorite, is_retweet));

			holder.setAccountColorEnabled(mShowAccountColor);

			if (mShowAccountColor) {
				holder.setAccountColor(getAccountColor(mContext, account_id));
			}

			holder.setTextSize(mTextSize);

			holder.setIsMyStatus(is_my_status && !mIndicateMyStatusDisabled);
			if (mLinkHighlightOption != LINK_HIGHLIGHT_OPTION_CODE_NONE) {
				holder.text.setText(Html.fromHtml(text));
				mLinkify.applyAllLinks(holder.text, account_id, is_possibly_sensitive);
				holder.text.setMovementMethod(null);
			} else {
				holder.text.setText(text);
			}
			holder.setUserType(is_verified, is_protected);
			holder.setDisplayNameFirst(mDisplayNameFirst);
			holder.setNicknameOnly(mNicknameOnly);
			final String nick = getUserNickname(context, user_id);
			holder.name.setText(TextUtils.isEmpty(nick) ? name : mNicknameOnly ? nick : context.getString(
					R.string.name_with_nickname, name, nick));
			holder.screen_name.setText("@" + screen_name);
			if (mLinkHighlightOption != LINK_HIGHLIGHT_OPTION_CODE_NONE) {
				mLinkify.applyUserProfileLink(holder.name, account_id, user_id, screen_name);
				mLinkify.applyUserProfileLink(holder.screen_name, account_id, user_id, screen_name);
				holder.name.setMovementMethod(null);
				holder.screen_name.setMovementMethod(null);
			}
			holder.time.setTime(status_timestamp);
			holder.setStatusType(!mFavoritesHighlightDisabled && is_favorite, has_location, has_media,
					is_possibly_sensitive);

			holder.setIsReplyRetweet(is_reply, is_retweet);
			if (is_retweet) {
				holder.setRetweetedBy(retweet_count, retweeted_by_user_id, retweeted_by_name, retweeted_by_screen_name);
			} else if (is_reply) {
				holder.setReplyTo(in_reply_to_user_id, in_reply_to_name, in_reply_to_screen_name);
			}

			if (mDisplayProfileImage) {
				final String profile_image_url = cursor.getString(mIndices.user_profile_image_url);
				mImageLoader.displayProfileImage(holder.my_profile_image, profile_image_url);
				mImageLoader.displayProfileImage(holder.profile_image, profile_image_url);
				holder.profile_image.setTag(position);
				holder.my_profile_image.setTag(position);
			} else {
				holder.profile_image.setVisibility(View.GONE);
				holder.my_profile_image.setVisibility(View.GONE);
			}
			final boolean has_preview = mDisplayImagePreview && has_media;
			holder.image_preview_container.setVisibility(has_preview ? View.VISIBLE : View.GONE);
			if (has_preview) {
				if (is_possibly_sensitive && !mDisplaySensitiveContents) {
					holder.image_preview.setImageDrawable(null);
					holder.image_preview.setBackgroundResource(R.drawable.image_preview_nsfw);
					holder.image_preview_progress.setVisibility(View.GONE);
				} else if (!media_link.equals(mImageLoadingHandler.getLoadingUri(holder.image_preview))) {
					holder.image_preview.setBackgroundResource(0);
					mImageLoader.displayPreviewImage(holder.image_preview, media_link, mImageLoadingHandler);
				}
				holder.image_preview.setTag(position);
			}
			holder.item_menu.setTag(position);
		}
	}

	@Override
	public long findItemIdByPosition(final int position) {
		if (position >= 0 && position < getCount()) return getItem(position).getLong(mIndices.status_id);
		return -1;
	}

	@Override
	public int findItemPositionByStatusId(final long status_id) {
		for (int i = 0, count = getCount(); i < count; i++) {
			if (getItem(i).getLong(mIndices.status_id) == status_id) return i;
		}
		return -1;
	}

	@Override
	public int getCount() {
		final int count = super.getCount();
		return mFiltersEnabled && mIsLastItemFiltered && count > 0 ? count - 1 : count;
	}

	@Override
	public Cursor getItem(final int position) {
		return (Cursor) super.getItem(position);
	}

	@Override
	public ParcelableStatus getLastStatus() {
		final Cursor c = getCursor();
		if (c == null || c.isClosed() || c.getCount() == 0) return null;
		c.moveToLast();
		final long account_id = c.getLong(mIndices.account_id);
		final long status_id = c.getLong(mIndices.status_id);
		return findStatusInDatabases(mContext, account_id, status_id);
	}

	@Override
	public ParcelableStatus getStatus(final int position) {
		final Cursor cur = getItem(position);
		final long account_id = cur.getLong(mIndices.account_id);
		final long status_id = cur.getLong(mIndices.status_id);
		return findStatusInDatabases(mContext, account_id, status_id);
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		final View view = super.getView(position, convertView, parent);
		final Object tag = view.getTag();
		// animate the item
		if (tag instanceof StatusViewHolder && position > mMaxAnimationPosition) {
			if (mAnimationEnabled) {
				view.startAnimation(((StatusViewHolder) tag).item_animation);
			}
			mMaxAnimationPosition = position;
		}
		return view;
	}

	@Override
	public boolean isLastItemFiltered() {
		return mFiltersEnabled && mIsLastItemFiltered;
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof StatusViewHolder)) {
			final StatusViewHolder holder = new StatusViewHolder(view);
			holder.profile_image.setOnClickListener(this);
			holder.my_profile_image.setOnClickListener(this);
			holder.image_preview.setOnClickListener(this);
			holder.item_menu.setOnClickListener(this);
			view.setTag(holder);
		}
		return view;
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
	public void setData(final Cursor data) {
		swapCursor(data);
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

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		mIndices = cursor != null ? new StatusCursorIndices(cursor) : null;
		rebuildFilterInfo();
		return super.swapCursor(cursor);
	}

	private void rebuildFilterInfo() {
		final Cursor c = getCursor();
		if (c != null && !c.isClosed() && mIndices != null && c.getCount() > 0) {
			if (c.getCount() > 0) {
				c.moveToLast();
				final long user_id = mFilterIgnoreUser ? -1 : c.getLong(mIndices.user_id);
				final String text_plain = mFilterIgnoreTextPlain ? null : c.getString(mIndices.text_plain);
				final String text_html = mFilterIgnoreTextHtml ? null : c.getString(mIndices.text_html);
				final String source = mFilterIgnoreSource ? null : c.getString(mIndices.source);
				final long retweeted_by_id = mFilterRetweetedById ? -1 : c.getLong(mIndices.retweeted_by_user_id);
				;
				mIsLastItemFiltered = isFiltered(mDatabase, user_id, text_plain, text_html, source, retweeted_by_id);
			} else {
				mIsLastItemFiltered = false;
			}
		} else {
			mIsLastItemFiltered = false;
		}
	}
}
