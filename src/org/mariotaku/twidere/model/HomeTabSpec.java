package org.mariotaku.twidere.model;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class HomeTabSpec extends TabSpec {

	public HomeTabSpec(String name, Integer icon, Class<? extends Fragment> cls, Bundle args, int position) {
		super(name, icon, cls, args, position);
	}

}
