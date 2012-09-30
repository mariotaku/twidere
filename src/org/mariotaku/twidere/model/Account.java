package org.mariotaku.twidere.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Context;
import android.database.Cursor;

public class Account implements CharSequence {

	public final String username;
	public final long account_id;

	Account(final Cursor cursor, final Indices indices) {
		username = cursor.getString(indices.username);
		account_id = cursor.getLong(indices.account_id);
	}

	@Override
	public char charAt(final int index) {
		return username.charAt(index);
	}

	@Override
	public int length() {
		return username.length();
	}

	@Override
	public CharSequence subSequence(final int start, final int end) {
		return username.subSequence(start, end);
	}

	@Override
	public String toString() {
		return username;
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

		public final int username, account_id;

		public Indices(final Cursor cursor) {
			username = cursor.getColumnIndex(Accounts.SCREEN_NAME);
			account_id = cursor.getColumnIndex(Accounts.ACCOUNT_ID);
		}
	}
}
