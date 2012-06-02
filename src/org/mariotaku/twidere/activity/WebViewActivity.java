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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends BaseActivity {

	private WebView mWebview;

	public final WebView getWebView() {
		return mWebview;
	}

	public final void loadUrl(String url) {
		mWebview.loadUrl(url);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setTheme();
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);
		mWebview = (WebView) findViewById(R.id.webview);
		mWebview.setWebViewClient(new DefaultWebViewClient());
		mWebview.getSettings().setBuiltInZoomControls(true);

	}

	@Override
	public void onDestroy() {
		mWebview.clearCache(true);
		super.onDestroy();
	}

	public final void setWebViewClient(WebViewClient client) {
		mWebview.setWebViewClient(client);
	}

	public class DefaultWebViewClient extends WebViewClient {

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			setProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			setProgressBarIndeterminateVisibility(true);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
	}
}
