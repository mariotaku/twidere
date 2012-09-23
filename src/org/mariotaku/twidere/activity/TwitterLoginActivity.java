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
import static org.mariotaku.twidere.util.Utils.getBrowserUserAgent;
import static org.mariotaku.twidere.util.Utils.getColorPreviewBitmap;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ColorAnalyser;
import org.mariotaku.twidere.util.OAuthPasswordAuthenticator;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.auth.TwipOModeAuthorization;
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
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

public class TwitterLoginActivity extends BaseActivity implements OnClickListener, TextWatcher {

	private static final String TWITTER_SIGNUP_URL = "https://twitter.com/signup";
	private static final int MESSAGE_ID_BACK_TIMEOUT = 0;

	private String mRESTBaseURL, mSearchBaseURL, mUploadBaseURL, mSigningRESTBaseURL, mOAuthBaseURL,
			mSigningOAuthBaseURL;
	private String mUsername, mPassword;
	private int mAuthType, mUserColor;
	private boolean mUserColorSet;
	private long mLoggedId;
	private boolean mBackPressed = false;
	private String mBrowserUserAgent;

	private EditText mEditUsername, mEditPassword;
	private Button mSignInButton, mSignUpButton;
	private LinearLayout mSigninSignup, mUsernamePassword;
	private ImageButton mSetColorButton;
	private TwidereApplication mApplication;

