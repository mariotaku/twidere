package org.mariotaku.twidere.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.view.Window;
import com.google.inject.Inject;

public class MapActivity extends WebViewActivity implements LocationListener {

	private Uri mUri = Uri.parse("file:///android_asset/mapview.html");
	private Location mostRecentLocation;
	@Inject private LocationManager mLocationManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		getLocation();
		setupWebView();

	}

	/** Sets the mostRecentLocation object to the current location of the device **/
	@Override
	public void onLocationChanged(Location location) {
		mostRecentLocation = location;
	}

	/**
	 * The following methods are only necessary because WebMapActivity
	 * implements LocationListener
	 **/
	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	/**
	 * The Location Manager manages location providers. This code searches for
	 * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
	 * mechanism) and finds the last known location.
	 **/
	private void getLocation() {
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		String provider = mLocationManager.getBestProvider(criteria, true);

		// In order to make sure the device is getting location, request
		// updates. locationManager.requestLocationUpdates(provider, 1, 0,
		// this);
		mostRecentLocation = mLocationManager.getLastKnownLocation(provider);
	}

	/** Sets up the WebView object and loads the URL of the page **/
	private void setupWebView() {

		WebView webview = getWebView();
		webview.getSettings().setJavaScriptEnabled(true);
		webview.setVerticalScrollBarEnabled(false);
		setWebViewClient(new MapWebViewClient());
		loadUrl(mUri.toString());

		/** Allows JavaScript calls to access application resources **/
		webview.addJavascriptInterface(new JavaScriptInterface(), "android");

	}

	/**
	 * Sets up the interface for getting access to Latitude and Longitude data
	 * from device
	 **/
	@SuppressWarnings("unused")
	private class JavaScriptInterface {

		public double getLatitude() {
			return mostRecentLocation.getLatitude();
		}

		public double getLongitude() {
			return mostRecentLocation.getLongitude();
		}

	}

	private class MapWebViewClient extends WebViewClient {

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description,
				String failingUrl) {
			Log.e(LOGTAG, description);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri uri = Uri.parse(url);
			if (uri.getScheme().equals(mUri.getScheme())) return false;

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	}

}