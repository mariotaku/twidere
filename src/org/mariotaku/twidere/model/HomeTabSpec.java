package org.mariotaku.twidere.model;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class HomeTabSpec extends TabSpec {

	public HomeTabSpec(final String name, final Integer icon, final Class<? extends Fragment> cls, final Bundle args,
			final int position) {
		super(name, icon, cls, args, position);
	}

}
