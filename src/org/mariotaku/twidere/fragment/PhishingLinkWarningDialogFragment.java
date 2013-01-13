package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;

public class PhishingLinkWarningDialogFragment extends BaseDialogFragment implements OnClickListener {

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				final Bundle args = getArguments();
				if (args == null) return;
				final Uri uri = args.getParcelable(INTENT_KEY_URI);
				if (uri == null) return;
				final Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(uri);
				startActivity(intent);
				break;
			}
		}

	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final LayoutInflater inflater = LayoutInflater.from(getActivity());
		builder.setTitle(android.R.string.dialog_alert_title);
		builder.setView(inflater.inflate(R.layout.phishing_link_warning, null));
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}

}
