package org.mariotaku.twidere.receiver;

import java.util.List;

import org.mariotaku.twidere.Constants;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.util.Log;

public class CommandReceiver extends BroadcastReceiver implements Constants {

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (!INTENT_ACTION_SERVICE_COMMAND.equals(intent.getAction())) return;
		if (!checkPermission(context)) return;
	}

	private boolean checkPermission(final Context context) {
		final String pname = getCallingPackageName(context);
		Log.d(LOGTAG, "Package " + pname + " called.");
		// TODO Stub!
		return true;
	}

	private static String getCallingPackageName(final Context context) {
		final int pid = Binder.getCallingPid();
		final ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		final List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
		if (processes == null) return null;
		for (final RunningAppProcessInfo process : processes) {
			if (process.pid == pid && process.pkgList.length > 0) return process.pkgList[0];
		}
		return null;
	}

	private static boolean isSameCertificate(final Context context) {
		final PackageManager pm = context.getPackageManager();
		return true;
	}

}
