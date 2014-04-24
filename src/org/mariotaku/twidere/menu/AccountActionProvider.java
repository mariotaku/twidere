package org.mariotaku.twidere.menu;

import android.content.Context;
import android.content.Intent;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.mariotaku.twidere.TwidereConstants;
import org.mariotaku.twidere.model.Account;

public class AccountActionProvider extends ActionProvider implements TwidereConstants {

	public static final int MENU_GROUP = 201;

	private final Context mContext;

	private final Account[] mAccounts;

	public AccountActionProvider(final Context context) {
		super(context);
		mContext = context;
		mAccounts = Account.getAccounts(context, false, false);
	}

	@Override
	public boolean hasSubMenu() {
		return true;
	}

	@Override
	public View onCreateActionView() {
		return null;
	}

	@Override
	public void onPrepareSubMenu(final SubMenu subMenu) {
		subMenu.removeGroup(MENU_GROUP);
		for (final Account account : mAccounts) {
			final int accountHash = System.identityHashCode(account.account_id);
			final MenuItem item = subMenu.add(MENU_GROUP, accountHash, 0, account.name);
			final Intent intent = new Intent();
			intent.putExtra(EXTRA_ACCOUNT, account);
			item.setIntent(intent);
		}
		subMenu.setGroupCheckable(MENU_GROUP, true, true);
	}

}
