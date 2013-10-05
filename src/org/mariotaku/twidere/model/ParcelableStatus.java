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
import static org.mariotaku.twidere.util.Utils.formatStatusText;
import static org.mariotaku.twidere.util.Utils.getAsBoolean;
import static org.mariotaku.twidere.util.Utils.getAsInteger;
import static org.mariotaku.twidere.util.Utils.getAsLong;
import static org.mariotaku.twidere.util.Utils.getBiggerTwitterProfileImage;
import static org.mariotaku.twidere.util.Utils.getInReplyToName;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.mariotaku.jsonserializer.JSONParcel;
import org.mariotaku.jsonserializer.JSONParcelable;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.util.ParseUtils;

import twitter4j.Status;
import twitter4j.User;

import java.util.Comparator;
import java.util.Date;

public class ParcelableStatus implements Parcelable, JSONParcelable, Comparable<ParcelableStatus> {

	public static final Parcelable.Creator<ParcelableStatus> CREATOR = new Parcelable.Creator<ParcelableStatus>() {
		@Override
		public ParcelableStatus createFromParcel(final Parcel in) {
			return new ParcelableStatus(in);
		}

		@Override
		public ParcelableStatus[] newArray(final int size) {
			return new ParcelableStatus[size];
		}
	};

	public static final JSONParcelable.Creator<ParcelableStatus> JSON_CREATOR = new JSONParcelable.Creator<ParcelableStatus>() {
		@Override
		public ParcelableStatus createFromParcel(final JSONParcel in) {
			return new ParcelableStatus(in);
		}

		@Override
		public ParcelableStatus[] newArray(final int size) {
			return new ParcelableStatus[size];
		}
	};

	public final long retweet_id, retweeted_by_id, id, account_id, user_id, timestamp, retweet_count,
			in_reply_to_status_id, in_reply_to_user_id, my_retweet_id;

	public final boolean is_gap, is_retweet, is_favorite, has_media, is_possibly_sensitive, user_is_following,
			user_is_protected, user_is_verified;

	public final String retweeted_by_name, retweeted_by_screen_name, text_html, text_plain, user_name,
			user_screen_name, in_reply_to_name, in_reply_to_screen_name, source, user_profile_image_url,
			image_preview_url, image_original_url, text_unescaped;

	public final ParcelableLocation location;

