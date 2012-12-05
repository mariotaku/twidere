package org.mariotaku.twidere.util;

import static android.text.TextUtils.*;

import org.mariotaku.twidere.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Process;

public class PermissionManager implements Constants {
	
	private final SharedPreferences mPreferences;
	private final PackageManager mPackageManager;
	
	public PermissionManager(final Context context) {
		mPreferences = context.getSharedPreferences(PERMISSION_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPackageManager = context.getPackageManager();
	}
	
	public boolean checkPermission(final int uid, final int level) {
		if (mPackageManager.checkSignatures(Process.myUid(), uid) == PackageManager.SIGNATURE_MATCH) return true;
		final String pname = getPackageNameByUid(uid);
		return getPermissionLevel(pname) >= level;
	}
	
	public int getPermissionLevel(final String package_name) {
		if (isEmpty(package_name)) return PERMISSION_LEVEL_INVALID;
		return mPreferences.getInt(package_name, PERMISSION_LEVEL_NONE);
	}
	
	public boolean accept(final String package_name, final int level) {
		if (package_name == null || level <= PERMISSION_LEVEL_NONE || level > PERMISSION_LEVEL_ACCOUNTS) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(package_name, level);
		return editor.commit();
	}
	
	public boolean deny(final String package_name) {
		if (package_name == null ) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(package_name, PERMISSION_LEVEL_DENIED);
		return editor.commit();
		
	}
	
	public boolean revoke(final String package_name) {
		if (package_name == null ) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.remove(package_name);
		return editor.commit();
	}
	

	private String getPackageNameByUid(final int uid) {
		final String[] pkgs = mPackageManager.getPackagesForUid(uid);
		if (pkgs != null && pkgs.length > 0) return pkgs[0];
		return null;
	}
}
