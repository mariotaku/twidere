/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter;

import static org.mariotaku.twidere.util.Utils.configBaseCardAdapter;
import static org.mariotaku.twidere.util.Utils.findDirectMessageInDatabases;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.openUserProfile;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.iface.IDirectMessagesAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.MultiSelectManager;
import org.mariotaku.twidere.view.holder.DirectMessageConversationViewHolder;

public class DirectMessagesConversationAdapter extends BaseCursorAdapter implements IDirectMessagesAdapter,
		OnClickListener {

	private final ImageLoaderWrapper mImageLoader;
	private final Context mContext;
	private final MultiSelectManager mMultiSelectManager;

	private MenuButtonClickListener mListener;

	private boolean mAnimationEnabled = true;
	private int mMaxAnimationPosition;

	private ParcelableDirectMessage.CursorIndices mIndices;

	public DirectMessagesConversationAdapter(final Context context) {
		super(context, R.layout.card_item_message_conversation, null, new String[0], new int[0], 0);
		mContext = context;
		final TwidereApplication app = TwidereApplication.getInstance(context);
		mMultiSelectManager = app.getMultiSelectManager();
		mImageLoader = app.getImageLoaderWrapper();
		configBaseCardAdapter(context, this);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final int position = cursor.getPosition();
		final DirectMessageConversationViewHolder holder = (DirectMessageConversationViewHolder) view.getTag();
		final boolean displayProfileImage = isDisplayProfileImage();
		final long accountId = cursor.getLong(mIndices.account_id);
		final long timestamp = cursor.getLong(mIndices.message_timestamp);
		final boolean is_outgoing = cursor.getInt(mIndices.is_outgoing) == 1;

		// Clear images in prder to prevent images in recycled view shown.
		holder.incoming_profile_image.setImageDrawable(null);
		holder.outgoing_profile_image.setImageDrawable(null);

		holder.incoming_message_container.setVisibility(is_outgoing ? View.GONE : View.VISIBLE);
		holder.outgoing_message_container.setVisibility(is_outgoing ? View.VISIBLE : View.GONE);
		holder.setTextSize(getTextSize());
		holder.incoming_text.setText(Html.fromHtml(cursor.getString(mIndices.text)));
		holder.outgoing_text.setText(Html.fromHtml(cursor.getString(mIndices.text)));
		getLinkify().applyAllLinks(holder.incoming_text, accountId, false);
		getLinkify().applyAllLinks(holder.outgoing_text, accountId, false);
		holder.incoming_text.setMovementMethod(null);
		holder.outgoing_text.setMovementMethod(null);
		holder.incoming_time.setText(formatToLongTimeString(mContext, timestamp));
		holder.outgoing_time.setText(formatToLongTimeString(mContext, timestamp));
		holder.incoming_profile_image_container.setVisibility(displayProfileImage ? View.VISIBLE : View.GONE);
		holder.outgoing_profile_image_container.setVisibility(displayProfileImage ? View.VISIBLE : View.GONE);
		if (displayProfileImage) {
			final String profile_image_url_string = cursor.getString(mIndices.sender_profile_image_url);
			mImageLoader.displayProfileImage(holder.incoming_profile_image, profile_image_url_string);
			mImageLoader.displayProfileImage(holder.outgoing_profile_image, profile_image_url_string);
			holder.incoming_profile_image.setTag(position);
			holder.outgoing_profile_image.setTag(position);
		}
		if (position > mMaxAnimationPosition) {
			if (mAnimationEnabled) {
				view.startAnimation(holder.item_animation);
			}
			mMaxAnimationPosition = position;
		}
		holder.incoming_item_menu.setTag(position);
		holder.outgoing_item_menu.setTag(position);
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
		final Cursor c = getCursor();
		if (c == null || c.isClosed()) return null;
		c.moveToPosition(position);
		final long account_id = c.getLong(mIndices.account_id);
		final long message_id = c.getLong(mIndices.message_id);
		return findDirectMessageInDatabases(mContext, account_id, message_id);
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
			holder.incoming_item_menu.setOnClickListener(this);
			holder.outgoing_item_menu.setOnClickListener(this);
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
			case R.id.incoming_profile_image:
			case R.id.outgoing_profile_image: {
				final ParcelableDirectMessage message = getDirectMessage(position);
				if (message == null) return;
				if (mContext instanceof Activity) {
					openUserProfile((Activity) mContext, message.account_id, message.sender_id,
							message.sender_screen_name);
				}
				break;
			}
			case R.id.incoming_item_menu:
			case R.id.outgoing_item_menu: {
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
	public void setMaxAnimationPosition(final int position) {
		mMaxAnimationPosition = position;
	}

	@Override
	public void setMenuButtonClickListener(final MenuButtonClickListener listener) {
		mListener = listener;
	}

	@Override
	public Cursor swapCursor(final Cursor cursor) {
		if (cursor != null) {
			mIndices = new ParcelableDirectMessage.CursorIndices(cursor);
		} else {
			mIndices = null;
		}
		return super.swapCursor(cursor);
	}
}
