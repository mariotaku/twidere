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

import static org.mariotaku.twidere.util.Utils.configBaseAdapter;
import static org.mariotaku.twidere.util.Utils.findDirectMessageInDatabases;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IDirectMessagesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.DirectMessageCursorIndices;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.util.OnDirectMessageLinkClickHandler;
import org.mariotaku.twidere.util.TwidereLinkify;
import org.mariotaku.twidere.view.holder.DirectMessageConversationViewHolder;

public class DirectMessagesConversationAdapter extends SimpleCursorAdapter implements IDirectMessagesAdapter,
		OnClickListener {

	private final ImageLoaderWrapper mImageLoader;
	private final Context mContext;
	private final TwidereLinkify mLinkify;
	private final MultiSelectManager mMultiSelectManager;

	private DirectMessageCursorIndices mIndices;
	private boolean mDisplayProfileImage;
	private float mTextSize;

	private MenuButtonClickListener mListener;

	public DirectMessagesConversationAdapter(final Context context) {
		super(context, R.layout.direct_messages_conversation_list_item, null, new String[0], new int[0], 0);
		mContext = context;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mMultiSelectManager = app.getMultiSelectManager();
		mImageLoader = app.getImageLoaderWrapper();
		mLinkify = new TwidereLinkify(new OnDirectMessageLinkClickHandler(context));
		configBaseAdapter(context, this);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int position = cursor.getPosition();
		final DirectMessageConversationViewHolder holder = (DirectMessageConversationViewHolder) view.getTag();

		final long account_id = cursor.getLong(mIndices.account_id);
		final long message_timestamp = cursor.getLong(mIndices.message_timestamp);
		final boolean is_outgoing = cursor.getInt(mIndices.is_outgoing) == 1;
		holder.incoming_message_container.setVisibility(is_outgoing ? View.GONE : View.VISIBLE);
		holder.outgoing_message_container.setVisibility(is_outgoing ? View.VISIBLE : View.GONE);
		holder.setTextSize(mTextSize);
		holder.incoming_text.setText(Html.fromHtml(cursor.getString(mIndices.text)));
		holder.outgoing_text.setText(Html.fromHtml(cursor.getString(mIndices.text)));
		mLinkify.applyAllLinks(holder.incoming_text, account_id, false);
		mLinkify.applyAllLinks(holder.outgoing_text, account_id, false);
		holder.incoming_text.setMovementMethod(null);
		holder.outgoing_text.setMovementMethod(null);
		holder.incoming_time.setText(formatToLongTimeString(mContext, message_timestamp));
		holder.outgoing_time.setText(formatToLongTimeString(mContext, message_timestamp));
		holder.incoming_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		holder.outgoing_profile_image_container.setVisibility(mDisplayProfileImage ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			final String profile_image_url_string = cursor.getString(mIndices.sender_profile_image_url);
			mImageLoader.displayProfileImage(holder.incoming_profile_image, profile_image_url_string);
			mImageLoader.displayProfileImage(holder.outgoing_profile_image, profile_image_url_string);
			holder.incoming_profile_image.setTag(position);
			holder.outgoing_profile_image.setTag(position);
		}

		super.bindView(view, context, cursor);
	}

	@Override
	public ParcelableDirectMessage findItem(final long id) {
		for (int i = 0, count = getCount(); i < count; i++) {
			if (getItemId(i) == id) return getDirectMessage(i);
		}
		return null;
	}

	public ParcelableDirectMessage getDirectMessage(final int position) {
		final Cursor item = getItem(position);
		final long account_id = item.getLong(mIndices.account_id);
		final long message_id = item.getLong(mIndices.message_id);
		return findDirectMessageInDatabases(mContext, account_id, message_id);
	}

	@Override
	public Cursor getItem(final int position) {
		return (Cursor) super.getItem(position);
	}

	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		final View view = super.newView(context, cursor, parent);
		final Object tag = view.getTag();
		if (!(tag instanceof DirectMessageConversationViewHolder)) {
			final DirectMessageConversationViewHolder holder = new DirectMessageConversationViewHolder(view);
			view.setTag(holder);
			holder.incoming_profile_image.setOnClickListener(this);
			holder.outgoing_profile_image.setOnClickListener(this);
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
			case R.id.profile_image:
			case R.id.my_profile_image: {
				final ParcelableDirectMessage message = getDirectMessage(position);
				if (message == null) return;
				if (mContext instanceof Activity) {
					openUserProfile((Activity) mContext, message.account_id, message.sender_id,
							message.sender_screen_name);
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
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
			notifyDataSetChanged();
		}
	}

	@Override
	public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
		mListener = listener;
	}

	@Override
	public void setNameDisplayOption(final String option) {
	}

	@Override
	public void setNicknameOnly(final boolean nickname_only) {

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
			mIndices = new DirectMessageCursorIndices(cursor);
		} else {
			mIndices = null;
		}
		return super.swapCursor(cursor);
	}
}
