/*
 *              Copyright (C) 2011 The MusicMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *            http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.parseInt;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.WebViewProxySettings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class AuthorizationActivity extends BaseActivity {

	private Uri authUrl;
	private SharedPreferences mPreferences;
	private WebView mWebView;
	private WebSettings mWebSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		authUrl = getIntent().getData();
		if (authUrl == null) {
			Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT);
			finish();
			return;
		}

		setContentView(R.layout.webview);
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

	private class AuthorizationWebViewClient extends WebViewClient {

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
			Toast.makeText(AuthorizationActivity.this, R.string.error_occurred, Toast.LENGTH_SHORT);
			finish();
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
