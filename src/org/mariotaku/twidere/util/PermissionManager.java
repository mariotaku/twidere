package org.mariotaku.twidere.util;
import android.content.Context;
import android.content.SharedPreferences;
import org.mariotaku.twidere.Constants;
import static android.text.TextUtils.*;

public class PermissionManager implements Constants {
	
	private final SharedPreferences mPreferences;
	
	public PermissionManager(final Context context) {
		mPreferences = context.getSharedPreferences(PERMISSION_PREFERENCES_NAME, Context.MODE_PRIVATE);
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
}
