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

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.view.Window;

public class WebViewActivity extends BaseActivity {

	private Uri mUri = Uri.parse("about:blank");

	private WebView mWebview;

	public final WebView getWebView() {
		return mWebview;
	}

	public final void loadUrl(String url) {
		mUri = Uri.parse(url);
		mWebview.loadUrl(url);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setTheme();
		super.onCreate(savedInstanceState);
		mWebview = new WebView(this);
		setContentView(mWebview, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
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
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Uri uri = Uri.parse(url);
			if (uri.getScheme().equals(mUri.getScheme()) && uri.getHost().equals(mUri.getHost()))
				return false;
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	}
}