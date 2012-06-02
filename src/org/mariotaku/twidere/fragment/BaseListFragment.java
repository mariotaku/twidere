package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.BaseActivity;

import android.support.v4.app.ListFragment;

public class BaseListFragment extends ListFragment implements Constants {
	public BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}
}