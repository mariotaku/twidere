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

import static org.mariotaku.twidere.util.Utils.parseInt;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.WebViewProxySettings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressLint({ "SetJavaScriptEnabled" })
public class AuthorizationActivity extends BaseActivity {

	private Uri authUrl;
	private SharedPreferences mPreferences;
	private WebView mWebView;
	private WebSettings mWebSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		authUrl = getIntent().getData();
		if (authUrl == null) {
			Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		setContentView(R.layout.webview);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mWebView = (WebView) findViewById(R.id.webview);
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.loadUrl(authUrl.toString());
		mWebView.setWebViewClient(new AuthorizationWebViewClient());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebSettings = mWebView.getSettings();
		mWebSettings.setLoadsImagesAutomatically(true);
		mWebSettings.setJavaScriptEnabled(true);
		mWebSettings.setBlockNetworkImage(false);
		mWebSettings.setSaveFormData(true);
		mWebSettings.setSavePassword(true);

		final boolean enable_proxy = mPreferences.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		final String proxy_host = mPreferences.getString(PREFERENCE_KEY_PROXY_HOST, null);
		final int proxy_port = parseInt(mPreferences.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
		if (enable_proxy && proxy_host != null && proxy_port > 0) {
			WebViewProxySettings.setProxy(mWebView, proxy_host, proxy_port);
		} else {
			WebViewProxySettings.resetProxy(mWebView);
		}

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
			Toast.makeText(AuthorizationActivity.this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
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
			if (uri.getHost().equals(authUrl.getHost()))
				return false;
			else if (url.startsWith(DEFAULT_OAUTH_CALLBACK)) {
				final String oauth_verifier = uri.getQueryParameter(OAUTH_VERIFIER);
				if (oauth_verifier != null) {
					final Bundle bundle = new Bundle();
					bundle.putString(OAUTH_VERIFIER, oauth_verifier);
					setResult(RESULT_OK, new Intent().putExtras(bundle));
					finish();
				}
				return true;
			}
			final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			finish();
			return true;
		}
	}
}
