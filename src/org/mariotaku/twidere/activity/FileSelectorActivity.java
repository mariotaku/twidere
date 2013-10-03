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

import android.app.LoaderManager.LoaderCallbacks;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.util.ArrayUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class FileSelectorActivity extends BaseSupportActivity implements OnItemClickListener,
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
				getLoaderManager().restartLoader(0, getIntent().getExtras(), this);
				return;
			}
		}
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public Loader<List<File>> onCreateLoader(final int id, final Bundle args) {
		final String[] extensions = args != null ? args.getStringArray(EXTRA_FILE_EXTENSIONS) : null;
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
			getLoaderManager().restartLoader(0, getIntent().getExtras(), this);
		} else if (file.isFile() && !isPickDirectory()) {
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
		if (mCurrentDirectory != null) {
			getActionBar().setTitle(mCurrentDirectory.getName());
		}
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
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		mCurrentDirectory = data != null ? new File(data.getPath()) : getExternalStorageDirectory();
		if (mCurrentDirectory == null) {
			mCurrentDirectory = new File("/");
		}
		final String action = getIntent().getAction();
		if (!INTENT_ACTION_PICK_FILE.equals(action) && !INTENT_ACTION_PICK_DIRECTORY.equals(action)) {
			finish();
			return;
		}
		setContentView(android.R.layout.list_content);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		mAdapter = new FilesAdapter(this);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		getLoaderManager().initLoader(0, getIntent().getExtras(), this);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private boolean isPickDirectory() {
		return INTENT_ACTION_PICK_DIRECTORY.equals(getIntent().getAction());
	}

	static class FilesAdapter extends ArrayAdapter<File> {

		private final int mPadding;

		public FilesAdapter(final Context context) {
			super(context, android.R.layout.simple_list_item_1);
			mPadding = (int) (4 * context.getResources().getDisplayMetrics().density);
		}

		@Override
		public long getItemId(final int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final TextView text = (TextView) (view instanceof TextView ? view : view.findViewById(android.R.id.text1));
			final File file = getItem(position);
			if (file == null || text == null) return view;
			text.setText(file.getName());
			text.setSingleLine(true);
			text.setEllipsize(TruncateAt.MARQUEE);
			text.setPadding(mPadding, mPadding, position, mPadding);
			text.setCompoundDrawablesWithIntrinsicBounds(
					file.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file, 0, 0, 0);
			return view;
		}

		public void setData(final List<File> data) {
			clear();
			if (data != null) {
				addAll(data);
			}
		}

	}

	static class FilesLoader extends AsyncTaskLoader<List<File>> {

		private final File path;
		private final String[] extensions;
		private final Pattern extensions_regex;

		private static final Comparator<File> NAME_COMPARATOR = new Comparator<File>() {
			@Override
			public int compare(final File file1, final File file2) {
				final Locale loc = Locale.getDefault();
				return file1.getName().toLowerCase(loc).compareTo(file2.getName().toLowerCase(loc));
			}
		};

		public FilesLoader(final Context context, final File path, final String[] extensions) {
			super(context);
			this.path = path;
			this.extensions = extensions;
			extensions_regex = extensions != null ? Pattern.compile(ArrayUtils.toString(extensions, '|', false),
					Pattern.CASE_INSENSITIVE) : null;
		}

		@Override
		public List<File> loadInBackground() {
			if (path == null || !path.isDirectory()) return Collections.emptyList();
			final File[] listed_files = path.listFiles();
			if (listed_files == null) return Collections.emptyList();
			final List<File> dirs = new ArrayList<File>();
			final List<File> files = new ArrayList<File>();
			for (final File file : listed_files) {
				if (!file.canRead() || file.isHidden()) {
					continue;
				}
				if (file.isDirectory()) {
					dirs.add(file);
				} else if (file.isFile()) {
					final String name = file.getName();
					final int idx = name.lastIndexOf(".");
					if (extensions == null || extensions.length == 0 || idx == -1 || idx > -1
							&& extensions_regex.matcher(name.substring(idx + 1)).matches()) {
						files.add(file);
					}
				}
			}
			Collections.sort(dirs, NAME_COMPARATOR);
			Collections.sort(files, NAME_COMPARATOR);
			final List<File> list = new ArrayList<File>(dirs);
			list.addAll(files);
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
