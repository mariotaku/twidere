package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.getGeoLocationFromString;
import static org.mariotaku.twidere.util.Utils.getSpannedStatusString;
import static org.mariotaku.twidere.util.Utils.getSpannedStatusText;
import static org.mariotaku.twidere.util.Utils.getSpannedTweetText;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.net.URL;
import java.util.Comparator;
import java.util.Date;

import twitter4j.GeoLocation;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.Tweet;
import twitter4j.User;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import android.text.Spanned;

public class ParcelableStatus implements Parcelable {

	public static final Parcelable.Creator<ParcelableStatus> CREATOR = new Parcelable.Creator<ParcelableStatus>() {
		@Override
		public ParcelableStatus createFromParcel(Parcel in) {
			return new ParcelableStatus(in);
		}

		@Override
		public ParcelableStatus[] newArray(int size) {
			return new ParcelableStatus[size];
		}
	};

	public final long retweet_id, retweeted_by_id, status_id, account_id, user_id, status_timestamp, retweet_count,
			in_reply_to_status_id, in_reply_to_user_id;

	public final boolean is_gap, is_retweet, is_favorite, is_protected, has_media;

	public final String retweeted_by_name, retweeted_by_screen_name, text_plain, name, screen_name,
			in_reply_to_screen_name, source;
	public final Spanned text;
	public final GeoLocation location;

	public URL profile_image_url;

