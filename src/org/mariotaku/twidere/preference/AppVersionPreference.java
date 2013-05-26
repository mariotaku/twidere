package org.mariotaku.twidere.preference;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.Preference;
import android.util.AttributeSet;

public class AppVersionPreference extends Preference {

	public AppVersionPreference(final Context context) {
		this(context, null);
	}

	public AppVersionPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public AppVersionPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		final PackageManager pm = context.getPackageManager();
		try {
			final PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);
			setTitle(info.applicationInfo.loadLabel(pm));
			setSummary(info.versionName);
		} catch (final PackageManager.NameNotFoundException e) {
			
		}
	}
	
}
