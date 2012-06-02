package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.BaseActivity;

import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment implements Constants {

	public BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}
}