	public static final Comparator<ParcelableStatus> TIMESTAMP_COMPARATOR = new Comparator<ParcelableStatus>() {

		@Override
		public int compare(ParcelableStatus object1, ParcelableStatus object2) {
			long diff = object2.status_timestamp - object1.status_timestamp;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public static final Comparator<ParcelableStatus> STATUS_ID_COMPARATOR = new Comparator<ParcelableStatus>() {

		@Override
		public int compare(ParcelableStatus object1, ParcelableStatus object2) {
			long diff = object2.status_id - object1.status_id;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public ParcelableStatus(Cursor cursor, StatusesCursorIndices indices) {
		retweet_id = indices.retweet_id != -1 ? cursor.getLong(indices.retweet_id) : -1;
		retweeted_by_id = indices.retweeted_by_id != -1 ? cursor.getLong(indices.retweeted_by_id) : -1;
		status_id = indices.status_id != -1 ? cursor.getLong(indices.status_id) : -1;
		account_id = indices.account_id != -1 ? cursor.getLong(indices.account_id) : -1;
		user_id = indices.user_id != -1 ? cursor.getLong(indices.user_id) : -1;
		status_timestamp = indices.status_timestamp != -1 ? cursor.getLong(indices.status_timestamp) : 0;
		retweet_count = indices.retweet_count != -1 ? cursor.getLong(indices.retweet_count) : -1;
		in_reply_to_status_id = indices.in_reply_to_status_id != -1 ? cursor.getLong(indices.in_reply_to_status_id)
				: -1;
		in_reply_to_user_id = indices.in_reply_to_user_id != -1 ? cursor.getLong(indices.in_reply_to_user_id) : -1;
		is_gap = indices.is_gap != -1 ? cursor.getInt(indices.is_gap) == 1 : false;
		is_retweet = indices.is_retweet != -1 ? cursor.getInt(indices.is_retweet) == 1 : false;
		is_favorite = indices.is_favorite != -1 ? cursor.getInt(indices.is_favorite) == 1 : false;
		is_protected = indices.is_protected != -1 ? cursor.getInt(indices.is_protected) == 1 : false;
		has_media = indices.has_media != -1 ? cursor.getInt(indices.has_media) == 1 : false;
		retweeted_by_name = indices.retweeted_by_name != -1 ? cursor.getString(indices.retweeted_by_name) : null;
		retweeted_by_screen_name = indices.retweeted_by_screen_name != -1 ? cursor
				.getString(indices.retweeted_by_screen_name) : null;
		text = indices.text != -1 ? Html.fromHtml(cursor.getString(indices.text)) : null;
		text_plain = indices.text_plain != -1 ? cursor.getString(indices.text_plain) : null;
		name = indices.name != -1 ? cursor.getString(indices.name) : null;
		screen_name = indices.screen_name != -1 ? cursor.getString(indices.screen_name) : null;
		in_reply_to_screen_name = indices.in_reply_to_screen_name != -1 ? cursor
				.getString(indices.in_reply_to_screen_name) : null;
		source = indices.source != -1 ? cursor.getString(indices.source) : null;
		location = indices.location != -1 ? getGeoLocationFromString(cursor.getString(indices.location)) : null;
		profile_image_url = indices.profile_image_url != -1 ? parseURL(cursor.getString(indices.profile_image_url))
				: null;
	}

	public ParcelableStatus(Parcel in) {
		retweet_id = in.readLong();
		retweeted_by_id = in.readLong();
		status_id = in.readLong();
		account_id = in.readLong();
		user_id = in.readLong();
		status_timestamp = in.readLong();
		retweet_count = in.readLong();
		in_reply_to_status_id = in.readLong();
		in_reply_to_user_id = in.readLong();
		is_gap = in.readInt() == 1;
		is_retweet = in.readInt() == 1;
		is_favorite = in.readInt() == 1;
		is_protected = in.readInt() == 1;
		has_media = in.readInt() == 1;
		retweeted_by_name = in.readString();
		retweeted_by_screen_name = in.readString();
		text = Html.fromHtml(in.readString());
		text_plain = in.readString();
		name = in.readString();
		screen_name = in.readString();
		in_reply_to_screen_name = in.readString();
		source = in.readString();
		location = (GeoLocation) in.readSerializable();
		profile_image_url = (URL) in.readSerializable();
	}

	public ParcelableStatus(Status status, long account_id, boolean is_gap) {

		this.is_gap = is_gap;
		status_id = status.getId();
		this.account_id = account_id;
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
		profile_image_url = user != null ? user.getProfileImageUrlHttps() : null;
		is_protected = user != null ? user.isProtected() : false;
		final MediaEntity[] medias = status.getMediaEntities();

		status_timestamp = getTime(status.getCreatedAt());
		text = getSpannedStatusText(status, account_id);
		text_plain = status.getText();
		retweet_count = status.getRetweetCount();
		in_reply_to_screen_name = status.getInReplyToScreenName();
		in_reply_to_status_id = status.getInReplyToStatusId();
		in_reply_to_user_id = status.getInReplyToUserId();
		source = status.getSource();
		location = status.getGeoLocation();
		is_favorite = status.isFavorited();
		has_media = medias != null && medias.length > 0;

	}

	public ParcelableStatus(Tweet tweet, long account_id, boolean is_gap) {

		this.is_gap = is_gap;
		status_id = tweet.getId();
		this.account_id = account_id;
		is_retweet = false;
		retweet_id = -1;
		retweeted_by_id = -1;
		retweeted_by_name = null;
		retweeted_by_screen_name = null;
		user_id = tweet.getFromUserId();
		name = tweet.getFromUser();
		screen_name = tweet.getFromUser();
		profile_image_url = parseURL(tweet.getProfileImageUrl());
		is_protected = false;
		final MediaEntity[] medias = tweet.getMediaEntities();

		status_timestamp = getTime(tweet.getCreatedAt());
		text = getSpannedTweetText(tweet, account_id);
		text_plain = tweet.getText();
		retweet_count = -1;
		in_reply_to_screen_name = tweet.getToUser();
		in_reply_to_status_id = -1;
		in_reply_to_user_id = tweet.getToUserId();
		source = tweet.getSource();
		location = tweet.getGeoLocation();
		is_favorite = false;
		has_media = medias != null && medias.length > 0;

	}

	@Override
	public int describeContents() {
		return hashCode();
	}

	@Override
	public String toString() {
		return text_plain;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(retweet_id);
		out.writeLong(retweeted_by_id);
		out.writeLong(status_id);
		out.writeLong(account_id);
		out.writeLong(user_id);
		out.writeLong(status_timestamp);
		out.writeLong(retweet_count);
		out.writeLong(in_reply_to_status_id);
		out.writeLong(in_reply_to_user_id);
		out.writeInt(is_gap ? 1 : 0);
		out.writeInt(is_retweet ? 1 : 0);
		out.writeInt(is_favorite ? 1 : 0);
		out.writeInt(is_protected ? 1 : 0);
		out.writeInt(has_media ? 1 : 0);
		out.writeString(retweeted_by_name);
		out.writeString(retweeted_by_screen_name);
		out.writeString(getSpannedStatusString(text));
		out.writeString(text_plain);
		out.writeString(name);
		out.writeString(screen_name);
		out.writeString(in_reply_to_screen_name);
		out.writeString(source);
		out.writeSerializable(location);
		out.writeSerializable(profile_image_url);
	}

	private long getTime(Date date) {
		return date != null ? date.getTime() : 0;
	}
}