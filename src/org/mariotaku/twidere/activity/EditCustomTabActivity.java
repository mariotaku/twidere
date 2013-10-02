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

package org.mariotaku.twidere.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.CustomTabConfiguration;

public class EditCustomTabActivity extends BaseSupportDialogActivity {

    private Spinner mAccountSpinner;
    private TextView mEditTabName;
    private AccountsSpinnerAdapter mAccountsAdapter;
    private CustomTabConfiguration mTabConfiguration;

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        mEditTabName = (TextView) findViewById(R.id.tab_name);
        mAccountSpinner = (Spinner) findViewById(R.id.account_spinner);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intent = getIntent();
        final String type = intent.getStringExtra(INTENT_KEY_TYPE);
        mTabConfiguration = CustomTabConfiguration.get(type);
        if (mTabConfiguration == null) {
            finish();
            return;
        }
        setContentView(R.layout.edit_custom_tab);
        mAccountsAdapter = new AccountsSpinnerAdapter(this);
        mAccountsAdapter.addAll(Account.getAccounts(this, false));
        mAccountSpinner.setVisibility(mTabConfiguration.isAccountIdRequired() ? View.VISIBLE
                : View.GONE);
        mAccountSpinner.setAdapter(mAccountsAdapter);
        mEditTabName.setText(mTabConfiguration.getDefaultTitle());
    }

}
