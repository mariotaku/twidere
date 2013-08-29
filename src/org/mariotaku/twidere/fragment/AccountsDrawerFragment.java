package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AccountsDrawerFragment extends BaseFragment {

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.accounts_drawer, container, false);
		return view;
	}

}
