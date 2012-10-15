package edu.ucdavis.earlybird;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class UploadReceiver extends BroadcastReceiver {
	public static boolean isWifi = false;
	public static boolean isCharging = false;

	public static final String ACTION_UPLOAD_PROFILE = "edu.ucdavis.earlybird.UPLOAD_PROFILE";

	@Override
	public void onReceive(Context context, Intent intent) {
		isWifi = ProfilingUtil.isOnWifi(context.getApplicationContext());
		isCharging = ProfilingUtil.isCharging(context.getApplicationContext());

		if (isWifi && isCharging) {
			new UploadTask(context).execute();
		}
	}
}
