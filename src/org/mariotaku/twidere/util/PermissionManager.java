package org.mariotaku.twidere.util;
import android.content.pm.PackageManager;
import android.app.ActivityManager;
import android.content.SharedPreferences;

public class PermissionManager {
	PackageManager mPackageManager;
	ActivityManager am;
	SharedPreferences prefs;
	private String getPackageNameByPid(final int pid) {
		for (ActivityManager.RunningAppProcessInfo proc : am.getRunningAppProcesses()) {
			if (proc.pid == pid && proc.pkgList.length > 0) return proc.pkgList[0];
		}
		return null;
	}
}
