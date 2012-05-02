package org.mariotaku.twidere.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class GoogleWebMapFragment extends WebViewFragment {

	private final Uri mUri = Uri.parse("file:///android_asset/mapview.html");

	private double latitude, longitude;

	private boolean disable_ui = false;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getLocation();
		setupWebView();
	}

	/**
	 * The Location Manager manages location providers. This code searches for
	 * the best provider of data (GPS, WiFi/cell phone tower lookup, some other
	 * mechanism) and finds the last known location.
	 **/
	private void getLocation() {
		Bundle bundle = getArguments();
		if (bundle != null) {
			latitude = bundle.getDouble(INTENT_KEY_LATITUDE, 0.0);
			longitude = bundle.getDouble(INTENT_KEY_LONGITUDE, 0.0);
		}
	}

	/** Sets up the WebView object and loads the URL of the page **/
	private void setupWebView() {

		WebView webview = getWebView();
		webview.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
		setWebViewClient(new MapWebViewClient(getSherlockActivity()));
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
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public boolean isUIDisabled() {
			return disable_ui;
		}

	}

	private class MapWebViewClient extends DefaultWebViewClient {

		public MapWebViewClient(SherlockFragmentActivity activity) {
			super(activity);
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
