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

package org.mariotaku.twidere.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import edu.ucdavis.earlybird.UCDService;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.service.RefreshService;

public class ConnectivityStateReceiver extends BroadcastReceiver implements Constants {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final SharedPreferences preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE);
		if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) return;
		if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, true)) {
			context.stopService(new Intent(context, RefreshService.class));
			return;
		}
		if (preferences.getBoolean(PREFERENCE_KEY_AUTO_REFRESH, false)) {
			context.startService(new Intent(context, RefreshService.class));
		}
		if (preferences.getBoolean(PREFERENCE_KEY_UCD_DATA_PROFILING, false)) {
			context.startService(new Intent(context, UCDService.class));
		}
	}
}
