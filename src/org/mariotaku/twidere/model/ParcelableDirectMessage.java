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

package org.mariotaku.twidere.model;

import static org.mariotaku.twidere.util.Utils.parseURL;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;

import twitter4j.DirectMessage;
import twitter4j.User;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableDirectMessage implements Parcelable {

	public static final Parcelable.Creator<ParcelableDirectMessage> CREATOR = new Parcelable.Creator<ParcelableDirectMessage>() {
		@Override
		public ParcelableDirectMessage createFromParcel(final Parcel in) {
			return new ParcelableDirectMessage(in);
		}

		@Override
		public ParcelableDirectMessage[] newArray(final int size) {
			return new ParcelableDirectMessage[size];
		}
	};

	public static final Comparator<ParcelableDirectMessage> MESSAGE_ID_COMPARATOR = new Comparator<ParcelableDirectMessage>() {

		@Override
		public int compare(final ParcelableDirectMessage object1, final ParcelableDirectMessage object2) {
			final long diff = object2.message_id - object1.message_id;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public final long account_id, message_id, message_timestamp;
	public final long sender_id, recipient_id;

	public final boolean is_gap;

	public final String text;
	public final String sender_name, recipient_name, sender_screen_name, recipient_screen_name;

	public final URL sender_profile_image_url, recipient_profile_image_url;

	public ParcelableDirectMessage(final Cursor cursor, final DirectMessageCursorIndices indices) {
		account_id = indices.account_id != -1 ? cursor.getLong(indices.account_id) : -1;
		is_gap = indices.is_gap != -1 ? cursor.getShort(indices.is_gap) == 1 : null;
		message_id = indices.message_id != -1 ? cursor.getLong(indices.message_id) : -1;
		message_timestamp = indices.message_timestamp != -1 ? cursor.getLong(indices.message_timestamp) : -1;
		sender_id = indices.sender_id != -1 ? cursor.getLong(indices.sender_id) : -1;
		recipient_id = indices.recipient_id != -1 ? cursor.getLong(indices.recipient_id) : -1;
		text = indices.text != -1 ? cursor.getString(indices.text) : null;
		sender_name = indices.sender_name != -1 ? cursor.getString(indices.sender_name) : null;
		recipient_name = indices.recipient_name != -1 ? cursor.getString(indices.recipient_name) : null;
		sender_screen_name = indices.sender_screen_name != -1 ? cursor.getString(indices.sender_screen_name) : null;
		recipient_screen_name = indices.recipient_screen_name != -1 ? cursor.getString(indices.recipient_screen_name)
				: null;
		sender_profile_image_url = indices.sender_profile_image_url != -1 ? parseURL(cursor
				.getString(indices.sender_profile_image_url)) : null;
		recipient_profile_image_url = indices.recipient_profile_image_url != -1 ? parseURL(cursor
				.getString(indices.recipient_profile_image_url)) : null;
	}

	public ParcelableDirectMessage(final DirectMessage message, final long account_id, final boolean is_gap) {
		this.account_id = account_id;
		this.is_gap = is_gap;
		final User sender = message.getSender(), recipient = message.getRecipient();
		message_id = message.getId();
		message_timestamp = getTime(message.getCreatedAt());
		sender_id = sender != null ? sender.getId() : -1;
		recipient_id = recipient != null ? recipient.getId() : -1;
		text = message.getText();
		sender_name = sender != null ? sender.getName() : null;
		recipient_name = recipient != null ? recipient.getName() : null;
		sender_screen_name = sender != null ? sender.getScreenName() : null;
		recipient_screen_name = recipient != null ? recipient.getScreenName() : null;
		sender_profile_image_url = sender != null ? sender.getProfileImageURL() : null;
		recipient_profile_image_url = recipient != null ? recipient.getProfileImageURL() : null;
	}

	public ParcelableDirectMessage(final Parcel in) {
		account_id = in.readLong();
		message_id = in.readLong();
		message_timestamp = in.readLong();
		sender_id = in.readLong();
		recipient_id = in.readLong();
		is_gap = in.readInt() == 1;
		text = in.readString();
		sender_name = in.readString();
		recipient_name = in.readString();
		sender_screen_name = in.readString();
		recipient_screen_name = in.readString();
		sender_profile_image_url = (URL) in.readSerializable();
		recipient_profile_image_url = (URL) in.readSerializable();
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public String toString() {
		return text;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeLong(account_id);
		out.writeLong(message_id);
		out.writeLong(message_timestamp);
		out.writeLong(sender_id);
		out.writeLong(recipient_id);
		out.writeInt(is_gap ? 1 : 0);
		out.writeString(text);
		out.writeString(sender_name);
		out.writeString(recipient_name);
		out.writeString(sender_screen_name);
		out.writeString(recipient_screen_name);
		out.writeSerializable(sender_profile_image_url);
		out.writeSerializable(recipient_profile_image_url);
	}

	private long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}
}
