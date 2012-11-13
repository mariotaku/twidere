package android.support.v4.app;

import android.view.MenuItem;

public class FragmentManagerTrojan {

	public static boolean dispatchOptionsItemSelected(final FragmentManager fm, final MenuItem item) {
		if (fm instanceof FragmentManagerImpl) return ((FragmentManagerImpl) fm).dispatchOptionsItemSelected(item);
		return false;
	}

	public static boolean isStateSaved(final FragmentManager fm) {
		if (fm instanceof FragmentManagerImpl) return ((FragmentManagerImpl) fm).mStateSaved;
		return false;
	}

}
