package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getActivatedAccounts;
import static org.mariotaku.twidere.util.Utils.getColorPreviewBitmap;
import static org.mariotaku.twidere.util.Utils.isUserLoggedIn;
import static org.mariotaku.twidere.util.Utils.makeAccountContentValues;
import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ColorAnalyser;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.BasicAuthorization;
import twitter4j.auth.RequestToken;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class TwitterLoginActivity extends BaseActivity implements OnClickListener, TextWatcher {

	private static final String TWITTER_SIGNUP_URL = "https://twitter.com/signup";

	private String mRestAPIBase, mSearchAPIBase, mUsername, mPassword;

	private int mAuthType, mUserColor;

	private boolean mUserColorSet;

	private EditText mEditUsername, mEditPassword;

	private Button mSignInButton, mSignUpButton;

	private LinearLayout mSigninSignup, mUsernamePassword;

	private ImageButton mSetColorButton;

	private AbstractTask<?> mTask;

	private RequestToken mRequestToken;

	private long mLoggedId;

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_EDIT_API:
				if (resultCode == RESULT_OK) {
					Bundle bundle = new Bundle();
					if (data != null) {
						bundle = data.getExtras();
					}
					if (bundle != null) {
						mRestAPIBase = bundle.getString(Accounts.REST_API_BASE);
						mSearchAPIBase = bundle.getString(Accounts.SEARCH_API_BASE);
						mAuthType = bundle.getInt(Accounts.AUTH_TYPE);
						findViewById(R.id.username_password).setVisibility(
								mAuthType == Accounts.AUTH_TYPE_OAUTH ? View.GONE : View.VISIBLE);
						((LinearLayout) findViewById(R.id.sign_in_sign_up))
								.setOrientation(mAuthType == Accounts.AUTH_TYPE_OAUTH ? LinearLayout.VERTICAL
										: LinearLayout.HORIZONTAL);
					}
				}
				setSignInButton();
				break;
			case REQUEST_GOTO_AUTHORIZATION:
				if (resultCode == RESULT_OK) {
					Bundle bundle = new Bundle();
					if (data != null) {
						bundle = data.getExtras();
					}
					if (bundle != null) {
						String oauth_verifier = bundle.getString(OAUTH_VERIFIER);
						if (oauth_verifier != null && mRequestToken != null) {
							if (mTask != null) {
								mTask.cancel(true);
							}
							mTask = new CallbackAuthTask(mRequestToken, oauth_verifier);
							mTask.execute();
						}
					}
				}
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
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
			case R.id.sign_up:
				intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(TWITTER_SIGNUP_URL));
				startActivity(intent);
				break;
			case R.id.sign_in:
				saveEditedText();
				if (mTask != null) {
					mTask.cancel(true);
				}
				mTask = new LoginTask();
				mTask.execute();
				break;
			case R.id.set_color:
				intent = new Intent(INTENT_ACTION_SET_COLOR);
				Bundle bundle = new Bundle();
				bundle.putInt(Accounts.USER_COLOR, mUserColor);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_SET_COLOR);
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.twitter_login);
		mEditUsername = (EditText) findViewById(R.id.username);
		mEditPassword = (EditText) findViewById(R.id.password);
		mSignInButton = (Button) findViewById(R.id.sign_in);
		mSignUpButton = (Button) findViewById(R.id.sign_up);
		mSigninSignup = (LinearLayout) findViewById(R.id.sign_in_sign_up);
		mUsernamePassword = (LinearLayout) findViewById(R.id.username_password);
		mSetColorButton = (ImageButton) findViewById(R.id.set_color);
		setSupportProgressBarIndeterminateVisibility(false);
		long[] account_ids = getActivatedAccounts(this);
		boolean called_from_twidere = getPackageName().equals(getCallingPackage());
		getSupportActionBar().setDisplayHomeAsUpEnabled(account_ids.length > 0 && called_from_twidere);

		Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
		if (bundle == null) {
			bundle = new Bundle();
		}
		mRestAPIBase = bundle.getString(Accounts.REST_API_BASE);
		mSearchAPIBase = bundle.getString(Accounts.SEARCH_API_BASE);

		if (mRestAPIBase == null) {
			mRestAPIBase = DEFAULT_REST_API_BASE;
		}
		if (mSearchAPIBase == null) {
			mSearchAPIBase = DEFAULT_SEARCH_API_BASE;
		}

		mUsername = bundle.getString(Accounts.USERNAME);
		mPassword = bundle.getString(Accounts.PASSWORD);
		mAuthType = bundle.getInt(Accounts.AUTH_TYPE);
		mSignInButton.setOnClickListener(this);
		mSignUpButton.setOnClickListener(this);
		mSetColorButton.setOnClickListener(this);
		mUsernamePassword.setVisibility(mAuthType == Accounts.AUTH_TYPE_OAUTH ? View.GONE : View.VISIBLE);
		mSigninSignup.setOrientation(mAuthType == Accounts.AUTH_TYPE_OAUTH ? LinearLayout.VERTICAL
				: LinearLayout.HORIZONTAL);

		mEditUsername.setText(mUsername);
		mEditUsername.addTextChangedListener(this);
		mEditPassword.setText(mPassword);
		mEditPassword.addTextChangedListener(this);
		setSignInButton();
		setUserColorButton();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_login, menu);
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
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent = new Intent();

		switch (item.getItemId()) {
			case MENU_HOME:
				long[] account_ids = getActivatedAccounts(this);
				boolean called_from_twidere = getPackageName().equals(getCallingPackage());
				if (account_ids.length > 0 && called_from_twidere) {
					finish();
				}
				break;
			case MENU_SETTINGS:
				intent = new Intent(INTENT_ACTION_GLOBAL_SETTINGS);
				startActivity(intent);
				break;
			case MENU_EDIT_API:
				intent = new Intent(INTENT_ACTION_EDIT_API);
				Bundle bundle = new Bundle();
				bundle.putString(Accounts.REST_API_BASE, mRestAPIBase);
				bundle.putString(Accounts.SEARCH_API_BASE, mSearchAPIBase);
				bundle.putInt(Accounts.AUTH_TYPE, mAuthType);
				intent.putExtras(bundle);
				startActivityForResult(intent, REQUEST_EDIT_API);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		saveEditedText();
		outState.putString(Accounts.REST_API_BASE, mRestAPIBase);
		outState.putString(Accounts.SEARCH_API_BASE, mSearchAPIBase);
		outState.putString(Accounts.USERNAME, mUsername);
		outState.putString(Accounts.PASSWORD, mPassword);
		outState.putInt(Accounts.USER_COLOR, mUserColor);
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		setSignInButton();
	}

	@Override
	public void setRefreshState() {
		// Do nothing.
	}

	private void analyseUserProfileColor(String url_string) {
		try {
			URL url = new URL(url_string);
			InputStream is = url.openConnection().getInputStream();
			Bitmap bm = BitmapFactory.decodeStream(is);
			mUserColor = ColorAnalyser.analyse(bm);
			mUserColorSet = true;
			return;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
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

	private void setSignInButton() {
		mSignInButton.setEnabled(mEditPassword.getText().length() > 0 && mEditUsername.getText().length() > 0
				|| mAuthType == Accounts.AUTH_TYPE_OAUTH);
	}

	private void setUserColorButton() {
		if (mUserColorSet) {
			mSetColorButton.setImageBitmap(getColorPreviewBitmap(this, mUserColor));
		} else {
			mSetColorButton.setImageResource(android.R.color.transparent);
		}

	}

	private abstract class AbstractTask<Result> extends AsyncTask<Void, Void, Result> {

		@Override
		protected void onPostExecute(Result result) {
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

	private class CallbackAuthTask extends AbstractTask<CallbackAuthTask.Response> {

		private RequestToken requestToken;
		private String oauthVerifier;

		public CallbackAuthTask(RequestToken requestToken, String oauthVerifier) {
			this.requestToken = requestToken;
			this.oauthVerifier = oauthVerifier;
		}

		@Override
		protected Response doInBackground(Void... params) {
			final SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
			final boolean enable_gzip_compressing = preferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
			final boolean ignore_ssl_error = preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
			final ContentResolver resolver = getContentResolver();
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setRestBaseURL(mRestAPIBase);
			cb.setSearchBaseURL(mSearchAPIBase);
			cb.setOAuthConsumerKey(CONSUMER_KEY);
			cb.setOAuthConsumerSecret(CONSUMER_SECRET);
			cb.setGZIPEnabled(enable_gzip_compressing);
			cb.setIgnoreSSLError(ignore_ssl_error);
			final Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			AccessToken accessToken = null;
			User user = null;
			try {
				accessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
				user = twitter.showUser(accessToken.getUserId());
			} catch (TwitterException e) {
				return new Response(false, false, e);
			}
			if (!mUserColorSet) {
				analyseUserProfileColor(user.getProfileImageURL().toString());
			}
			mLoggedId = user.getId();
			if (isUserLoggedIn(TwitterLoginActivity.this, mLoggedId)) return new Response(false, true, null);
			ContentValues values = makeAccountContentValues(mUserColor, accessToken, user, mRestAPIBase,
					mSearchAPIBase, null, Accounts.AUTH_TYPE_OAUTH);
			resolver.insert(Accounts.CONTENT_URI, values);
			return new Response(true, false, null);
		}

		@Override
		protected void onPostExecute(Response result) {
			if (result.succeed) {
				Intent intent = new Intent(INTENT_ACTION_HOME);
				Bundle bundle = new Bundle();
				bundle.putLongArray(INTENT_KEY_IDS, new long[] { mLoggedId });
				intent.putExtras(bundle);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				finish();
			} else if (result.is_logged_in) {

			} else {
				showErrorToast(TwitterLoginActivity.this, result.exception, true);
			}
			super.onPostExecute(result);
		}

		private class Response {
			public boolean succeed, is_logged_in;
			public TwitterException exception;

			public Response(boolean succeed, boolean is_logged_in, TwitterException exception) {
				this.succeed = succeed;
				this.is_logged_in = is_logged_in;
				this.exception = exception;
			}
		}

	}

	private class LoginTask extends AbstractTask<LoginTask.Response> {

		@Override
		protected Response doInBackground(Void... params) {
			return doAuth();
		}

		@Override
		protected void onPostExecute(Response result) {

			if (result.succeed) {
				Intent intent = new Intent(INTENT_ACTION_HOME);
				Bundle bundle = new Bundle();
				bundle.putLongArray(INTENT_KEY_IDS, new long[] { mLoggedId });
				intent.putExtras(bundle);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				startActivity(intent);
				finish();
			} else if (result.open_browser) {
				mRequestToken = result.request_token;
				Uri uri = Uri.parse(mRequestToken.getAuthorizationURL());
				startActivityForResult(new Intent(Intent.ACTION_DEFAULT, uri, getApplicationContext(),
						AuthorizationActivity.class), REQUEST_GOTO_AUTHORIZATION);
			} else if (result.already_logged_in) {
				Toast.makeText(TwitterLoginActivity.this, R.string.error_already_logged_in, Toast.LENGTH_SHORT).show();
			} else {
				showErrorToast(TwitterLoginActivity.this, result.exception, true);
			}
			super.onPostExecute(result);
		}

		private Response authBasic() {
			final SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
			final boolean enable_gzip_compressing = preferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
			final boolean ignore_ssl_error = preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
			final ContentResolver resolver = getContentResolver();
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setRestBaseURL(mRestAPIBase);
			cb.setSearchBaseURL(mSearchAPIBase);
			cb.setGZIPEnabled(enable_gzip_compressing);
			cb.setIgnoreSSLError(ignore_ssl_error);
			
			Twitter twitter = new TwitterFactory(cb.build()).getInstance(new BasicAuthorization(mUsername, mPassword));
			boolean account_valid = false;
			User user = null;
			try {
				account_valid = twitter.test();
				user = twitter.verifyCredentials();
			} catch (TwitterException e) {
				return new Response(false, false, false, Accounts.AUTH_TYPE_BASIC, null, e);
			}

			if (account_valid && user != null) {
				String profile_image_url = user.getProfileImageURL().toString();
				if (!mUserColorSet) {
					analyseUserProfileColor(profile_image_url);
				}

				mLoggedId = user.getId();
				if (isUserLoggedIn(TwitterLoginActivity.this, mLoggedId))
					return new Response(false, true, false, Accounts.AUTH_TYPE_BASIC, null, null);
				ContentValues values = makeAccountContentValues(mUserColor, null, user, mRestAPIBase, mSearchAPIBase,
						mPassword, Accounts.AUTH_TYPE_BASIC);
				resolver.insert(Accounts.CONTENT_URI, values);
				return new Response(false, false, true, Accounts.AUTH_TYPE_BASIC, null, null);

			}
			return new Response(false, false, false, Accounts.AUTH_TYPE_BASIC, null, null);
		}

		private Response authOAuth() {
			final SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
			final boolean enable_gzip_compressing = preferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
			final boolean ignore_ssl_error = preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setRestBaseURL(mRestAPIBase);
			cb.setSearchBaseURL(mSearchAPIBase);
			cb.setOAuthConsumerKey(CONSUMER_KEY);
			cb.setOAuthConsumerSecret(CONSUMER_SECRET);
			cb.setGZIPEnabled(enable_gzip_compressing);
			cb.setIgnoreSSLError(ignore_ssl_error);
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			RequestToken requestToken = null;
			try {
				requestToken = twitter.getOAuthRequestToken(DEFAULT_OAUTH_CALLBACK);
			} catch (TwitterException e) {
				return new Response(false, false, false, Accounts.AUTH_TYPE_OAUTH, null, e);
			}
			if (requestToken != null)
				return new Response(true, false, false, Accounts.AUTH_TYPE_OAUTH, requestToken, null);
			return new Response(false, false, false, Accounts.AUTH_TYPE_OAUTH, null, null);
		}

		private Response authxAuth() {
			final SharedPreferences preferences = getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
			final boolean enable_gzip_compressing = preferences.getBoolean(PREFERENCE_KEY_GZIP_COMPRESSING, false);
			final boolean ignore_ssl_error = preferences.getBoolean(PREFERENCE_KEY_IGNORE_SSL_ERROR, false);
			final ContentResolver resolver = getContentResolver();
			final ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setRestBaseURL(mRestAPIBase);
			cb.setSearchBaseURL(mSearchAPIBase);
			cb.setOAuthConsumerKey(CONSUMER_KEY);
			cb.setOAuthConsumerSecret(CONSUMER_SECRET);
			cb.setGZIPEnabled(enable_gzip_compressing);
			cb.setIgnoreSSLError(ignore_ssl_error);
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			AccessToken accessToken = null;
			User user = null;
			try {
				accessToken = twitter.getOAuthAccessToken(mUsername, mPassword);
				user = twitter.showUser(accessToken.getUserId());
			} catch (TwitterException e) {
				return new Response(false, false, false, Accounts.AUTH_TYPE_XAUTH, null, e);
			}
			if (!mUserColorSet) {
				analyseUserProfileColor(user.getProfileImageURL().toString());
			}

			mLoggedId = user.getId();
			if (isUserLoggedIn(TwitterLoginActivity.this, mLoggedId))
				return new Response(false, true, false, Accounts.AUTH_TYPE_XAUTH, null, null);
			ContentValues values = makeAccountContentValues(mUserColor, accessToken, user, mRestAPIBase,
					mSearchAPIBase, null, Accounts.AUTH_TYPE_XAUTH);
			resolver.insert(Accounts.CONTENT_URI, values);
			return new Response(false, false, true, Accounts.AUTH_TYPE_XAUTH, null, null);

		}

		private Response doAuth() {
			switch (mAuthType) {
				case Accounts.AUTH_TYPE_OAUTH:
					return authOAuth();
				case Accounts.AUTH_TYPE_XAUTH:
					return authxAuth();
				case Accounts.AUTH_TYPE_BASIC:
					return authBasic();
				default:
					break;
			}
			mAuthType = Accounts.AUTH_TYPE_OAUTH;
			return authOAuth();
		}

		private class Response {

			public boolean open_browser, already_logged_in, succeed;
			public RequestToken request_token;
			public TwitterException exception;

			public Response(boolean open_browser, boolean already_logged_in, boolean succeed, int auth_type,
					RequestToken request_token, TwitterException exception) {
				this.open_browser = open_browser;
				this.already_logged_in = already_logged_in;
				this.succeed = succeed;
				if (exception != null) {
					this.exception = exception;
					return;
				}
				switch (auth_type) {
					case Accounts.AUTH_TYPE_OAUTH:
						if (open_browser && request_token == null)
							throw new IllegalArgumentException("Request Token cannot be null in oauth mode!");
						this.request_token = request_token;
						break;
					case Accounts.AUTH_TYPE_XAUTH:
					case Accounts.AUTH_TYPE_BASIC:
						if (request_token != null)
							throw new IllegalArgumentException("Request Token must be null in xauth/basic mode!");
						break;
				}
			}
		}
	}

}
