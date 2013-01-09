package org.mariotaku.twidere.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.DialogFragment;
import org.mariotaku.twidere.fragment.PhishingLinkWarningDialogFragment;
import android.os.Bundle;
import android.net.Uri;

public class OnDirectMessageLinkClickHandler extends OnLinkClickHandler {

	private static final String[] SHORT_LINK_SERVICES = new String[] {"bit.ly", "ow.ly", "tinyurl.com", "goo.gl"};

	public OnDirectMessageLinkClickHandler(final Context context, final long account_id) {
		super(context, account_id, false);
	}

	protected void openLink(final String link) {
		if (link == null) return;
		if (hasShortenedLinks(link)) {
			final SharedPreferences prefs = activity.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
			if (activity instanceof FragmentActivity && prefs.getBoolean(PREFERENCE_KEY_PHISHING_LINK_WARNING, true)) {
				final FragmentActivity a = (FragmentActivity) activity;
				final FragmentManager fm = a.getSupportFragmentManager();
				final DialogFragment fragment = new PhishingLinkWarningDialogFragment();
				final Bundle args = new Bundle();
				args.putParcelable(INTENT_KEY_URI, Uri.parse(link));
				fragment.setArguments(args);
				fragment.show(fm, "phishing_link_warning");
			} else {
				super.openLink(link);
			}
		} else {
			super.openLink(link);
		}
	}
	
	private boolean hasShortenedLinks(final String link) {
		for (final String short_link_service : SHORT_LINK_SERVICES) {
			if (link.contains(short_link_service)) return true;
		}
		return false;
	}
}
