/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.preference;

import static android.text.TextUtils.isEmpty;
import static org.mariotaku.twidere.util.ParseUtils.parseString;
import static org.mariotaku.twidere.util.Utils.getNonEmptyString;
import static org.mariotaku.twidere.util.Utils.trim;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import twitter4j.TwitterConstants;

public class DefaultAPIPreference extends DialogPreference implements Constants, TwitterConstants,
		OnCheckedChangeListener, OnClickListener {

	private EditText mEditRestBaseURL, mEditSigningRESTBaseURL, mEditOAuthBaseURL, mEditSigningOAuthBaseURL;
	private EditText mEditConsumerKey, mEditConsumerSecret;
	private RadioGroup mEditAuthType;
	private RadioButton mButtonOAuth, mButtonxAuth, mButtonBasic, mButtonTwipOMode;
	private TextView mAdvancedAPIConfigLabel;
	private View mAdvancedAPIConfigContainer;

	private String mRestBaseURL, mSigningRestBaseURL, mOAuthBaseURL, mSigningOAuthBaseURL;
	private String mConsumerKey, mConsumerSecret;
	private int mAuthType;

	public DefaultAPIPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public DefaultAPIPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setDialogLayoutResource(R.layout.api_editor_content);
		setPositiveButtonText(android.R.string.ok);
	}

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
		final boolean is_oauth = mAuthType == Accounts.AUTH_TYPE_OAUTH || mAuthType == Accounts.AUTH_TYPE_XAUTH;
		mAdvancedAPIConfigContainer.setVisibility(is_oauth ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onClick(final View v) {
		final View stubView = mAdvancedAPIConfigContainer.findViewById(R.id.stub_advanced_api_config);
		final View inflatedView = mAdvancedAPIConfigContainer.findViewById(R.id.advanced_api_config);
		if (stubView != null) {
			stubView.setVisibility(View.VISIBLE);
			mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(R.drawable.expander_open_holo, 0, 0, 0);
			mEditSigningRESTBaseURL = (EditText) mAdvancedAPIConfigContainer.findViewById(R.id.signing_rest_base_url);
			mEditOAuthBaseURL = (EditText) mAdvancedAPIConfigContainer.findViewById(R.id.oauth_base_url);
			mEditSigningOAuthBaseURL = (EditText) mAdvancedAPIConfigContainer.findViewById(R.id.signing_oauth_base_url);
			mEditConsumerKey = (EditText) mAdvancedAPIConfigContainer.findViewById(R.id.consumer_key);
			mEditConsumerSecret = (EditText) mAdvancedAPIConfigContainer.findViewById(R.id.consumer_secret);

			mEditSigningRESTBaseURL.setText(isEmpty(mSigningRestBaseURL) ? DEFAULT_SIGNING_REST_BASE_URL
					: mSigningRestBaseURL);
			mEditOAuthBaseURL.setText(isEmpty(mOAuthBaseURL) ? DEFAULT_OAUTH_BASE_URL : mOAuthBaseURL);
			mEditSigningOAuthBaseURL.setText(isEmpty(mSigningOAuthBaseURL) ? DEFAULT_SIGNING_OAUTH_BASE_URL
					: mSigningOAuthBaseURL);
			mEditConsumerKey.setText(isEmpty(mConsumerKey) ? TWITTER_CONSUMER_KEY_2 : mConsumerKey);
			mEditConsumerSecret.setText(isEmpty(mConsumerSecret) ? TWITTER_CONSUMER_SECRET_2 : mConsumerSecret);
		} else if (inflatedView != null) {
			final boolean is_visible = inflatedView.getVisibility() == View.VISIBLE;
			final int compound_res = is_visible ? R.drawable.expander_close_holo : R.drawable.expander_open_holo;
			mAdvancedAPIConfigLabel.setCompoundDrawablesWithIntrinsicBounds(compound_res, 0, 0, 0);
			inflatedView.setVisibility(is_visible ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	protected void onBindDialogView(final View view) {
		final SharedPreferences pref = getSharedPreferences();
		mConsumerKey = getNonEmptyString(pref, KEY_CONSUMER_KEY, TWITTER_CONSUMER_KEY_2);
		mConsumerSecret = getNonEmptyString(pref, KEY_CONSUMER_SECRET, TWITTER_CONSUMER_SECRET_2);
		mRestBaseURL = getNonEmptyString(pref, KEY_REST_BASE_URL, DEFAULT_REST_BASE_URL);
		mOAuthBaseURL = getNonEmptyString(pref, KEY_OAUTH_BASE_URL, DEFAULT_OAUTH_BASE_URL);
		mSigningRestBaseURL = getNonEmptyString(pref, KEY_SIGNING_REST_BASE_URL, DEFAULT_SIGNING_REST_BASE_URL);
		mSigningOAuthBaseURL = getNonEmptyString(pref, KEY_SIGNING_OAUTH_BASE_URL, DEFAULT_SIGNING_OAUTH_BASE_URL);
		mAuthType = pref.getInt(KEY_AUTH_TYPE, Accounts.AUTH_TYPE_OAUTH);

		mEditRestBaseURL.setText(isEmpty(mRestBaseURL) ? DEFAULT_REST_BASE_URL : mRestBaseURL);
		mButtonOAuth.setChecked(mAuthType == Accounts.AUTH_TYPE_OAUTH);
		mButtonxAuth.setChecked(mAuthType == Accounts.AUTH_TYPE_XAUTH);
		mButtonBasic.setChecked(mAuthType == Accounts.AUTH_TYPE_BASIC);
		mButtonTwipOMode.setChecked(mAuthType == Accounts.AUTH_TYPE_TWIP_O_MODE);
		if (mEditAuthType.getCheckedRadioButtonId() == -1) {
			mButtonOAuth.setChecked(true);
		}
	}

	@Override
	protected View onCreateDialogView() {
		final View view = super.onCreateDialogView();
		mEditRestBaseURL = (EditText) view.findViewById(R.id.rest_base_url);
		mEditAuthType = (RadioGroup) view.findViewById(R.id.auth_type);
		mButtonOAuth = (RadioButton) view.findViewById(R.id.oauth);
		mButtonxAuth = (RadioButton) view.findViewById(R.id.xauth);
		mButtonBasic = (RadioButton) view.findViewById(R.id.basic);
		mButtonTwipOMode = (RadioButton) view.findViewById(R.id.twip_o);
		mAdvancedAPIConfigContainer = view.findViewById(R.id.advanced_api_config_container);
		mAdvancedAPIConfigLabel = (TextView) view.findViewById(R.id.advanced_api_config_label);

		mEditAuthType.setOnCheckedChangeListener(this);
		mAdvancedAPIConfigLabel.setOnClickListener(this);

		return view;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		if (!positiveResult) return;
		saveEditedText();
		final SharedPreferences.Editor editor = getSharedPreferences().edit();
		if (!isEmpty(mConsumerKey) && !isEmpty(mConsumerSecret)) {
			editor.putString(KEY_CONSUMER_KEY, mConsumerKey);
			editor.putString(KEY_CONSUMER_SECRET, mConsumerSecret);
		} else {
			editor.remove(KEY_CONSUMER_KEY);
			editor.remove(KEY_CONSUMER_SECRET);
		}
		editor.putString(KEY_REST_BASE_URL, isEmpty(mRestBaseURL) ? null : mRestBaseURL);
		editor.putString(KEY_OAUTH_BASE_URL, isEmpty(mOAuthBaseURL) ? null : mOAuthBaseURL);
		editor.putString(KEY_SIGNING_REST_BASE_URL, isEmpty(mSigningRestBaseURL) ? null : mSigningRestBaseURL);
		editor.putString(KEY_SIGNING_OAUTH_BASE_URL, isEmpty(mSigningOAuthBaseURL) ? null : mSigningOAuthBaseURL);
		editor.putInt(KEY_AUTH_TYPE, mAuthType);
		editor.apply();
	}

	@Override
	protected void onRestoreInstanceState(final Parcelable state) {
		final Bundle savedInstanceState = (Bundle) state;
		super.onRestoreInstanceState(savedInstanceState.getParcelable(EXTRA_DATA));
		mRestBaseURL = savedInstanceState.getString(Accounts.REST_BASE_URL);
		mSigningRestBaseURL = savedInstanceState.getString(Accounts.SIGNING_REST_BASE_URL);
		mOAuthBaseURL = savedInstanceState.getString(Accounts.OAUTH_BASE_URL);
		mSigningOAuthBaseURL = savedInstanceState.getString(Accounts.SIGNING_OAUTH_BASE_URL);
		mConsumerKey = trim(savedInstanceState.getString(Accounts.CONSUMER_KEY));
		mConsumerSecret = trim(savedInstanceState.getString(Accounts.CONSUMER_SECRET));
		mAuthType = savedInstanceState.getInt(Accounts.AUTH_TYPE);
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		saveEditedText();
		final Bundle outState = new Bundle();
		outState.putParcelable(EXTRA_DATA, super.onSaveInstanceState());
		outState.putString(Accounts.REST_BASE_URL, mRestBaseURL);
		outState.putString(Accounts.SIGNING_REST_BASE_URL, mSigningRestBaseURL);
		outState.putString(Accounts.OAUTH_BASE_URL, mOAuthBaseURL);
		outState.putString(Accounts.SIGNING_OAUTH_BASE_URL, mSigningOAuthBaseURL);
		outState.putString(Accounts.CONSUMER_KEY, mConsumerKey);
		outState.putString(Accounts.CONSUMER_SECRET, mConsumerSecret);
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		return outState;
	}

	private void saveEditedText() {
		if (mEditRestBaseURL != null) {
			mRestBaseURL = parseString(mEditRestBaseURL.getText());
		}
		if (mEditSigningRESTBaseURL != null) {
			mSigningRestBaseURL = parseString(mEditSigningRESTBaseURL.getText());
		}
		if (mEditOAuthBaseURL != null) {
			mOAuthBaseURL = parseString(mEditOAuthBaseURL.getText());
		}
		if (mEditSigningOAuthBaseURL != null) {
			mSigningOAuthBaseURL = parseString(mEditSigningOAuthBaseURL.getText());
		}
		if (mEditConsumerKey != null) {
			mConsumerKey = parseString(mEditConsumerKey.getText());
		}
		if (mEditConsumerSecret != null) {
			mConsumerSecret = parseString(mEditConsumerSecret.getText());
		}
	}
}
