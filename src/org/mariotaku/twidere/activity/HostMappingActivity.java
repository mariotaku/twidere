/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.activity;

import static android.text.TextUtils.isEmpty;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.fragment.ProgressDialogFragment;
import org.mariotaku.twidere.util.AsyncTask;
import org.mariotaku.twidere.util.HostsFileParser;
import org.mariotaku.twidere.util.ParseUtils;

import java.util.Map;

public class HostMappingActivity extends BaseSupportActivity implements MultiChoiceModeListener {

	private ListView mListView;
	private HostMappingAdapter mAdapter;
	private SharedPreferences mPreferences;

	private DialogFragment mDialogFragment;

	@Override
	public boolean onActionItemClicked(final ActionMode mode, final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_DELETE: {
				final SharedPreferences.Editor editor = mPreferences.edit();
				final SparseBooleanArray array = mListView.getCheckedItemPositions();
				if (array == null) return false;
				for (int i = 0, size = array.size(); i < size; i++) {
					if (array.valueAt(i)) {
						editor.remove(mAdapter.getItem(i));
					}
				}
				editor.apply();
				reload();
				break;
			}
			default: {
				return false;
			}
		}
		mode.finish();
		return true;
	}

	@Override
	public boolean onCreateActionMode(final ActionMode mode, final Menu menu) {
		getMenuInflater().inflate(R.menu.action_multi_select_items, menu);
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_host_mapping, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onDestroyActionMode(final ActionMode mode) {

	}

	@Override
	public void onItemCheckedStateChanged(final ActionMode mode, final int position, final long id,
			final boolean checked) {
		updateTitle(mode);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				onBackPressed();
				break;
			case MENU_ADD:
				mDialogFragment = (DialogFragment) Fragment.instantiate(this, AddMappingDialogFragment.class.getName());
				mDialogFragment.show(getFragmentManager(), "add_mapping");
				break;
			case MENU_IMPORT_FROM:
				final Intent intent = new Intent(INTENT_ACTION_PICK_FILE);
				intent.setClass(this, FileSelectorActivity.class);
				startActivityForResult(intent, REQUEST_PICK_FILE);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareActionMode(final ActionMode mode, final Menu menu) {
		return true;
	}

	public void reload() {
		if (mAdapter == null) return;
		mAdapter.reload();
	}

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
		switch (requestCode) {
			case REQUEST_PICK_FILE: {
				if (resultCode == RESULT_OK) {
					final Uri uri = intent != null ? intent.getData() : null;
					if (uri != null) {
						new ImportHostsTask(this, uri.getPath()).execute();
					}
				}
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
		setContentView(android.R.layout.list_content);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mAdapter = new HostMappingAdapter(this);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		mListView.setMultiChoiceModeListener(this);
		reload();
	}

	private void updateTitle(final ActionMode mode) {
		if (mListView == null) return;
		final int count = mListView.getCheckedItemCount();
		mode.setTitle(getResources().getQuantityString(R.plurals.Nitems_selected, count, count));
	}

	public static class AddMappingDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener {

		private EditText mEditHost, mEditAddress;
		private String mHost, mAddress;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mHost = ParseUtils.parseString(mEditHost.getText());
					mAddress = ParseUtils.parseString(mEditAddress.getText());
					if (isEmpty(mHost) || isEmpty(mAddress)) return;
					final SharedPreferences prefs = getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME,
							Context.MODE_PRIVATE);
					final SharedPreferences.Editor editor = prefs.edit();
					editor.putString(mHost, mAddress);
					editor.commit();
					final Activity activity = getActivity();
					if (activity instanceof HostMappingActivity) {
						((HostMappingActivity) activity).reload();
					}
					break;
				}
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final Bundle bundle = savedInstanceState == null ? getArguments() : savedInstanceState;
			mHost = bundle != null ? bundle.getString(EXTRA_TEXT1) : null;
			mAddress = bundle != null ? bundle.getString(EXTRA_TEXT2) : null;
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final View view = LayoutInflater.from(getActivity()).inflate(R.layout.host_mapping_dialog_view, null);
			builder.setView(view);
			mEditHost = (EditText) view.findViewById(R.id.host);
			mEditAddress = (EditText) view.findViewById(R.id.address);
			if (mHost != null) {
				mEditHost.setText(mHost);
			}
			if (mAddress != null) {
				mEditAddress.setText(mAddress);
			}
			builder.setTitle(R.string.add_host_mapping);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, null);
			return builder.create();
		}

		@Override
		public void onSaveInstanceState(final Bundle outState) {
			outState.putString(EXTRA_TEXT1, mHost);
			outState.putString(EXTRA_TEXT2, mAddress);
			super.onSaveInstanceState(outState);
		}

	}

	static class HostMappingAdapter extends ArrayAdapter<String> {

		private final SharedPreferences mPreferences;

		public HostMappingAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_activated_2);
			mPreferences = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
			final String key = getItem(position);
			text1.setText(key);
			text2.setText(mPreferences.getString(key, null));
			return view;
		}

		public void reload() {
			clear();
			final Map<String, ?> all = mPreferences.getAll();
			addAll(all.keySet());
		}

	}

	static class ImportHostsTask extends AsyncTask<Void, Void, Boolean> {

		private final SharedPreferences mPreferences;
		private final HostMappingActivity mActivity;
		private final String mPath;

		ImportHostsTask(final HostMappingActivity activity, final String path) {
			mActivity = activity;
			mPath = path;
			mPreferences = activity.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
		}

		@Override
		protected Boolean doInBackground(final Void... params) {
			final HostsFileParser hosts = new HostsFileParser(mPath);
			final boolean result = hosts.reload();
			final SharedPreferences.Editor editor = mPreferences.edit();
			for (final Map.Entry<String, String> entry : hosts.getAll().entrySet()) {
				editor.putString(entry.getKey(), entry.getValue());
			}
			return result && editor.commit();
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			final FragmentManager fm = mActivity.getFragmentManager();
			final Fragment f = fm.findFragmentByTag("import_hosts_progress");
			if (f instanceof DialogFragment) {
				((DialogFragment) f).dismiss();
			}
			mActivity.reload();
		}

		@Override
		protected void onPreExecute() {
			final FragmentManager fm = mActivity.getFragmentManager();
			final DialogFragment f = new ProgressDialogFragment();
			f.setCancelable(false);
			f.show(fm, "import_hosts_progress");
		}
	}
}
