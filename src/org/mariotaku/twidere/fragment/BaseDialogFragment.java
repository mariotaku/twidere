package org.mariotaku.twidere.fragment;

import org.mariotaku.actionbarcompat.ActionBarFragmentActivity;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.BaseActivity;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;

public class BaseDialogFragment extends DialogFragment implements Constants {

	public ActionBarFragmentActivity getActionBarActivity() {
		final Activity activity = getActivity();
		if (activity instanceof ActionBarFragmentActivity) return (ActionBarFragmentActivity) activity;
		return null;
	}

	public Application getApplication() {
		final Activity activity = getActivity();
		if (activity != null) return activity.getApplication();
		return null;
	}

	public ContentResolver getContentResolver() {
		final Activity activity = getActivity();
		if (activity != null) return activity.getContentResolver();
		return null;
	}

	public SharedPreferences getSharedPreferences(String name, int mode) {
		final Activity activity = getActivity();
		if (activity != null) return activity.getSharedPreferences(name, mode);
		return null;
	}

	public Object getSystemService(String name) {
		final Activity activity = getActivity();
		if (activity != null) return activity.getSystemService(name);
		return null;
	}

	public void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.registerReceiver(receiver, filter);
	}

	public void setProgressBarIndeterminateVisibility(boolean visible) {
		final Activity activity = getActivity();
		if (activity instanceof BaseActivity) {
			((BaseActivity) activity).setSupportProgressBarIndeterminateVisibility(visible);
		}
	}

	public void unregisterReceiver(BroadcastReceiver receiver) {
		final Activity activity = getActivity();
		if (activity == null) return;
		activity.unregisterReceiver(receiver);
	}
}
