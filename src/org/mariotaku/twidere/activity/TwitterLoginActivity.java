package org.mariotaku.twidere.activity;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ColorAnalyser;
import org.mariotaku.twidere.util.CommonUtils;

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
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class TwitterLoginActivity extends BaseActivity implements OnClickListener, TextWatcher {

	private static final String TWITTER_SIGNUP_URL = "https://twitter.com/signup";

	private String mRestAPIBase, mSearchAPIBase, mUsername, mPassword;
	private int mAuthType, mUserColor;
	private boolean mUserColorSet;
	private EditText mEditUsername, mEditPassword;
	private Button mSignInButton, mSignUpButton;
	private LinearLayout mSigninSignup, mUsernamePassword;
	private ImageButton mSetColorButton;
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
				if (resultCode == BaseActivity.RESULT_OK)
					if (data != null && data.getExtras() != null) {
						mUserColor = data.getIntExtra(Accounts.USER_COLOR, Color.TRANSPARENT);
						mUserColorSet = true;
					} else {
						mUserColor = Color.TRANSPARENT;
						mUserColorSet = false;
					}
				setUserColorButton();
				break;
		}
		setSignInButton();
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
		Cursor cur = getContentResolver().query(Accounts.CONTENT_URI, new String[] {}, null, null,
				null);
		getSupportActionBar().setDisplayHomeAsUpEnabled(cur != null && cur.getCount() > 0);
		if (cur != null) {
			cur.close();
		}

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
		mUsernamePassword.setVisibility(mAuthType == Accounts.AUTH_TYPE_OAUTH ? View.GONE
				: View.VISIBLE);
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
				Cursor cur = getContentResolver().query(Accounts.CONTENT_URI, new String[] {},
						null, null, null);
				if (cur != null && cur.getCount() > 0) {
					finish();
				}
				if (cur != null) {
					cur.close();
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
		mSignInButton.setEnabled(mEditPassword.getText().length() > 0
				&& mEditUsername.getText().length() > 0 || mAuthType == Accounts.AUTH_TYPE_OAUTH);
	}

	private void setUserColorButton() {
		if (mUserColorSet) {
			mSetColorButton.setImageBitmap(CommonUtils.getColorPreviewBitmap(this, mUserColor));
		} else {
			mSetColorButton.setImageResource(android.R.color.transparent);
		}

	}

	private abstract class AbstractTask extends AsyncTask<Void, Void, Object> {

		@Override
		protected void onPostExecute(Object result_obj) {
			setSupportProgressBarIndeterminateVisibility(false);
			mTask = null;
		}

		@Override
		protected void onPreExecute() {
			setSupportProgressBarIndeterminateVisibility(true);
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
			User user = null;
			try {
				accessToken = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
				user = twitter.showUser(accessToken.getUserId());
			} catch (TwitterException e) {
				return CommonUtils.getErrorCode(e);
			}
			if (!mUserColorSet) {
				analyseUserProfileColor(user.getProfileImageURL().toString());
			}
			long userid = accessToken.getUserId();
			if (CommonUtils.isUserLoggedIn(TwitterLoginActivity.this, userid))
				return RESULT_ALREADY_LOGGED_IN;
			ContentValues values = CommonUtils.makeAccountContentValues(mUserColor, accessToken,
					user, mRestAPIBase, mSearchAPIBase, null, Accounts.AUTH_TYPE_OAUTH);
			resolver.insert(Accounts.CONTENT_URI, values);
			return RESULT_SUCCESS;
		}

		@Override
		protected void onPostExecute(Object result_obj) {
			Integer result = (Integer) result_obj;
			switch (result) {
				case RESULT_SUCCESS:
					Intent intent = new Intent(INTENT_ACTION_HOME);
					Bundle bundle = new Bundle();
					bundle.putBoolean(INTENT_KEY_REFRESH_ALL, true);
					intent.putExtras(bundle);
					intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
					startActivity(intent);
					finish();
					break;
				default:
					CommonUtils.showErrorMessage(TwitterLoginActivity.this, result);
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
				return new Result(CommonUtils.getErrorCode(e), Accounts.AUTH_TYPE_BASIC, null);
			}

			if (account_valid && user != null) {
				String profile_image_url = user.getProfileImageURL().toString();
				if (!mUserColorSet) {
					analyseUserProfileColor(profile_image_url);
				}

				if (CommonUtils.isUserLoggedIn(TwitterLoginActivity.this, user.getId()))
					return new Result(RESULT_ALREADY_LOGGED_IN, Accounts.AUTH_TYPE_BASIC, null);
				ContentValues values = CommonUtils.makeAccountContentValues(mUserColor, null, user,
						mRestAPIBase, mSearchAPIBase, mPassword, Accounts.AUTH_TYPE_BASIC);
				resolver.insert(Accounts.CONTENT_URI, values);
				return new Result(RESULT_SUCCESS, Accounts.AUTH_TYPE_BASIC, null);

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
				return new Result(CommonUtils.getErrorCode(e), Accounts.AUTH_TYPE_OAUTH, null);
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
			AccessToken accessToken = null;
			User user = null;
			try {
				accessToken = twitter.getOAuthAccessToken(mUsername, mPassword);
				user = twitter.showUser(accessToken.getUserId());
			} catch (TwitterException e) {
				return new Result(CommonUtils.getErrorCode(e), Accounts.AUTH_TYPE_XAUTH, null);
			}
			if (!mUserColorSet) {
				analyseUserProfileColor(user.getProfileImageURL().toString());
			}
			if (CommonUtils.isUserLoggedIn(TwitterLoginActivity.this, user.getId()))
				return new Result(RESULT_ALREADY_LOGGED_IN, Accounts.AUTH_TYPE_XAUTH, null);
			ContentValues values = CommonUtils.makeAccountContentValues(mUserColor, accessToken,
					user, mRestAPIBase, mSearchAPIBase, null, Accounts.AUTH_TYPE_XAUTH);
			resolver.insert(Accounts.CONTENT_URI, values);
			return new Result(RESULT_SUCCESS, Accounts.AUTH_TYPE_XAUTH, null);

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
					Intent intent = new Intent(TwitterLoginActivity.this, HomeActivity.class);
					Bundle bundle = new Bundle();
					bundle.putBoolean(INTENT_KEY_REFRESH_ALL, true);
					intent.putExtras(bundle);
					startActivity(intent);
					finish();
					break;
				case RESULT_OPEN_BROWSER:
					mRequestToken = result.request_token;
					Uri uri = Uri.parse(mRequestToken.getAuthorizationURL());
					startActivityForResult(new Intent(Intent.ACTION_DEFAULT, uri,
							getApplicationContext(), AuthorizationActivity.class),
							REQUEST_GOTO_AUTHORIZATION);
					break;
				default:
					CommonUtils.showErrorMessage(TwitterLoginActivity.this, result.result_code);
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
