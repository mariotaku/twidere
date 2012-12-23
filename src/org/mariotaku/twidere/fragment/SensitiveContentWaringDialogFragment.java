package org.mariotaku.twidere.fragment;

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

public class SensitiveContentWaringDialogFragment extends BaseDialogFragment implements OnClickListener {

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				final Context context = getActivity();
				final Bundle args = getArguments();
				if (args == null) return;
				final Uri uri = args.getParcelable(INTENT_KEY_URI);
				if (uri == null) return;
				final Intent intent = new Intent(INTENT_ACTION_VIEW_IMAGE);
				intent.setDataAndType(uri, "image/*");
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD_MR1) {
					intent.setClass(context, ImageViewerGLActivity.class);
				} else {
					intent.setClass(context, ImageViewerActivity.class);
				}
				startActivity(intent);
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
