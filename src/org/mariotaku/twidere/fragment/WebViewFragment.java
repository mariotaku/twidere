package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.BaseActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewFragment extends BaseFragment {

	private WebView mWebView;

	public final WebView getWebView() {
		return mWebView;
	}

	public final void loadUrl(String url) {
		mWebView.loadUrl(url == null ? "about:blank" : url);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mWebView = (WebView) getView().findViewById(R.id.webview);
		mWebView.setWebViewClient(new DefaultWebViewClient(getActivity()));
		mWebView.getSettings().setBuiltInZoomControls(true);
		mWebView.getSettings().setJavaScriptEnabled(true);
		final Bundle bundle = getArguments();
		if (bundle != null) {
			final String url = bundle.getString(INTENT_KEY_URI);
			loadUrl(url);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.webview, container, false);
	}

	public final void setWebViewClient(WebViewClient client) {
		mWebView.setWebViewClient(client);
	}

	public static class DefaultWebViewClient extends WebViewClient {

		private FragmentActivity mActivity;

		public DefaultWebViewClient(FragmentActivity activity) {
			mActivity = activity;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if (mActivity instanceof BaseActivity) {
				((BaseActivity) mActivity).setSupportProgressBarIndeterminateVisibility(false);
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (mActivity instanceof BaseActivity) {
				((BaseActivity) mActivity).setSupportProgressBarIndeterminateVisibility(true);
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			return false;
		}
	}
}
