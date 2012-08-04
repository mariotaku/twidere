package org.mariotaku.twidere.loader;

import java.util.List;

import org.mariotaku.twidere.activity.ExtensionsListActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.AsyncTaskLoader;

public class ExtensionsListLoader extends AsyncTaskLoader<List<ResolveInfo>> {

	private ExtensionsListLoader.PackageIntentReceiver mPackageObserver;
	private ExtensionsListLoader.InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
	private PackageManager mPackageManager;

	public ExtensionsListLoader(Context context, PackageManager pm) {
		super(context);
		mPackageManager = pm;
	}

	@Override
	public List<ResolveInfo> loadInBackground() {
		final Intent intent = new Intent(ExtensionsListActivity.INTENT_ACTION_EXTENSION_SETTINGS);
		return mPackageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();

		// Ensure the loader is stopped
		onStopLoading();

		// Stop monitoring for changes.
		if (mPackageObserver != null) {
			getContext().unregisterReceiver(mPackageObserver);
			mPackageObserver = null;
		}
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {

		// Start watching for changes in the app data.
		if (mPackageObserver == null) {
			mPackageObserver = new PackageIntentReceiver(this);
		}

		// Has something interesting in the configuration changed since we
		// last built the app list?
		final boolean configChange = mLastConfig.applyNewConfig(getContext().getResources());

		if (takeContentChanged() || configChange) {
			// If the data has changed since the last time it was loaded
			// or is not currently available, start a load.
			forceLoad();
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Helper for determining if the configuration has changed in an interesting
	 * way so we need to rebuild the app list.
	 */
	public static class InterestingConfigChanges {

		final Configuration mLastConfiguration = new Configuration();
		int mLastDensity;

		boolean applyNewConfig(Resources res) {
			final int configChanges = mLastConfiguration.updateFrom(res.getConfiguration());
			final boolean densityChanged = mLastDensity != res.getDisplayMetrics().densityDpi;
			if (densityChanged
					|| (configChanges & (ActivityInfo.CONFIG_LOCALE | ActivityInfo.CONFIG_UI_MODE | ActivityInfo.CONFIG_SCREEN_LAYOUT)) != 0) {
				mLastDensity = res.getDisplayMetrics().densityDpi;
				return true;
			}
			return false;
		}
	}

	/**
	 * Helper class to look for interesting changes to the installed apps so
	 * that the loader can be updated.
	 */
	public static class PackageIntentReceiver extends BroadcastReceiver {

		final ExtensionsListLoader mLoader;

		public PackageIntentReceiver(ExtensionsListLoader loader) {
			mLoader = loader;
			final IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
			filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
			filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
			filter.addDataScheme("package");
			mLoader.getContext().registerReceiver(this, filter);
			// Register for events related to sdcard installation.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
				final IntentFilter sdFilter = new IntentFilter();
				sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
				sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
				mLoader.getContext().registerReceiver(this, sdFilter);
			}
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			// Tell the loader about the change.
			mLoader.onContentChanged();
		}
	}

}