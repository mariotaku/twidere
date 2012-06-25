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

import org.mariotaku.twidere.R;

import android.content.Intent;
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