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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AccountsSpinnerAdapter;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.BaseSupportDialogFragment;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.CustomTabConfiguration;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.ImageLoaderWrapper;
import org.mariotaku.twidere.util.ParseUtils;

public class EditCustomTabActivity extends BaseSupportDialogActivity implements OnClickListener {

	private AccountsSpinnerAdapter mAccountsAdapter;
	private ImageLoaderWrapper mImageLoader;

	private View mAccountContainer, mSecondaryFieldContainer;
	private Spinner mAccountSpinner;
	private EditText mEditTabName;
	private TextView mSecondaryFieldLabel;

	private CustomTabConfiguration mTabConfiguration;
	private Object mSecondaryFieldValue;

	@Override
	public void onClick(final View v) {
		final CustomTabConfiguration conf = mTabConfiguration;
		if (conf == null) return;
		switch (v.getId()) {
			case R.id.secondary_field: {
				switch (conf.getSecondaryFieldType()) {
					case CustomTabConfiguration.FIELD_TYPE_USER: {
						final Intent intent = new Intent(this, UserListSelectorActivity.class);
						intent.setAction(INTENT_ACTION_SELECT_USER);
						intent.putExtra(INTENT_KEY_ACCOUNT_ID, getAccountId());
						startActivityForResult(intent, REQUEST_SELECT_USER);
						break;
					}
					case CustomTabConfiguration.FIELD_TYPE_USER_LIST: {
						final Intent intent = new Intent(this, UserListSelectorActivity.class);
						intent.setAction(INTENT_ACTION_SELECT_USER_LIST);
						intent.putExtra(INTENT_KEY_ACCOUNT_ID, getAccountId());
						startActivityForResult(intent, REQUEST_SELECT_USER_LIST);
						break;
					}
					case CustomTabConfiguration.FIELD_TYPE_TEXT: {
						SecondaryFieldEditTextDialogFragment.show(this, ParseUtils.parseString(mSecondaryFieldValue));
						break;
					}
				}
				break;
			}
		}
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mAccountContainer = findViewById(R.id.account_container);
		mSecondaryFieldContainer = findViewById(R.id.secondary_field_container);
		mEditTabName = (EditText) findViewById(R.id.tab_name);
		mSecondaryFieldLabel = (TextView) findViewById(R.id.secondary_field_label);
		mAccountSpinner = (Spinner) findViewById(R.id.account_spinner);
	}

	public void setSecondaryFieldValue(final Object value) {
		mSecondaryFieldValue = value;
		final TextView text1 = (TextView) mSecondaryFieldContainer.findViewById(android.R.id.text1);
		final TextView text2 = (TextView) mSecondaryFieldContainer.findViewById(android.R.id.text2);
		final ImageView icon = (ImageView) mSecondaryFieldContainer.findViewById(android.R.id.icon);
		if (value instanceof ParcelableUser) {
			final ParcelableUser user = (ParcelableUser) value;
			text1.setText(user.name);
			text2.setText("@" + user.screen_name);
			mImageLoader.displayProfileImage(icon, user.profile_image_url);
		}
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode != RESULT_OK) return;
		switch (requestCode) {
			case REQUEST_SELECT_USER: {
				setSecondaryFieldValue(data.getParcelableExtra(INTENT_KEY_USER));
				break;
			}
			case REQUEST_SELECT_USER_LIST: {
				setSecondaryFieldValue(data.getParcelableExtra(INTENT_KEY_USER_LIST));
				break;
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final Bundle extras = intent.getExtras();
		final String type = extras.getString(INTENT_KEY_TYPE);
		final CustomTabConfiguration conf = mTabConfiguration = CustomTabConfiguration.get(type);
		if (conf == null) {
			finish();
			return;
		}
		final boolean has_secondary_field = conf.getSecondaryFieldType() != CustomTabConfiguration.FIELD_TYPE_NONE;
		setContentView(R.layout.edit_custom_tab);
		mImageLoader = TwidereApplication.getInstance(this).getImageLoaderWrapper();
		mAccountsAdapter = new AccountsSpinnerAdapter(this);
		mAccountsAdapter.addAll(Account.getAccounts(this, false));
		mAccountContainer.setVisibility(conf.isAccountIdRequired() ? View.VISIBLE : View.GONE);
		mSecondaryFieldContainer.setVisibility(has_secondary_field ? View.VISIBLE : View.GONE);
		if (conf.getSecondaryFieldTitle() != 0) {
			mSecondaryFieldLabel.setText(conf.getSecondaryFieldTitle());
		} else {
			switch (conf.getSecondaryFieldType()) {
				case CustomTabConfiguration.FIELD_TYPE_USER: {
					mSecondaryFieldLabel.setText(R.string.user);
					break;
				}
				case CustomTabConfiguration.FIELD_TYPE_USER_LIST: {
					mSecondaryFieldLabel.setText(R.string.user_list);
					break;
				}
				default: {
					mSecondaryFieldLabel.setText(R.string.content);
					break;
				}
			}
		}
		mAccountSpinner.setAdapter(mAccountsAdapter);
		mEditTabName.setText(mTabConfiguration.getDefaultTitle());
	}

	private long getAccountId() {
		return mAccountsAdapter.getItem(mAccountSpinner.getSelectedItemPosition()).account_id;
	}

	static class SecondaryFieldEditTextDialogFragment extends BaseSupportDialogFragment implements
			DialogInterface.OnClickListener {
		private static final String FRAGMENT_TAG_EDIT_SECONDARY_FIELD = "edit_secondary_field";
		private EditText mEditText;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			final FragmentActivity activity = getActivity();
			if (activity instanceof EditCustomTabActivity) {
				((EditCustomTabActivity) activity).setSecondaryFieldValue(ParseUtils.parseString(mEditText.getText()));
			}
		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final Bundle args = getArguments();
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.set_nickname);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, null);
			final FrameLayout view = new FrameLayout(getActivity());
			mEditText = new EditText(getActivity());
			final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			lp.leftMargin = lp.topMargin = lp.bottomMargin = lp.rightMargin = getResources().getDimensionPixelSize(
					R.dimen.default_element_spacing);
			view.addView(mEditText, lp);
			builder.setView(view);
			mEditText.setText(args.getString(INTENT_KEY_TEXT));
			return builder.create();
		}

		public static SecondaryFieldEditTextDialogFragment show(final FragmentActivity activity, final String text) {
			final SecondaryFieldEditTextDialogFragment f = new SecondaryFieldEditTextDialogFragment();
			final Bundle args = new Bundle();
			args.putString(INTENT_KEY_TEXT, text);
			f.setArguments(args);
			f.show(activity.getSupportFragmentManager(), FRAGMENT_TAG_EDIT_SECONDARY_FIELD);
			return f;
		}
	}

}
