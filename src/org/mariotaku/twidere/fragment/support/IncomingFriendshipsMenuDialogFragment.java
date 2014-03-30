package org.mariotaku.twidere.fragment.support;

import android.view.Menu;
import android.view.MenuInflater;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.Account;
import org.mariotaku.twidere.model.Account.AccountWithCredentials;
import org.mariotaku.twidere.model.ParcelableUser;

public class IncomingFriendshipsMenuDialogFragment extends UserMenuDialogFragment {

	@Override
	protected void onPrepareItemMenu(final Menu menu, final ParcelableUser user) {
		final AccountWithCredentials account = Account.getAccountWithCredentials(getActivity(), user.account_id);
		if (AccountWithCredentials.isOfficialCredentials(getActivity(), account)) {
			final MenuInflater inflater = new MenuInflater(getActivity());
			inflater.inflate(R.menu.action_incoming_friendship, menu);
		}
	}

}
