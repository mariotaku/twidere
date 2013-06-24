/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.Utils;

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

	public void center() {
		final Bundle extras = getIntent().getExtras();
		if (extras == null || !extras.containsKey(INTENT_KEY_LATITUDE) || !extras.containsKey(INTENT_KEY_LONGITUDE))
			return;
		final double lat = extras.getDouble(INTENT_KEY_LATITUDE, 0.0), lng = extras
				.getDouble(INTENT_KEY_LONGITUDE, 0.0);
		final GeoPoint gp = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		final MapController mc = mMapView.getController();
		mc.animateTo(gp);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle extras = getIntent().getExtras();
		if (extras == null || !extras.containsKey(INTENT_KEY_LATITUDE) || !extras.containsKey(INTENT_KEY_LONGITUDE)) {
			finish();
			return;
		}
		final boolean debug_build = Utils.isDebugBuild(this);
		mMapView = new MapView(this, debug_build ? GOOGLE_MAPS_API_KEY_DEBUG : GOOGLE_MAPS_API_KEY_RELEASE);
		mMapView.setClickable(true);
		final List<Overlay> overlays = mMapView.getOverlays();
		final double lat = extras.getDouble(INTENT_KEY_LATITUDE, 0.0), lng = extras
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

	static class Itemization extends ItemizedOverlay<OverlayItem> {

		private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public Itemization(final Drawable defaultMarker) {
			super(boundCenterBottom(defaultMarker));
		}

		public void addOverlay(final OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		@Override
		protected OverlayItem createItem(final int i) {
			return mOverlays.get(i);
		}

		protected static Drawable boundCenterBottom(final Drawable d) {
			d.setBounds(-d.getIntrinsicWidth() / 2, -d.getIntrinsicHeight(), d.getIntrinsicWidth() / 2, 0);
			return d;
		}

	}
}
