package android.support.v4.app;

public class BackStackEntryTrojan {

	public static Fragment getFragmentInBackStackRecord(FragmentManager.BackStackEntry entry) {
		if (entry instanceof BackStackRecord) return ((BackStackRecord) entry).mHead.fragment;
		return null;
	}
}
