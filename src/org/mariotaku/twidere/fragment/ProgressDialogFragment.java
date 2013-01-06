package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

public class ProgressDialogFragment extends BaseDialogFragment {

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final ProgressDialog dialog = new ProgressDialog(getActivity());
		dialog.setMessage(getString(R.string.please_wait));
		return dialog;
	}

}