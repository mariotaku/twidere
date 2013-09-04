package org.mariotaku.twidere.util;

import java.lang.reflect.Field;

import android.app.FragmentManager;

public class FragmentManagerAccessor {

	public static boolean isStateSaved(final FragmentManager fm) {
		try {
			final Field mStateSavedField = FragmentManager.class.getField("mStateSaved");
			final Object mStateSaved = mStateSavedField.get(fm);
			if (mStateSaved instanceof Boolean) return (Boolean) mStateSaved;
		} catch (final NoSuchFieldException e) {
		} catch (final IllegalArgumentException e) {
		} catch (final IllegalAccessException e) {
		}
		return false;
	}

}
