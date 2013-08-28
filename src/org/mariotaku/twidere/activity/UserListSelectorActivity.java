package org.mariotaku.twidere.activity;

import java.net.URISyntaxException;

import android.content.Intent;
import android.os.Bundle;

public class UserListSelectorActivity extends BaseDialogActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			startActivity(Intent.parseUri("twidere://status?status_id=231757738231877633", 0));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		finish();
	}

}
