package org.mariotaku.twidere.menu;

import android.content.Context;
import android.view.ActionProvider;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import org.mariotaku.twidere.model.Account;

public class AccountActionProvider extends ActionProvider {

	private static final int MENU_GROUP = 201;

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
			final MenuItem item = subMenu.add(MENU_GROUP, (int) account.account_id, 0, account.name);
		}
		subMenu.setGroupCheckable(MENU_GROUP, true, true);
	}

}
