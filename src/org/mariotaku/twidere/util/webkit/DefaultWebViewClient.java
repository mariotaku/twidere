package org.mariotaku.twidere.util.webkit;

import static org.mariotaku.twidere.util.Utils.showErrorMessage;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.mariotaku.twidere.activity.support.BaseSupportActivity;
import org.mariotaku.twidere.fragment.BaseWebViewFragment;

public class DefaultWebViewClient extends WebViewClient {

	private final Activity mActivity;
	private final SharedPreferences mPreferences;

	public DefaultWebViewClient(final Activity activity) {
		mActivity = activity;
		mPreferences = activity.getSharedPreferences(BaseWebViewFragment.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}

	@Override
	public void onPageFinished(final WebView view, final String url) {
		super.onPageFinished(view, url);
		if (mActivity instanceof BaseSupportActivity) {
			mActivity.setTitle(view.getTitle());
			((BaseSupportActivity) mActivity).setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
		super.onPageStarted(view, url, favicon);
		if (mActivity instanceof BaseSupportActivity) {
			((BaseSupportActivity) mActivity).setProgressBarIndeterminateVisibility(true);
		}
	}

	@Override
	public void onReceivedSslError(final WebView view, final SslErrorHandler handler, final SslError error) {
		if (mPreferences.getBoolean(BaseWebViewFragment.PREFERENCE_KEY_IGNORE_SSL_ERROR, false)) {
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