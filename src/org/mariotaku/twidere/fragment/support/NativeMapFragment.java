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

package org.mariotaku.twidere.fragment.support;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.MapInterface;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.ResourceProxyImpl;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;
import java.util.List;

public class NativeMapFragment extends BaseSupportFragment implements MapInterface, Constants {

	private static final int MAPVIEW_ID = 0x70000001;

	@Override
	public void center() {
		final Bundle extras = getArguments();
		if (extras == null || !extras.containsKey(EXTRA_LATITUDE) || !extras.containsKey(EXTRA_LONGITUDE)) return;
		final double lat = extras.getDouble(EXTRA_LATITUDE, 0.0), lng = extras.getDouble(EXTRA_LONGITUDE, 0.0);
		final GeoPoint gp = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		final MapView mapView = (MapView) getView().findViewById(MAPVIEW_ID);
		final IMapController mc = mapView.getController();
		mc.animateTo(gp);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		final MapView mapView = (MapView) getView().findViewById(MAPVIEW_ID);
		mapView.setMultiTouchControls(true);
		mapView.setBuiltInZoomControls(true);
		final List<Overlay> overlays = mapView.getOverlays();
		final double lat = args.getDouble(EXTRA_LATITUDE, 0.0), lng = args.getDouble(EXTRA_LONGITUDE, 0.0);
		final GeoPoint gp = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		final Drawable d = getResources().getDrawable(R.drawable.ic_map_marker);
		final Itemization markers = new Itemization(d, mapView.getResourceProxy());
		final OverlayItem overlayitem = new OverlayItem("", "", gp);
		markers.addOverlay(overlayitem);
		overlays.add(markers);
		final IMapController mc = mapView.getController();
		mc.setZoom(12);
		mc.animateTo(gp);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = new MapView(getActivity(), 256, new ResourceProxyImpl(getActivity()));
		view.setId(MAPVIEW_ID);
		return view;
	}

	static class Itemization extends ItemizedOverlay<OverlayItem> {

		private final ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

		public Itemization(final Drawable defaultMarker, final ResourceProxy proxy) {
			super(boundCenterBottom(defaultMarker), proxy);
		}

		public void addOverlay(final OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		public boolean onSnapToItem(final int x, final int y, final Point snapPoint, final IMapView mapView) {
			return false;
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