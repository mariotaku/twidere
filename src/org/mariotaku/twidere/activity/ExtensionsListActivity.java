package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ExtensionsViewHolder;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class ExtensionsListActivity extends BaseActivity implements Constants, LoaderCallbacks<List<ResolveInfo>>,
		OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener {

	private PluginAdapter mAdapter;
	private PackageManager mPackageManager;
	private ResolveInfo mSelectedResolveInfo;
	private ListView mListView;
	private PopupMenu mPopupMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mPackageManager = getPackageManager();
		mAdapter = new PluginAdapter(this, mPackageManager);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<List<ResolveInfo>> onCreateLoader(int id, Bundle args) {
		setSupportProgressBarIndeterminateVisibility(true);
		return new PluginsListLoader(this, mPackageManager);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final ResolveInfo info = mAdapter.getItem(position);
		if (info == null || info.activityInfo == null) return;
		final Intent intent = new Intent(INTENT_ACTION_EXTENSIONS);
		intent.setClassName(info.activityInfo.packageName, info.activityInfo.name);
		try {
			startActivity(intent);
		} catch (final ActivityNotFoundException e) {
			showErrorToast(this, e, false);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mSelectedResolveInfo = null;
		mSelectedResolveInfo = mAdapter.getItem(position);
		mPopupMenu = PopupMenu.getInstance(this, view);
		mPopupMenu.inflate(R.menu.action_extension);
		mPopupMenu.setOnMenuItemClickListener(this);
		mPopupMenu.show();
		return true;
	}

	@Override
	public void onLoaderReset(Loader<List<ResolveInfo>> loader) {
		mAdapter.setData(null);
	}

	@Override
	public void onLoadFinished(Loader<List<ResolveInfo>> loader, List<ResolveInfo> data) {
		mAdapter.setData(data);
		setSupportProgressBarIndeterminateVisibility(false);
	}

	@Override
	public boolean onMenuItemClick(MenuItem item) {
		if (mSelectedResolveInfo == null) return false;
		switch (item.getItemId()) {
			case MENU_SETTINGS: {
				final Intent intent = new Intent(INTENT_ACTION_EXTENSIONS);
				intent.setClassName(mSelectedResolveInfo.activityInfo.packageName,
						mSelectedResolveInfo.activityInfo.name);
				try {
					startActivity(intent);
				} catch (final ActivityNotFoundException e) {
					showErrorToast(this, e, false);
				}
				break;
			}
			case MENU_DELETE: {
				final Uri packageUri = Uri.parse("package:" + mSelectedResolveInfo.activityInfo.packageName);
				final Intent uninstallIntent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
				startActivity(uninstallIntent);
				break;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

	public static class PluginsListLoader extends AsyncTaskLoader<List<ResolveInfo>> {

		private PackageIntentReceiver mPackageObserver;
		private InterestingConfigChanges mLastConfig = new InterestingConfigChanges();
		private PackageManager mPackageManager;

		public PluginsListLoader(Context context, PackageManager pm) {
			super(context);
			mPackageManager = pm;
		}

		@Override
		public List<ResolveInfo> loadInBackground() {
			final Intent intent = new Intent(INTENT_ACTION_EXTENSION_SETTINGS);
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
		 * Helper for determining if the configuration has changed in an
		 * interesting way so we need to rebuild the app list.
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

			final PluginsListLoader mLoader;

			public PackageIntentReceiver(PluginsListLoader loader) {
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

	private static class PluginAdapter extends BaseAdapter {

		private PackageManager pm;
		private Context context;

		private final List<ResolveInfo> mData = new ArrayList<ResolveInfo>();

		public PluginAdapter(Context context, PackageManager pm) {
			this.pm = pm;
			this.context = context;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public ResolveInfo getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view = convertView != null ? convertView : LayoutInflater.from(context).inflate(
					R.layout.extension_list_item, parent, false);
			final ExtensionsViewHolder viewholder = view.getTag() == null ? new ExtensionsViewHolder(view)
					: (ExtensionsViewHolder) view.getTag();

			final ResolveInfo info = getItem(position);
			viewholder.text1.setText(info.loadLabel(pm));
			viewholder.text2.setText(info.activityInfo.applicationInfo.loadDescription(pm));
			viewholder.icon.setImageDrawable(info.loadIcon(pm));
			return view;
		}

		public void setData(List<ResolveInfo> data) {
			mData.clear();
			if (data != null) {
				mData.addAll(data);
			}
			notifyDataSetChanged();
		}

	}

}
