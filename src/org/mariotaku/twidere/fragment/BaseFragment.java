package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.Constants;

import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;
import roboguice.RoboGuice;

public class BaseFragment extends SherlockFragment implements Constants {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		RoboGuice.getInjector(getSherlockActivity()).injectMembersWithoutViews(this);
	}

	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		RoboGuice.getInjector(getSherlockActivity()).injectViewMembers(this);
	}
}