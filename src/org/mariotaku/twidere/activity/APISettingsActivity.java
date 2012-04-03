package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.Constants;
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

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class APISettingsActivity extends SherlockFragmentActivity implements
		Constants, OnCheckedChangeListener, OnClickListener {

	private EditText mEditRestAPIBase, mEditSearchAPIBase;
	private RadioGroup mEditAuthType;
	private RadioButton mButtonOAuth, mButtonxAuth, mButtonBasic;
	private Button mSaveButton;
	private String mRestAPIBase, mSearchAPIBase;
	private int mAuthType;

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
			case R.id.oauth:
				mAuthType = Accounts.AUTH_TYPE_OAUTH;
				break;
			case R.id.xauth:
				mAuthType = Accounts.AUTH_TYPE_XAUTH;
				break;
			case R.id.basic:
				mAuthType = Accounts.AUTH_TYPE_BASIC;
				break;
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.save:
				saveEditedText();
				Bundle bundle = new Bundle();
				bundle.putString(Accounts.REST_API_BASE, mRestAPIBase);
				bundle.putString(Accounts.SEARCH_API_BASE, mSearchAPIBase);
				bundle.putInt(Accounts.AUTH_TYPE, mAuthType);
				setResult(RESULT_OK, new Intent().putExtras(bundle));
				finish();
				break;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		configureActivity();
		Bundle bundle = savedInstanceState == null ? getIntent().getExtras() : savedInstanceState;
		if (bundle == null) {
			bundle = new Bundle();
		}
		mRestAPIBase = bundle.getString(Accounts.REST_API_BASE);
		mSearchAPIBase = bundle.getString(Accounts.SEARCH_API_BASE);
		mAuthType = bundle.getInt(Accounts.AUTH_TYPE);
		mEditRestAPIBase
				.setText(mRestAPIBase != null ? mRestAPIBase : DEFAULT_REST_API_BASE);
		mEditSearchAPIBase.setText(mSearchAPIBase != null ? mSearchAPIBase
				: DEFAULT_SEARCH_API_BASE);
		mButtonOAuth.setChecked(mAuthType == Accounts.AUTH_TYPE_OAUTH);
		mButtonxAuth.setChecked(mAuthType == Accounts.AUTH_TYPE_XAUTH);
		mButtonBasic.setChecked(mAuthType == Accounts.AUTH_TYPE_BASIC);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		saveEditedText();
		outState.putString(Accounts.REST_API_BASE, mRestAPIBase);
		outState.putString(Accounts.SEARCH_API_BASE, mSearchAPIBase);
		outState.putInt(Accounts.AUTH_TYPE, mAuthType);
		super.onSaveInstanceState(outState);
	}

	private void configureActivity() {
		setContentView(R.layout.api_settings);
		mEditRestAPIBase = (EditText) findViewById(R.id.rest_api_base);
		mEditSearchAPIBase = (EditText) findViewById(R.id.search_api_base);
		mEditAuthType = (RadioGroup) findViewById(R.id.auth_type);
		mButtonOAuth = (RadioButton) findViewById(R.id.oauth);
		mButtonxAuth = (RadioButton) findViewById(R.id.xauth);
		mButtonBasic = (RadioButton) findViewById(R.id.basic);
		mSaveButton = (Button) findViewById(R.id.save);
		mEditAuthType.setOnCheckedChangeListener(this);
		mSaveButton.setOnClickListener(this);
	}

	private void saveEditedText() {
		Editable ed = mEditRestAPIBase.getText();
		if (ed != null) {
			mRestAPIBase = ed.toString();
		}
		ed = mEditSearchAPIBase.getText();
		if (ed != null) {
			mSearchAPIBase = ed.toString();
		}
	}
}
