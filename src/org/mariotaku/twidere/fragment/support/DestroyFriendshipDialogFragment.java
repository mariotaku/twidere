package org.mariotaku.twidere.fragment.support;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.util.AsyncTwitterWrapper;
import org.mariotaku.twidere.util.Utils;

public class DestroyFriendshipDialogFragment extends BaseSupportDialogFragment implements
		DialogInterface.OnClickListener {

	public static final String FRAGMENT_TAG = "destroy_friendship";

	@Override
	public void onClick(final DialogInterface dialog, final int which) {
		switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				final ParcelableUser user = getUser();
				final AsyncTwitterWrapper twitter = getTwitterWrapper();
				if (user == null || twitter == null) return;
				twitter.destroyFriendshipAsync(user.account_id, user.id);
				break;
			default:
				break;
		}
	}

	@Override
	public Dialog onCreateDialog(final Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final ParcelableUser user = getUser();
		if (user != null) {
			final String display_name = Utils.getDisplayName(getActivity(), user.id, user.name, user.screen_name);
			builder.setTitle(getString(R.string.unfollow_user, display_name));
			builder.setMessage(getString(R.string.unfollow_user_confirm_message, display_name));
		}
		builder.setPositiveButton(android.R.string.ok, this);
		builder.setNegativeButton(android.R.string.cancel, null);
		return builder.create();
	}

	private ParcelableUser getUser() {
		final Bundle args = getArguments();
		if (!args.containsKey(EXTRA_USER)) return null;
		return args.getParcelable(EXTRA_USER);
	}

	public static DestroyFriendshipDialogFragment show(final FragmentManager fm, final ParcelableUser user) {
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_USER, user);
		final DestroyFriendshipDialogFragment f = new DestroyFriendshipDialogFragment();
		f.setArguments(args);
		f.show(fm, FRAGMENT_TAG);
		return f;
	}
}
