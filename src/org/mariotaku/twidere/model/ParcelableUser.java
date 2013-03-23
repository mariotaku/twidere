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

import static org.mariotaku.twidere.util.HtmlEscapeHelper.toPlainText;
import static org.mariotaku.twidere.util.Utils.formatUserDescription;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.parseString;
import static org.mariotaku.twidere.util.Utils.*;

import java.io.Serializable;
import java.util.Date;

import org.mariotaku.twidere.provider.TweetStore.CachedUsers;

import twitter4j.URLEntity;
import twitter4j.User;
import android.content.ContentValues;
import android.database.Cursor;
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

	public final boolean is_protected, is_verified, is_follow_request_sent, is_following;

	public final String description_plain, name, screen_name, location, profile_image_url, profile_banner_url, url,
			url_expanded, description_html, description_unescaped, description_expanded;

	public final int followers_count, friends_count, statuses_count, favorites_count;

	public final boolean is_cache;

	public ParcelableUser(final Cursor cursor, final long account_id) {
		this.account_id = account_id;
		position = -1;
		is_follow_request_sent = false;
		user_id = cursor.getLong(cursor.getColumnIndex(CachedUsers.USER_ID));
		name = cursor.getString(cursor.getColumnIndex(CachedUsers.NAME));
		screen_name = cursor.getString(cursor.getColumnIndex(CachedUsers.SCREEN_NAME));
		profile_image_url = cursor.getString(cursor.getColumnIndex(CachedUsers.PROFILE_IMAGE_URL));
		created_at = cursor.getLong(cursor.getColumnIndex(CachedUsers.CREATED_AT));
		is_protected = cursor.getInt(cursor.getColumnIndex(CachedUsers.IS_PROTECTED)) == 1;
		is_verified = cursor.getInt(cursor.getColumnIndex(CachedUsers.IS_VERIFIED)) == 1;
		favorites_count = cursor.getInt(cursor.getColumnIndex(CachedUsers.FAVORITES_COUNT));
		followers_count = cursor.getInt(cursor.getColumnIndex(CachedUsers.FOLLOWERS_COUNT));
		friends_count = cursor.getInt(cursor.getColumnIndex(CachedUsers.FRIENDS_COUNT));
		statuses_count = cursor.getInt(cursor.getColumnIndex(CachedUsers.STATUSES_COUNT));
		location = cursor.getString(cursor.getColumnIndex(CachedUsers.LOCATION));
		description_plain = cursor.getString(cursor.getColumnIndex(CachedUsers.DESCRIPTION_PLAIN));
		description_html = cursor.getString(cursor.getColumnIndex(CachedUsers.DESCRIPTION_HTML));
		description_expanded = cursor.getString(cursor.getColumnIndex(CachedUsers.DESCRIPTION_EXPANDED));
		url = cursor.getString(cursor.getColumnIndex(CachedUsers.URL));
		url_expanded = cursor.getString(cursor.getColumnIndex(CachedUsers.URL_EXPANDED));
		profile_banner_url = cursor.getString(cursor.getColumnIndex(CachedUsers.PROFILE_BANNER_URL));
		is_cache = true;
		description_unescaped = toPlainText(description_html);
		is_following = cursor.getInt(cursor.getColumnIndex(CachedUsers.IS_FOLLOWING)) == 1;
	}

	public ParcelableUser(final Parcel in) {
		position = in.readLong();
		account_id = in.readLong();
		user_id = in.readLong();
		created_at = in.readLong();
		is_protected = in.readInt() == 1;
		is_verified = in.readInt() == 1;
		name = in.readString();
		screen_name = in.readString();
		description_plain = in.readString();
		location = in.readString();
		profile_image_url = in.readString();
		profile_banner_url = in.readString();
		url = in.readString();
		is_follow_request_sent = in.readInt() == 1;
		followers_count = in.readInt();
		friends_count = in.readInt();
		statuses_count = in.readInt();
		favorites_count = in.readInt();
		is_cache = in.readInt() == 1;
		description_html = in.readString();
		description_expanded = in.readString();
		url_expanded = in.readString();
		is_following = in.readInt() == 1;
		description_unescaped = toPlainText(description_html);
	}

	public ParcelableUser(final User user, final long account_id, final boolean large_profile_image) {
		this(user, account_id, 0, large_profile_image);
	}

	public ParcelableUser(final User user, final long account_id, final long position, final boolean large_profile_image) {
		this.position = position;
		this.account_id = account_id;
		final String profile_image_url_orig = parseString(user.getProfileImageUrlHttps());
		final URLEntity[] urls_url_entities = user.getURLsURLEntities();
		user_id = user.getId();
		created_at = getTime(user.getCreatedAt());
		is_protected = user.isProtected();
		is_verified = user.isVerified();
		name = user.getName();
		screen_name = user.getScreenName();
		description_plain = user.getDescription();
		description_html = formatUserDescription(user);
		description_expanded = formatExpandedUserDescription(user);
		location = user.getLocation();
		profile_image_url = large_profile_image ? getBiggerTwitterProfileImage(profile_image_url_orig)
				: profile_image_url_orig;
		profile_banner_url = user.getProfileBannerImageUrl();
		url = parseString(user.getURL());
		url_expanded = url != null && urls_url_entities != null && urls_url_entities.length > 0 ? parseString(urls_url_entities[0]
				.getExpandedURL()) : null;
		is_follow_request_sent = user.isFollowRequestSent();
		followers_count = user.getFollowersCount();
		friends_count = user.getFriendsCount();
		statuses_count = user.getStatusesCount();
		favorites_count = user.getFavouritesCount();
		is_cache = false;
		// TODO: implememt this in twitter4j lib
		// is_following = user.isFollowing();
		is_following = false;
		description_unescaped = toPlainText(description_html);
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
				+ ", is_follow_request_sent=" + is_follow_request_sent + ", description=" + description_plain
				+ ", name=" + name + ", screen_name=" + screen_name + ", location=" + location
				+ ", profile_image_url_string=" + profile_image_url + ", profile_banner_url_string="
				+ profile_banner_url + ", url_string=" + url + ", followers_count=" + followers_count
				+ ", friends_count=" + friends_count + ", statuses_count=" + statuses_count + ", favorites_count="
				+ favorites_count + "}";
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
		out.writeString(description_plain);
		out.writeString(location);
		out.writeString(profile_image_url);
		out.writeString(profile_banner_url);
		out.writeString(url);
		out.writeInt(is_follow_request_sent ? 1 : 0);
		out.writeInt(followers_count);
		out.writeInt(friends_count);
		out.writeInt(statuses_count);
		out.writeInt(favorites_count);
		out.writeInt(is_cache ? 1 : 0);
		out.writeString(description_html);
		out.writeString(description_expanded);
		out.writeString(url_expanded);
		out.writeInt(is_following ? 1 : 0);
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
		values.put(CachedUsers.PROFILE_IMAGE_URL, user.profile_image_url);
		values.put(CachedUsers.CREATED_AT, user.created_at);
		values.put(CachedUsers.IS_PROTECTED, user.is_protected);
		values.put(CachedUsers.IS_VERIFIED, user.is_verified);
		values.put(CachedUsers.FAVORITES_COUNT, user.favorites_count);
		values.put(CachedUsers.FOLLOWERS_COUNT, user.followers_count);
		values.put(CachedUsers.FRIENDS_COUNT, user.friends_count);
		values.put(CachedUsers.STATUSES_COUNT, user.statuses_count);
		values.put(CachedUsers.LOCATION, user.location);
		values.put(CachedUsers.DESCRIPTION_PLAIN, user.description_plain);
		values.put(CachedUsers.DESCRIPTION_HTML, user.description_html);
		values.put(CachedUsers.DESCRIPTION_EXPANDED, user.description_expanded);
		values.put(CachedUsers.URL, user.url);
		values.put(CachedUsers.URL_EXPANDED, user.url_expanded);
		values.put(CachedUsers.PROFILE_BANNER_URL, user.profile_banner_url);
		values.put(CachedUsers.IS_FOLLOWING, user.is_following);
		return values;
	}
}
