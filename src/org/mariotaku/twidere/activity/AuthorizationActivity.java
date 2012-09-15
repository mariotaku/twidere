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

import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.parseInt;
import static org.mariotaku.twidere.util.Utils.*;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.WebViewProxySettings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import twitter4j.HostAddressResolver;
import org.mariotaku.twidere.util.TwidereHostAddressResolver;
import java.io.IOException;
import android.os.Build;
import java.util.HashMap;
import org.mariotaku.twidere.util.WebViewLoadUrlAccessor;
import android.annotation.TargetApi;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import org.mariotaku.actionbarcompat.ActionBar;
import java.net.URL;
import android.os.AsyncTask;
import org.mariotaku.twidere.util.Utils;
import java.net.HttpURLConnection;
import java.net.Proxy;

@SuppressLint({ "SetJavaScriptEnabled" })
public class AuthorizationActivity extends BaseActivity {

	private Uri authUrl;
	private SharedPreferences mPreferences;
	private WebView mWebView;
	private WebSettings mWebSettings;
	private HostAddressResolver mResolver;
	private ActionBar mActionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		try {
			mResolver = new TwidereHostAddressResolver(this, true);
		} catch (IOException e) {}
		super.onCreate(savedInstanceState);
		authUrl = getIntent().getData();
		if (authUrl == null) {
			Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		mWebView = new WebView(this);
		setContentView(mWebView);
		mActionBar = getSupportActionBar();
		mActionBar.setDisplayHomeAsUpEnabled(true);
		mWebView.getSettings().setBuiltInZoomControls(true);
		loadUrl(authUrl.toString());
		mWebView.setWebViewClient(new AuthorizationWebViewClient());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebSettings = mWebView.getSettings();
		mWebSettings.setJavaScriptEnabled(true);
		mWebSettings.setBlockNetworkImage(false);
		mWebSettings.setSaveFormData(true);
		mWebSettings.setSavePassword(true);

		final boolean enable_proxy = mPreferences.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		final String proxy_host = mPreferences.getString(PREFERENCE_KEY_PROXY_HOST, null);
		final int proxy_port = parseInt(mPreferences.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
		if (enable_proxy && !isNullOrEmpty(proxy_host) && proxy_port > 0) {
			WebViewProxySettings.setProxy(mWebView, proxy_host, proxy_port);
		} else {
			WebViewProxySettings.resetProxy(mWebView);
		}

	}
	
	private void loadUrl(String url_string) {
		final URL url = parseURL(url_string);
		final String host = url.getHost();
		if (mResolver == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			mWebView.loadUrl(url_string);
			return;
		}
		final String resolved_host;
		try {
			resolved_host = mResolver.resolve(host);
		} catch (IOException e) {
			resolved_host = null;
		}		
		if (resolved_host == null) {
			mWebView.loadUrl(url_string);
			return;
		}
		final String resolved_url_string = url_string.replace("://" + host, "://" + resolved_host);
		final HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("Host", host);
		WebViewLoadUrlAccessor.loadUrl(mWebView, resolved_url_string, headers);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	class WebPageLoader extends AsyncTask<Void, Void, String> {

		protected String doInBackground(Void... args) {
			try {
				final URL url = parseURL(null);
				final Proxy proxy = getProxy(AuthorizationActivity.this);
				final boolean ignore_ssl_error = mPreferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
				final HttpURLConnection conn = getConnection(url, ignore_ssl_error, proxy, mResolver);
				conn.addRequestProperty("User-Agent", mWebSettings.getUserAgentString());
			} catch (IOException e) {}
			return null;
		}
		
		
	}

	class AuthorizationWebViewClient extends WebViewClient {

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
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);		
			showErrorToast(AuthorizationActivity.this, description, true);
			finish();
		}

		@TargetApi(8)
		@Override
		public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
			if (mPreferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false)) {
				handler.proceed();
			} else {
				handler.cancel();
			}
		}
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			final Uri uri = Uri.parse(url);
			if (url.startsWith(DEFAULT_OAUTH_CALLBACK)) {
				final String oauth_verifier = uri.getQueryParameter(OAUTH_VERIFIER);
				if (oauth_verifier != null) {
					final Bundle bundle = new Bundle();
					bundle.putString(OAUTH_VERIFIER, oauth_verifier);
					setResult(RESULT_OK, new Intent().putExtras(bundle));
					finish();
				}
			} else {
				loadUrl(url);
			}
			return true;
		}
	}
}
