package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class LoginActivity extends SherlockFragmentActivity implements Constants, OnClickListener,
		TextWatcher {

	private final static int API_SETTINGS = 1;
	private final static int GOTO_AUTHORIZATION = 2;

	private final static String TWITTER_SIGNUP_URL = "https://twitter.com/signup";

	private String mRestAPIBase, mSearchAPIBase, mUsername, mPassword;
	private int mAuthType;
	private EditText mEditUsername, mEditPassword;
	private Button mSignInButton, mSignUpButton;
	private AbstractTask mTask;
	private RequestToken mRequestToken;

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case API_SETTINGS:
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
				break;
			case GOTO_AUTHORIZATION:
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
		}
		setSignInButton();
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.sign_up:
				startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(TWITTER_SIGNUP_URL)));
				break;
			case R.id.sign_in:
				saveEditedText();
				if (mTask != null) {
					mTask.cancel(true);
				}
				mTask = new LoginTask();
				mTask.execute();
				break;
		}

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		configureActivity();
		setSupportProgressBarIndeterminateVisibility(false);

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
		findViewById(R.id.username_password).setVisibility(
				mAuthType == Accounts.AUTH_TYPE_OAUTH ? View.GONE : View.VISIBLE);
		((LinearLayout) findViewById(R.id.sign_in_sign_up))
				.setOrientation(mAuthType == Accounts.AUTH_TYPE_OAUTH ? LinearLayout.VERTICAL
						: LinearLayout.HORIZONTAL);

		mEditUsername.setText(mUsername);
		mEditUsername.addTextChangedListener(this);
		mEditPassword.setText(mPassword);
		mEditPassword.addTextChangedListener(this);
		setSignInButton();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.login, menu);
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
		switch (item.getItemId()) {
			case R.id.settings:
				Intent intent = new Intent(this, APISettingsActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString(Accounts.REST_API_BASE, mRestAPIBase);
				bundle.putString(Accounts.SEARCH_API_BASE, mSearchAPIBase);
				bundle.putInt(Accounts.AUTH_TYPE, mAuthType);
				intent.putExtras(bundle);
				startActivityForResult(intent, API_SETTINGS);
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
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		setSignInButton();
	}

	private void configureActivity() {
		setContentView(R.layout.login);
		mSignInButton = (Button) findViewById(R.id.sign_in);
		mSignUpButton = (Button) findViewById(R.id.sign_up);
		mEditUsername = (EditText) findViewById(R.id.username);
		mEditPassword = (EditText) findViewById(R.id.password);
		mSignInButton.setOnClickListener(this);
		mSignUpButton.setOnClickListener(this);
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
		mSignInButton.setEnabled(mEditPassword.getText().length() > 0
				&& mEditUsername.getText().length() > 0 || mAuthType == Accounts.AUTH_TYPE_OAUTH);
	}

	private abstract class AbstractTask extends AsyncTask<Void, Void, Object> {

		protected final static int RESULT_UNKNOWN_ERROR = -1;
		protected final static int RESULT_SUCCESS = 0;
		protected final static int RESULT_ALREADY_LOGGED_IN = 1;
		protected final static int RESULT_CONNECTIVITY_ERROR = 2;
		protected final static int RESULT_SERVER_ERROR = 3;
		protected final static int RESULT_BAD_ADDRESS = 4;
		protected final static int RESULT_NO_PERMISSION = 5;
		protected final static int RESULT_OPEN_BROWSER = 6;

		protected int getErrorCode(TwitterException e) {
			if (e == null) return RESULT_UNKNOWN_ERROR;
			int status_code = e.getStatusCode();
			if (status_code == -1)
				return RESULT_CONNECTIVITY_ERROR;
			else if (status_code >= 401 && status_code < 404)
				return RESULT_NO_PERMISSION;
			else if (status_code >= 404 && status_code < 500)
				return RESULT_BAD_ADDRESS;
			else if (status_code >= 500 && status_code < 600)
				return RESULT_SERVER_ERROR;
			else
				return RESULT_UNKNOWN_ERROR;
		}

		@Override
		protected void onPostExecute(Object result_obj) {
			setSupportProgressBarIndeterminateVisibility(false);
			mTask = null;
		}

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
		}

		protected void showErrorMessage(int error_code) {
			switch (error_code) {
				case RESULT_ALREADY_LOGGED_IN:
					Toast.makeText(getApplicationContext(), R.string.error_already_logged_in,
							Toast.LENGTH_SHORT).show();
					break;
				case RESULT_CONNECTIVITY_ERROR:
					Toast.makeText(getApplicationContext(), R.string.error_connectivity_error,
							Toast.LENGTH_SHORT).show();
					break;
				case RESULT_SERVER_ERROR:
					Toast.makeText(getApplicationContext(), R.string.error_server_error,
							Toast.LENGTH_SHORT).show();
					break;
				case RESULT_BAD_ADDRESS:
					Toast.makeText(getApplicationContext(), R.string.error_bad_address,
							Toast.LENGTH_SHORT).show();
					break;
				case RESULT_NO_PERMISSION:
					Toast.makeText(getApplicationContext(), R.string.error_no_permission,
							Toast.LENGTH_SHORT).show();
					break;
				case RESULT_UNKNOWN_ERROR:
					Toast.makeText(getApplicationContext(), R.string.error_unknown_error,
							Toast.LENGTH_SHORT).show();
					break;
			}
		}
	}

	private class CallbackAuthTask extends AbstractTask {

		private RequestToken requestToken;
		private String oauthVerifier;

		public CallbackAuthTask(RequestToken requestToken, String oauthVerifier) {
			this.requestToken = requestToken;
			this.oauthVerifier = oauthVerifier;
		}

		@Override
		protected Integer doInBackground(Void... params) {
			ContentResolver resolver = getContentResolver();
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setRestBaseURL(mRestAPIBase);
			cb.setSearchBaseURL(mSearchAPIBase);
			cb.setOAuthConsumerKey(CONSUMER_KEY);
			cb.setOAuthConsumerSecret(CONSUMER_SECRET);
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			AccessToken accessToken = null;
			try {
				accessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
			} catch (TwitterException e) {
				return getErrorCode(e);
			}
			if (accessToken != null) {
				long userid = accessToken.getUserId();
				String[] cols = new String[] {};
				StringBuilder where = new StringBuilder();
				where.append(Accounts.USER_ID + "='" + userid + "'");
				Cursor cur = resolver.query(Accounts.CONTENT_URI, cols, where.toString(), null,
						null);
				if (cur != null) {
					if (cur.getCount() > 0) {
						cur.close();
						return RESULT_ALREADY_LOGGED_IN;
					} else {
						ContentValues values = new ContentValues();
						values.put(Accounts.AUTH_TYPE, Accounts.AUTH_TYPE_OAUTH);
						values.put(Accounts.USER_ID, userid);
						values.put(Accounts.REST_API_BASE, mRestAPIBase);
						values.put(Accounts.SEARCH_API_BASE, mSearchAPIBase);
						values.put(Accounts.USERNAME, accessToken.getScreenName());
						values.put(Accounts.OAUTH_TOKEN, accessToken.getToken());
						values.put(Accounts.TOKEN_SECRET, accessToken.getTokenSecret());
						values.put(Accounts.IS_ACTIVATED, 1);
						resolver.insert(Accounts.CONTENT_URI, values);
					}
					cur.close();
					return RESULT_SUCCESS;
				}
			}
			return RESULT_UNKNOWN_ERROR;
		}

		@Override
		protected void onPostExecute(Object result_obj) {
			Integer result = (Integer) result_obj;
			switch (result) {
				case RESULT_SUCCESS:
					finish();
					break;
				default:
					showErrorMessage(result);
					break;
			}
			super.onPostExecute(result_obj);
		}

	}

	private class LoginTask extends AbstractTask {

		private Result authBasic() {
			ContentResolver resolver = getContentResolver();
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setRestBaseURL(mRestAPIBase);
			cb.setSearchBaseURL(mSearchAPIBase);
			Twitter twitter = new TwitterFactory(cb.build()).getInstance(new BasicAuthorization(
					mUsername, mPassword));
			boolean account_valid = false;
			User user = null;
			try {
				account_valid = twitter.test();
				user = twitter.verifyCredentials();
			} catch (TwitterException e) {
				return new Result(getErrorCode(e), Accounts.AUTH_TYPE_BASIC, null);
			}

			if (account_valid && user != null) {
				String[] cols = new String[] {};
				StringBuilder where = new StringBuilder();
				where.append(Accounts.USER_ID + "='" + user.getId() + "'");
				Cursor cur = resolver.query(Accounts.CONTENT_URI, cols, where.toString(), null,
						null);
				if (cur != null) {
					if (cur.getCount() > 0) {
						cur.close();
						return new Result(RESULT_ALREADY_LOGGED_IN, Accounts.AUTH_TYPE_BASIC, null);
					} else {
						ContentValues values = new ContentValues();
						values.put(Accounts.AUTH_TYPE, Accounts.AUTH_TYPE_BASIC);
						values.put(Accounts.USER_ID, user.getId());
						values.put(Accounts.REST_API_BASE, mRestAPIBase);
						values.put(Accounts.SEARCH_API_BASE, mSearchAPIBase);
						values.put(Accounts.USERNAME, user.getScreenName());
						values.put(Accounts.BASIC_AUTH_PASSWORD, mPassword);
						values.put(Accounts.IS_ACTIVATED, 1);
						resolver.insert(Accounts.CONTENT_URI, values);
					}
					cur.close();
					return new Result(RESULT_SUCCESS, Accounts.AUTH_TYPE_BASIC, null);
				}
			}
			return new Result(RESULT_UNKNOWN_ERROR, Accounts.AUTH_TYPE_BASIC, null);
		}

		private Result authOAuth() {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setRestBaseURL(mRestAPIBase);
			cb.setSearchBaseURL(mSearchAPIBase);
			cb.setOAuthConsumerKey(CONSUMER_KEY);
			cb.setOAuthConsumerSecret(CONSUMER_SECRET);
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			RequestToken requestToken = null;
			try {
				requestToken = twitter.getOAuthRequestToken(DEFAULT_OAUTH_CALLBACK);
			} catch (TwitterException e) {
				return new Result(getErrorCode(e), Accounts.AUTH_TYPE_OAUTH, null);
			}
			if (requestToken != null)
				return new Result(RESULT_OPEN_BROWSER, Accounts.AUTH_TYPE_OAUTH, requestToken);
			return new Result(RESULT_UNKNOWN_ERROR, Accounts.AUTH_TYPE_OAUTH, null);
		}

		private Result authxAuth() {
			ContentResolver resolver = getContentResolver();
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setRestBaseURL(mRestAPIBase);
			cb.setSearchBaseURL(mSearchAPIBase);
			cb.setOAuthConsumerKey(CONSUMER_KEY);
			cb.setOAuthConsumerSecret(CONSUMER_SECRET);
			Twitter twitter = new TwitterFactory(cb.build()).getInstance();
			AccessToken accesstoken = null;
			try {
				accesstoken = twitter.getOAuthAccessToken(mUsername, mPassword);
			} catch (TwitterException e) {
				return new Result(getErrorCode(e), Accounts.AUTH_TYPE_XAUTH, null);
			}
			if (accesstoken != null) {
				long userid = accesstoken.getUserId();
				String[] cols = new String[] {};
				StringBuilder where = new StringBuilder();
				where.append(Accounts.USER_ID + "='" + userid + "'");
				Cursor cur = resolver.query(Accounts.CONTENT_URI, cols, where.toString(), null,
						null);
				if (cur != null) {
					if (cur.getCount() > 0) {
						cur.close();
						return new Result(RESULT_ALREADY_LOGGED_IN, Accounts.AUTH_TYPE_XAUTH, null);
					} else {
						ContentValues values = new ContentValues();
						values.put(Accounts.AUTH_TYPE, Accounts.AUTH_TYPE_XAUTH);
						values.put(Accounts.USER_ID, userid);
						values.put(Accounts.REST_API_BASE, mRestAPIBase);
						values.put(Accounts.SEARCH_API_BASE, mSearchAPIBase);
						values.put(Accounts.USERNAME, accesstoken.getScreenName());
						values.put(Accounts.OAUTH_TOKEN, accesstoken.getToken());
						values.put(Accounts.TOKEN_SECRET, accesstoken.getTokenSecret());
						values.put(Accounts.IS_ACTIVATED, 1);
						resolver.insert(Accounts.CONTENT_URI, values);
					}
					cur.close();
					return new Result(RESULT_SUCCESS, Accounts.AUTH_TYPE_XAUTH, null);
				}
			}
			return new Result(RESULT_UNKNOWN_ERROR, Accounts.AUTH_TYPE_XAUTH, null);
		}

		private Result doAuth() {
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

		@Override
		protected Result doInBackground(Void... params) {
			return doAuth();
		}

		@Override
		protected void onPostExecute(Object result_obj) {
			Result result = (Result) result_obj;
			switch (result.result_code) {
				case RESULT_SUCCESS:
					startActivity(new Intent(getApplicationContext(), HomeActivity.class));
					finish();
					break;
				case RESULT_OPEN_BROWSER:
					mRequestToken = result.request_token;
					Uri uri = Uri.parse(mRequestToken.getAuthorizationURL());
					startActivityForResult(new Intent(Intent.ACTION_DEFAULT, uri,
							getApplicationContext(), AuthorizationActivity.class),
							GOTO_AUTHORIZATION);
					break;
				default:
					showErrorMessage(result.result_code);
					break;
			}
			super.onPostExecute(result_obj);
		}

		private class Result {

			public int result_code;
			public RequestToken request_token;

			public Result(int result_code, int auth_type, RequestToken request_token) {
				this.result_code = result_code;
				switch (auth_type) {
					case Accounts.AUTH_TYPE_OAUTH:
						if (result_code == RESULT_OPEN_BROWSER && request_token == null)
							throw new IllegalArgumentException(
									"Request Token cannot be null in oauth mode!");
						this.request_token = request_token;
						break;
					case Accounts.AUTH_TYPE_XAUTH:
					case Accounts.AUTH_TYPE_BASIC:
						if (request_token != null)
							throw new IllegalArgumentException(
									"Request Token must be null in xauth/basic mode!");
						break;
				}
			}
		}
	}

}
