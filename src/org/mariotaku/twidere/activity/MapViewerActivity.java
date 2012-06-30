package org.mariotaku.twidere.activity;

import android.support.v4.app.FragmentActivity;

public class MapViewerActivity extends FragmentActivity {

	private boolean isNativeMapSupported() {
		try {
			Class.forName("com.google.android.maps.MapActivity");
			Class.forName("com.google.android.maps.MapView");
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}
}
