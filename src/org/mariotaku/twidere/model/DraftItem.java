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