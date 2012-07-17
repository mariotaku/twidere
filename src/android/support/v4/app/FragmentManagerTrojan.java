package android.support.v4.app;

public class FragmentManagerTrojan {

	public static boolean isStateSaved(FragmentManager fm) {
		if (fm instanceof FragmentManagerImpl) {
			return ((FragmentManagerImpl) fm).mStateSaved;
		}
		return false;
	}
}
