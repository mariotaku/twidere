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

import android.database.Cursor;

import org.mariotaku.twidere.provider.TweetStore.Statuses;

public class StatusCursorIndices {

	public final int _id, account_id, status_id, status_timestamp, user_name, user_screen_name, text_html, text_plain,
			text_unescaped, user_profile_image_url, is_favorite, is_retweet, is_gap, location, is_protected,
			is_verified, in_reply_to_status_id, in_reply_to_user_id, in_reply_to_user_name,
			in_reply_to_user_screen_name, my_retweet_id, retweeted_by_user_name, retweeted_by_user_screen_name,
			retweet_id, retweeted_by_user_id, user_id, source, retweet_count, is_possibly_sensitive, is_following,
			media_link, mentions;

	public StatusCursorIndices(final Cursor cursor) {
		_id = cursor.getColumnIndex(Statuses._ID);
		account_id = cursor.getColumnIndex(Statuses.ACCOUNT_ID);
		status_id = cursor.getColumnIndex(Statuses.STATUS_ID);
		status_timestamp = cursor.getColumnIndex(Statuses.STATUS_TIMESTAMP);
		user_name = cursor.getColumnIndex(Statuses.USER_NAME);
		user_screen_name = cursor.getColumnIndex(Statuses.USER_SCREEN_NAME);
		text_html = cursor.getColumnIndex(Statuses.TEXT_HTML);
		text_plain = cursor.getColumnIndex(Statuses.TEXT_PLAIN);
		text_unescaped = cursor.getColumnIndex(Statuses.TEXT_UNESCAPED);
		user_profile_image_url = cursor.getColumnIndex(Statuses.USER_PROFILE_IMAGE_URL);
		is_favorite = cursor.getColumnIndex(Statuses.IS_FAVORITE);
		is_retweet = cursor.getColumnIndex(Statuses.IS_RETWEET);
		is_gap = cursor.getColumnIndex(Statuses.IS_GAP);
		location = cursor.getColumnIndex(Statuses.LOCATION);
		is_protected = cursor.getColumnIndex(Statuses.IS_PROTECTED);
		is_verified = cursor.getColumnIndex(Statuses.IS_VERIFIED);
		in_reply_to_status_id = cursor.getColumnIndex(Statuses.IN_REPLY_TO_STATUS_ID);
		in_reply_to_user_id = cursor.getColumnIndex(Statuses.IN_REPLY_TO_USER_ID);
		in_reply_to_user_name = cursor.getColumnIndex(Statuses.IN_REPLY_TO_USER_NAME);
		in_reply_to_user_screen_name = cursor.getColumnIndex(Statuses.IN_REPLY_TO_USER_SCREEN_NAME);
		my_retweet_id = cursor.getColumnIndex(Statuses.MY_RETWEET_ID);
		retweet_id = cursor.getColumnIndex(Statuses.RETWEET_ID);
		retweeted_by_user_id = cursor.getColumnIndex(Statuses.RETWEETED_BY_USER_ID);
		retweeted_by_user_name = cursor.getColumnIndex(Statuses.RETWEETED_BY_USER_NAME);
		retweeted_by_user_screen_name = cursor.getColumnIndex(Statuses.RETWEETED_BY_USER_SCREEN_NAME);
		user_id = cursor.getColumnIndex(Statuses.USER_ID);
		source = cursor.getColumnIndex(Statuses.SOURCE);
		retweet_count = cursor.getColumnIndex(Statuses.RETWEET_COUNT);
		is_possibly_sensitive = cursor.getColumnIndex(Statuses.IS_POSSIBLY_SENSITIVE);
		is_following = cursor.getColumnIndex(Statuses.IS_FOLLOWING);
		media_link = cursor.getColumnIndex(Statuses.MEDIA_LINK);
		mentions = cursor.getColumnIndex(Statuses.MENTIONS);
	}

	@Override
	public String toString() {
		return "StatusCursorIndices{_id=" + _id + ", account_id=" + account_id + ", status_id=" + status_id
				+ ", status_timestamp=" + status_timestamp + ", user_name=" + user_name + ", user_screen_name="
				+ user_screen_name + ", text_html=" + text_html + ", text_plain=" + text_plain + ", text_unescaped="
				+ text_unescaped + ", user_profile_image_url=" + user_profile_image_url + ", is_favorite="
				+ is_favorite + ", is_retweet=" + is_retweet + ", is_gap=" + is_gap + ", location=" + location
				+ ", is_protected=" + is_protected + ", is_verified=" + is_verified + ", in_reply_to_status_id="
				+ in_reply_to_status_id + ", in_reply_to_user_id=" + in_reply_to_user_id + ", in_reply_to_user_name="
				+ in_reply_to_user_name + ", in_reply_to_user_screen_name=" + in_reply_to_user_screen_name
				+ ", my_retweet_id=" + my_retweet_id + ", retweeted_by_user_name=" + retweeted_by_user_name
				+ ", retweeted_by_user_screen_name=" + retweeted_by_user_screen_name + ", retweet_id=" + retweet_id
				+ ", retweeted_by_user_id=" + retweeted_by_user_id + ", user_id=" + user_id + ", source=" + source
				+ ", retweet_count=" + retweet_count + ", is_possibly_sensitive=" + is_possibly_sensitive
				+ ", is_following=" + is_following + ", media_link=" + media_link + ", mentions=" + mentions + "}";
	}
}
