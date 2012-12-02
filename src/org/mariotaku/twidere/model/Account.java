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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Context;
import android.database.Cursor;

public class Account implements CharSequence {

	public final String screen_name, name;
	public final long account_id;

	Account(final Cursor cursor, final Indices indices) {
		screen_name = cursor.getString(indices.screen_name);
		name = cursor.getString(indices.name);
		account_id = cursor.getLong(indices.account_id);
	}

	@Override
	public char charAt(final int index) {
		return screen_name.charAt(index);
	}

	@Override
	public int length() {
		return screen_name.length();
	}

	@Override
	public CharSequence subSequence(final int start, final int end) {
		return screen_name.subSequence(start, end);
	}

	@Override
	public String toString() {
		return screen_name;
	}

	public static List<Account> getAccounts(final Context context, final boolean activated_only) {
		if (context == null) {
			Collections.emptyList();
		}
		final ArrayList<Account> accounts = new ArrayList<Account>();
		final String[] cols = new String[] { Accounts.SCREEN_NAME, Accounts.ACCOUNT_ID };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols,
				activated_only ? Accounts.IS_ACTIVATED + " = 1" : null, null, null);
		if (cur != null) {
			final Indices indices = new Indices(cur);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				accounts.add(new Account(cur, indices));
				cur.moveToNext();
			}
			cur.close();
		}
		return accounts;
	}

	public static class Indices {

		public final int screen_name, name, account_id;

		public Indices(final Cursor cursor) {
			screen_name = cursor.getColumnIndex(Accounts.SCREEN_NAME);
			name = cursor.getColumnIndex(Accounts.NAME);
			account_id = cursor.getColumnIndex(Accounts.ACCOUNT_ID);
		}
	}
}
