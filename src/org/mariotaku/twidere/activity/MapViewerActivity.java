package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.NativeMapFragment;
import org.mariotaku.twidere.fragment.WebMapFragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;

public class MapViewerActivity extends FragmentActivity implements Constants, OnClickListener {

	private Fragment mFragment;

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.close: {
				onBackPressed();
				break;
			}
			case R.id.center: {
				break;
			}
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_viewer);
		final Uri uri = getIntent().getData();
		if (uri == null || !AUTHORITY_MAP.equals(uri.getAuthority())) {
			finish();
			return;
		}
		final Bundle bundle = new Bundle();
		final String param_lat = uri.getQueryParameter(QUERY_PARAM_LAT);
		final String param_lng = uri.getQueryParameter(QUERY_PARAM_LNG);
		if (param_lat == null || param_lng == null) {
			finish();
			return;
		}
		try {
			bundle.putDouble(INTENT_KEY_LATITUDE, Double.valueOf(param_lat));
			bundle.putDouble(INTENT_KEY_LONGITUDE, Double.valueOf(param_lng));
		} catch (final NumberFormatException e) {
			finish();
			return;
		}
		mFragment = isNativeMapSupported() ? new NativeMapFragment() : new WebMapFragment();
		mFragment.setArguments(bundle);
		final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.map_frame, mFragment).commit();
	}

	private boolean isNativeMapSupported() {
		try {
			Class.forName("com.google.android.maps.MapActivity");
			Class.forName("com.google.android.maps.MapView");
		} catch (final ClassNotFoundException e) {
			return false;
		}
		return true;
	}
}
