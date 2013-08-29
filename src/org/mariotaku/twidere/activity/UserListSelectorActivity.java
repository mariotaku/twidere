package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;

import android.os.Bundle;

public class UserListSelectorActivity extends BaseDialogActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_user_list);
	}

}
