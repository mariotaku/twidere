package org.mariotaku.twidere.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Context;
import android.database.Cursor;

public class Account {

	public final String username;
	public final long account_id;
	
	Account(Cursor cursor, Indices indices) {
		username = cursor.getString(indices.username);
		account_id = cursor.getLong(indices.account_id);
	}
	
	public static class Indices {
		
		public final int username, account_id;
		
		public Indices(Cursor cursor) {
			username = cursor.getColumnIndex(Accounts.USERNAME);
			account_id = cursor.getColumnIndex(Accounts.USER_ID);
		}
	}
	
	public static List<Account> getAccounts(Context context, boolean activated_only) {
		if (context == null) Collections.emptyList();
		final ArrayList<Account> accounts = new ArrayList<Account>();
		final String[] cols = new String[] { Accounts.USERNAME, Accounts.USER_ID };
		final Cursor cur = context.getContentResolver().query(Accounts.CONTENT_URI, cols, activated_only ? (Accounts.IS_ACTIVATED + " = 1") : null,
				null, null);
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
	
	public String toString() {
		return username;
	}
}