	private AbstractTask<?> mTask;

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
			case REQUEST_EDIT_API:
				if (resultCode == RESULT_OK) {
					Bundle bundle = new Bundle();
					if (data != null) {
						bundle = data.getExtras();
					}
					if (bundle != null) {
						mRESTBaseURL = bundle.getString(Accounts.REST_BASE_URL);
						mSearchBaseURL = bundle.getString(Accounts.SEARCH_BASE_URL);
						mUploadBaseURL = bundle.getString(Accounts.UPLOAD_BASE_URL);
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
			case REQUEST_SET_COLOR:
				if (resultCode == BaseActivity.RESULT_OK) if (data != null && data.getExtras() != null) {
					mUserColor = data.getIntExtra(Accounts.USER_COLOR, Color.TRANSPARENT);
					mUserColorSet = true;
				} else {
					mUserColor = Color.TRANSPARENT;
					mUserColorSet = false;
				}
				setUserColorButton();
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onBackPressed() {
		if (mTask != null && mTask.getStatus() == AsyncTask.Status.RUNNING) {
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
				saveEditedText();
				if (mTask != null) {
					mTask.cancel(true);
				}
				mTask = new LoginTask();
				mTask.execute();
				break;
			}
			case R.id.set_color: {
				final Intent intent = new Intent(INTENT_ACTION_SET_COLOR);
				final Bundle bundle = new Bundle();
				bundle.putInt(Accounts.USER_COLOR, mUserColor);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SET_COLOR);
				break;
			}
		}
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		mBrowserUserAgent = getBrowserUserAgent(this);
		mApplication = TwidereApplication.getInstance(this);
		setContentView(R.layout.twitter_login);
		mEditUsername = (EditText) findViewById(R.id.username);
		mEditPassword = (EditText) findViewById(R.id.password);
		mSignInButton = (Button) findViewById(R.id.sign_in);
		mSignUpButton = (Button) findViewById(R.id.sign_up);
		mSigninSignup = (LinearLayout) findViewById(R.id.sign_in_sign_up);
		mUsernamePassword = (LinearLayout) findViewById(R.id.username_password);
		mSetColorButton = (ImageButton) findViewById(R.id.set_color);
		setSupportProgressBarIndeterminateVisibility(false);
		final long[] account_ids = getActivatedAccountIds(this);
		getSupportActionBar().setDisplayHomeAsUpEnabled(account_ids.length > 0);

		Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
		if (bundle == null) {
			bundle = new Bundle();
		}
		mRESTBaseURL = bundle.getString(Accounts.REST_BASE_URL);
		mOAuthBaseURL = bundle.getString(Accounts.OAUTH_BASE_URL);
		mSearchBaseURL = bundle.getString(Accounts.SEARCH_BASE_URL);
		mUploadBaseURL = bundle.getString(Accounts.UPLOAD_BASE_URL);
		mSigningRESTBaseURL = bundle.getString(Accounts.SIGNING_REST_BASE_URL);
		mSigningOAuthBaseURL = bundle.getString(Accounts.SIGNING_OAUTH_BASE_URL);

		if (isNullOrEmpty(mRESTBaseURL)) {
			mRESTBaseURL = DEFAULT_REST_BASE_URL;
		}
		if (isNullOrEmpty(mOAuthBaseURL)) {
			mOAuthBaseURL = DEFAULT_OAUTH_BASE_URL;
		}
		if (isNullOrEmpty(mSearchBaseURL)) {
			mSearchBaseURL = DEFAULT_SEARCH_BASE_URL;
		}
		if (isNullOrEmpty(mUploadBaseURL)) {
			mUploadBaseURL = DEFAULT_UPLOAD_BASE_URL;
		}
		if (isNullOrEmpty(mSigningRESTBaseURL)) {
			mSigningRESTBaseURL = DEFAULT_SIGNING_REST_BASE_URL;
		}
		if (isNullOrEmpty(mSigningOAuthBaseURL)) {
			mSigningOAuthBaseURL = DEFAULT_SIGNING_OAUTH_BASE_URL;
		}

		mUsername = bundle.getString(Accounts.USERNAME);
		mPassword = bundle.getString(Accounts.PASSWORD);
		mAuthType = bundle.getInt(Accounts.AUTH_TYPE);
		mUsernamePassword.setVisibility(mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE ? View.GONE : View.VISIBLE);
		mSigninSignup.setOrientation(mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE ? LinearLayout.VERTICAL
				: LinearLayout.HORIZONTAL);

		mEditUsername.setText(mUsername);
		mEditUsername.addTextChangedListener(this);
		mEditPassword.setText(mPassword);
		mEditPassword.addTextChangedListener(this);
		setSignInButton();
		setUserColorButton();

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_login, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDestroy() {
		if (mTask != null) {
			mTask.cancel(true);
		}
		super.onDestroy();
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
				if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) return false;
				intent = new Intent(INTENT_ACTION_EDIT_API);
				final Bundle bundle = new Bundle();
				bundle.putString(Accounts.REST_BASE_URL, mRESTBaseURL);
				bundle.putString(Accounts.SEARCH_BASE_URL, mSearchBaseURL);
				bundle.putString(Accounts.UPLOAD_BASE_URL, mUploadBaseURL);
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
		outState.putString(Accounts.SEARCH_BASE_URL, mSearchBaseURL);
		outState.putString(Accounts.UPLOAD_BASE_URL, mUploadBaseURL);
		outState.putString(Accounts.SIGNING_REST_BASE_URL, mSigningRESTBaseURL);
		outState.putString(Accounts.SIGNING_OAUTH_BASE_URL, mSigningOAuthBaseURL);
		outState.putString(Accounts.USERNAME, mUsername);
		outState.putString(Accounts.PASSWORD, mPassword);
		outState.putInt(Accounts.USER_COLOR, mUserColor);
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		setSignInButton();
	}

