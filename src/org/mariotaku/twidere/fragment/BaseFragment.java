package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.Constants;

import roboguice.RoboGuice;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;

public class BaseFragment extends SherlockFragment implements Constants {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RoboGuice.getInjector(getSherlockActivity()).injectMembersWithoutViews(this);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RoboGuice.getInjector(getSherlockActivity()).injectViewMembers(this);
	}
}