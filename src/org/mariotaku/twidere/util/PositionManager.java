package org.mariotaku.twidere.util;
import android.content.Context;
import org.mariotaku.twidere.Constants;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class PositionManager implements Constants {

	private final SharedPreferences mPreferences;

	public PositionManager(final Context context) {
		mPreferences = context.getSharedPreferences(TIMELINE_POSITIONS_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}
	
	public boolean setPosition(final String key, final long status_id) {
		if (TextUtils.isEmpty(key)) return false;
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.putLong(key, status_id);
		return editor.commit();	
	}
	
	public long getPosition(final String key) {
		if (TextUtils.isEmpty(key)) return -1;
		return mPreferences.getLong(key, -1);
	}

}
