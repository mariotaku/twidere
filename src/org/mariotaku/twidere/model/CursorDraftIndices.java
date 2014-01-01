/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.model;

import android.database.Cursor;

import org.mariotaku.twidere.provider.TweetStore.Drafts;

public class CursorDraftIndices {

	public final int _id, account_ids, in_reply_to_status_id, text, location, media_uri, media_type,
			is_possibly_sensitive, timestamp, action_type, action_extras;

	public CursorDraftIndices(final Cursor cursor) {
		_id = cursor.getColumnIndex(Drafts._ID);
		account_ids = cursor.getColumnIndex(Drafts.ACCOUNT_IDS);
		in_reply_to_status_id = cursor.getColumnIndex(Drafts.IN_REPLY_TO_STATUS_ID);
		timestamp = cursor.getColumnIndex(Drafts.TIMESTAMP);
		text = cursor.getColumnIndex(Drafts.TEXT);
		media_uri = cursor.getColumnIndex(Drafts.MEDIA_URI);
		media_type = cursor.getColumnIndex(Drafts.MEDIA_TYPE);
		is_possibly_sensitive = cursor.getColumnIndex(Drafts.IS_POSSIBLY_SENSITIVE);
		location = cursor.getColumnIndex(Drafts.LOCATION);
		action_type = cursor.getColumnIndex(Drafts.ACTION_TYPE);
		action_extras = cursor.getColumnIndex(Drafts.ACTION_EXTRAS);
	}

}
