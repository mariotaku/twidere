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

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.Utils.setUserAgent;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ParseUtils;
import org.mariotaku.twidere.util.httpclient.HttpClientImpl;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class AuthorizeActivity extends BaseActivity implements LoaderCallbacks<RequestToken> {

	private SharedPreferences mPreferences;

	private WebView mWebView;
	private WebSettings mWebSettings;

	private boolean mLoaderInitialized;

	private String mAuthUrl;
	private RequestToken mRequestToken;

	@Override
	public Loader<RequestToken> onCreateLoader(final int id, final Bundle args) {
		setSupportProgressBarIndeterminateVisibility(true);
		return new RequestTokenLoader(this, args);
	}

	@Override
	public void onDestroy() {
		getSupportLoaderManager().destroyLoader(0);
		super.onDestroy();
	}

	@Override
	public void onLoaderReset(final Loader<RequestToken> loader) {

	}

	@Override
	public void onLoadFinished(final Loader<RequestToken> loader, final RequestToken data) {
		setSupportProgressBarIndeterminateVisibility(false);
		mRequestToken = data;
		if (data == null || mWebView == null) {
			Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		mWebView.loadUrl(mAuthUrl = data.getAuthorizationURL());
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				finish();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		final Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
			return;
		}
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		setContentView(mWebView = new WebView(this));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mWebView.setWebViewClient(new AuthorizationWebViewClient());
		mWebView.setVerticalScrollBarEnabled(false);
		mWebSettings = mWebView.getSettings();
		mWebSettings.setLoadsImagesAutomatically(true);
		mWebSettings.setJavaScriptEnabled(true);
		mWebSettings.setBlockNetworkImage(false);
		mWebSettings.setSaveFormData(true);
		mWebSettings.setSavePassword(true);
		getRequestToken();
	}

	private void getRequestToken() {
		final Bundle extras = getIntent().getExtras();
		if (extras == null) {
			Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		final LoaderManager lm = getSupportLoaderManager();
		lm.destroyLoader(0);
		if (mLoaderInitialized) {
			lm.restartLoader(0, extras, this);
		} else {
			lm.initLoader(0, extras, this);
			mLoaderInitialized = true;
		}
	}

	class AuthorizationWebViewClient extends WebViewClient {

		@Override
		public void onPageFinished(final WebView view, final String url) {
			super.onPageFinished(view, url);
			setSupportProgressBarIndeterminateVisibility(false);
		}

		@Override
		public void onPageStarted(final WebView view, final String url, final Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			setSupportProgressBarIndeterminateVisibility(true);
		}

		@Override
		public void onReceivedError(final WebView view, final int errorCode, final String description,
				final String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			Toast.makeText(AuthorizeActivity.this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
			finish();
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
			if (mAuthUrl == null) return true;
			final Uri uri = Uri.parse(url);
			if (uri.getHost().equals(Uri.parse(mAuthUrl).getHost()))
				return false;
			else if (url.startsWith(DEFAULT_OAUTH_CALLBACK)) {
				final String oauth_verifier = uri.getQueryParameter(INTENT_KEY_OAUTH_VERIFIER);
				if (oauth_verifier != null && mRequestToken != null) {
					final Bundle bundle = new Bundle();
					bundle.putString(INTENT_KEY_OAUTH_VERIFIER, oauth_verifier);
					bundle.putString(INTENT_KEY_REQUEST_TOKEN, mRequestToken.getToken());
					bundle.putString(INTENT_KEY_REQUEST_TOKEN_SECRET, mRequestToken.getTokenSecret());
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

	static class RequestTokenLoader extends AsyncTaskLoader<RequestToken> {

		private final String mRESTBaseURL;
		private final String mSigningRESTBaseURL;
		private final String mOAuthBaseURL;
		private final String mSigningOAuthBaseURL;
		private final TwidereApplication app;
		private final SharedPreferences prefs;
		private final Context context;

		public RequestTokenLoader(final Context context, final Bundle extras) {
			super(context);
			this.context = context;
			app = TwidereApplication.getInstance(context);
			prefs = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
			mRESTBaseURL = extras.getString(Accounts.REST_BASE_URL);
			mSigningRESTBaseURL = extras.getString(Accounts.SIGNING_REST_BASE_URL);
			mOAuthBaseURL = extras.getString(Accounts.OAUTH_BASE_URL);
			mSigningOAuthBaseURL = extras.getString(Accounts.SIGNING_OAUTH_BASE_URL);
		}

		@Override
		public RequestToken loadInBackground() {
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			final boolean enable_gzip_compressing = prefs.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
			final boolean ignore_ssl_error = prefs.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
			final boolean enable_proxy = prefs.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
			final String consumer_key = prefs.getString(PREFERENCE_KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
			final String consumer_secret = prefs.getString(PREFERENCE_KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET);
			cb.setHostAddressResolver(app.getHostAddressResolver());
			cb.setHttpClientImplementation(HttpClientImpl.class);
			setUserAgent(context, cb);
			if (!isEmpty(mRESTBaseURL)) {
				cb.setRestBaseURL(mRESTBaseURL);
			}
			if (!isEmpty(mOAuthBaseURL)) {
				cb.setOAuthBaseURL(mOAuthBaseURL);
			}
			if (!isEmpty(mSigningRESTBaseURL)) {
				cb.setSigningRestBaseURL(mSigningRESTBaseURL);
			}
			if (!isEmpty(mSigningOAuthBaseURL)) {
				cb.setSigningOAuthBaseURL(mSigningOAuthBaseURL);
			}
			if (isEmpty(consumer_key) || isEmpty(consumer_secret)) {
				cb.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				cb.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			} else {
				cb.setOAuthConsumerKey(consumer_key);
				cb.setOAuthConsumerSecret(consumer_secret);
			}
			cb.setGZIPEnabled(enable_gzip_compressing);
			cb.setIgnoreSSLError(ignore_ssl_error);
			if (enable_proxy) {
				final String proxy_host = prefs.getString(PREFERENCE_KEY_PROXY_HOST, null);
				final int proxy_port = ParseUtils.parseInt(prefs.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
				if (!isEmpty(proxy_host) && proxy_port > 0) {
					cb.setHttpProxyHost(proxy_host);
					cb.setHttpProxyPort(proxy_port);
				}
			}
			try {
				final Twitter twitter = new TwitterFactory(cb.build()).getInstance();
				return twitter.getOAuthRequestToken(OAUTH_CALLBACK_OOB);
			} catch (final TwitterException e) {

			}
			return null;
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

	}
}
