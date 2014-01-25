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

import static org.mariotaku.twidere.util.Utils.isOfficialConsumerKeySecret;
import static org.mariotaku.twidere.util.Utils.shouldForceUsingPrivateAPIs;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.content.ContentResolverUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Account implements Parcelable {

	public static final Parcelable.Creator<Account> CREATOR = new Parcelable.Creator<Account>() {

		@Override
		public Account createFromParcel(final Parcel in) {
			return new Account(in);
		}

		@Override
		public Account[] newArray(final int size) {
			return new Account[size];
		}
	};

	public final String screen_name, name, profile_image_url, profile_banner_url;
	public final long account_id;
	public final int color;
	public final boolean is_activated;
	public final boolean is_dummy;

	public Account(final Cursor cursor, final Indices indices) {
		is_dummy = false;
		screen_name = indices.screen_name != -1 ? cursor.getString(indices.screen_name) : null;
		name = indices.name != -1 ? cursor.getString(indices.name) : null;
		account_id = indices.account_id != -1 ? cursor.getLong(indices.account_id) : -1;
		profile_image_url = indices.profile_image_url != -1 ? cursor.getString(indices.profile_image_url) : null;
		profile_banner_url = indices.profile_banner_url != -1 ? cursor.getString(indices.profile_banner_url) : null;
		color = indices.color != -1 ? cursor.getInt(indices.color) : Color.TRANSPARENT;
		is_activated = indices.is_activated != -1 ? cursor.getInt(indices.is_activated) == 1 : false;
	}

	public Account(final Parcel source) {
		is_dummy = source.readInt() == 1;
		is_activated = source.readInt() == 1;
		account_id = source.readLong();
		name = source.readString();
		screen_name = source.readString();
		profile_image_url = source.readString();
		profile_banner_url = source.readString();
		color = source.readInt();
	}

	private Account() {
		is_dummy = true;
		screen_name = null;
		name = null;
		account_id = -1;
		profile_image_url = null;
		profile_banner_url = null;
		color = 0;
		is_activated = false;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "Account{screen_name=" + screen_name + ", name=" + name + ", profile_image_url=" + profile_image_url
				+ ", profile_banner_url=" + profile_banner_url + ", account_id=" + account_id + ", user_color=" + color
				+ ", is_activated=" + is_activated + "}";
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {
		out.writeInt(is_dummy ? 1 : 0);
		out.writeInt(is_activated ? 1 : 0);
		out.writeLong(account_id);
		out.writeString(name);
		out.writeString(screen_name);
		out.writeString(profile_image_url);
		out.writeString(profile_banner_url);
		out.writeInt(color);
	}

	public static Account dummyInstance() {
		return new Account();
	}

	public static Account getAccount(final Context context, final long account_id) {
		if (context == null) return null;
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				Accounts.COLUMNS, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
		if (cur != null) {
			try {
				if (cur.getCount() > 0 && cur.moveToFirst()) {
					final Indices indices = new Indices(cur);
					cur.moveToFirst();
					return new Account(cur, indices);
				}
			} finally {
				cur.close();
			}
		}
		return null;
	}

	public static List<Account> getAccounts(final Context context, final boolean activatedOnly) {
		return getAccounts(context, activatedOnly, false);
	}

	public static List<Account> getAccounts(final Context context, final boolean activatedOnly,
			final boolean officialKeyOnly) {
		if (context == null) return Collections.emptyList();
		final ArrayList<Account> accounts = new ArrayList<Account>();
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				Accounts.COLUMNS, activatedOnly ? Accounts.IS_ACTIVATED + " = 1" : null, null, null);
		if (cur != null) {
			final Indices indices = new Indices(cur);
			cur.moveToFirst();
			while (!cur.isAfterLast()) {
				if (!officialKeyOnly) {
					accounts.add(new Account(cur, indices));
				} else {
					final String consumerKey = cur.getString(indices.consumer_key);
					final String consumerSecret = cur.getString(indices.consumer_secret);
					if (shouldForceUsingPrivateAPIs(context)
							|| isOfficialConsumerKeySecret(context, consumerKey, consumerSecret)) {
						accounts.add(new Account(cur, indices));
					}
				}
				cur.moveToNext();
			}
			cur.close();
		}
		return accounts;
	}

	public static AccountWithCredentials getAccountWithCredentials(final Context context, final long account_id) {
		if (context == null) return null;
		final Cursor cur = ContentResolverUtils.query(context.getContentResolver(), Accounts.CONTENT_URI,
				Accounts.COLUMNS, Accounts.ACCOUNT_ID + " = " + account_id, null, null);
		if (cur != null) {
			try {
				if (cur.getCount() > 0 && cur.moveToFirst()) {
					final Indices indices = new Indices(cur);
					cur.moveToFirst();
					return new AccountWithCredentials(cur, indices);
				}
			} finally {
				cur.close();
			}
		}
		return null;
	}

	public static class AccountWithCredentials extends Account {

		public int auth_type;
		public String consumer_key, consumer_secret;

		public AccountWithCredentials(final Cursor cursor, final Indices indices) {
			super(cursor, indices);
			auth_type = cursor.getInt(indices.auth_type);
			consumer_key = cursor.getString(indices.consumer_key);
			consumer_secret = cursor.getString(indices.consumer_secret);
		}

		public static final boolean isOfficialCredentials(final Context context, final AccountWithCredentials account) {
			if (account == null) return false;
			final boolean isOAuth = account.auth_type == Accounts.AUTH_TYPE_OAUTH
					|| account.auth_type == Accounts.AUTH_TYPE_XAUTH;
			final String consumerKey = account.consumer_key, consumerSecret = account.consumer_secret;
			return isOAuth && isOfficialConsumerKeySecret(context, consumerKey, consumerSecret);
		}
	}

	public static final class Indices {

		public final int screen_name, name, account_id, profile_image_url, profile_banner_url, color, is_activated,
				auth_type, consumer_key, consumer_secret;

		public Indices(final Cursor cursor) {
			screen_name = cursor.getColumnIndex(Accounts.SCREEN_NAME);
			name = cursor.getColumnIndex(Accounts.NAME);
			account_id = cursor.getColumnIndex(Accounts.ACCOUNT_ID);
			profile_image_url = cursor.getColumnIndex(Accounts.PROFILE_IMAGE_URL);
			profile_banner_url = cursor.getColumnIndex(Accounts.PROFILE_BANNER_URL);
			color = cursor.getColumnIndex(Accounts.COLOR);
			is_activated = cursor.getColumnIndex(Accounts.IS_ACTIVATED);
			auth_type = cursor.getColumnIndex(Accounts.AUTH_TYPE);
			consumer_key = cursor.getColumnIndex(Accounts.CONSUMER_KEY);
			consumer_secret = cursor.getColumnIndex(Accounts.CONSUMER_SECRET);
		}

		@Override
		public String toString() {
			return "Indices{screen_name=" + screen_name + ", name=" + name + ", account_id=" + account_id
					+ ", profile_image_url=" + profile_image_url + ", profile_banner_url=" + profile_banner_url
					+ ", user_color=" + color + ", is_activated=" + is_activated + "}";
		}
	}
}
