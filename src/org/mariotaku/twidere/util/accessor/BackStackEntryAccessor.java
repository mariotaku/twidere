package org.mariotaku.twidere.util.accessor;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;

import java.lang.reflect.Field;

public class BackStackEntryAccessor {

	public static Fragment getFragmentInBackStackRecord(final FragmentManager.BackStackEntry entry) {
		try {
			final Field mHeadField = BackStackEntry.class.getField("mHead");
			final Object mHead = mHeadField.get(entry);
			final Field fragmentField = mHead.getClass().getField("fragment");
			final Object fragment = fragmentField.get(mHead);
			if (fragment instanceof Fragment) return (Fragment) fragment;
		} catch (final NoSuchFieldException e) {
		} catch (final IllegalArgumentException e) {
		} catch (final IllegalAccessException e) {
		}
		return null;
	}
}
