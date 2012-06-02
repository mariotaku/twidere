package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.BaseActivity;

import android.support.v4.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment implements Constants {
	public BaseActivity getBaseActivity() {
		return (BaseActivity) getActivity();
	}
}
