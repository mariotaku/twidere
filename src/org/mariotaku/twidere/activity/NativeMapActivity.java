package org.mariotaku.twidere.activity;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;

import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class NativeMapActivity extends MapActivity implements Constants {

	private MapView mMapView;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		final Bundle bundle = getIntent().getExtras();
		if (bundle == null || !bundle.containsKey(INTENT_KEY_LATITUDE) || !bundle.containsKey(INTENT_KEY_LONGITUDE)) {
			finish();
			return;
		}
		mMapView = new MapView(this, MAPS_API_KEY) {
			{
				setClickable(true);
			}
		};
		final List<Overlay> overlays = mMapView.getOverlays();
		final double lat = bundle.getDouble(INTENT_KEY_LATITUDE, 0.0), lng = bundle
				.getDouble(INTENT_KEY_LONGITUDE, 0.0);
		final GeoPoint gp = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		final Drawable d = getResources().getDrawable(R.drawable.ic_map_marker);
		final Itemization markers = new Itemization(d);
		final OverlayItem overlayitem = new OverlayItem(gp, "", "");
		markers.addOverlay(overlayitem);
		overlays.add(markers);
		final MapController mc = mMapView.getController();
		mc.setZoom(12);
		mc.animateTo(gp);
		setContentView(mMapView);
	}

	private static class Itemization extends ItemizedOverlay<OverlayItem> {

		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public Itemization(Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return mOverlays.get(i);
		}

	}
}
