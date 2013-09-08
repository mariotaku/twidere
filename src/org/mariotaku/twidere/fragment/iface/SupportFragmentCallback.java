package org.mariotaku.twidere.fragment.iface;

import android.support.v4.app.Fragment;

public interface SupportFragmentCallback {

	public Fragment getCurrentVisibleFragment();

	public void onSetUserVisibleHint(Fragment fragment, boolean isVisibleToUser);
}
