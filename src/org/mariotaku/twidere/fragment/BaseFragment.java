package org.mariotaku.twidere.fragment;

import org.mariotaku.actionbarcompat.app.ActionBarFragmentActivity;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.BaseActivity;

import android.app.Activity;
import android.content.ContentResolver;
import android.support.v4.app.Fragment;

public class BaseFragment extends Fragment implements Constants {

	public ActionBarFragmentActivity getActionBarActivity() {
		Activity activity = getActivity();
		if (activity instanceof ActionBarFragmentActivity) return (ActionBarFragmentActivity) activity;
		return null;
	}

	public ContentResolver getContentResolver() {
		Activity activity = getActivity();
		if (activity != null) return activity.getContentResolver();
		return null;
	}

	public void setProgressBarIndeterminateVisibility(boolean visible) {
		Activity activity = getActivity();
		if (activity instanceof BaseActivity) {
			((BaseActivity) activity).setSupportProgressBarIndeterminateVisibility(visible);
		}
	}
}