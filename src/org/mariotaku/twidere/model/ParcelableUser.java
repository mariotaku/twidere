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

import static org.mariotaku.twidere.util.Utils.parseString;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;

import twitter4j.User;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableUser implements Parcelable {

	public static final Parcelable.Creator<ParcelableUser> CREATOR = new Parcelable.Creator<ParcelableUser>() {
		@Override
		public ParcelableUser createFromParcel(final Parcel in) {
			return new ParcelableUser(in);
		}

		@Override
		public ParcelableUser[] newArray(final int size) {
			return new ParcelableUser[size];
		}
	};

	public final long account_id, user_id, created_at, position;

	public final boolean is_protected, is_verified;

	public final String description, name, screen_name, location, profile_image_url_string;

	public URL profile_image_url;

	public static final Comparator<ParcelableUser> POSITION_COMPARATOR = new Comparator<ParcelableUser>() {

		@Override
		public int compare(final ParcelableUser object1, final ParcelableUser object2) {
			final long diff = object1.position - object2.position;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public ParcelableUser(final Parcel in) {
		position = in.readLong();
		account_id = in.readLong();
		user_id = in.readLong();
		created_at = in.readLong();
		is_protected = in.readInt() == 1;
		is_verified = in.readInt() == 1;
		name = in.readString();
		screen_name = in.readString();
		description = in.readString();
		location = in.readString();
		profile_image_url_string = in.readString();
		profile_image_url = parseURL(profile_image_url_string);
	}

	public ParcelableUser(final User user, final long account_id) {
		this(user, account_id, 0);
	}

	public ParcelableUser(final User user, final long account_id, final long position) {
		this.position = position;
		this.account_id = account_id;
		user_id = user.getId();
		created_at = getTime(user.getCreatedAt());
		is_protected = user.isProtected();
		is_verified = user.isVerified();
		name = user.getName();
		screen_name = user.getScreenName();
		description = user.getDescription();
		location = user.getLocation();
		profile_image_url = user.getProfileImageURL();
		profile_image_url_string = parseString(profile_image_url);
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		if (!(o instanceof ParcelableUser)) return false;
		final ParcelableUser that = (ParcelableUser) o;
		return user_id == that.user_id;
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeLong(position);
		out.writeLong(account_id);
		out.writeLong(user_id);
		out.writeLong(created_at);
		out.writeInt(is_protected ? 1 : 0);
		out.writeInt(is_verified ? 1 : 0);
		out.writeString(name);
		out.writeString(screen_name);
		out.writeString(description);
		out.writeString(location);
		out.writeString(profile_image_url_string);
	}

	private long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}
}
