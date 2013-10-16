package org.mariotaku.twidere.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.UserHashtagAutoCompleteAdapter;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.ParseUtils;

public class AddUserListMemberDialogFragment extends BaseSupportDialogFragment implements
		DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "add_user_list_member";
	private AutoCompleteTextView mEditText;
	private UserHashtagAutoCompleteAdapter mUserAutoCompleteAdapter;

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final Bundle args = getArguments();
		if (args == null || !args.containsKey(EXTRA_ACCOUNT_ID) || !args.containsKey(EXTRA_LIST_ID)) return;
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				final String mText = ParseUtils.parseString(mEditText.getText());
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (mText == null || mText.length() <= 0 || twitter == null) return;
//				twitter.addUserListMembersAsync(args.getLong(EXTRA_ACCOUNT_ID), args.getInt(EXTRA_LIST_ID), mText);
				break;
			}
		}
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final View view = LayoutInflater.from(getActivity()).inflate(R.layout.auto_complete_textview, null);
		builder.setView(view);
		mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
		if (savedInstanceState != null) {
			mEditText.setText(savedInstanceState.getCharSequence(EXTRA_TEXT));
		}
		mUserAutoCompleteAdapter = new UserHashtagAutoCompleteAdapter(getActivity());
		mEditText.setAdapter(mUserAutoCompleteAdapter);
		mEditText.setThreshold(1);
		mEditText.setFilters(new InputFilter[] { new InputFilter.LengthFilter(20) });
		builder.setTitle(R.string.screen_name);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, this);
		return builder.create();
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		outState.putCharSequence(EXTRA_TEXT, mEditText.getText());
		super.onSaveInstanceState(outState);
	}

	public static AddUserListMemberDialogFragment show(final FragmentManager fm, final long account_id,
			final int list_id) {
		final Bundle args = new Bundle();
		args.putLong(EXTRA_ACCOUNT_ID, account_id);
		args.putInt(EXTRA_LIST_ID, list_id);
		final AddUserListMemberDialogFragment f = new AddUserListMemberDialogFragment();
		f.setArguments(args);
		f.show(fm, FRAGMENT_TAG);
		return f;
	}

}