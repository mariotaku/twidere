package org.mariotaku.twidere.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.mariotaku.twidere.provider.TweetStore;
import org.mariotaku.twidere.provider.TweetStore.UnreadCounts;

public class UnreadCountUtils {

	public static int getUnreadCount(final Context context, final int position) {
		if (context == null || position < 0) return 0;
		final ContentResolver resolver = context.getContentResolver();
		final Uri uri = TweetStore.UnreadCounts.CONTENT_URI.buildUpon().appendPath(ParseUtils.parseString(position))
				.build();
		final Cursor c = resolver.query(uri, new String[] { UnreadCounts.COUNT }, null, null, null);
		if (c == null) return 0;
		try {
			if (c.getCount() == 0) return 0;
			c.moveToFirst();
			return c.getInt(c.getColumnIndex(UnreadCounts.COUNT));
		} finally {
			c.close();
		}
	}
}
