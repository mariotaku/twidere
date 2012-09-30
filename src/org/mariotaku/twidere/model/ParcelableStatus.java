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

import static org.mariotaku.twidere.util.HtmlEscapeHelper.unescape;
import static org.mariotaku.twidere.util.Utils.formatStatusText;
import static org.mariotaku.twidere.util.Utils.getPreviewImage;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;

import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.User;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

public class ParcelableStatus implements Parcelable, Comparable<ParcelableStatus> {

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

	public final long retweet_id, retweeted_by_id, status_id, account_id, user_id, status_timestamp, retweet_count,
			in_reply_to_status_id;

	public final boolean is_gap, is_retweet, is_favorite, is_protected, is_verified, has_media;

	public final String retweeted_by_name, retweeted_by_screen_name, text_html, text_plain, name, screen_name,
			in_reply_to_screen_name, source, profile_image_url_string, image_preview_url_string, image_orig_url_string,
			location_string, text_unescaped;
	public final ParcelableLocation location;

	public final Spanned text;

	public final URL profile_image_url, image_preview_url;

	public static final Comparator<ParcelableStatus> TIMESTAMP_COMPARATOR = new Comparator<ParcelableStatus>() {

		@Override
		public int compare(final ParcelableStatus object1, final ParcelableStatus object2) {
			final long diff = object2.status_timestamp - object1.status_timestamp;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public ParcelableStatus(final Cursor cursor, final StatusCursorIndices indices) {
		retweet_id = indices.retweet_id != -1 ? cursor.getLong(indices.retweet_id) : -1;
		retweeted_by_id = indices.retweeted_by_id != -1 ? cursor.getLong(indices.retweeted_by_id) : -1;
		status_id = indices.status_id != -1 ? cursor.getLong(indices.status_id) : -1;
		account_id = indices.account_id != -1 ? cursor.getLong(indices.account_id) : -1;
		user_id = indices.user_id != -1 ? cursor.getLong(indices.user_id) : -1;
		status_timestamp = indices.status_timestamp != -1 ? cursor.getLong(indices.status_timestamp) : 0;
		retweet_count = indices.retweet_count != -1 ? cursor.getLong(indices.retweet_count) : -1;
		in_reply_to_status_id = indices.in_reply_to_status_id != -1 ? cursor.getLong(indices.in_reply_to_status_id)
				: -1;
		is_gap = indices.is_gap != -1 ? cursor.getInt(indices.is_gap) == 1 : false;
		is_retweet = indices.is_retweet != -1 ? cursor.getInt(indices.is_retweet) == 1 : false;
		is_favorite = indices.is_favorite != -1 ? cursor.getInt(indices.is_favorite) == 1 : false;
		is_protected = indices.is_protected != -1 ? cursor.getInt(indices.is_protected) == 1 : false;
		is_verified = indices.is_verified != -1 ? cursor.getInt(indices.is_verified) == 1 : false;
		retweeted_by_name = indices.retweeted_by_name != -1 ? cursor.getString(indices.retweeted_by_name) : null;
		retweeted_by_screen_name = indices.retweeted_by_screen_name != -1 ? cursor
				.getString(indices.retweeted_by_screen_name) : null;
		text_html = indices.text != -1 ? cursor.getString(indices.text) : null;
		final PreviewImage preview = getPreviewImage(text_html, true);
		has_media = preview.has_image;
		text_plain = indices.text_plain != -1 ? cursor.getString(indices.text_plain) : null;
		name = indices.name != -1 ? cursor.getString(indices.name) : null;
		screen_name = indices.screen_name != -1 ? cursor.getString(indices.screen_name) : null;
		in_reply_to_screen_name = indices.in_reply_to_screen_name != -1 ? cursor
				.getString(indices.in_reply_to_screen_name) : null;
		source = indices.source != -1 ? cursor.getString(indices.source) : null;
		location_string = cursor.getString(indices.location);
		location = indices.location != -1 ? new ParcelableLocation(location_string) : null;
		profile_image_url_string = indices.profile_image_url != -1 ? cursor.getString(indices.profile_image_url) : null;
		profile_image_url = parseURL(profile_image_url_string);

		text = text_html != null ? Html.fromHtml(text_html) : null;
		image_preview_url_string = preview.matched_url;
		image_orig_url_string = preview.orig_url;
		image_preview_url = parseURL(image_preview_url_string);
		text_unescaped = unescape(text_html);
	}

	public ParcelableStatus(final Parcel in) {
		retweet_id = in.readLong();
		retweeted_by_id = in.readLong();
		status_id = in.readLong();
		account_id = in.readLong();
		user_id = in.readLong();
		status_timestamp = in.readLong();
		retweet_count = in.readLong();
		in_reply_to_status_id = in.readLong();
		is_gap = in.readInt() == 1;
		is_retweet = in.readInt() == 1;
		is_favorite = in.readInt() == 1;
		is_protected = in.readInt() == 1;
		is_verified = in.readInt() == 1;
		has_media = in.readInt() == 1;
		retweeted_by_name = in.readString();
		retweeted_by_screen_name = in.readString();
		text_html = in.readString();
		text_plain = in.readString();
		name = in.readString();
		screen_name = in.readString();
		in_reply_to_screen_name = in.readString();
		source = in.readString();
		profile_image_url_string = in.readString();
		image_preview_url_string = in.readString();
		image_orig_url_string = in.readString();
		location_string = in.readString();
		location = new ParcelableLocation(location_string);
		image_preview_url = parseURL(image_preview_url_string);
		profile_image_url = parseURL(profile_image_url_string);
		text = text_html != null ? Html.fromHtml(text_html) : null;
		text_unescaped = unescape(text_html);
	}

	public ParcelableStatus(final SerializableStatus in) {
		retweet_id = in.retweet_id;
		retweeted_by_id = in.retweeted_by_id;
		status_id = in.status_id;
		account_id = in.account_id;
		user_id = in.user_id;
		status_timestamp = in.status_timestamp;
		retweet_count = in.retweet_count;
		in_reply_to_status_id = in.in_reply_to_status_id;
		is_gap = in.is_gap;
		is_retweet = in.is_retweet;
		is_favorite = in.is_favorite;
		is_protected = in.is_protected;
		is_verified = in.is_verified;
		has_media = in.has_media;
		retweeted_by_name = in.retweeted_by_name;
		retweeted_by_screen_name = in.retweeted_by_screen_name;
		text_html = in.text_html;
		text_plain = in.text_plain;
		name = in.name;
		screen_name = in.screen_name;
		in_reply_to_screen_name = in.in_reply_to_screen_name;
		source = in.source;
		profile_image_url_string = in.profile_image_url_string;
		image_preview_url_string = in.image_preview_url_string;
		image_orig_url_string = in.image_orig_url_string;
		location_string = in.location_string;
		location = new ParcelableLocation(in.location);
		image_preview_url = in.image_preview_url;
		profile_image_url = in.profile_image_url;
		text = text_html != null ? Html.fromHtml(text_html) : null;
		text_unescaped = unescape(text_html);
	}

	public ParcelableStatus(Status status, final long account_id, final boolean is_gap) {

		this.is_gap = is_gap;
		this.account_id = account_id;
		status_id = status.getId();
		is_retweet = status.isRetweet();
		final Status retweeted_status = is_retweet ? status.getRetweetedStatus() : null;
		final User retweet_user = retweeted_status != null ? status.getUser() : null;
		retweet_id = retweeted_status != null ? retweeted_status.getId() : -1;
		retweeted_by_id = retweet_user != null ? retweet_user.getId() : -1;
		retweeted_by_name = retweet_user != null ? retweet_user.getName() : null;
		retweeted_by_screen_name = retweet_user != null ? retweet_user.getScreenName() : null;
		if (retweeted_status != null) {
			status = retweeted_status;
		}
		final User user = status.getUser();
		user_id = user != null ? user.getId() : -1;
		name = user != null ? user.getName() : null;
		screen_name = user != null ? user.getScreenName() : null;
		profile_image_url = user != null ? user.getProfileImageURL() : null;
		profile_image_url_string = profile_image_url != null ? profile_image_url.toString() : null;
		is_protected = user != null ? user.isProtected() : false;
		is_verified = user != null ? user.isVerified() : false;
		final MediaEntity[] medias = status.getMediaEntities();

		status_timestamp = getTime(status.getCreatedAt());
		text_html = formatStatusText(status);
		final PreviewImage preview = getPreviewImage(text_html, true);
		text_plain = status.getText();
		retweet_count = status.getRetweetCount();
		in_reply_to_screen_name = status.getInReplyToScreenName();
		in_reply_to_status_id = status.getInReplyToStatusId();
		source = status.getSource();
		location = new ParcelableLocation(status.getGeoLocation());
		location_string = location.toString();
		is_favorite = status.isFavorited();
		has_media = medias != null && medias.length > 0 || preview.has_image;
		text = text_html != null ? Html.fromHtml(text_html) : null;
		image_preview_url_string = preview.matched_url;
		image_orig_url_string = preview.orig_url;
		image_preview_url = parseURL(image_preview_url_string);
		text_unescaped = unescape(text_html);
	}

	@Override
	public int compareTo(final ParcelableStatus another) {
		if (another == null) return 0;
		final long diff = another.status_id - status_id;
		if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
		if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
		return (int) diff;
	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public final boolean equals(final Object o) {
		if (!(o instanceof ParcelableStatus)) return false;
		return status_id == ((ParcelableStatus) o).status_id;
	}

	@Override
	public String toString() {
		return unescape(text_html);
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeLong(retweet_id);
		out.writeLong(retweeted_by_id);
		out.writeLong(status_id);
		out.writeLong(account_id);
		out.writeLong(user_id);
		out.writeLong(status_timestamp);
		out.writeLong(retweet_count);
		out.writeLong(in_reply_to_status_id);
		out.writeInt(is_gap ? 1 : 0);
		out.writeInt(is_retweet ? 1 : 0);
		out.writeInt(is_favorite ? 1 : 0);
		out.writeInt(is_protected ? 1 : 0);
		out.writeInt(is_verified ? 1 : 0);
		out.writeInt(has_media ? 1 : 0);
		out.writeString(retweeted_by_name);
		out.writeString(retweeted_by_screen_name);
		out.writeString(text_html);
		out.writeString(text_plain);
		out.writeString(name);
		out.writeString(screen_name);
		out.writeString(in_reply_to_screen_name);
		out.writeString(source);
		out.writeString(profile_image_url_string);
		out.writeString(image_preview_url_string);
		out.writeString(image_orig_url_string);
		out.writeString(location_string);
	}

	private static long getTime(final Date date) {
		return date != null ? date.getTime() : 0;
	}
}
