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

import static org.mariotaku.twidere.util.Utils.isValidUrl;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;
import org.mariotaku.twidere.util.ParseUtils;

import twitter4j.TwitterConstants;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class EditAPIActivity extends BaseDialogActivity implements TwitterConstants, OnCheckedChangeListener,
		OnClickListener {

	private EditText mEditRestBaseURL, mEditSigningRESTBaseURL, mEditOAuthBaseURL, mEditSigningOAuthBaseURL;
	private RadioGroup mEditAuthType;
	private RadioButton mButtonOAuth, mButtonxAuth, mButtonBasic, mButtonTwipOMode;
	private Button mSaveButton;
	private String mRestBaseURL, mSigningRESTBaseURL, mOAuthBaseURL, mSigningOAuthBaseURL;
	private TextView mAdvancedAPIConfigLabel;
	private int mAuthType;

	@Override
	public void onCheckedChanged(final RadioGroup group, final int checkedId) {
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
	public void onClick(final View v) {
		switch (v.getId()) {
			case R.id.save: {
				saveEditedText();
				if (checkUrlErrors()) return;
				final Bundle bundle = new Bundle();
				bundle.putString(Accounts.REST_BASE_URL, mRestBaseURL);
				bundle.putString(Accounts.SIGNING_REST_BASE_URL, mSigningRESTBaseURL);
				bundle.putString(Accounts.OAUTH_BASE_URL, mOAuthBaseURL);
				bundle.putString(Accounts.SIGNING_OAUTH_BASE_URL, mSigningOAuthBaseURL);
				bundle.putInt(Accounts.AUTH_TYPE, mAuthType);
				setResult(RESULT_OK, new Intent().putExtras(bundle));
				finish();
				break;
			}
			case R.id.advanced_api_config_label: {
				final View stub_view = findViewById(R.id.stub_advanced_api_config);
				final View inflated_view = findViewById(R.id.advanced_api_config);
				if (stub_view != null) {
					stub_view.setVisibility(View.VISIBLE);
					mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.expander_open_holo, 0,
							0, 0);
					mEditSigningRESTBaseURL = (EditText) findViewById(R.id.signing_rest_base_url);
					mEditSigningRESTBaseURL.setText(mSigningRESTBaseURL != null ? mSigningRESTBaseURL
							: DEFAULT_SIGNING_REST_BASE_URL);
					mEditOAuthBaseURL = (EditText) findViewById(R.id.oauth_base_url);
					mEditOAuthBaseURL.setText(mOAuthBaseURL != null ? mOAuthBaseURL : DEFAULT_OAUTH_BASE_URL);
					mEditSigningOAuthBaseURL = (EditText) findViewById(R.id.signing_oauth_base_url);
					mEditSigningOAuthBaseURL.setText(mSigningOAuthBaseURL != null ? mSigningOAuthBaseURL
							: DEFAULT_SIGNING_OAUTH_BASE_URL);
				} else if (inflated_view != null) {
					final boolean is_visible = inflated_view.getVisibility() == View.VISIBLE;
					final int compound_res = is_visible ? R.drawable.expander_close_holo
							: R.drawable.expander_open_holo;
					mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(compound_res, 0, 0, 0);
					inflated_view.setVisibility(is_visible ? View.GONE : View.VISIBLE);
				}
				break;
			}
		}
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		saveEditedText();
		outState.putString(Accounts.REST_BASE_URL, mRestBaseURL);
		outState.putString(Accounts.SIGNING_REST_BASE_URL, mSigningRESTBaseURL);
		outState.putString(Accounts.OAUTH_BASE_URL, mOAuthBaseURL);
		outState.putString(Accounts.SIGNING_OAUTH_BASE_URL, mSigningOAuthBaseURL);
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
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
		mSigningRESTBaseURL = bundle.getString(Accounts.SIGNING_REST_BASE_URL);
		mOAuthBaseURL = bundle.getString(Accounts.OAUTH_BASE_URL);
		mSigningOAuthBaseURL = bundle.getString(Accounts.SIGNING_OAUTH_BASE_URL);

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

	private boolean checkUrlErrors() {
		boolean urlHasErrors = false;
		if (mEditRestBaseURL != null) {
			if (!isValidUrl(mEditRestBaseURL.getText())) {
				mEditRestBaseURL.setError(getString(R.string.wrong_url_format));
				urlHasErrors = true;
			}
		}
		if (mEditSigningRESTBaseURL != null) {
			if (!isValidUrl(mEditSigningRESTBaseURL.getText())) {
				mEditSigningRESTBaseURL.setError(getString(R.string.wrong_url_format));
				urlHasErrors = true;
			}
		}
		if (mEditOAuthBaseURL != null) {
			if (!isValidUrl(mEditOAuthBaseURL.getText())) {
				mEditOAuthBaseURL.setError(getString(R.string.wrong_url_format));
				urlHasErrors = true;
			}
		}
		if (mEditSigningOAuthBaseURL != null) {
			if (!isValidUrl(mEditSigningOAuthBaseURL.getText())) {
				mEditSigningOAuthBaseURL.setError(getString(R.string.wrong_url_format));
				urlHasErrors = true;
			}
		}
		return urlHasErrors;
	}

	private void saveEditedText() {
		if (mEditRestBaseURL != null) {
			mRestBaseURL = ParseUtils.parseString(mEditRestBaseURL.getText());
		}
		if (mEditSigningRESTBaseURL != null) {
			mSigningRESTBaseURL = ParseUtils.parseString(mEditSigningRESTBaseURL.getText());
		}
		if (mEditOAuthBaseURL != null) {
			mOAuthBaseURL = ParseUtils.parseString(mEditOAuthBaseURL.getText());
		}
		if (mEditSigningOAuthBaseURL != null) {
			mSigningOAuthBaseURL = ParseUtils.parseString(mEditSigningOAuthBaseURL.getText());
		}
	}
}
