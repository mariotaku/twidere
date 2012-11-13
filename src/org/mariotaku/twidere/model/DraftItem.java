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

import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.util.ArrayUtils;

import android.database.Cursor;

public class DraftItem {

	public final long[] account_ids;
	public final long _id, in_reply_to_status_id;
	public final String text, media_uri, in_reply_to_name, in_reply_to_screen_name;
	public final boolean is_quote, is_image_attached, is_photo_attached;

	public DraftItem(final Cursor cursor, final int position) {
		cursor.moveToPosition(position);
		_id = cursor.getLong(cursor.getColumnIndex(Drafts._ID));
		text = cursor.getString(cursor.getColumnIndex(Drafts.TEXT));
		media_uri = cursor.getString(cursor.getColumnIndex(Drafts.IMAGE_URI));
		account_ids = ArrayUtils.fromString(cursor.getString(cursor.getColumnIndex(Drafts.ACCOUNT_IDS)), ',');
		in_reply_to_status_id = cursor.getLong(cursor.getColumnIndex(Drafts.IN_REPLY_TO_STATUS_ID));
		in_reply_to_name = cursor.getString(cursor.getColumnIndex(Drafts.IN_REPLY_TO_NAME));
		in_reply_to_screen_name = cursor.getString(cursor.getColumnIndex(Drafts.IN_REPLY_TO_SCREEN_NAME));
		is_quote = cursor.getShort(cursor.getColumnIndex(Drafts.IS_QUOTE)) == 1;
		is_image_attached = cursor.getShort(cursor.getColumnIndex(Drafts.IS_IMAGE_ATTACHED)) == 1;
		is_photo_attached = cursor.getShort(cursor.getColumnIndex(Drafts.IS_PHOTO_ATTACHED)) == 1;
	}

}
