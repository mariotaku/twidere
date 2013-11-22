package org.mariotaku.twidere.model;

import android.content.Context;
import android.content.SharedPreferences;

import org.mariotaku.twidere.Constants;

public class AccountPreferences implements Constants {

	private final long mAccountId;
	private final SharedPreferences mPreferences;

	public AccountPreferences(final Context context, final long accountId) {
		mAccountId = accountId;
		final String name = ACCOUNT_PREFERENCES_NAME_PREFIX + accountId;
		mPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	public long getAccountId() {
		return mAccountId;
	}

	public boolean isAutoRefreshDirectMessagesEnabled() {
		return mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH_DIRECT_MESSAGES,
				PREFERENCE_DEFAULT_AUTO_REFRESH_DIRECT_MESSAGES);
	}

	public boolean isAutoRefreshEnabled() {
		return mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, PREFERENCE_DEFAULT_AUTO_REFRESH);
	}

	public boolean isAutoRefreshHomeTimelineEnabled() {
		return mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH_HOME_TIMELINE,
				PREFERENCE_DEFAULT_AUTO_REFRESH_HOME_TIMELINE);
	}

	public boolean isAutoRefreshMentionsEnabled() {
		return mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH_MENTIONS, PREFERENCE_DEFAULT_AUTO_REFRESH_MENTIONS);
	}

	public boolean isAutoRefreshTrendsEnabled() {
		return mPreferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH_TRENDS, PREFERENCE_DEFAULT_AUTO_REFRESH_TRENDS);
	}

	public boolean isNotificationEnabled() {
		return mPreferences.getBoolean(PREFERENCE_KEY_NOTIFICATION, PREFERENCE_DEFAULT_NOTIFICATION);
	}

	public static AccountPreferences[] getAccountPreferences(final Context context, final long[] accountIds) {
		if (context == null || accountIds == null) return null;
		final AccountPreferences[] preferences = new AccountPreferences[accountIds.length];
		for (int i = 0, j = preferences.length; i < j; i++) {
			preferences[i] = new AccountPreferences(context, accountIds[i]);
		}
		return preferences;
	}

	public static long[] getAutoRefreshEnabledAccountIds(final Context context, final long[] accountIds) {
		if (context == null || accountIds == null) return null;
		final long[] temp = new long[accountIds.length];
		int i = 0;
		for (final long accountId : accountIds) {
			if (new AccountPreferences(context, accountId).isAutoRefreshEnabled()) {
				temp[i++] = accountId;
			}
		}
		final long[] enabledIds = new long[i];
		System.arraycopy(temp, 0, enabledIds, 0, i);
		return enabledIds;
	}
}
