/*
 * Copyright (C) 2012 SP-time. All right reserved.
 *
 */

package org.mariotaku.twidere.receiver;

import org.mariotaku.twidere.Constants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * 
 * @author mariotaku
 * 
 */
public class BootCompletedReceiver extends BroadcastReceiver implements Constants {

	@Override
	public void onReceive(Context context, Intent intent) {

		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		final boolean background_service_not_allowed = preferences.getBoolean(PREFERENCE_KEY_STOP_SERVICE_AFTER_CLOSED,
				true);
		if (background_service_not_allowed) return;
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			context.startService(new Intent(INTENT_ACTION_SERVICE));
		}

	}
}