package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class EditAPIActivity extends BaseDialogActivity implements OnCheckedChangeListener, OnClickListener {

	private EditText mEditRestBaseURL, mEditSearchBaseURL, mEditUploadBaseURL, mEditoAuthAccessTokenURL,
			mEditoAuthenticationURL, mEditoAuthorizationURL, mEditoAuthRequestTokenURL;
	private RadioGroup mEditAuthType;
	private RadioButton mButtonOAuth, mButtonxAuth, mButtonBasic, mButtonTwipOMode;
	private Button mSaveButton;
	private String mRestBaseURL, mSearchBaseURL, mUploadBaseURL, mOAuthAccessTokenURL, mOAuthAuthenticationURL,
			mOAuthAuthorizationURL, mOAuthRequestTokenURL;
	private TextView mAdvancedAPIConfigLabel;
	private int mAuthType;

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.oauth: {
				mAuthType = Accounts.AUTH_TYPE_OAUTH;
				break;
			}
			case R.id.xauth: {
				mAuthType = Accounts.AUTH_TYPE_XAUTH;
				break;
			}
			case R.id.basic: {
				mAuthType = Accounts.AUTH_TYPE_BASIC;
				break;
			}
			case R.id.twip_o: {
				mAuthType = Accounts.AUTH_TYPE_TWIP_O_MODE;
				break;
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.save: {
				saveEditedText();
				Bundle bundle = new Bundle();
				bundle.putString(Accounts.REST_BASE_URL, mRestBaseURL);
				bundle.putString(Accounts.SEARCH_BASE_URL, mSearchBaseURL);
				bundle.putString(Accounts.UPLOAD_BASE_URL, mUploadBaseURL);
				bundle.putString(Accounts.OAUTH_ACCESS_TOKEN_URL, mOAuthAccessTokenURL);
				bundle.putString(Accounts.OAUTH_AUTHENTICATION_URL, mOAuthAuthenticationURL);
				bundle.putString(Accounts.OAUTH_AUTHORIZATION_URL, mOAuthAuthorizationURL);
				bundle.putString(Accounts.OAUTH_REQUEST_TOKEN_URL, mOAuthRequestTokenURL);
				bundle.putInt(Accounts.AUTH_TYPE, mAuthType);
				setResult(RESULT_OK, new Intent().putExtras(bundle));
				finish();
				break;
			}
			case R.id.advanced_api_config_label: {
				View stub_view = findViewById(R.id.stub_advanced_api_config);
				View inflated_view = findViewById(R.id.advanced_api_config);
				if (stub_view != null) {
					stub_view.setVisibility(View.VISIBLE);
					mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.expander_ic_maximized,
							0, 0, 0);
					mEditSearchBaseURL = (EditText) findViewById(R.id.search_base_url);
					mEditSearchBaseURL.setText(mSearchBaseURL != null ? mSearchBaseURL : DEFAULT_SEARCH_BASE_URL);
					mEditUploadBaseURL = (EditText) findViewById(R.id.upload_base_url);
					mEditUploadBaseURL.setText(mUploadBaseURL != null ? mUploadBaseURL : DEFAULT_UPLOAD_BASE_URL);
					mEditoAuthAccessTokenURL = (EditText) findViewById(R.id.oauth_access_token_url);
					mEditoAuthAccessTokenURL.setText(mOAuthAccessTokenURL != null ? mOAuthAccessTokenURL
							: DEFAULT_OAUTH_ACCESS_TOKEN_URL);
					mEditoAuthenticationURL = (EditText) findViewById(R.id.oauth_authentication_url);
					mEditoAuthenticationURL.setText(mOAuthAuthenticationURL != null ? mOAuthAuthenticationURL
							: DEFAULT_OAUTH_AUTHENTICATION_URL);
					mEditoAuthorizationURL = (EditText) findViewById(R.id.oauth_authorization_url);
					mEditoAuthorizationURL.setText(mOAuthAuthorizationURL != null ? mOAuthAuthorizationURL
							: DEFAULT_OAUTH_AUTHORIZATION_URL);
					mEditoAuthRequestTokenURL = (EditText) findViewById(R.id.oauth_request_token_url);
					mEditoAuthRequestTokenURL.setText(mOAuthRequestTokenURL != null ? mOAuthRequestTokenURL
							: DEFAULT_OAUTH_REQUEST_TOKEN_URL);
				} else if (inflated_view != null) {
					boolean is_visible = inflated_view.getVisibility() == View.VISIBLE;
					int compound_res = is_visible ? R.drawable.expander_ic_maximized : R.drawable.expander_ic_minimized;
					mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(compound_res, 0, 0, 0);
					inflated_view.setVisibility(is_visible ? View.GONE : View.VISIBLE);
				}
				break;
			}
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_api);
		mEditRestBaseURL = (EditText) findViewById(R.id.rest_base_url);
		mEditAuthType = (RadioGroup) findViewById(R.id.auth_type);
		mButtonOAuth = (RadioButton) findViewById(R.id.oauth);
		mButtonxAuth = (RadioButton) findViewById(R.id.xauth);
		mButtonBasic = (RadioButton) findViewById(R.id.basic);
		mButtonTwipOMode = (RadioButton) findViewById(R.id.twip_o);
		mAdvancedAPIConfigLabel = (TextView) findViewById(R.id.advanced_api_config_label);
		mSaveButton = (Button) findViewById(R.id.save);
		Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
		if (bundle == null) {
			bundle = new Bundle();
		}
		mRestBaseURL = bundle.getString(Accounts.REST_BASE_URL);
		mSearchBaseURL = bundle.getString(Accounts.SEARCH_BASE_URL);
		mUploadBaseURL = bundle.getString(Accounts.UPLOAD_BASE_URL);
		mOAuthAccessTokenURL = bundle.getString(Accounts.OAUTH_ACCESS_TOKEN_URL);
		mOAuthAuthenticationURL = bundle.getString(Accounts.OAUTH_AUTHENTICATION_URL);
		mOAuthAuthorizationURL = bundle.getString(Accounts.OAUTH_AUTHORIZATION_URL);
		mOAuthRequestTokenURL = bundle.getString(Accounts.OAUTH_REQUEST_TOKEN_URL);

		mAuthType = bundle.getInt(Accounts.AUTH_TYPE);
		mEditAuthType.setOnCheckedChangeListener(this);
		mAdvancedAPIConfigLabel.setOnClickListener(this);
		mSaveButton.setOnClickListener(this);
		mEditRestBaseURL.setText(mRestBaseURL != null ? mRestBaseURL : DEFAULT_REST_BASE_URL);

		mButtonOAuth.setChecked(mAuthType == Accounts.AUTH_TYPE_OAUTH);
		mButtonxAuth.setChecked(mAuthType == Accounts.AUTH_TYPE_XAUTH);
		mButtonBasic.setChecked(mAuthType == Accounts.AUTH_TYPE_BASIC);
		mButtonTwipOMode.setChecked(mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		saveEditedText();
		outState.putString(Accounts.REST_BASE_URL, mRestBaseURL);
		outState.putString(Accounts.SEARCH_BASE_URL, mSearchBaseURL);
		outState.putString(Accounts.UPLOAD_BASE_URL, mUploadBaseURL);
		outState.putString(Accounts.OAUTH_ACCESS_TOKEN_URL, mOAuthAccessTokenURL);
		outState.putString(Accounts.OAUTH_AUTHENTICATION_URL, mOAuthAuthenticationURL);
		outState.putString(Accounts.OAUTH_AUTHORIZATION_URL, mOAuthAuthorizationURL);
		outState.putString(Accounts.OAUTH_REQUEST_TOKEN_URL, mOAuthRequestTokenURL);
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		super.onSaveInstanceState(outState);
	}

	private void saveEditedText() {
		if (mEditRestBaseURL != null) {
			Editable ed = mEditRestBaseURL.getText();
			if (ed != null) {
				mRestBaseURL = ed.toString();
			}
		}
		if (mEditSearchBaseURL != null) {
			Editable ed = mEditSearchBaseURL.getText();
			if (ed != null) {
				mSearchBaseURL = ed.toString();
			}
		}
		if (mEditUploadBaseURL != null) {
			Editable ed = mEditUploadBaseURL.getText();
			if (ed != null) {
				mUploadBaseURL = ed.toString();
			}
		}
		if (mEditoAuthAccessTokenURL != null) {
			Editable ed = mEditoAuthAccessTokenURL.getText();
			if (ed != null) {
				mOAuthAccessTokenURL = ed.toString();
			}
		}
		if (mEditoAuthenticationURL != null) {
			Editable ed = mEditoAuthenticationURL.getText();
			if (ed != null) {
				mOAuthAuthenticationURL = ed.toString();
			}
		}
		if (mEditoAuthorizationURL != null) {
			Editable ed = mEditoAuthorizationURL.getText();
			if (ed != null) {
				mOAuthAuthorizationURL = ed.toString();
			}
		}
		if (mEditoAuthRequestTokenURL != null) {
			Editable ed = mEditoAuthRequestTokenURL.getText();
			if (ed != null) {
				mOAuthRequestTokenURL = ed.toString();
			}
		}
	}
}