	public static final Comparator<ParcelableStatus> TIMESTAMP_COMPARATOR = new Comparator<ParcelableStatus>() {

		@Override
		public int compare(final ParcelableStatus object1, final ParcelableStatus object2) {
			final long diff = object2.timestamp - object1.timestamp;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public static final Comparator<ParcelableStatus> REVERSE_ID_COMPARATOR = new Comparator<ParcelableStatus>() {

		@Override
		public int compare(final ParcelableStatus object1, final ParcelableStatus object2) {
			final long diff = object1.id - object2.id;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public ParcelableStatus(final ContentValues values) {
		account_id = getAsLong(values, Statuses.ACCOUNT_ID, -1);
		id = getAsLong(values, Statuses.STATUS_ID, -1);
		timestamp = getAsLong(values, Statuses.STATUS_TIMESTAMP, -1);
		user_name = values.getAsString(Statuses.NAME);
		user_screen_name = values.getAsString(Statuses.SCREEN_NAME);
		text_html = values.getAsString(Statuses.TEXT_HTML);
		text_plain = values.getAsString(Statuses.TEXT_PLAIN);
		user_profile_image_url = values.getAsString(Statuses.PROFILE_IMAGE_URL);
		is_favorite = getAsBoolean(values, Statuses.IS_FAVORITE, false);
		is_retweet = getAsBoolean(values, Statuses.IS_RETWEET, false);
		is_gap = getAsBoolean(values, Statuses.IS_GAP, false);
		location = ParcelableLocation.fromString(values.getAsString(Statuses.LOCATION));
		user_is_protected = getAsBoolean(values, Statuses.IS_PROTECTED, false);
		user_is_verified = getAsBoolean(values, Statuses.IS_VERIFIED, false);
		in_reply_to_status_id = getAsLong(values, Statuses.IN_REPLY_TO_STATUS_ID, -1);
		in_reply_to_user_id = getAsLong(values, Statuses.IN_REPLY_TO_USER_ID, -1);
		in_reply_to_name = values.getAsString(Statuses.IN_REPLY_TO_NAME);
		in_reply_to_screen_name = values.getAsString(Statuses.IN_REPLY_TO_SCREEN_NAME);
		my_retweet_id = getAsLong(values, Statuses.MY_RETWEET_ID, -1);
		retweeted_by_name = values.getAsString(Statuses.RETWEETED_BY_NAME);
		retweeted_by_screen_name = values.getAsString(Statuses.RETWEETED_BY_SCREEN_NAME);
		retweet_id = getAsLong(values, Statuses.RETWEET_ID, -1);
		retweeted_by_id = getAsLong(values, Statuses.RETWEETED_BY_USER_ID, -1);
		user_id = getAsLong(values, Statuses.USER_ID, -1);
		source = values.getAsString(Statuses.SOURCE);
		retweet_count = getAsInteger(values, Statuses.RETWEET_COUNT, 0);
		text_unescaped = values.getAsString(Statuses.TEXT_UNESCAPED);
		final PreviewImage preview = PreviewImage.getPreviewImage(text_html, true);
		has_media = preview != null;
		image_preview_url = preview != null ? preview.image_preview_url : null;
		image_original_url = preview != null ? preview.image_original_url : null;
		is_possibly_sensitive = getAsBoolean(values, Statuses.IS_POSSIBLY_SENSITIVE, false);
		user_is_following = getAsBoolean(values, Statuses.IS_FOLLOWING, false);
	}

	public ParcelableStatus(final Cursor cursor, final StatusCursorIndices indices) {
		retweet_id = indices.retweet_id != -1 ? cursor.getLong(indices.retweet_id) : -1;
		retweeted_by_id = indices.retweeted_by_user_id != -1 ? cursor.getLong(indices.retweeted_by_user_id) : -1;
		id = indices.status_id != -1 ? cursor.getLong(indices.status_id) : -1;
		account_id = indices.account_id != -1 ? cursor.getLong(indices.account_id) : -1;
		user_id = indices.user_id != -1 ? cursor.getLong(indices.user_id) : -1;
		timestamp = indices.status_timestamp != -1 ? cursor.getLong(indices.status_timestamp) : 0;
		retweet_count = indices.retweet_count != -1 ? cursor.getLong(indices.retweet_count) : -1;
		in_reply_to_status_id = indices.in_reply_to_status_id != -1 ? cursor.getLong(indices.in_reply_to_status_id)
				: -1;
		in_reply_to_user_id = indices.in_reply_to_user_id != -1 ? cursor.getLong(indices.in_reply_to_user_id) : -1;
		is_gap = indices.is_gap != -1 ? cursor.getInt(indices.is_gap) == 1 : false;
		is_retweet = indices.is_retweet != -1 ? cursor.getInt(indices.is_retweet) == 1 : false;
		is_favorite = indices.is_favorite != -1 ? cursor.getInt(indices.is_favorite) == 1 : false;
		user_is_protected = indices.is_protected != -1 ? cursor.getInt(indices.is_protected) == 1 : false;
		user_is_verified = indices.is_verified != -1 ? cursor.getInt(indices.is_verified) == 1 : false;
		retweeted_by_name = indices.retweeted_by_name != -1 ? cursor.getString(indices.retweeted_by_name) : null;
		retweeted_by_screen_name = indices.retweeted_by_screen_name != -1 ? cursor
				.getString(indices.retweeted_by_screen_name) : null;
		text_html = indices.text_html != -1 ? cursor.getString(indices.text_html) : null;
		final PreviewImage preview = PreviewImage.getPreviewImage(text_html, true);
		has_media = preview != null;
		image_preview_url = preview != null ? preview.image_preview_url : null;
		image_original_url = preview != null ? preview.image_original_url : null;
		text_plain = indices.text_plain != -1 ? cursor.getString(indices.text_plain) : null;
		user_name = indices.user_name != -1 ? cursor.getString(indices.user_name) : null;
		user_screen_name = indices.user_screen_name != -1 ? cursor.getString(indices.user_screen_name) : null;
		in_reply_to_name = indices.in_reply_to_name != -1 ? cursor.getString(indices.in_reply_to_name) : null;
		in_reply_to_screen_name = indices.in_reply_to_screen_name != -1 ? cursor
				.getString(indices.in_reply_to_screen_name) : null;
		source = indices.source != -1 ? cursor.getString(indices.source) : null;
		location = indices.location != -1 ? new ParcelableLocation(cursor.getString(indices.location)) : null;
		user_profile_image_url = indices.user_profile_image_url != -1 ? cursor
				.getString(indices.user_profile_image_url) : null;
		text_unescaped = indices.text_unescaped != -1 ? cursor.getString(indices.text_unescaped) : null;
		my_retweet_id = indices.my_retweet_id != -1 ? cursor.getLong(indices.my_retweet_id) : -1;
		is_possibly_sensitive = indices.is_possibly_sensitive != -1 ? cursor.getInt(indices.is_possibly_sensitive) == 1
				: false;
		user_is_following = indices.is_following != -1 ? cursor.getInt(indices.is_following) == 1 : false;
	}

	public ParcelableStatus(final JSONParcel in) {
		retweet_id = in.readLong("retweet_id");
		retweeted_by_id = in.readLong("retweeted_by_id");
		id = in.readLong("status_id");
		account_id = in.readLong("account_id");
		user_id = in.readLong("user_id");
		timestamp = in.readLong("status_timestamp");
		retweet_count = in.readLong("retweet_count");
		in_reply_to_status_id = in.readLong("in_reply_to_status_id");
		in_reply_to_user_id = in.readLong("in_reply_to_user_id");
		is_gap = in.readBoolean("is_gap");
		is_retweet = in.readBoolean("is_retweet");
		is_favorite = in.readBoolean("is_favorite");
		user_is_protected = in.readBoolean("is_protected");
		user_is_verified = in.readBoolean("is_verified");
		has_media = in.readBoolean("has_media");
		retweeted_by_name = in.readString("retweeted_by_name");
		retweeted_by_screen_name = in.readString("retweeted_by_screen_name");
		text_html = in.readString("text_html");
		text_plain = in.readString("text_plain");
		user_name = in.readString("name");
		user_screen_name = in.readString("scrren_name");
		in_reply_to_name = in.readString("in_reply_to_name");
		in_reply_to_screen_name = in.readString("in_reply_to_screen_name");
		source = in.readString("source");
		user_profile_image_url = in.readString("profile_image_url");
		image_preview_url = in.readString("image_preview_url");
		image_original_url = in.readString("image_orig_url");
		location = in.readParcelable("location", ParcelableLocation.JSON_CREATOR);
		my_retweet_id = in.readLong("my_retweet_id");
		is_possibly_sensitive = in.readBoolean("is_possibly_sensitive");
		text_unescaped = in.readString("text_unescaped");
		user_is_following = in.readBoolean("is_following");
	}

	public ParcelableStatus(final Parcel in) {
		retweet_id = in.readLong();
		retweeted_by_id = in.readLong();
		id = in.readLong();
		account_id = in.readLong();
		user_id = in.readLong();
		timestamp = in.readLong();
		retweet_count = in.readLong();
		in_reply_to_status_id = in.readLong();
		is_gap = in.readInt() == 1;
		is_retweet = in.readInt() == 1;
		is_favorite = in.readInt() == 1;
		user_is_protected = in.readInt() == 1;
		user_is_verified = in.readInt() == 1;
		has_media = in.readInt() == 1;
		retweeted_by_name = in.readString();
		retweeted_by_screen_name = in.readString();
		text_html = in.readString();
		text_plain = in.readString();
		user_name = in.readString();
		user_screen_name = in.readString();
		in_reply_to_screen_name = in.readString();
		source = in.readString();
		user_profile_image_url = in.readString();
		image_preview_url = in.readString();
		image_original_url = in.readString();
		location = in.readParcelable(ParcelableLocation.class.getClassLoader());
		my_retweet_id = in.readLong();
		is_possibly_sensitive = in.readInt() == 1;
		user_is_following = in.readInt() == 1;
		text_unescaped = in.readString();
		in_reply_to_user_id = in.readLong();
		in_reply_to_name = in.readString();
	}

	public ParcelableStatus(final Status orig, final long account_id, final boolean is_gap,
			final boolean large_profile_image) {
		this.is_gap = is_gap;
		this.account_id = account_id;
		id = orig.getId();
		is_retweet = orig.isRetweet();
		final Status retweeted_status = is_retweet ? orig.getRetweetedStatus() : null;
		final User retweet_user = retweeted_status != null ? orig.getUser() : null;
		retweet_id = retweeted_status != null ? retweeted_status.getId() : -1;
		retweeted_by_id = retweet_user != null ? retweet_user.getId() : -1;
		retweeted_by_name = retweet_user != null ? retweet_user.getName() : null;
		retweeted_by_screen_name = retweet_user != null ? retweet_user.getScreenName() : null;
		final Status status = retweeted_status != null ? retweeted_status : orig;
		final User user = status.getUser();
		user_id = user != null ? user.getId() : -1;
		user_name = user != null ? user.getName() : null;
		user_screen_name = user != null ? user.getScreenName() : null;
		final String profile_image_url_orig = user != null ? ParseUtils.parseString(user.getProfileImageUrlHttps())
				: null;
		user_profile_image_url = large_profile_image ? getBiggerTwitterProfileImage(profile_image_url_orig)
				: profile_image_url_orig;
		user_is_protected = user != null ? user.isProtected() : false;
		user_is_verified = user != null ? user.isVerified() : false;
		user_is_following = user != null ? user.isFollowing() : false;
		timestamp = getTime(status.getCreatedAt());
		text_html = formatStatusText(status);
		final PreviewImage preview = PreviewImage.getPreviewImage(text_html, true);
		has_media = preview != null;
		image_preview_url = preview != null ? preview.image_preview_url : null;
		image_original_url = preview != null ? preview.image_original_url : null;
		text_plain = status.getText();
		retweet_count = status.getRetweetCount();
		in_reply_to_name = getInReplyToName(status);
		in_reply_to_screen_name = status.getInReplyToScreenName();
		in_reply_to_status_id = status.getInReplyToStatusId();
		in_reply_to_user_id = status.getInReplyToUserId();
		source = status.getSource();
		location = new ParcelableLocation(status.getGeoLocation());
		is_favorite = status.isFavorited();
		text_unescaped = toPlainText(text_html);
		my_retweet_id = retweeted_by_id == account_id ? id : -1;
		is_possibly_sensitive = status.isPossiblySensitive();
	}

	@Override
	public int compareTo(final ParcelableStatus another) {
		if (another == null) return 0;
		final long diff = another.id - id;
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
		if (!(obj instanceof ParcelableStatus)) return false;
		final ParcelableStatus other = (ParcelableStatus) obj;
		if (account_id != other.account_id) return false;
		if (id != other.id) return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (account_id ^ account_id >>> 32);
		result = prime * result + (int) (id ^ id >>> 32);
		return result;
	}

	@Override
	public String toString() {
		return "ParcelableStatus{retweet_id=" + retweet_id + ", retweeted_by_id=" + retweeted_by_id + ", id=" + id
				+ ", account_id=" + account_id + ", user_id=" + user_id + ", timestamp=" + timestamp
				+ ", retweet_count=" + retweet_count + ", in_reply_to_status_id=" + in_reply_to_status_id
				+ ", in_reply_to_user_id=" + in_reply_to_user_id + ", my_retweet_id=" + my_retweet_id + ", is_gap="
				+ is_gap + ", is_retweet=" + is_retweet + ", is_favorite=" + is_favorite + ", has_media=" + has_media
				+ ", is_possibly_sensitive=" + is_possibly_sensitive + ", user_is_following=" + user_is_following
				+ ", user_is_protected=" + user_is_protected + ", user_is_verified=" + user_is_verified
				+ ", retweeted_by_name=" + retweeted_by_name + ", retweeted_by_screen_name=" + retweeted_by_screen_name
				+ ", text_html=" + text_html + ", text_plain=" + text_plain + ", user_name=" + user_name
				+ ", user_screen_name=" + user_screen_name + ", in_reply_to_name=" + in_reply_to_name
				+ ", in_reply_to_screen_name=" + in_reply_to_screen_name + ", source=" + source
				+ ", user_profile_image_url=" + user_profile_image_url + ", image_preview_url=" + image_preview_url
				+ ", image_original_url=" + image_original_url + ", text_unescaped=" + text_unescaped + ", location="
				+ location + "}";
	}

	@Override
	public void writeToParcel(final JSONParcel out) {
		out.writeLong("retweet_id", retweet_id);
		out.writeLong("retweeted_by_id", retweeted_by_id);
		out.writeLong("status_id", id);
		out.writeLong("account_id", account_id);
		out.writeLong("user_id", user_id);
		out.writeLong("status_timestamp", timestamp);
		out.writeLong("retweet_count", retweet_count);
		out.writeLong("in_reply_to_status_id", in_reply_to_status_id);
		out.writeLong("in_reply_to_user_id", in_reply_to_user_id);
		out.writeBoolean("is_gap", is_gap);
		out.writeBoolean("is_retweet", is_retweet);
		out.writeBoolean("is_favorite", is_favorite);
		out.writeBoolean("is_protected", user_is_protected);
		out.writeBoolean("is_verified", user_is_verified);
		out.writeBoolean("has_media", has_media);
		out.writeString("retweeted_by_name", retweeted_by_name);
		out.writeString("retweeted_by_screen_name", retweeted_by_screen_name);
		out.writeString("text_html", text_html);
		out.writeString("text_plain", text_plain);
		out.writeString("text_unescaped", text_unescaped);
		out.writeString("name", user_name);
		out.writeString("scrren_name", user_screen_name);
		out.writeString("in_reply_to_name", in_reply_to_name);
		out.writeString("in_reply_to_screen_name", in_reply_to_screen_name);
		out.writeString("source", source);
		out.writeString("profile_image_url", user_profile_image_url);
		out.writeString("image_preview_url", image_preview_url);
		out.writeString("image_orig_url", image_original_url);
		out.writeParcelable("location", location);
		out.writeLong("my_retweet_id", my_retweet_id);
		out.writeBoolean("is_possibly_sensitive", is_possibly_sensitive);
		out.writeBoolean("is_following", user_is_following);
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeLong(retweet_id);
		out.writeLong(retweeted_by_id);
		out.writeLong(id);
		out.writeLong(account_id);
		out.writeLong(user_id);
		out.writeLong(timestamp);
		out.writeLong(retweet_count);
		out.writeLong(in_reply_to_status_id);
		out.writeInt(is_gap ? 1 : 0);
		out.writeInt(is_retweet ? 1 : 0);
		out.writeInt(is_favorite ? 1 : 0);
		out.writeInt(user_is_protected ? 1 : 0);
		out.writeInt(user_is_verified ? 1 : 0);
		out.writeInt(has_media ? 1 : 0);
		out.writeString(retweeted_by_name);
		out.writeString(retweeted_by_screen_name);
		out.writeString(text_html);
		out.writeString(text_plain);
		out.writeString(user_name);
		out.writeString(user_screen_name);
		out.writeString(in_reply_to_screen_name);
		out.writeString(source);
		out.writeString(user_profile_image_url);
		out.writeString(image_preview_url);
		out.writeString(image_original_url);
		out.writeParcelable(location, flags);
		out.writeLong(my_retweet_id);
		out.writeInt(is_possibly_sensitive ? 1 : 0);
		out.writeInt(user_is_following ? 1 : 0);
		out.writeString(text_unescaped);
		out.writeLong(in_reply_to_user_id);
		out.writeString(in_reply_to_name);
	}

	private static long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}
}
