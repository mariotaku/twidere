package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class DashboardFragment extends BaseFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment fragment = MULTIPLE_ACCOUNTS_ENABLED ? new AccountsFragment() : new MeFragment();
		ft.replace(R.id.dashboard, fragment);
		ft.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.dashboard, null, false);
	}
}
