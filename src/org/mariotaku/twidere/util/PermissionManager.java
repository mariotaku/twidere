package org.mariotaku.twidere.util;

import static android.text.TextUtils.isEmpty;

import org.mariotaku.twidere.Constants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Process;

public class PermissionManager implements Constants {

	private final SharedPreferences mPreferences;
	private final PackageManager mPackageManager;
	private final Context mContext;

	public PermissionManager(final Context context) {
		mContext = context;
		mPreferences = context.getSharedPreferences(PERMISSION_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPackageManager = context.getPackageManager();
	}

	public boolean accept(final String package_name, final int level) {
		if (package_name == null || level <= PERMISSION_LEVEL_NONE || level > PERMISSION_LEVEL_ACCOUNTS) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(package_name, level);
		return editor.commit();
	}

	public boolean checkCallingPermission(final int level) {
		return checkPermission(Binder.getCallingUid(), level);
	}

	public boolean checkPermission(final int uid, final int level) {
		if (Process.myUid() == uid) return true;
		if (isSameSignature(uid)) return true;
		final String pname = getPackageNameByUid(uid);
		return getPermissionLevel(pname) >= level;
	}

	public boolean deny(final String package_name) {
		if (package_name == null) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(package_name, PERMISSION_LEVEL_DENIED);
		return editor.commit();

	}

	public int getPermissionLevel(final String package_name) {
		if (isEmpty(package_name)) return PERMISSION_LEVEL_INVALID;
		return mPreferences.getInt(package_name, PERMISSION_LEVEL_NONE);
	}

	public boolean isSameSignature(final int uid) {
		return mPackageManager.checkSignatures(getPackageNameByUid(uid), mContext.getPackageName()) == PackageManager.SIGNATURE_MATCH;
	}

	public boolean revoke(final String package_name) {
		if (package_name == null) return false;
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
