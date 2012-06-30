package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;

import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class NativeMapActivity extends MapActivity implements Constants {
    
	private MapView mMapView; 
	
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mMapView = new MapView(this, MAPS_API_KEY);
        setContentView(mMapView);
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
}
