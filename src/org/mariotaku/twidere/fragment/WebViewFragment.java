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

package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.showErrorMessage;

import org.mariotaku.twidere.activity.BaseActivity;
import org.mariotaku.twidere.view.WebSettingsAccessor;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class WebViewFragment extends BaseFragment {

	private WebView mWebView;

	public final WebView getWebView() {
		return mWebView;
	}

	public final void loadUrl(final String url) {
		mWebView.loadUrl(url == null ? "about:blank" : url);
	}

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mWebView.setWebViewClient(new DefaultWebViewClient(getActivity()));
		final WebSettings settings = mWebView.getSettings();
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptEnabled(true);
		WebSettingsAccessor.setAllowUniversalAccessFromFileURLs(settings, true);
		final Bundle bundle = getArguments();
		if (bundle != null) {
			final String url = bundle.getString(INTENT_KEY_URI);
			loadUrl(url);
		}
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		return mWebView = new WebView(getActivity());
	}

	public final void setWebViewClient(final WebViewClient client) {
		mWebView.setWebViewClient(client);
	}

	public static class DefaultWebViewClient extends WebViewClient {

		private final FragmentActivity mActivity;
		private final SharedPreferences mPreferences;

		public DefaultWebViewClient(final FragmentActivity activity) {
			mActivity = activity;
			mPreferences = activity.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		}

		@Override
		public void onPageFinished(final WebView view, final String url) {
			super.onPageFinished(view, url);
			if (mActivity instanceof BaseActivity) {
				mActivity.setTitle(view.getTitle());
				((BaseActivity) mActivity).setProgressBarIndeterminateVisibility(false);
			}
		}

		@Override
		public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			if (mActivity instanceof BaseActivity) {
				((BaseActivity) mActivity).setProgressBarIndeterminateVisibility(true);
			}
		}

		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
			if (mPreferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false)) {
				handler.proceed();
			} else {
				handler.cancel();
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
			try {
				mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			} catch (final ActivityNotFoundException e) {
				showErrorMessage(mActivity, null, e, false);
			}
			return true;
		}
	}
}
