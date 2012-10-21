package edu.ucdavis.earlybird;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UploadReceiver extends BroadcastReceiver {

	public static final String ACTION_UPLOAD_PROFILE = "edu.ucdavis.earlybird.UPLOAD_PROFILE";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final boolean isWifi = ProfilingUtil.isOnWifi(context.getApplicationContext());
		final boolean isCharging = ProfilingUtil.isCharging(context.getApplicationContext());

		if (isWifi && isCharging) {
			new UploadTask(context).execute();
		}
	}
}
