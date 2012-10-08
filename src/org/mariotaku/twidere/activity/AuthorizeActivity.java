package org.mariotaku.twidere.activity;

import android.os.Bundle;
import android.webkit.WebView;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

public class AuthorizeActivity extends BaseActivity {

	private WebView mWebView;

	private String mRESTBaseURL;
	private String mSigningRESTBaseURL;
	private String mOAuthBaseURL;
	private String mSigningOAuthBaseURL;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(mWebView = new WebView(this));
		final Bundle extras = getIntent().getExtras();

		mRESTBaseURL = extras.getString(Accounts.REST_BASE_URL);
		mSigningRESTBaseURL = extras.getString(Accounts.SIGNING_REST_BASE_URL);
		mOAuthBaseURL = extras.getString(Accounts.OAUTH_BASE_URL);
		mSigningOAuthBaseURL = extras.getString(Accounts.SIGNING_OAUTH_BASE_URL);
	}
}
