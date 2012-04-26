package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends BaseFragment {

	private WebView mWebview;

	public final WebView getWebView() {
		return mWebview;
	}

	public final void loadUrl(String url) {
		mWebview.loadUrl(url);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWebview = (WebView) getView().findViewById(R.id.webview);
		mWebview.setWebViewClient(new DefaultWebViewClient());
		mWebview.getSettings().setBuiltInZoomControls(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.id.webview, container, false);
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
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
	}
}
