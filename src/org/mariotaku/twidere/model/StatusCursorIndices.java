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

import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.database.Cursor;

public class StatusCursorIndices {

	public final int account_id, status_id, status_timestamp, name, screen_name, text, text_plain, profile_image_url,
			is_retweet, is_favorite, is_gap, location, is_protected, is_verified, in_reply_to_status_id,
			in_reply_to_screen_name, retweeted_by_name, retweeted_by_screen_name, retweet_id, retweeted_by_id, user_id,
			source, retweet_count;

	public StatusCursorIndices(final Cursor cursor) {
		account_id = cursor.getColumnIndex(Statuses.ACCOUNT_ID);
		status_id = cursor.getColumnIndex(Statuses.STATUS_ID);
		status_timestamp = cursor.getColumnIndex(Statuses.STATUS_TIMESTAMP);
		name = cursor.getColumnIndex(Statuses.NAME);
		screen_name = cursor.getColumnIndex(Statuses.SCREEN_NAME);
		text = cursor.getColumnIndex(Statuses.TEXT);
		text_plain = cursor.getColumnIndex(Statuses.TEXT_PLAIN);
		profile_image_url = cursor.getColumnIndex(Statuses.PROFILE_IMAGE_URL);
		is_favorite = cursor.getColumnIndex(Statuses.IS_FAVORITE);
		is_retweet = cursor.getColumnIndex(Statuses.IS_RETWEET);
		is_gap = cursor.getColumnIndex(Statuses.IS_GAP);
		location = cursor.getColumnIndex(Statuses.LOCATION);
		is_protected = cursor.getColumnIndex(Statuses.IS_PROTECTED);
		is_verified = cursor.getColumnIndex(Statuses.IS_VERIFIED);
		in_reply_to_status_id = cursor.getColumnIndex(Statuses.IN_REPLY_TO_STATUS_ID);
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
