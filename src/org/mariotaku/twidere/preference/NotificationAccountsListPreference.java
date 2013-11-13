package org.mariotaku.twidere.preference;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.fragment.AccountNotificationSettingsFragment;
import org.mariotaku.twidere.model.Account;

public class NotificationAccountsListPreference extends AccountsListPreference implements TwidereConstants {

	public NotificationAccountsListPreference(final Context context) {
		super(context);
	}

	public NotificationAccountsListPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public NotificationAccountsListPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void setupPreference(final AccountItemPreference preference, final Account account) {
		preference.setFragment(AccountNotificationSettingsFragment.class.getName());
		final Bundle args = preference.getExtras();
		args.putParcelable(EXTRA_ACCOUNT, account);
	}

}
