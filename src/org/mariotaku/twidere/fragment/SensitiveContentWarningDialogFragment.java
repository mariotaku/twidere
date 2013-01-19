package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.openImageDirectly;
import static org.mariotaku.twidere.util.Utils.parseString;

import org.mariotaku.gallery3d.app.ImageViewerGLActivity;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.ImageViewerActivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

public class SensitiveContentWarningDialogFragment extends BaseDialogFragment implements OnClickListener {

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				final Context context = getActivity();
				final Bundle args = getArguments();
				if (args == null || context == null) return;
				final Uri uri = args.getParcelable(INTENT_KEY_URI);
				final Uri orig = args.getParcelable(INTENT_KEY_URI_ORIG);
				openImageDirectly(context, parseString(uri), parseString(orig));
				break;
			}
		}

	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(android.R.string.dialog_alert_title);
		builder.setMessage(R.string.sensitive_content_warning);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}

}
