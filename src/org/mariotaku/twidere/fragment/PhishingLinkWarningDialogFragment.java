package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.AssetsImageGetter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;

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
		builder.setTitle(android.R.string.dialog_alert_title);
		final String html = getString(R.string.phishing_link_warning_message);
		final ImageGetter getter = new AssetsImageGetter(getActivity());
		final Spanned text = Html.fromHtml(html, getter, null);
		builder.setMessage(text);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}

}
