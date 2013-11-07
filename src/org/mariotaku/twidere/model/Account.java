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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;

import org.mariotaku.twidere.provider.TweetStore.Accounts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account {

	public final String screen_name, name, profile_image_url, profile_banner_url;
	public final long account_id;
	public final int user_color;
	public final boolean is_activated;
	public final boolean is_dummy;

	public Account(final Cursor cursor, final Indices indices) {
		is_dummy = false;
		screen_name = indices.screen_name != -1 ? cursor.getString(indices.screen_name) : null;
		name = indices.name != -1 ? cursor.getString(indices.name) : null;
		account_id = indices.account_id != -1 ? cursor.getLong(indices.account_id) : -1;
		profile_image_url = indices.profile_image_url != -1 ? cursor.getString(indices.profile_image_url) : null;
		profile_banner_url = indices.profile_banner_url != -1 ? cursor.getString(indices.profile_banner_url) : null;
		user_color = indices.user_color != -1 ? cursor.getInt(indices.user_color) : Color.TRANSPARENT;
		is_activated = indices.is_activated != -1 ? cursor.getInt(indices.is_activated) == 1 : false;
	}

	private Account() {
		is_dummy = true;
		screen_name = null;
		name = null;
		account_id = -1;
		profile_image_url = null;
		profile_banner_url = null;
		user_color = 0;
		is_activated = false;
	}

	@Override
	public String toString() {
		return "Account{screen_name=" + screen_name + ", name=" + name + ", profile_image_url=" + profile_image_url
				+ ", profile_banner_url=" + profile_banner_url + ", account_id=" + account_id + ", user_color="
				+ user_color + ", is_activated=" + is_activated + "}";
	}

	public static Account dummyInstance() {
		return new Account();
	}

	public static Account getAccount(final Context context, final long account_id) {
		if (context == null) return null;
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS,
				Accounts.ACCOUNT_ID + " = " + account_id, null, null);
		if (cur != null) {
			try {
				final Indices indices = new Indices(cur);
				cur.moveToFirst();
				return new Account(cur, indices);
			} finally {
				cur.close();
			}
		}
		return null;
	}

	public static List<Account> getAccounts(final Context context, final boolean activated_only) {
		if (context == null) {
			Collections.emptyList();
		}
		final ArrayList<Account> accounts = new ArrayList<Account>();
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, Accounts.COLUMNS,
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

		public final int screen_name, name, account_id, profile_image_url, profile_banner_url, user_color,
				is_activated;

		public Indices(final Cursor cursor) {
			screen_name = cursor.getColumnIndex(Accounts.SCREEN_NAME);
			name = cursor.getColumnIndex(Accounts.NAME);
			account_id = cursor.getColumnIndex(Accounts.ACCOUNT_ID);
			profile_image_url = cursor.getColumnIndex(Accounts.PROFILE_IMAGE_URL);
			profile_banner_url = cursor.getColumnIndex(Accounts.PROFILE_BANNER_URL);
			user_color = cursor.getColumnIndex(Accounts.USER_COLOR);
			is_activated = cursor.getColumnIndex(Accounts.IS_ACTIVATED);
		}

		@Override
		public String toString() {
			return "Indices{screen_name=" + screen_name + ", name=" + name + ", account_id=" + account_id
					+ ", profile_image_url=" + profile_image_url + ", profile_banner_url=" + profile_banner_url
					+ ", user_color=" + user_color + ", is_activated=" + is_activated + "}";
		}
	}
}
