package org.mariotaku.twidere.fragment;

import org.mariotaku.actionbarcompat.app.ActionBarFragmentActivity;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.BaseActivity;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment implements Constants {

	public void setProgressBarIndeterminateVisibility(boolean visible) {
		Activity activity = getActivity();
		if (activity instanceof BaseActivity) {
			((BaseActivity) activity).setSupportProgressBarIndeterminateVisibility(visible);
		}
	}
	
	public ActionBarFragmentActivity getActionBarActivity() {
		Activity activity = getActivity();
		if (activity instanceof ActionBarFragmentActivity) {
			return (ActionBarFragmentActivity) activity;
		}
		return null;
	}
}
