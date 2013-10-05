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

import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_ACCOUNT_ID;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_CONVERSATION_ID;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_NAME;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_PROFILE_IMAGE_URL;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_SCREEN_NAME;
import static org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry.IDX_TEXT;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.getAccountColor;
import static org.mariotaku.twidere.util.Utils.getUserColor;
import static org.mariotaku.twidere.util.Utils.getUserNickname;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IBaseCardAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages.ConversationsEntry;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.view.holder.DirectMessageEntryViewHolder;

public class DirectMessagesEntryAdapter extends SimpleCursorAdapter implements IBaseCardAdapter, OnClickListener {

	private final ImageLoaderWrapper mLazyImageLoader;
	private final MultiSelectManager mMultiSelectManager;

	private boolean mDisplayProfileImage, mShowAccountColor, mNicknameOnly, mAnimationEnabled;
	private float mTextSize;
	private int mMaxAnimationPosition;

	private MenuButtonClickListener mListener;

	public DirectMessagesEntryAdapter(final Context context) {
		super(context, R.layout.direct_messages_entry_list_item, null, new String[0], new int[0], 0);
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mMultiSelectManager = app.getMultiSelectManager();
		mLazyImageLoader = app.getImageLoaderWrapper();
		configBaseCardAdapter(context, this);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final DirectMessageEntryViewHolder holder = (DirectMessageEntryViewHolder) view.getTag();
		final int position = cursor.getPosition();
		final long account_id = cursor.getLong(ConversationsEntry.IDX_ACCOUNT_ID);
		final long conversation_id = cursor.getLong(ConversationsEntry.IDX_CONVERSATION_ID);
		final long message_timestamp = cursor.getLong(ConversationsEntry.IDX_MESSAGE_TIMESTAMP);
		final boolean is_outgoing = cursor.getInt(ConversationsEntry.IDX_IS_OUTGOING) == 1;

		final String name = cursor.getString(IDX_NAME);
		final String screen_name = cursor.getString(IDX_SCREEN_NAME);

		holder.setAccountColorEnabled(mShowAccountColor);

		if (mShowAccountColor) {
			holder.setAccountColor(getAccountColor(mContext, account_id));
		}

		holder.setUserColor(getUserColor(mContext, conversation_id));

		holder.setTextSize(mTextSize);
		final String nick = getUserNickname(context, conversation_id);
		holder.name.setText(TextUtils.isEmpty(nick) ? name : mNicknameOnly ? nick : context.getString(
				R.string.name_with_nickname, name, nick));
		holder.screen_name.setText("@" + screen_name);
		holder.screen_name.setVisibility(View.VISIBLE);
		holder.text.setText(toPlainText(cursor.getString(IDX_TEXT)));
		holder.time.setTime(message_timestamp);
		holder.setIsOutgoing(is_outgoing);
		holder.profile_image.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			holder.profile_image.setTag(position);
			final String profile_image_url_string = cursor.getString(IDX_PROFILE_IMAGE_URL);
			mLazyImageLoader.displayProfileImage(holder.profile_image, profile_image_url_string);
		}
		if (position > mMaxAnimationPosition) {
			if (mAnimationEnabled) {
				view.startAnimation(holder.item_animation);
			}
			mMaxAnimationPosition = position;
		}
		super.bindView(view, context, cursor);
	}

	public long getAccountId(final int position) {
		return ((Cursor) getItem(position)).getLong(IDX_ACCOUNT_ID);
	}

	public long getConversationId(final int position) {
		return ((Cursor) getItem(position)).getLong(IDX_CONVERSATION_ID);
	}

	public String getScreenName(final int position) {
		return ((Cursor) getItem(position)).getString(IDX_SCREEN_NAME);
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof DirectMessageEntryViewHolder)) {
			final DirectMessageEntryViewHolder holder = new DirectMessageEntryViewHolder(view);
			view.setTag(holder);
			holder.profile_image.setOnClickListener(this);
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
			case R.id.profile_image: {
				if (mContext instanceof Activity) {
					final long account_id = getAccountId(position);
					final long user_id = getConversationId(position);
					final String screen_name = getScreenName(position);
					openUserProfile((Activity) mContext, account_id, user_id, screen_name);
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
	public void setDisplayNameFirst(final boolean name_first) {

	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setLinkHighlightOption(final String option) {

	}

	@Override
	public void setMaxAnimationPosition(final int position) {
		mMaxAnimationPosition = position;
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
