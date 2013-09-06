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

package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

public class APIUpgradeConfirmDialog extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		final SharedPreferences prefs = getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		if (prefs == null) return;
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
