package org.mariotaku.twidere.util;

import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.database.Cursor;

public class StatusesCursorIndices {

	public final int account_id, status_id, status_timestamp, name, screen_name, text, profile_image_url, is_retweet,
			is_favorite, is_gap, location, has_media, is_protected, in_reply_to_status_id, in_reply_to_user_id,
			in_reply_to_screen_name, retweeted_by_name, retweeted_by_screen_name, retweet_id, retweeted_by_id, user_id,
			source, retweet_count;

	public StatusesCursorIndices(Cursor cursor) {
		account_id = cursor.getColumnIndexOrThrow(Statuses.ACCOUNT_ID);
		status_id = cursor.getColumnIndexOrThrow(Statuses.STATUS_ID);
		status_timestamp = cursor.getColumnIndexOrThrow(Statuses.STATUS_TIMESTAMP);
		name = cursor.getColumnIndexOrThrow(Statuses.NAME);
		screen_name = cursor.getColumnIndexOrThrow(Statuses.SCREEN_NAME);
		text = cursor.getColumnIndexOrThrow(Statuses.TEXT);
		profile_image_url = cursor.getColumnIndexOrThrow(Statuses.PROFILE_IMAGE_URL);
		is_favorite = cursor.getColumnIndexOrThrow(Statuses.IS_FAVORITE);
		is_retweet = cursor.getColumnIndexOrThrow(Statuses.IS_RETWEET);
		is_gap = cursor.getColumnIndexOrThrow(Statuses.IS_GAP);
		location = cursor.getColumnIndexOrThrow(Statuses.LOCATION);
		has_media = cursor.getColumnIndexOrThrow(Statuses.HAS_MEDIA);
		is_protected = cursor.getColumnIndexOrThrow(Statuses.IS_PROTECTED);
		in_reply_to_status_id = cursor.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_STATUS_ID);
		in_reply_to_user_id = cursor.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_USER_ID);
		in_reply_to_screen_name = cursor.getColumnIndexOrThrow(Statuses.IN_REPLY_TO_SCREEN_NAME);
		retweeted_by_name = cursor.getColumnIndexOrThrow(Statuses.RETWEETED_BY_NAME);
		retweeted_by_screen_name = cursor.getColumnIndexOrThrow(Statuses.RETWEETED_BY_SCREEN_NAME);
		retweet_id = cursor.getColumnIndexOrThrow(Statuses.RETWEET_ID);
		retweeted_by_id = cursor.getColumnIndexOrThrow(Statuses.RETWEETED_BY_ID);
		user_id = cursor.getColumnIndexOrThrow(Statuses.USER_ID);
		source = cursor.getColumnIndexOrThrow(Statuses.SOURCE);
		retweet_count = cursor.getColumnIndex(Statuses.RETWEET_COUNT);
	}
}
