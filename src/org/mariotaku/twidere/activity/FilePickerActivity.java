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

import static android.os.Environment.getExternalStorageDirectory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.util.ArrayUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FilePickerActivity extends BaseDialogWhenLargeActivity implements OnItemClickListener,
		LoaderCallbacks<List<File>> {

	private File mCurrentDirectory;
	private ListView mListView;
	private FilesAdapter mAdapter;

	@Override
	public void onBackPressed() {
		if (mCurrentDirectory != null) {
			final File parent = mCurrentDirectory.getParentFile();
			if (parent != null) {
				mCurrentDirectory = parent;
				getSupportLoaderManager().restartLoader(0, null, this);
				return;
			}
		}
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		final String action = getIntent().getAction();
		mCurrentDirectory = getExternalStorageDirectory();
		if (!INTENT_ACTION_PICK_FILE.equals(action) || mCurrentDirectory == null) {
			finish();
			return;
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mAdapter = new FilesAdapter(this);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<List<File>> onCreateLoader(final int id, Bundle args) {
		if (args == null) {
			args = getIntent().getExtras();
		}
		final String[] extensions = args != null ? args.getStringArray(INTENT_KEY_FILE_EXTENSIONS) : new String[0];
		return new FilesLoader(this, mCurrentDirectory, extensions);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_file_picker, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		final File file = mAdapter.getItem(position);
		if (file == null) return;
		if (file.isDirectory()) {
			mCurrentDirectory = file;
			getSupportLoaderManager().restartLoader(0, null, this);
		} else if (file.isFile()) {
			final Intent intent = new Intent();
			intent.setData(Uri.fromFile(file));
			setResult(RESULT_OK, intent);
			finish();
		}

	}

	@Override
	public void onLoaderReset(final Loader<List<File>> loader) {
		mAdapter.setData(null);

	}

	@Override
	public void onLoadFinished(final Loader<List<File>> loader, final List<File> data) {
		mAdapter.setData(data);

	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				break;
			}
			case MENU_CANCEL: {
				setResult(RESULT_CANCELED);
				finish();
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	static class FilesAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;
		private final Context mContext;

		private final List<File> mData = new ArrayList<File>();

		public FilesAdapter(final Context context) {
			mInflater = LayoutInflater.from(context);
			mContext = context;
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public File getItem(final int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(final int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = convertView != null ? convertView : mInflater.inflate(
					android.R.layout.simple_list_item_1, parent, false);
			final TextView text = (TextView) (view instanceof TextView ? view : view.findViewById(android.R.id.text1));
			final File file = getItem(position);
			if (file == null || text == null) return view;
			text.setText(file.getName());
			final int padding = (int) (4 * mContext.getResources().getDisplayMetrics().density);
			text.setSingleLine(true);
			text.setEllipsize(TruncateAt.MARQUEE);
			text.setPadding(padding, padding, position, padding);
			text.setCompoundDrawablesWithIntrinsicBounds(
					file.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file, 0, 0, 0);
			return view;
		}

		public void setData(final List<File> data) {
			mData.clear();
			if (data != null) {
				mData.addAll(data);
			}
			notifyDataSetChanged();
		}

	}

	static class FilesLoader extends AsyncTaskLoader<List<File>> {

		private final File path;
		private final Pattern extensions_regex;

		private static final Comparator<File> NAME_COMPARATOR = new Comparator<File>() {
			@Override
			public int compare(final File file1, final File file2) {
				return file1.getName().compareTo(file2.getName());
			}
		};

		public FilesLoader(final Context context, final File path, final String[] extensions) {
			super(context);
			this.path = path;
			extensions_regex = Pattern.compile(ArrayUtils.toString(extensions, '|', false), Pattern.CASE_INSENSITIVE);
		}

		@Override
		public List<File> loadInBackground() {
			final List<File> list = new ArrayList<File>();
			if (path != null && path.isDirectory()) {
				final File[] files = path.listFiles();
				if (files != null) {
					for (final File file : files) {
						if (file.canRead()) {
							if (file.isFile()) {
								final String name = file.getName();
								final int idx = name.lastIndexOf(".");
								if (idx == -1) {
									continue;
								}
								final Matcher m = extensions_regex.matcher(name.substring(idx + 1));
								if (!m.matches()) {
									continue;
								}
							}
							list.add(file);
						}
					}
				}
				Collections.sort(list, NAME_COMPARATOR);
			}
			return list;
		}

		@Override
		protected void onStartLoading() {
			forceLoad();
		}

		@Override
		protected void onStopLoading() {
			cancelLoad();
		}
	}

}
