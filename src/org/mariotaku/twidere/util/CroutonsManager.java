package org.mariotaku.twidere.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.mariotaku.twidere.activity.HomeActivity;

import android.app.Activity;
import android.content.Context;

public final class CroutonsManager {

	private final Set<Activity> mMessageCallbacks = Collections.synchronizedSet(new HashSet<Activity>());
	private final Context mContext;
	
	public CroutonsManager(final Context context) {
		mContext = context;
	}
	
	public boolean addMessageCallback(final Activity activity) {
		if (activity == null) return false;
		return mMessageCallbacks.add(activity);
	}

	public boolean removeMessageCallback(final Activity activity) {
		if (activity == null) return false;
		// If we only have one activity, don't remove it unless it force removes itself.
		if (mMessageCallbacks.size() == 1) return false;
		return mMessageCallbacks.remove(activity);
	}

	public boolean removeMessageCallbackForce(final Activity activity) {
		if (activity == null) return false;
		return mMessageCallbacks.remove(activity);
	}
	
	public void showErrorMessage(final String message, final boolean long_message) {
		final HomeActivity home = getVisibleHomeActivity();
		if (home != null) {
			Utils.showErrorMessage(home, message, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showErrorMessage(activity, message, long_message);
		}
	}

	public void showErrorMessage(final int action_res, final Exception e, final boolean long_message) {
		final String message = mContext.getString(action_res);
		final HomeActivity home = getVisibleHomeActivity();
		if (home != null) {
			Utils.showErrorMessage(home, message, e, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showErrorMessage(activity, message, e, long_message);
		}
	}

	public void showInfoMessage(final int message_res, final boolean long_message) {
		final HomeActivity home = getVisibleHomeActivity();
		if (home != null) {
			Utils.showInfoMessage(home, message_res, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showInfoMessage(activity, message_res, long_message);
		}
	}

	public void showOkMessage(final int message_res, final boolean long_message) {
		final HomeActivity home = getVisibleHomeActivity();
		if (home != null) {
			Utils.showOkMessage(home, message_res, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showOkMessage(activity, message_res, long_message);
		}
	}

	public void showWarnMessage(final int message_res, final boolean long_message) {
		final HomeActivity home = getVisibleHomeActivity();
		if (home != null) {
			Utils.showWarnMessage(home, message_res, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showWarnMessage(activity, message_res, long_message);
		}
	}

	private boolean hasHomeActivityInMessageCallbacks() {
		for (final Activity activity : mMessageCallbacks) {
			if (activity instanceof HomeActivity) return true;
		}
		return false;
	}
	
	private HomeActivity getVisibleHomeActivity() {
		for (final Activity activity : mMessageCallbacks) {
			if (activity instanceof HomeActivity) {
				final HomeActivity home = (HomeActivity) activity;
				if (home.isVisible()) return home;
			}
		}
		return null;
	}

}
