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

import java.net.URL;
import java.util.Comparator;

import twitter4j.User;
import twitter4j.UserList;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableUserList implements Parcelable {

	public static final Parcelable.Creator<ParcelableUserList> CREATOR = new Parcelable.Creator<ParcelableUserList>() {
		@Override
		public ParcelableUserList createFromParcel(Parcel in) {
			return new ParcelableUserList(in);
		}

		@Override
		public ParcelableUserList[] newArray(int size) {
			return new ParcelableUserList[size];
		}
	};

	public final int list_id;

	public final long account_id, user_id, position;

	public final boolean is_public, is_following;

	public final String description, name, user_screen_name, user_name;

	public URL user_profile_image_url;

	public static final Comparator<ParcelableUserList> POSITION_COMPARATOR = new Comparator<ParcelableUserList>() {

		@Override
		public int compare(ParcelableUserList object1, ParcelableUserList object2) {
			final long diff = object1.position - object2.position;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public ParcelableUserList(Parcel in) {
		position = in.readLong();
		account_id = in.readLong();
		list_id = in.readInt();
		is_public = in.readInt() == 1;
		is_following = in.readInt() == 1;
		name = in.readString();
		description = in.readString();
		user_id = in.readLong();
		user_name = in.readString();
		user_screen_name = in.readString();
		user_profile_image_url = (URL) in.readSerializable();
	}

	public ParcelableUserList(UserList user, long account_id) {
		this(user, account_id, 0);
	}

	public ParcelableUserList(UserList list, long account_id, long position) {
		final User user = list.getUser();
		this.position = position;
		this.account_id = account_id;
		list_id = list.getId();
		is_public = list.isPublic();
		is_following = list.isFollowing();
		name = list.getName();
		description = list.getDescription();
		user_id = user.getId();
		user_name = user.getName();
		user_screen_name = user.getScreenName();
		user_profile_image_url = user.getProfileImageURL();
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(position);
		out.writeLong(account_id);
		out.writeInt(list_id);
		out.writeInt(is_public ? 1 : 0);
		out.writeInt(is_following ? 1 : 0);
		out.writeString(name);
		out.writeString(description);
		out.writeLong(user_id);
		out.writeString(user_name);
		out.writeString(user_screen_name);
		out.writeSerializable(user_profile_image_url);

	}

}
