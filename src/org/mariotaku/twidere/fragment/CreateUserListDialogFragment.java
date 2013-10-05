package org.mariotaku.twidere.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ParseUtils;

public class CreateUserListDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {

	private EditText mEditName, mEditDescription;
	private CheckBox mPublicCheckBox;
	private String mName, mDescription;
	private long mAccountId;
	private int mListId;
	private boolean mIsPublic = true;
	private AsyncTwitterWrapper mTwitterWrapper;

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		if (mAccountId <= 0) return;
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				mName = ParseUtils.parseString(mEditName.getText());
				mDescription = ParseUtils.parseString(mEditDescription.getText());
				mIsPublic = mPublicCheckBox.isChecked();
				if (mName == null || mName.length() <= 0) return;
				mTwitterWrapper.createUserListAsync(mAccountId, mName, mIsPublic, mDescription);
				break;
			}
		}

	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		mTwitterWrapper = getApplication().getTwitterWrapper();
		final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
		mAccountId = bundle != null ? bundle.getLong(EXTRA_ACCOUNT_ID, -1) : -1;
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final View view = LayoutInflater.from(getActivity()).inflate(R.layout.edit_user_list_detail, null);
		builder.setView(view);
		mEditName = (EditText) view.findViewById(R.id.name);
		mEditDescription = (EditText) view.findViewById(R.id.description);
		mPublicCheckBox = (CheckBox) view.findViewById(R.id.is_public);
		if (mName != null) {
			mEditName.setText(mName);
		}
		if (mDescription != null) {
			mEditDescription.setText(mDescription);
		}
		mPublicCheckBox.setChecked(mIsPublic);
		builder.setTitle(R.string.new_user_list);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);
		return builder.create();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putLong(EXTRA_ACCOUNT_ID, mAccountId);
		outState.putInt(EXTRA_LIST_ID, mListId);
		outState.putString(EXTRA_LIST_NAME, mName);
		outState.putString(EXTRA_DESCRIPTION, mDescription);
		outState.putBoolean(EXTRA_IS_PUBLIC, mIsPublic);
		super.onSaveInstanceState(outState);
	}

}
