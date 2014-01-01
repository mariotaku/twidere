/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.SparseBooleanArray;
import android.widget.Button;
import android.widget.ListView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.iface.IDialogFragmentCallback;

public final class DataExportImportTypeSelectorDialogFragment extends BaseSupportDialogFragment implements
		OnMultiChoiceClickListener, OnClickListener, OnShowListener {

	private static final int[] ALL_FLAGS = { FLAG_PREFERENCES, FLAG_NICKNAMES, FLAG_USER_COLORS };

	private static final int[] ENTRIES_RES = { R.string.settings, R.string.nicknames, R.string.user_colors };

	@Override
	public void onCancel(final DialogInterface dialog) {
		super.onCancel(dialog);
		final FragmentActivity a = getActivity();
		if (a instanceof Callback) {
			((Callback) a).onCancelled();
		}
	}

	@Override
	public final void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE: {
				final int flags = getCheckedFlags(dialog);
				onPositiveButtonClicked(flags);
				break;
			}
		}
	}

	@Override
	public final void onClick(final DialogInterface dialog, final int which, final boolean isChecked) {
		updatePositiveButton(dialog);
	}

	@Override
	public final Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(getTitle());
		builder.setMultiChoiceItems(getEntries(), null, this);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		final AlertDialog dialog = builder.create();
		dialog.setOnShowListener(this);
		return dialog;
	}

	@Override
	public void onDismiss(final DialogInterface dialog) {
		super.onDismiss(dialog);
		final FragmentActivity a = getActivity();
		if (a instanceof Callback) {
			((Callback) a).onDismissed();
		}
	}

	@Override
	public final void onShow(final DialogInterface dialog) {
		updatePositiveButton(dialog);
	}

	private int getCheckedFlags(final DialogInterface dialog) {
		if (!(dialog instanceof AlertDialog)) return 0;
		final AlertDialog alertDialog = (AlertDialog) dialog;
		final ListView listView = alertDialog.getListView();
		final SparseBooleanArray checked = listView.getCheckedItemPositions();
		int flags = 0;
		for (int i = 0, j = checked.size(); i < j; i++) {
			if (checked.valueAt(i)) {
				flags |= ALL_FLAGS[i];
			}
		}
		return flags;
	}

	private String[] getEntries() {
		final String[] entries = new String[ENTRIES_RES.length];
		for (int i = 0, j = ENTRIES_RES.length; i < j; i++) {
			entries[i] = getString(ENTRIES_RES[i]);
		}
		return entries;
	}

	private CharSequence getTitle() {
		final Bundle args = getArguments();
		if (args == null) return null;
		return args.getCharSequence(EXTRA_TITLE);
	}

	private void onPositiveButtonClicked(final int flags) {
		final FragmentActivity a = getActivity();
		final Bundle args = getArguments();
		if (args == null) return;
		final String path = args.getString(EXTRA_PATH);
		if (a instanceof Callback) {
			((Callback) a).onPositiveButtonClicked(path, flags);
		}
	}

	private void updatePositiveButton(final DialogInterface dialog) {
		if (!(dialog instanceof AlertDialog)) return;
		final AlertDialog alertDialog = (AlertDialog) dialog;
		final Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
		positiveButton.setEnabled(getCheckedFlags(dialog) != 0);
	}

	public static interface Callback extends IDialogFragmentCallback {
		void onPositiveButtonClicked(String path, int flags);
	}

}
