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

import static org.mariotaku.twidere.util.Utils.findDirectMessageInDatabases;
import static org.mariotaku.twidere.util.Utils.formatToLongTimeString;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.DirectMessageConversationViewHolder;
import org.mariotaku.twidere.model.DirectMessageCursorIndices;
import org.mariotaku.twidere.model.ParcelableDirectMessage;
import org.mariotaku.twidere.util.DirectMessagesAdapterInterface;
import org.mariotaku.twidere.util.LazyImageLoader;
import org.mariotaku.twidere.util.OnLinkClickHandler;
import org.mariotaku.twidere.util.TwidereLinkify;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class DirectMessagesConversationAdapter extends SimpleCursorAdapter implements DirectMessagesAdapterInterface {

	private boolean mDisplayProfileImage;
	private final LazyImageLoader mImageLoader;
	private float mTextSize;
	private final Context mContext;
	private DirectMessageCursorIndices mIndices;
	private final boolean mDisplayHiResProfileImage;

	public DirectMessagesConversationAdapter(final Context context, final LazyImageLoader loader) {
		super(context, R.layout.direct_message_list_item, null, new String[0], new int[0], 0);
		mContext = context;
		mImageLoader = loader;
		mDisplayHiResProfileImage = context.getResources().getBoolean(R.bool.hires_profile_image);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {
		final DirectMessageConversationViewHolder holder = (DirectMessageConversationViewHolder) view.getTag();

		final long account_id = cursor.getLong(mIndices.account_id);
		final long message_timestamp = cursor.getLong(mIndices.message_timestamp);
		final boolean is_outgoing = cursor.getInt(mIndices.is_outgoing) == 1;
		final String name = cursor.getString(mIndices.sender_name);
		final String screen_name = cursor.getString(mIndices.sender_screen_name);

		holder.setTextSize(mTextSize);
		holder.name.setText(name);
		holder.screen_name.setText("@" + screen_name);
		final FrameLayout.LayoutParams lp = (LayoutParams) holder.name_container.getLayoutParams();
		lp.gravity = is_outgoing ? Gravity.LEFT : Gravity.RIGHT;
		holder.name_container.setLayoutParams(lp);
		holder.text.setText(Html.fromHtml(cursor.getString(mIndices.text)));
		final TwidereLinkify linkify = new TwidereLinkify(holder.text);
		linkify.setOnLinkClickListener(new OnLinkClickHandler(context, account_id));
		linkify.addAllLinks();
		holder.text.setGravity(is_outgoing ? Gravity.LEFT : Gravity.RIGHT);
		holder.time.setText(formatToLongTimeString(mContext, message_timestamp));
		holder.time.setGravity(is_outgoing ? Gravity.RIGHT : Gravity.LEFT);
		holder.profile_image_left.setVisibility(mDisplayProfileImage && is_outgoing ? View.VISIBLE : View.GONE);
		holder.profile_image_right.setVisibility(mDisplayProfileImage && !is_outgoing ? View.VISIBLE : View.GONE);
		if (mDisplayProfileImage) {
			final String sender_profile_image_url_string = cursor.getString(mIndices.sender_profile_image_url);
			final URL sender_profile_image_url = parseURL(mDisplayHiResProfileImage ? getBiggerTwitterProfileImage(sender_profile_image_url_string)
					: sender_profile_image_url_string);

			mImageLoader.displayImage(sender_profile_image_url, holder.profile_image_left);
			mImageLoader.displayImage(sender_profile_image_url, holder.profile_image_right);
		}

		super.bindView(view, context, cursor);
	}

	@Override
	public ParcelableDirectMessage findItem(final long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) {
				final Cursor item = getItem(i);
				final long account_id = item.getLong(mIndices.account_id);
				final long message_id = item.getLong(mIndices.message_id);
				return findDirectMessageInDatabases(mContext, account_id, message_id);
			}
		}
		return null;
	}

	public long findItemIdByPosition(final int position) {
		if (position >= 0 && position < getCount()) return getItem(position).getLong(mIndices.message_id);
		return -1;
	}

	public int findItemPositionByStatusId(final long status_id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItem(i).getLong(mIndices.message_id) == status_id) return i;
		}
		return -1;
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
			view.setTag(new DirectMessageConversationViewHolder(view));
		}
		return view;
	}

	@Override
	public void setDisplayProfileImage(final boolean display) {
		if (display != mDisplayProfileImage) {
			mDisplayProfileImage = display;
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
			mIndices = new DirectMessageCursorIndices(cursor);
		} else {
			mIndices = null;
		}
		return super.swapCursor(cursor);
	}
}