	private void analyseUserProfileColor(final String url_string) {
		final boolean ignore_ssl_error = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE).getBoolean(
				PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
		try {
			final URL url = new URL(url_string);
			final URLConnection conn = url.openConnection();
			final InputStream is = conn.getInputStream();
			if (ignore_ssl_error) {
				setIgnoreSSLError(conn);
			}
			final Bitmap bm = BitmapFactory.decodeStream(is);
			mUserColor = ColorAnalyser.analyse(bm);
			mUserColorSet = true;
			return;
		} catch (final MalformedURLException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		mUserColorSet = false;
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

	private ConfigurationBuilder setAPI(final ConfigurationBuilder cb) {
		final SharedPreferences preferences = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final boolean enable_gzip_compressing = preferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
		final boolean ignore_ssl_error = preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
		final boolean enable_proxy = preferences.getBoolean(PREFERENCE_KEY_ENABLE_PROXY, false);
		final String consumer_key = preferences.getString(PREFERENCE_KEY_CONSUMER_KEY, CONSUMER_KEY);
		final String consumer_secret = preferences.getString(PREFERENCE_KEY_CONSUMER_SECRET, CONSUMER_SECRET);
		cb.setHostAddressResolver(mApplication.getHostAddressResolver());
		setUserAgent(this, cb);
		if (!isNullOrEmpty(mRESTBaseURL)) {
			cb.setRestBaseURL(mRESTBaseURL);
		}
		if (!isNullOrEmpty(mOAuthBaseURL)) {
			cb.setOAuthBaseURL(mOAuthBaseURL);
		}
		if (!isNullOrEmpty(mSearchBaseURL)) {
			cb.setSearchBaseURL(mSearchBaseURL);
		}
		if (!isNullOrEmpty(mUploadBaseURL)) {
			cb.setUploadBaseURL(mUploadBaseURL);
		}
		if (!isNullOrEmpty(mSigningRESTBaseURL)) {
			cb.setSigningRestBaseURL(mSigningRESTBaseURL);
		}
		if (!isNullOrEmpty(mSigningOAuthBaseURL)) {
			cb.setSigningOAuthBaseURL(mSigningOAuthBaseURL);
		}
		if (isNullOrEmpty(consumer_key) || isNullOrEmpty(consumer_secret)) {
			cb.setOAuthConsumerKey(CONSUMER_KEY);
			cb.setOAuthConsumerSecret(CONSUMER_SECRET);
		} else {
			cb.setOAuthConsumerKey(consumer_key);
			cb.setOAuthConsumerSecret(consumer_secret);
		}
		cb.setGZIPEnabled(enable_gzip_compressing);
		cb.setIgnoreSSLError(ignore_ssl_error);
		if (enable_proxy) {
			final String proxy_host = preferences.getString(PREFERENCE_KEY_PROXY_HOST, null);
			final int proxy_port = parseInt(preferences.getString(PREFERENCE_KEY_PROXY_PORT, "-1"));
			if (!isNullOrEmpty(proxy_host) && proxy_port > 0) {
				cb.setHttpProxyHost(proxy_host);
				cb.setHttpProxyPort(proxy_port);
			}
		}
		return cb;
	}

	private void setSignInButton() {
		mSignInButton.setEnabled(mEditPassword.getText().length() > 0 && mEditUsername.getText().length() > 0
				|| mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE);
	}

	private void setUserColorButton() {
		if (mUserColorSet) {
			mSetColorButton.setImageBitmap(getColorPreviewBitmap(this, mUserColor));
		} else {
			mSetColorButton.setImageResource(R.drawable.ic_menu_color_palette);
		}

	}

	private abstract class AbstractTask<Result> extends AsyncTask<Void, Void, Result> {

		@Override
		protected void onPostExecute(final Result result) {
			setSupportProgressBarIndeterminateVisibility(false);
			mTask = null;
			mEditPassword.setEnabled(true);
			mEditUsername.setEnabled(true);
			mSignInButton.setEnabled(true);
			mSignUpButton.setEnabled(true);
			mSetColorButton.setEnabled(true);
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setSupportProgressBarIndeterminateVisibility(true);
			mEditPassword.setEnabled(false);
			mEditUsername.setEnabled(false);
			mSignInButton.setEnabled(false);
			mSignUpButton.setEnabled(false);
			mSetColorButton.setEnabled(false);
		}

	}

	class LoginTask extends AbstractTask<LoginTask.LoginResponse> {

		@Override
		protected LoginResponse doInBackground(final Void... params) {
			return doAuth();
		}

		@Override
		protected void onPostExecute(final LoginResponse result) {

			if (result.succeed) {
				final Intent intent = new Intent(INTENT_ACTION_HOME);
				final Bundle bundle = new Bundle();
				bundle.putLongArray(INTENT_KEY_IDS, new long[] { mLoggedId });
				intent.putExtras(bundle);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				finish();
			} else if (result.already_logged_in) {
				Toast.makeText(TwitterLoginActivity.this, R.string.error_already_logged_in, Toast.LENGTH_SHORT).show();
			} else {
				result.exception.printStackTrace();
				showErrorToast(TwitterLoginActivity.this, result.exception, true);
			}
			super.onPostExecute(result);
		}

		private LoginResponse authBasic() {
			final ContentResolver resolver = getContentResolver();
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			setAPI(cb);

			final Twitter twitter = new TwitterFactory(cb.build()).getInstance(new BasicAuthorization(mUsername,
					mPassword));
			User user = null;
			try {
				user = twitter.verifyCredentials();
			} catch (final TwitterException e) {
				return new LoginResponse(false, false, Accounts.AUTH_TYPE_BASIC, e);
			}

			if (user != null && user.getId() > 0) {
				final String profile_image_url = user.getProfileImageURL().toString();
				if (!mUserColorSet) {
					analyseUserProfileColor(profile_image_url);
				}

				mLoggedId = user.getId();
				if (isUserLoggedIn(TwitterLoginActivity.this, mLoggedId))
					return new LoginResponse(true, false, Accounts.AUTH_TYPE_BASIC, null);
				final ContentValues values = makeAccountContentValues(mUserColor, null, user, mRESTBaseURL,
						mOAuthBaseURL, mSigningRESTBaseURL, mSigningOAuthBaseURL, mSearchBaseURL, mUploadBaseURL,
						mPassword, Accounts.AUTH_TYPE_BASIC);
				if (values != null) {
					resolver.insert(Accounts.CONTENT_URI, values);
				}
				return new LoginResponse(false, true, Accounts.AUTH_TYPE_BASIC, null);

			}
			return new LoginResponse(false, false, Accounts.AUTH_TYPE_BASIC, null);
		}

		private LoginResponse authOAuth() {
			final ContentResolver resolver = getContentResolver();
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			setAPI(cb);
			final Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			final OAuthPasswordAuthenticator authenticator = new OAuthPasswordAuthenticator(twitter,
					getProxy(TwitterLoginActivity.this), mBrowserUserAgent);
			try {
				final AccessToken access_token = authenticator.getOAuthAccessToken(mUsername, mPassword);
				if (access_token.getUserId() > 0) {
					final User user = twitter.showUser(access_token.getUserId());
					final String profile_image_url = parseString(user.getProfileImageURL());
					if (!mUserColorSet) {
						analyseUserProfileColor(profile_image_url);
					}
					mLoggedId = access_token.getUserId();
					if (isUserLoggedIn(TwitterLoginActivity.this, mLoggedId))
						return new LoginResponse(true, false, Accounts.AUTH_TYPE_OAUTH, null);
					final ContentValues values = makeAccountContentValues(mUserColor, access_token, user, mRESTBaseURL,
							mOAuthBaseURL, mSigningRESTBaseURL, mSigningOAuthBaseURL, mSearchBaseURL, mUploadBaseURL,
							null, Accounts.AUTH_TYPE_OAUTH);
					if (values != null) {
						resolver.insert(Accounts.CONTENT_URI, values);
					}
					return new LoginResponse(false, true, Accounts.AUTH_TYPE_OAUTH, null);
				}
			} catch (final OAuthPasswordAuthenticator.AuthenticationException e) {
				return new LoginResponse(false, false, Accounts.AUTH_TYPE_OAUTH, e);
			} catch (final TwitterException e) {
				return new LoginResponse(false, false, Accounts.AUTH_TYPE_OAUTH, e);
			}
			return new LoginResponse(false, false, Accounts.AUTH_TYPE_OAUTH, null);
		}

		private LoginResponse authTwipOMode() {
			final ContentResolver resolver = getContentResolver();
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			setAPI(cb);

			final Twitter twitter = new TwitterFactory(cb.build()).getInstance(new TwipOModeAuthorization());
			User user = null;
			try {
				user = twitter.verifyCredentials();
			} catch (final TwitterException e) {
				return new LoginResponse(false, false, Accounts.AUTH_TYPE_TWIP_O_MODE, e);
			}

			if (user != null && user.getId() > 0) {
				final String profile_image_url = parseString(user.getProfileImageURL());
				if (!mUserColorSet) {
					analyseUserProfileColor(profile_image_url);
				}

				mLoggedId = user.getId();
				if (isUserLoggedIn(TwitterLoginActivity.this, mLoggedId))
					return new LoginResponse(true, false, Accounts.AUTH_TYPE_TWIP_O_MODE, null);
				final ContentValues values = makeAccountContentValues(mUserColor, null, user, mRESTBaseURL,
						mOAuthBaseURL, mSigningRESTBaseURL, mSigningOAuthBaseURL, mSearchBaseURL, mUploadBaseURL, null,
						Accounts.AUTH_TYPE_TWIP_O_MODE);
				if (values != null) {
					resolver.insert(Accounts.CONTENT_URI, values);
				}
				return new LoginResponse(false, true, Accounts.AUTH_TYPE_TWIP_O_MODE, null);

			}
			return new LoginResponse(false, false, Accounts.AUTH_TYPE_TWIP_O_MODE, null);
		}

		private LoginResponse authxAuth() {
			final ContentResolver resolver = getContentResolver();
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			setAPI(cb);
			final Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			AccessToken accessToken = null;
			User user = null;
			try {
				accessToken = twitter.getOAuthAccessToken(mUsername, mPassword);
				user = twitter.showUser(accessToken.getUserId());
			} catch (final TwitterException e) {
				return new LoginResponse(false, false, Accounts.AUTH_TYPE_XAUTH, e);
			}
			if (!mUserColorSet) {
				analyseUserProfileColor(user.getProfileImageURL().toString());
			}

			mLoggedId = user.getId();
			if (isUserLoggedIn(TwitterLoginActivity.this, mLoggedId))
				return new LoginResponse(true, false, Accounts.AUTH_TYPE_XAUTH, null);
			final ContentValues values = makeAccountContentValues(mUserColor, accessToken, user, mRESTBaseURL,
					mOAuthBaseURL, mSigningRESTBaseURL, mSigningOAuthBaseURL, mSearchBaseURL, mUploadBaseURL, null,
					Accounts.AUTH_TYPE_XAUTH);
			if (values != null) {
				resolver.insert(Accounts.CONTENT_URI, values);
			}
			return new LoginResponse(false, true, Accounts.AUTH_TYPE_XAUTH, null);

		}

		private LoginResponse doAuth() {
			switch (mAuthType) {
				case Accounts.AUTH_TYPE_OAUTH:
					return authOAuth();
				case Accounts.AUTH_TYPE_XAUTH:
					return authxAuth();
				case Accounts.AUTH_TYPE_BASIC:
					return authBasic();
				case Accounts.AUTH_TYPE_TWIP_O_MODE:
					return authTwipOMode();
				default:
					break;
			}
			mAuthType = Accounts.AUTH_TYPE_OAUTH;
			return authOAuth();
		}

		private String parseString(final Object obj) {
			if (obj == null) return null;
			return obj.toString();
		}

		class LoginResponse {

			public final boolean already_logged_in, succeed;
			public final Exception exception;

			public LoginResponse(final boolean already_logged_in, final boolean succeed, final int auth_type,
					final Exception exception) {
				this.already_logged_in = already_logged_in;
				this.succeed = succeed;
				this.exception = exception;
			}
		}
	}

}
