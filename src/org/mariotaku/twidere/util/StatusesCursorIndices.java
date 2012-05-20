package org.mariotaku.twidere.util;

import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.database.Cursor;

public class StatusesCursorIndices {

	public final int account_id, sort_id, status_id, status_timestamp, name, screen_name, text, profile_image_url, is_retweet,
			is_favorite, is_gap, location, has_media, is_protected, in_reply_to_status_id, in_reply_to_user_id,
			in_reply_to_screen_name, retweeted_by_name, retweeted_by_screen_name, retweet_id, retweeted_by_id, user_id,
			source, retweet_count;

	public StatusesCursorIndices(Cursor cursor) {
		account_id = cursor.getColumnIndex(Statuses.ACCOUNT_ID);
		sort_id = cursor.getColumnIndexOrThrow(Statuses.SORT_ID);
		status_id = cursor.getColumnIndex(Statuses.STATUS_ID);
		status_timestamp = cursor.getColumnIndex(Statuses.STATUS_TIMESTAMP);
		name = cursor.getColumnIndex(Statuses.NAME);
		screen_name = cursor.getColumnIndex(Statuses.SCREEN_NAME);
		text = cursor.getColumnIndex(Statuses.TEXT);
		profile_image_url = cursor.getColumnIndex(Statuses.PROFILE_IMAGE_URL);
		is_favorite = cursor.getColumnIndex(Statuses.IS_FAVORITE);
		is_retweet = cursor.getColumnIndex(Statuses.IS_RETWEET);
		is_gap = cursor.getColumnIndex(Statuses.IS_GAP);
		location = cursor.getColumnIndex(Statuses.LOCATION);
		has_media = cursor.getColumnIndex(Statuses.HAS_MEDIA);
		is_protected = cursor.getColumnIndex(Statuses.IS_PROTECTED);
		in_reply_to_status_id = cursor.getColumnIndex(Statuses.IN_REPLY_TO_STATUS_ID);
		in_reply_to_user_id = cursor.getColumnIndex(Statuses.IN_REPLY_TO_USER_ID);
		in_reply_to_screen_name = cursor.getColumnIndex(Statuses.IN_REPLY_TO_SCREEN_NAME);
		retweeted_by_name = cursor.getColumnIndex(Statuses.RETWEETED_BY_NAME);
		retweeted_by_screen_name = cursor.getColumnIndex(Statuses.RETWEETED_BY_SCREEN_NAME);
		retweet_id = cursor.getColumnIndex(Statuses.RETWEET_ID);
		retweeted_by_id = cursor.getColumnIndex(Statuses.RETWEETED_BY_ID);
		user_id = cursor.getColumnIndex(Statuses.USER_ID);
		source = cursor.getColumnIndex(Statuses.SOURCE);
		retweet_count = cursor.getColumnIndex(Statuses.RETWEET_COUNT);
	}
}
