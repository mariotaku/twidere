package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ExtensionsAdapter;
import org.mariotaku.twidere.loader.ExtensionsListLoader;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class ExtensionsListActivity extends BaseActivity implements Constants, LoaderCallbacks<List<ResolveInfo>>,
		OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener {

	private ExtensionsAdapter mAdapter;
	private PackageManager mPackageManager;
	private ResolveInfo mSelectedResolveInfo;
	private ListView mListView;
	private PopupMenu mPopupMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mPackageManager = getPackageManager();
		mAdapter = new ExtensionsAdapter(this, mPackageManager);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<List<ResolveInfo>> onCreateLoader(int id, Bundle args) {
		setSupportProgressBarIndeterminateVisibility(true);
		return new ExtensionsListLoader(this, mPackageManager);
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

}
