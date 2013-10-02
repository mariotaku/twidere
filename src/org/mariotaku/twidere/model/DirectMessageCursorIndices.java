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

import org.mariotaku.twidere.provider.TweetStore.DirectMessages;

public class DirectMessageCursorIndices {

    public final int account_id, message_id, message_timestamp, sender_name, sender_screen_name,
            text, text_plain,
            recipient_name, recipient_screen_name, sender_profile_image_url, is_outgoing,
            recipient_profile_image_url,
            sender_id, recipient_id;

    public DirectMessageCursorIndices(final Cursor cursor) {
        account_id = cursor.getColumnIndex(DirectMessages.ACCOUNT_ID);
        message_id = cursor.getColumnIndex(DirectMessages.MESSAGE_ID);
        message_timestamp = cursor.getColumnIndex(DirectMessages.MESSAGE_TIMESTAMP);
        sender_id = cursor.getColumnIndex(DirectMessages.SENDER_ID);
        recipient_id = cursor.getColumnIndex(DirectMessages.RECIPIENT_ID);
        is_outgoing = cursor.getColumnIndex(DirectMessages.IS_OUTGOING);
        text = cursor.getColumnIndex(DirectMessages.TEXT_HTML);
        text_plain = cursor.getColumnIndex(DirectMessages.TEXT_PLAIN);
        sender_name = cursor.getColumnIndex(DirectMessages.SENDER_NAME);
        recipient_name = cursor.getColumnIndex(DirectMessages.RECIPIENT_NAME);
        sender_screen_name = cursor.getColumnIndex(DirectMessages.SENDER_SCREEN_NAME);
        recipient_screen_name = cursor.getColumnIndex(DirectMessages.RECIPIENT_SCREEN_NAME);
        sender_profile_image_url = cursor.getColumnIndex(DirectMessages.SENDER_PROFILE_IMAGE_URL);
        recipient_profile_image_url = cursor
                .getColumnIndex(DirectMessages.RECIPIENT_PROFILE_IMAGE_URL);
    }
}
