package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

public class APIUpgradeConfirmDialog extends BaseDialogFragment implements DialogInterface.OnClickListener {

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(PREFERENCE_KEY_API_UPGRADE_CONFIRMED, true).commit();
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		setCancelable(false);
		builder.setCancelable(false);
		builder.setTitle(R.string.api_upgrade_notice_title);
		builder.setMessage(R.string.api_upgrade_notice_message);
		builder.setPositiveButton(android.R.string.ok, this);
		return builder.create();
	}

}