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

import java.io.Serializable;
import java.util.Date;

import org.mariotaku.twidere.provider.TweetStore.CachedUsers;

import twitter4j.User;
import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableUser implements Parcelable, Serializable, Comparable<ParcelableUser> {

	private static final long serialVersionUID = 5977877636776748705L;

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

	public final boolean is_protected, is_verified, is_follow_request_sent;

	public final String description, name, screen_name, location, profile_image_url_string, profile_banner_url_string,
			url_string;

	public final int followers_count, friends_count, statuses_count, favorites_count;

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
		profile_banner_url_string = in.readString();
		url_string = in.readString();
		is_follow_request_sent = in.readInt() == 1;
		followers_count = in.readInt();
		friends_count = in.readInt();
		statuses_count = in.readInt();
		favorites_count = in.readInt();
	}

	public ParcelableUser(final User user, final long account_id, boolean large_profile_image) {
		this(user, account_id, 0, large_profile_image);
	}

	public ParcelableUser(final User user, final long account_id, final long position, boolean large_profile_image) {
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
		profile_image_url_string = parseString(user.getProfileImageUrlHttps());
		profile_banner_url_string = user.getProfileBannerImageUrl();
		url_string = parseString(user.getURL());
		is_follow_request_sent = user.isFollowRequestSent();
		followers_count = user.getFollowersCount();
		friends_count = user.getFriendsCount();
		statuses_count = user.getStatusesCount();
		favorites_count = user.getFavouritesCount();
	}

	@Override
	public int compareTo(final ParcelableUser that) {
		final long diff = that != null ? position - that.position : position;
		if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
		return (int) diff;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (!(obj instanceof ParcelableUser)) return false;
		final ParcelableUser other = (ParcelableUser) obj;
		if (account_id != other.account_id) return false;
		if (user_id != other.user_id) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (account_id ^ account_id >>> 32);
		result = prime * result + (int) (user_id ^ user_id >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "ParcelableUser{account_id=" + account_id + ", user_id=" + user_id + ", created_at=" + created_at
				+ ", position=" + position + ", is_protected=" + is_protected + ", is_verified=" + is_verified
				+ ", is_follow_request_sent=" + is_follow_request_sent + ", description=" + description + ", name="
				+ name + ", screen_name=" + screen_name + ", location=" + location + ", profile_image_url_string="
				+ profile_image_url_string + ", profile_banner_url_string=" + profile_banner_url_string
				+ ", url_string=" + url_string + ", followers_count=" + followers_count + ", friends_count="
				+ friends_count + ", statuses_count=" + statuses_count + ", favorites_count=" + favorites_count + "}";
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
		out.writeString(profile_banner_url_string);
		out.writeString(url_string);
		out.writeInt(is_follow_request_sent ? 1 : 0);
		out.writeInt(followers_count);
		out.writeInt(friends_count);
		out.writeInt(statuses_count);
		out.writeInt(favorites_count);
	}

	private long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}

	public static ContentValues makeCachedUserContentValues(final ParcelableUser user) {
		if (user == null) return null;
		final ContentValues values = new ContentValues();
		values.put(CachedUsers.USER_ID, user.user_id);
		values.put(CachedUsers.NAME, user.name);
		values.put(CachedUsers.SCREEN_NAME, user.screen_name);
		values.put(CachedUsers.PROFILE_IMAGE_URL, user.profile_banner_url_string);
		return values;
	}
}
