package org.mariotaku.twidere.fragment;

import static org.mariotaku.twidere.util.Utils.showErrorToast;

import java.util.List;

import org.mariotaku.popupmenu.PopupMenu;
import org.mariotaku.popupmenu.PopupMenu.OnMenuItemClickListener;
import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ExtensionsAdapter;
import org.mariotaku.twidere.loader.ExtensionsListLoader;
import org.mariotaku.twidere.model.Panes;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class ExtensionsListFragment extends BaseListFragment implements Constants, LoaderCallbacks<List<ResolveInfo>>,
		OnItemClickListener, OnItemLongClickListener, OnMenuItemClickListener, Panes.Right {

	private ExtensionsAdapter mAdapter;
	private PackageManager mPackageManager;
	private ResolveInfo mSelectedResolveInfo;
	private ListView mListView;
	private PopupMenu mPopupMenu;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mPackageManager = getActivity().getPackageManager();
		mAdapter = new ExtensionsAdapter(getActivity(), mPackageManager);
		setListAdapter(mAdapter);
		mListView = getListView();
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
		setListShown(false);
	}

	@Override
	public Loader<List<ResolveInfo>> onCreateLoader(int id, Bundle args) {
		return new ExtensionsListLoader(getActivity(), mPackageManager);
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
			showErrorToast(getActivity(), e, false);
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mSelectedResolveInfo = null;
		mSelectedResolveInfo = mAdapter.getItem(position);
		mPopupMenu = PopupMenu.getInstance(getActivity(), view);
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
		setListShown(true);
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
					showErrorToast(getActivity(), e, false);
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
	public void onStop() {
		if (mPopupMenu != null) {
			mPopupMenu.dismiss();
		}
		super.onStop();
	}

}
