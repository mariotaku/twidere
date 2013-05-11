package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.Context;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.mariotaku.twidere.activity.BaseActivity;
import org.mariotaku.twidere.activity.HomeActivity;

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
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showErrorMessage(best, message, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showErrorMessage(activity, message, long_message);
			return;
		}
	}

	public void showErrorMessage(final int action_res, final Exception e, final boolean long_message) {
		final String message = mContext.getString(action_res);
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showErrorMessage(best, message, e, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showErrorMessage(activity, message, e, long_message);
			return;
		}
	}

	public void showInfoMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showInfoMessage(best, message_res, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showInfoMessage(activity, message_res, long_message);
			return;
		}
	}

	public void showOkMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showOkMessage(best, message_res, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showOkMessage(activity, message_res, long_message);
			return;
		}
	}

	public void showWarnMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showWarnMessage(best, message_res, long_message);
			return;
		}
		for (final Activity activity : mMessageCallbacks) {
			Utils.showWarnMessage(activity, message_res, long_message);
			return;
		}
	}

	private boolean hasHomeActivityInMessageCallbacks() {
		for (final Activity activity : mMessageCallbacks) {
			if (activity instanceof HomeActivity) return true;
		}
		return false;
	}
	
	private Activity getBestActivity() {
		for (final Activity activity : mMessageCallbacks) {
			if (activity instanceof HomeActivity) {
				final HomeActivity home = (HomeActivity) activity;
				if (home.isVisible()) return home;
			} else if (activity instanceof BaseActivity) {
				final BaseActivity base = (BaseActivity) activity;
				if (base.isOnTop()) return base;
			}
		}
		return null;
	}

}
