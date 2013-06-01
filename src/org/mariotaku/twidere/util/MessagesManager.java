package org.mariotaku.twidere.util;

import android.app.Activity;
import android.content.Context;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.BaseActivity;
import org.mariotaku.twidere.activity.HomeActivity;
import android.content.SharedPreferences;

public final class MessagesManager implements Constants {

	private final Set<Activity> mMessageCallbacks = Collections.synchronizedSet(new HashSet<Activity>());
	private final Context mContext;
	private final SharedPreferences mPreferences;
	
	public MessagesManager(final Context context) {
		mContext = context;
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
	}
	
	public boolean addMessageCallback(final Activity activity) {
		if (activity == null) return false;
		return mMessageCallbacks.add(activity);
	}

	public boolean removeMessageCallback(final Activity activity) {
		if (activity == null) return false;
		return mMessageCallbacks.remove(activity);
	}
	
	public void showErrorMessage(final CharSequence message, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showErrorMessage(best, message, long_message);
			return;
		}
		if (showToast()) {
			Utils.showErrorMessage(mContext, message, long_message);
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
		if (showToast()) {
			Utils.showErrorMessage(mContext, message, e, long_message);
			return;
		}
	}

	public void showInfoMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showInfoMessage(best, message_res, long_message);
			return;
		}
		if (showToast()) {
			Utils.showInfoMessage(mContext, message_res, long_message);
			return;
		}
	}

	public void showInfoMessage(final CharSequence message, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showInfoMessage(best, message, long_message);
			return;
		}
		if (showToast()) {
			Utils.showInfoMessage(mContext, message, long_message);
			return;
		}
	}

	public void showOkMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showOkMessage(best, message_res, long_message);
			return;
		}
		if (showToast()) {
			Utils.showOkMessage(mContext, message_res, long_message);
			return;
		}
	}

	public void showOkMessage(final  CharSequence message, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showOkMessage(best, message, long_message);
			return;
		}
		if (showToast()) {
			Utils.showOkMessage(mContext, message, long_message);
		}
	}
	
	public void showWarnMessage(final int message_res, final boolean long_message) {
		final Activity best = getBestActivity();
		if (best != null) {
			Utils.showWarnMessage(best, message_res, long_message);
			return;
		}
		if (showToast()) {
			Utils.showWarnMessage(mContext, message_res, long_message);
		}
	}
	
	private Activity getBestActivity() {
		for (final Activity activity : mMessageCallbacks) {
			if (activity instanceof HomeActivity) {
				final HomeActivity home = (HomeActivity) activity;
				if (home.isVisible()) return home;
			}
		}
		for (final Activity activity : mMessageCallbacks) {
			if (activity instanceof BaseActivity) {
				final BaseActivity base = (BaseActivity) activity;
				if (base.isOnTop()) return base;
			}
		}
		for (final Activity activity : mMessageCallbacks) {
			return activity;
		}
		return null;
	}
	
	private boolean showToast() {
		return mPreferences.getBoolean(PREFERENCE_KEY_BACKGROUND_TOAST_NOTIFICATION, false);
	}

}
