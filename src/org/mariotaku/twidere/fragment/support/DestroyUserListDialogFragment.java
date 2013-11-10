package org.mariotaku.twidere.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;

public class DestroyUserListDialogFragment extends BaseSupportDialogFragment implements DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "destroy_user_list";

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final ParcelableUserList user_list = getUserList();
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (user_list == null || twitter == null) return;
				twitter.destroyUserListAsync(user_list.account_id, user_list.id);
				break;
			default:
				break;
		}
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final ParcelableUserList user_list = getUserList();
		if (user_list != null) {
			builder.setTitle(getString(R.string.delete_user_list, user_list.name));
			builder.setMessage(getString(R.string.delete_user_list_confirm_message, user_list.name));
		}
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}

	private ParcelableUserList getUserList() {
		final Bundle args = getArguments();
		if (!args.containsKey(EXTRA_USER_LIST)) return null;
		return args.getParcelable(EXTRA_USER_LIST);
	}

	public static DestroyUserListDialogFragment show(final FragmentManager fm, final ParcelableUserList user_list) {
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_USER_LIST, user_list);
		final DestroyUserListDialogFragment f = new DestroyUserListDialogFragment();
		f.setArguments(args);
		f.show(fm, FRAGMENT_TAG);
		return f;
	}
}
