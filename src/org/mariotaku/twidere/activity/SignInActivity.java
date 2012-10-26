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

import static org.mariotaku.twidere.util.Utils.getActivatedAccountIds;
import static org.mariotaku.twidere.util.Utils.getColorPreviewBitmap;
import static org.mariotaku.twidere.util.Utils.getConnection;
import static org.mariotaku.twidere.util.Utils.getProxy;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.isUserLoggedIn;
import static org.mariotaku.twidere.util.Utils.makeAccountContentValues;
import static org.mariotaku.twidere.util.Utils.parseInt;
import static org.mariotaku.twidere.util.Utils.setIgnoreSSLError;
import static org.mariotaku.twidere.util.Utils.setUserAgent;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.APIUpgradeConfirmDialog;
import org.mariotaku.twidere.util.http.HttpClientImpl;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ColorAnalyser;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.AuthenticationException;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator.CallbackURLException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.auth.RequestToken;
import twitter4j.auth.TwipOModeAuthorization;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SignInActivity extends BaseActivity implements OnClickListener, TextWatcher,
		LoaderCallbacks<SignInActivity.LoginResponse> {

	private static final String TWITTER_SIGNUP_URL = "https://twitter.com/signup";
	private static final int MESSAGE_ID_BACK_TIMEOUT = 0;
	private static final String INTENT_KEY_IS_BROWSER_SIGN_IN = "is_browser_sign_in";

	private String mRESTBaseURL, mSigningRESTBaseURL, mOAuthBaseURL, mSigningOAuthBaseURL;
	private String mUsername, mPassword;
	private int mAuthType;
	private Integer mUserColor;
	private long mLoggedId;
	private boolean mBackPressed;
	private boolean mLoaderInitialized;

	private EditText mEditUsername, mEditPassword;
	private Button mSignInButton, mSignUpButton, mBrowserSignInButton;
	private LinearLayout mSigninSignup, mUsernamePassword;
	private ImageButton mSetColorButton;

	private TwidereApplication mApplication;
	private SharedPreferences mPreferences;
	private ContentResolver mResolver;

	private final Handler mBackPressedHandler = new Handler() {

		@Override
		public void handleMessage(final Message msg) {
			mBackPressed = false;
		}

	};

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_EDIT_API: {
				if (resultCode == RESULT_OK) {
					Bundle bundle = new Bundle();
					if (data != null) {
						bundle = data.getExtras();
					}
					if (bundle != null) {
						mRESTBaseURL = bundle.getString(Accounts.REST_BASE_URL);
						mSigningRESTBaseURL = bundle.getString(Accounts.SIGNING_REST_BASE_URL);
						mOAuthBaseURL = bundle.getString(Accounts.OAUTH_BASE_URL);
						mSigningOAuthBaseURL = bundle.getString(Accounts.SIGNING_OAUTH_BASE_URL);

						mAuthType = bundle.getInt(Accounts.AUTH_TYPE);
						final boolean hide_username_password = mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE;
						findViewById(R.id.username_password).setVisibility(
								hide_username_password ? View.GONE : View.VISIBLE);
						((LinearLayout) findViewById(R.id.sign_in_sign_up))
								.setOrientation(hide_username_password ? LinearLayout.VERTICAL
										: LinearLayout.HORIZONTAL);
					}
				}
				setSignInButton();
				break;
			}
			case REQUEST_SET_COLOR: {
				if (resultCode == BaseActivity.RESULT_OK) if (data != null) {
					mUserColor = data.getIntExtra(Accounts.USER_COLOR, Color.TRANSPARENT);
				}
				setUserColorButton();
				break;
			}
			case REQUEST_BROWSER_SIGN_IN: {
				if (resultCode == BaseActivity.RESULT_OK) if (data != null && data.getExtras() != null) {
					doLogin(true, data.getExtras());
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if (getSupportLoaderManager().hasRunningLoaders()) {
			mBackPressedHandler.removeMessages(MESSAGE_ID_BACK_TIMEOUT);
			if (!mBackPressed) {
				Toast.makeText(this, R.string.signing_in_please_wait, Toast.LENGTH_SHORT).show();
				mBackPressed = true;
				mBackPressedHandler.sendEmptyMessageDelayed(MESSAGE_ID_BACK_TIMEOUT, 2000L);
				return;
			}
			mBackPressed = false;
		}
		super.onBackPressed();
	}

	@Override
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.sign_up: {
				final Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(TWITTER_SIGNUP_URL));
				startActivity(intent);
				break;
			}
			case R.id.sign_in: {
				doLogin(false, null);
				break;
			}
			case R.id.set_color: {
				final Intent intent = new Intent(this, SetColorActivity.class);
				final Bundle bundle = new Bundle();
				if (mUserColor != null) {
					bundle.putInt(Accounts.USER_COLOR, mUserColor);
				}
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SET_COLOR);
				break;
			}
			case R.id.browser_sign_in: {
				final Intent intent = new Intent(this, AuthorizeActivity.class);
				final Bundle extras = new Bundle();
				extras.putString(Accounts.REST_BASE_URL, mRESTBaseURL);
				extras.putString(Accounts.OAUTH_BASE_URL, mOAuthBaseURL);
				extras.putString(Accounts.SIGNING_REST_BASE_URL, mSigningRESTBaseURL);
				extras.putString(Accounts.SIGNING_OAUTH_BASE_URL, mSigningOAuthBaseURL);
				intent.putExtras(extras);
				startActivityForResult(intent, REQUEST_BROWSER_SIGN_IN);
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mEditUsername = (EditText) findViewById(R.id.username);
		mEditPassword = (EditText) findViewById(R.id.password);
		mSignInButton = (Button) findViewById(R.id.sign_in);
		mSignUpButton = (Button) findViewById(R.id.sign_up);
		mBrowserSignInButton = (Button) findViewById(R.id.browser_sign_in);
		mSigninSignup = (LinearLayout) findViewById(R.id.sign_in_sign_up);
		mUsernamePassword = (LinearLayout) findViewById(R.id.username_password);
		mSetColorButton = (ImageButton) findViewById(R.id.set_color);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		mResolver = getContentResolver();
		mApplication = TwidereApplication.getInstance(this);
		setContentView(R.layout.sign_in);
		setSupportProgressBarIndeterminateVisibility(false);
		final long[] account_ids = getActivatedAccountIds(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(account_ids.length > 0);

		Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
		if (bundle == null) {
			bundle = new Bundle();
		}
		mRESTBaseURL = bundle.getString(Accounts.REST_BASE_URL);
		mOAuthBaseURL = bundle.getString(Accounts.OAUTH_BASE_URL);
		mSigningRESTBaseURL = bundle.getString(Accounts.SIGNING_REST_BASE_URL);
		mSigningOAuthBaseURL = bundle.getString(Accounts.SIGNING_OAUTH_BASE_URL);

		if (isNullOrEmpty(mRESTBaseURL)) {
			mRESTBaseURL = DEFAULT_REST_BASE_URL;
		}
		if (isNullOrEmpty(mOAuthBaseURL)) {
			mOAuthBaseURL = DEFAULT_OAUTH_BASE_URL;
		}
		if (isNullOrEmpty(mSigningRESTBaseURL)) {
			mSigningRESTBaseURL = DEFAULT_SIGNING_REST_BASE_URL;
		}
		if (isNullOrEmpty(mSigningOAuthBaseURL)) {
			mSigningOAuthBaseURL = DEFAULT_SIGNING_OAUTH_BASE_URL;
		}

		mUsername = bundle.getString(Accounts.SCREEN_NAME);
		mPassword = bundle.getString(Accounts.PASSWORD);
		mAuthType = bundle.getInt(Accounts.AUTH_TYPE);
		if (bundle.containsKey(Accounts.USER_COLOR)) {
			mUserColor = bundle.getInt(Accounts.USER_COLOR, Color.TRANSPARENT);
		}
		mUsernamePassword.setVisibility(mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE ? View.GONE : View.VISIBLE);
		mSigninSignup.setOrientation(mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE ? LinearLayout.VERTICAL
				: LinearLayout.HORIZONTAL);

		mEditUsername.setText(mUsername);
		mEditUsername.addTextChangedListener(this);
		mEditPassword.setText(mPassword);
		mEditPassword.addTextChangedListener(this);
		setSignInButton();
		setUserColorButton();
		if (!mPreferences.getBoolean(PREFERENCE_KEY_API_UPGRADE_CONFIRMED, false)) {
			final FragmentManager fm = getSupportFragmentManager();
			if (fm.findFragmentByTag(FRAGMENT_TAG_API_UPGRADE_NOTICE) == null
					|| !fm.findFragmentByTag(FRAGMENT_TAG_API_UPGRADE_NOTICE).isAdded()) {
				new APIUpgradeConfirmDialog().show(getSupportFragmentManager(), "api_upgrade_notice");
			}
		}
	}

	@Override
	public Loader<LoginResponse> onCreateLoader(final int id, final Bundle args) {
		setSupportProgressBarIndeterminateVisibility(true);
		mEditPassword.setEnabled(false);
		mEditUsername.setEnabled(false);
		mSignInButton.setEnabled(false);
		mSignUpButton.setEnabled(false);
		mBrowserSignInButton.setEnabled(false);
		mSetColorButton.setEnabled(false);
		saveEditedText();
		final Configuration conf = getConfiguration();
		if (args.getBoolean(INTENT_KEY_IS_BROWSER_SIGN_IN))
			return new BrowserSigninUserCredentialsLoader(this, conf, args.getString(INTENT_KEY_REQUEST_TOKEN),
					args.getString(INTENT_KEY_REQUEST_TOKEN_SECRET), args.getString(INTENT_KEY_OAUTH_VERIFIER),
					mUserColor);
		return new UserCredentialsLoader(this, conf, mUsername, mPassword, mAuthType, mUserColor);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_login, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDestroy() {
		getSupportLoaderManager().destroyLoader(0);
		super.onDestroy();
	}

	@Override
	public void onLoaderReset(final Loader<LoginResponse> loader) {
	}

	@Override
	public void onLoadFinished(final Loader<LoginResponse> loader, final SignInActivity.LoginResponse result) {
		if (result.succeed) {
			final ContentValues values = makeAccountContentValues(result.conf, result.basic_password,
					result.access_token, result.user, result.auth_type, result.color);
			if (values != null) {
				mResolver.insert(Accounts.CONTENT_URI, values);
			}
			mPreferences.edit().putBoolean(PREFERENCE_KEY_API_UPGRADE_CONFIRMED, true).commit();
			final Intent intent = new Intent(INTENT_ACTION_HOME);
			final Bundle bundle = new Bundle();
			bundle.putLongArray(INTENT_KEY_IDS, new long[] { mLoggedId });
			intent.putExtras(bundle);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			startActivity(intent);
			finish();
		} else if (result.already_logged_in) {
			Toast.makeText(SignInActivity.this, R.string.error_already_logged_in, Toast.LENGTH_SHORT).show();
		} else {
			if (result.exception instanceof CallbackURLException) {
				showErrorToast(this, getString(R.string.cannot_get_callback_url), true);
			} else {
				showErrorToast(this, getString(R.string.signing_in), result.exception, true);
			}
		}
		setSupportProgressBarIndeterminateVisibility(false);
		mEditPassword.setEnabled(true);
		mEditUsername.setEnabled(true);
		mSignInButton.setEnabled(true);
		mSignUpButton.setEnabled(true);
		mBrowserSignInButton.setEnabled(true);
		mSetColorButton.setEnabled(true);
		setSignInButton();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		Intent intent = new Intent();

		switch (item.getItemId()) {
			case MENU_HOME: {
				final long[] account_ids = getActivatedAccountIds(this);
				if (account_ids.length > 0) {
					finish();
				}
				break;
			}
			case MENU_SETTINGS: {
				intent = new Intent(INTENT_ACTION_SETTINGS);
				startActivity(intent);
				break;
			}
			case MENU_EDIT_API: {
				if (getSupportLoaderManager().hasRunningLoaders()) return false;
				intent = new Intent(this, EditAPIActivity.class);
				final Bundle bundle = new Bundle();
				bundle.putString(Accounts.REST_BASE_URL, mRESTBaseURL);
				bundle.putString(Accounts.SIGNING_REST_BASE_URL, mSigningRESTBaseURL);
				bundle.putString(Accounts.OAUTH_BASE_URL, mOAuthBaseURL);
				bundle.putString(Accounts.SIGNING_OAUTH_BASE_URL, mSigningOAuthBaseURL);
				bundle.putInt(Accounts.AUTH_TYPE, mAuthType);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_EDIT_API);
				break;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		saveEditedText();
		outState.putString(Accounts.REST_BASE_URL, mRESTBaseURL);
		outState.putString(Accounts.OAUTH_BASE_URL, mOAuthBaseURL);
		outState.putString(Accounts.SIGNING_REST_BASE_URL, mSigningRESTBaseURL);
		outState.putString(Accounts.SIGNING_OAUTH_BASE_URL, mSigningOAuthBaseURL);
		outState.putString(Accounts.SCREEN_NAME, mUsername);
		outState.putString(Accounts.PASSWORD, mPassword);
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		if (mUserColor != null) {
			outState.putInt(Accounts.USER_COLOR, mUserColor);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		setSignInButton();
	}

	private void doLogin(final boolean is_browser_sign_in, final Bundle extras) {
		final LoaderManager lm = getSupportLoaderManager();
		final Bundle args = new Bundle();
		if (extras != null) {
			args.putAll(extras);
		}
		args.putBoolean(INTENT_KEY_IS_BROWSER_SIGN_IN, is_browser_sign_in);
		lm.destroyLoader(0);
		if (mLoaderInitialized) {
			lm.restartLoader(0, args, this);
		} else {
			lm.initLoader(0, args, this);
			mLoaderInitialized = true;
		}
	}

	private Configuration getConfiguration() {
		final ConfigurationBuilder cb = new ConfigurationBuilder();
		final boolean enable_gzip_compressing = mPreferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
		final boolean ignore_ssl_error = mPreferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
		final boolean enable_proxy = mPreferences.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		final String consumer_key = mPreferences.getString(PREFERENCE_KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY);
		final String consumer_secret = mPreferences.getString(PREFERENCE_KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET);
		cb.setHostAddressResolver(mApplication.getHostAddressResolver());
		cb.setHttpClientImplementation(HttpClientImpl.class);
		setUserAgent(this, cb);
		if (!isNullOrEmpty(mRESTBaseURL)) {
			cb.setRestBaseURL(mRESTBaseURL);
		}
		if (!isNullOrEmpty(mOAuthBaseURL)) {
			cb.setOAuthBaseURL(mOAuthBaseURL);
		}
		if (!isNullOrEmpty(mSigningRESTBaseURL)) {
			cb.setSigningRestBaseURL(mSigningRESTBaseURL);
		}
		if (!isNullOrEmpty(mSigningOAuthBaseURL)) {
			cb.setSigningOAuthBaseURL(mSigningOAuthBaseURL);
		}
		if (isNullOrEmpty(consumer_key) || isNullOrEmpty(consumer_secret)) {
			cb.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			cb.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
		} else {
			cb.setOAuthConsumerKey(consumer_key);
			cb.setOAuthConsumerSecret(consumer_secret);
		}
		cb.setGZIPEnabled(enable_gzip_compressing);
		cb.setIgnoreSSLError(ignore_ssl_error);
		if (enable_proxy) {
			final String proxy_host = mPreferences.getString(PREFERENCE_KEY_PROXY_HOST, null);
			final int proxy_port = parseInt(mPreferences.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
			if (!isNullOrEmpty(proxy_host) && proxy_port > 0) {
				cb.setHttpProxyHost(proxy_host);
				cb.setHttpProxyPort(proxy_port);
			}
		}
		return cb.build();
	}

	private void saveEditedText() {
		Editable ed = mEditUsername.getText();
		if (ed != null) {
			mUsername = ed.toString();
		}
		ed = mEditPassword.getText();
		if (ed != null) {
			mPassword = ed.toString();
		}
	}

	private void setSignInButton() {
		mSignInButton.setEnabled(mEditPassword.getText().length() > 0 && mEditUsername.getText().length() > 0
				|| mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE);
	}

	private void setUserColorButton() {
		if (mUserColor != null) {
			mSetColorButton.setImageBitmap(getColorPreviewBitmap(this, mUserColor));
		} else {
			mSetColorButton.setImageResource(R.drawable.ic_menu_color_palette);
		}

	}

	public static abstract class AbstractUserCredentialsLoader extends AsyncTaskLoader<LoginResponse> {

		private final Configuration conf;
		private final Context context;
		private final SharedPreferences preferences;

		public AbstractUserCredentialsLoader(final Context context, final Configuration conf) {
			super(context);
			preferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
			this.context = context;
			this.conf = conf;
		}

		int analyseUserProfileColor(final URL url) throws IOException {
			final boolean ignore_ssl_error = preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
			final int connection_timeout = preferences.getInt(PREFERENCE_KEY_CONNECTION_TIMEOUT, 10) * 1000;
			final URLConnection conn = getConnection(url, connection_timeout, true, getProxy(context),
					conf.getHostAddressResolver());
			final InputStream is = conn.getInputStream();
			if (ignore_ssl_error) {
				setIgnoreSSLError(conn);
			}
			final Bitmap bm = BitmapFactory.decodeStream(is);
			return ColorAnalyser.analyse(bm);
		}

	}

	public static class BrowserSigninUserCredentialsLoader extends AbstractUserCredentialsLoader {

		private final Configuration conf;
		private final String request_token, request_token_secret, oauth_verifier;
		private final Integer user_color;

		private final Context context;

		public BrowserSigninUserCredentialsLoader(final Context context, final Configuration conf,
				final String request_token, final String request_token_secret, final String oauth_verifier,
				final Integer user_color) {
			super(context, conf);
			this.context = context;
			this.conf = conf;
			this.request_token = request_token;
			this.request_token_secret = request_token_secret;
			this.oauth_verifier = oauth_verifier;
			this.user_color = user_color;
		}

		@Override
		public LoginResponse loadInBackground() {
			try {
				final Twitter twitter = new TwitterFactory(conf).getInstance();
				final AccessToken access_token = twitter.getOAuthAccessToken(new RequestToken(conf, request_token,
						request_token_secret), oauth_verifier);
				final long user_id = access_token.getUserId();
				if (user_id <= 0) return new LoginResponse(false, false, null);
				final User user = twitter.showUser(user_id);
				if (isUserLoggedIn(context, user_id)) return new LoginResponse(true, false, null);
				final int color = user_color != null ? user_color : analyseUserProfileColor(user.getProfileImageURL());
				return new LoginResponse(false, true, null, conf, null, access_token, user, Accounts.AUTH_TYPE_OAUTH,
						color);
			} catch (final IOException e) {
				e.printStackTrace();
			} catch (final TwitterException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}
	}

	public static class UserCredentialsLoader extends AbstractUserCredentialsLoader {

		private final Configuration conf;
		private final String username, password;
		private final int auth_type;
		private final Integer user_color;

		private final Context context;

		public UserCredentialsLoader(final Context context, final Configuration conf, final String username,
				final String password, final int auth_type, final Integer user_color) {
			super(context, conf);
			this.context = context;
			this.conf = conf;
			this.username = username;
			this.password = password;
			this.auth_type = auth_type;
			this.user_color = user_color;
		}

		@Override
		public LoginResponse loadInBackground() {
			try {
				switch (auth_type) {
					case Accounts.AUTH_TYPE_OAUTH:
						return authOAuth();
					case Accounts.AUTH_TYPE_XAUTH:
						return authxAuth();
					case Accounts.AUTH_TYPE_BASIC:
						return authBasic();
					case Accounts.AUTH_TYPE_TWIP_O_MODE:
						return authTwipOMode();
				}
				return authOAuth();
			} catch (final TwitterException e) {
				return new LoginResponse(false, false, e);
			} catch (final CallbackURLException e) {
				return new LoginResponse(false, false, e);
			} catch (final AuthenticationException e) {
				return new LoginResponse(false, false, e);
			} catch (final IOException e) {
				return new LoginResponse(false, false, e);
			} catch (final NullPointerException e) {
				return new LoginResponse(false, false, e);
			}
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		private LoginResponse authBasic() throws TwitterException, IOException {
			final Twitter twitter = new TwitterFactory(conf).getInstance(new BasicAuthorization(username, password));
			final User user = twitter.verifyCredentials();
			final long user_id = user.getId();
			if (user_id <= 0) return new LoginResponse(false, false, null);
			if (isUserLoggedIn(context, user_id)) return new LoginResponse(true, false, null);
			final int color = user_color != null ? user_color : analyseUserProfileColor(user.getProfileImageURL());
			return new LoginResponse(false, true, null, conf, password, null, user, Accounts.AUTH_TYPE_BASIC, color);
		}

		private LoginResponse authOAuth() throws AuthenticationException, TwitterException, IOException {
			final Twitter twitter = new TwitterFactory(conf).getInstance();
			final OAuthPasswordAuthenticator authenticator = new OAuthPasswordAuthenticator(twitter, getProxy(context),
					conf.getUserAgent());
			final AccessToken access_token = authenticator.getOAuthAccessToken(username, password);
			final long user_id = access_token.getUserId();
			if (user_id <= 0) return new LoginResponse(false, false, null);
			final User user = twitter.showUser(user_id);
			if (isUserLoggedIn(context, user_id)) return new LoginResponse(true, false, null);
			final int color = user_color != null ? user_color : analyseUserProfileColor(user.getProfileImageURL());
			return new LoginResponse(false, true, null, conf, null, access_token, user, Accounts.AUTH_TYPE_OAUTH, color);
		}

		private LoginResponse authTwipOMode() throws TwitterException, IOException {
			final Twitter twitter = new TwitterFactory(conf).getInstance(new TwipOModeAuthorization());
			final User user = twitter.verifyCredentials();
			final long user_id = user.getId();
			if (user_id <= 0) return new LoginResponse(false, false, null);
			if (isUserLoggedIn(context, user_id)) return new LoginResponse(true, false, null);
			final int color = user_color != null ? user_color : analyseUserProfileColor(user.getProfileImageURL());
			return new LoginResponse(false, true, null, conf, null, null, user, Accounts.AUTH_TYPE_TWIP_O_MODE, color);
		}

		private LoginResponse authxAuth() throws TwitterException, IOException {
			final Twitter twitter = new TwitterFactory(conf).getInstance();
			final AccessToken access_token = twitter.getOAuthAccessToken(username, password);
			final User user = twitter.showUser(access_token.getUserId());
			final long user_id = user.getId();
			if (user_id <= 0) return new LoginResponse(false, false, null);
			if (isUserLoggedIn(context, user_id)) return new LoginResponse(true, false, null);
			final int color = user_color != null ? user_color : analyseUserProfileColor(user.getProfileImageURL());
			return new LoginResponse(false, true, null, conf, null, access_token, user, Accounts.AUTH_TYPE_XAUTH, color);
		}

	}

	static class LoginResponse {

		public final boolean already_logged_in, succeed;
		public final Exception exception;
		final Configuration conf;
		final String basic_password;
		final AccessToken access_token;
		final User user;
		final int auth_type, color;

		public LoginResponse(final boolean already_logged_in, final boolean succeed, final Exception exception) {
			this(already_logged_in, succeed, exception, null, null, null, null, 0, 0);
		}

		public LoginResponse(final boolean already_logged_in, final boolean succeed, final Exception exception,
				final Configuration conf, final String basic_password, final AccessToken access_token, final User user,
				final int auth_type, final int color) {
			this.already_logged_in = already_logged_in;
			this.succeed = succeed;
			this.exception = exception;
			this.conf = conf;
			this.basic_password = basic_password;
			this.access_token = access_token;
			this.user = user;
			this.auth_type = auth_type;
			this.color = color;
		}
	}
}
