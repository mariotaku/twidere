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

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.DatabaseUpgradeHelper.safeUpgrade;
import static org.mariotaku.twidere.util.Utils.trim;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Drafts;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

import java.util.HashMap;

public final class DatabaseHelper extends SQLiteOpenHelper implements Constants {

	private final Context mContext;

	public DatabaseHelper(final Context context, final String name, final int version) {
		super(context, name, null, version);
		mContext = context;
	}

	@Override
	public void onCreate(final SQLiteDatabase db) {
		db.beginTransaction();
		db.execSQL(createTable(TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, true));
		db.execSQL(createTable(TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, true));
		db.execSQL(createTable(TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, true));
		db.execSQL(createTable(TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, true));
		db.execSQL(createTable(TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, true));
		db.execSQL(createTable(TABLE_CACHED_STATUSES, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true));
		db.execSQL(createTable(TABLE_CACHED_HASHTAGS, CachedHashtags.COLUMNS, CachedHashtags.TYPES, true));
		db.execSQL(createTable(TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, true));
		db.execSQL(createTable(TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true));
		db.execSQL(createTable(TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true));
		db.execSQL(createTable(TABLE_FILTERED_LINKS, Filters.Links.COLUMNS, Filters.Links.TYPES, true));
		db.execSQL(createTable(TABLE_DIRECT_MESSAGES_INBOX, DirectMessages.Inbox.COLUMNS, DirectMessages.Inbox.TYPES,
				true));
		db.execSQL(createTable(TABLE_DIRECT_MESSAGES_OUTBOX, DirectMessages.Outbox.COLUMNS,
				DirectMessages.Outbox.TYPES, true));
		db.execSQL(createTable(TABLE_TRENDS_LOCAL, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true));
		db.execSQL(createTable(TABLE_TABS, Tabs.COLUMNS, Tabs.TYPES, true));
		db.setTransactionSuccessful();
		db.endTransaction();
	}

	@Override
	public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		handleVersionChange(db);
	}

	@Override
	public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
		handleVersionChange(db);
		if (oldVersion <= 43 && newVersion >= 44) {
			final ContentValues values = new ContentValues();
			final SharedPreferences prefs = mContext
					.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
			// Here I use old consumer key/secret because it's default key for
			// older versions
			final String pref_consumer_key = prefs.getString(PREFERENCE_KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
			final String pref_consumer_secret = prefs
					.getString(PREFERENCE_KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET);
			values.put(Accounts.CONSUMER_KEY, trim(pref_consumer_key));
			values.put(Accounts.CONSUMER_SECRET, trim(pref_consumer_secret));
			db.update(TABLE_ACCOUNTS, values, null, null);
		}
	}

	private String createTable(final String tableName, final String[] columns, final String[] types,
			final boolean create_if_not_exists) {
		if (tableName == null || columns == null || types == null || types.length != columns.length
				|| types.length == 0)
			throw new IllegalArgumentException("Invalid parameters for creating table " + tableName);
		final StringBuilder stringBuilder = new StringBuilder(create_if_not_exists ? "CREATE TABLE IF NOT EXISTS "
				: "CREATE TABLE ");

		stringBuilder.append(tableName);
		stringBuilder.append(" (");
		final int length = columns.length;
		for (int n = 0, i = length; n < i; n++) {
			if (n > 0) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(columns[n]).append(' ').append(types[n]);
		}
		return stringBuilder.append(");").toString();
	}

	private void handleVersionChange(final SQLiteDatabase db) {
		final HashMap<String, String> account_db_table_alias = new HashMap<String, String>();
		account_db_table_alias.put(Accounts.SCREEN_NAME, "username");
		account_db_table_alias.put(Accounts.NAME, "username");
		account_db_table_alias.put(Accounts.ACCOUNT_ID, "user_id");
		account_db_table_alias.put(Accounts.SIGNING_OAUTH_BASE_URL, "oauth_rest_base_url");
		final HashMap<String, String> filters_db_table_alias = new HashMap<String, String>();
		filters_db_table_alias.put(Filters.VALUE, "text");
		safeUpgrade(db, TABLE_ACCOUNTS, Accounts.COLUMNS, Accounts.TYPES, true, false, account_db_table_alias);
		safeUpgrade(db, TABLE_STATUSES, Statuses.COLUMNS, Statuses.TYPES, true, true, null);
		safeUpgrade(db, TABLE_MENTIONS, Mentions.COLUMNS, Mentions.TYPES, true, true, null);
		safeUpgrade(db, TABLE_DRAFTS, Drafts.COLUMNS, Drafts.TYPES, true, false, null);
		safeUpgrade(db, TABLE_CACHED_USERS, CachedUsers.COLUMNS, CachedUsers.TYPES, true, true, null);
		safeUpgrade(db, TABLE_CACHED_STATUSES, CachedStatuses.COLUMNS, CachedStatuses.TYPES, true, true, null);
		safeUpgrade(db, TABLE_CACHED_HASHTAGS, CachedHashtags.COLUMNS, CachedHashtags.TYPES, true, true, null);
		safeUpgrade(db, TABLE_FILTERED_USERS, Filters.Users.COLUMNS, Filters.Users.TYPES, true, false,
				filters_db_table_alias);
		safeUpgrade(db, TABLE_FILTERED_KEYWORDS, Filters.Keywords.COLUMNS, Filters.Keywords.TYPES, true, false,
				filters_db_table_alias);
		safeUpgrade(db, TABLE_FILTERED_SOURCES, Filters.Sources.COLUMNS, Filters.Sources.TYPES, true, false,
				filters_db_table_alias);
		safeUpgrade(db, TABLE_FILTERED_LINKS, Filters.Links.COLUMNS, Filters.Links.TYPES, true, false,
				filters_db_table_alias);
		safeUpgrade(db, TABLE_DIRECT_MESSAGES_INBOX, DirectMessages.Inbox.COLUMNS, DirectMessages.Inbox.TYPES, true,
				true, null);
		safeUpgrade(db, TABLE_DIRECT_MESSAGES_OUTBOX, DirectMessages.Outbox.COLUMNS, DirectMessages.Outbox.TYPES, true,
				true, null);
		safeUpgrade(db, TABLE_TRENDS_LOCAL, CachedTrends.Local.COLUMNS, CachedTrends.Local.TYPES, true, true, null);
		safeUpgrade(db, TABLE_TABS, Tabs.COLUMNS, Tabs.TYPES, true, false, null);
	}

}
