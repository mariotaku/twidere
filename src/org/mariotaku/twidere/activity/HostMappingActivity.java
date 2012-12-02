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
import static org.mariotaku.twidere.util.Utils.parseString;

import java.util.Map;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseDialogFragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class HostMappingActivity extends BaseDialogWhenLargeActivity implements OnItemClickListener,
		OnItemLongClickListener {

	private ListView mListView;
	private HostMappingAdapter mAdapter;
	private SharedPreferences mPreferences;

	private DialogFragment mDialogFragment;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
		setContentView(R.layout.base_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mAdapter = new HostMappingAdapter(this);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);
		reload();
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_host_mapping, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View v, final int position, final long id) {
		Toast.makeText(this, R.string.longclick_to_delete, Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onItemLongClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		final SharedPreferences.Editor editor = mPreferences.edit();
		editor.remove(mAdapter.getItem(position));
		final boolean ret = editor.commit();
		reload();
		return ret;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				onBackPressed();
				break;
			case MENU_ADD:
				mDialogFragment = (DialogFragment) Fragment.instantiate(this, AddMappingDialogFragment.class.getName());
				mDialogFragment.show(getSupportFragmentManager(), "add_mapping");
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void reload() {
		if (mAdapter == null) return;
		mAdapter.reload();
	}

	public static class AddMappingDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener {

		private EditText mEditHost, mEditAddress;
		private String mHost, mAddress;

		public AddMappingDialogFragment() {

		}

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					mHost = parseString(mEditHost.getText());
					mAddress = parseString(mEditAddress.getText());
					if (isEmpty(mHost) || isEmpty(mAddress)) return;
					final SharedPreferences prefs = getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME,
							Context.MODE_PRIVATE);
					final SharedPreferences.Editor editor = prefs.edit();
					editor.putString(mHost, mAddress);
					editor.commit();
					final FragmentActivity activity = getActivity();
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
			mHost = bundle != null ? bundle.getString(INTENT_KEY_TEXT1) : null;
			mAddress = bundle != null ? bundle.getString(INTENT_KEY_TEXT2) : null;
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
			outState.putString(INTENT_KEY_TEXT1, mHost);
			outState.putString(INTENT_KEY_TEXT2, mAddress);
			super.onSaveInstanceState(outState);
		}

	}

	static class HostMappingAdapter extends BaseAdapter {

		private final SharedPreferences mPreferences;
		private final LayoutInflater mInflater;
		private Map<String, ?> mData;
		private String[] mKeys;

		public HostMappingAdapter(final Context context) {
			mPreferences = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return mData != null ? mData.size() : 0;
		}

		@Override
		public String getItem(final int position) {
			return mKeys.length > 0 && position < mKeys.length ? mKeys[position] : null;
		}

		@Override
		public long getItemId(final int position) {
			final Object obj = getItem(position);
			return obj != null ? obj.hashCode() : 0;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(
					android.R.layout.simple_list_item_2, null);
			final TextView text1 = (TextView) view.findViewById(android.R.id.text1);
			final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
			final String key = getItem(position);
			text1.setText(key);
			text2.setText(mPreferences.getString(key, null));
			return view;
		}

		public void reload() {
			mData = mPreferences.getAll();
			mKeys = mData.keySet().toArray(new String[mData.size()]);
			notifyDataSetChanged();
		}

	}
}
