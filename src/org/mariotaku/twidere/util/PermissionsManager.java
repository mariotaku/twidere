/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static android.text.TextUtils.isEmpty;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Process;

import org.mariotaku.twidere.Constants;

import java.util.HashMap;
import java.util.Map;

public class PermissionsManager implements Constants {

	private final SharedPreferences mPreferences;
	private final PackageManager mPackageManager;
	private final Context mContext;

	public PermissionsManager(final Context context) {
		mContext = context;
		mPreferences = context.getSharedPreferences(PERMISSION_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPackageManager = context.getPackageManager();
	}

	public boolean accept(final String package_name, final int permissions) {
		if (package_name == null || permissions < PERMISSION_NONE) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(package_name, permissions);
		return editor.commit();
	}

	public boolean checkCallingPermission(final int level) {
		return checkPermission(Binder.getCallingUid(), level);
	}

	public boolean checkPermission(final int uid, final int required_permissions) {
		if (required_permissions < PERMISSION_NONE)
			throw new IllegalArgumentException("invalid permissions " + required_permissions);
		if (required_permissions == PERMISSION_NONE) return true;
		if (Process.myUid() == uid) return true;
		if (checkSignature(uid)) return true;
		final String pname = getPackageNameByUid(uid);
		final int permissions = getPermissions(pname);
		return permissions > PERMISSION_NONE && permissions % required_permissions == 0;
	}

	public boolean checkPermission(final String pname, final int required_permissions) {
		if (pname == null) throw new NullPointerException();
		if (required_permissions < PERMISSION_NONE)
			throw new IllegalArgumentException("invalid permissions " + required_permissions);
		if (required_permissions == PERMISSION_NONE) return true;
		if (mContext.getPackageName().equals(pname)) return true;
		// if (checkSignature(pname)) return true;
		final int permissions = getPermissions(pname);
		return permissions > PERMISSION_NONE && permissions % required_permissions == 0;
	}

	public boolean checkSignature(final int uid) {
		final String pname = getPackageNameByUid(uid);
		return checkSignature(pname);
	}

	public boolean checkSignature(final String pname) {
		return mPackageManager.checkSignatures(pname, mContext.getPackageName()) == PackageManager.SIGNATURE_MATCH;
	}

	public boolean deny(final String package_name) {
		if (package_name == null) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(package_name, PERMISSION_DENIED);
		return editor.commit();

	}

	public Map<String, Integer> getAll() {
		final Map<String, Integer> map = new HashMap<String, Integer>();
		for (final Map.Entry<String, ?> entry : mPreferences.getAll().entrySet()) {
			if (entry.getValue() instanceof Integer) {
				map.put(entry.getKey(), (Integer) entry.getValue());
			}
		}
		return map;
	}

	public String getPackageNameByUid(final int uid) {
		final String[] pkgs = mPackageManager.getPackagesForUid(uid);
		if (pkgs != null && pkgs.length > 0) return pkgs[0];
		return null;
	}

	public int getPermissions(final int uid) {
		return getPermissions(getPackageNameByUid(uid));
	}

	public int getPermissions(final String package_name) {
		if (isEmpty(package_name)) return PERMISSION_INVALID;
		return mPreferences.getInt(package_name, PERMISSION_NONE);
	}

	public boolean revoke(final String package_name) {
		if (package_name == null) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.remove(package_name);
		return editor.commit();
	}
}
