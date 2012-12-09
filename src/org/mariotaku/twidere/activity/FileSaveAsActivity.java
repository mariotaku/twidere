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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.util.FileUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileSaveAsActivity extends BaseDialogWhenLargeActivity implements OnItemClickListener,
		LoaderCallbacks<List<File>>, DialogInterface.OnClickListener, TextWatcher {

	private File mCurrentDirectory, mSourceFile;
	private ListView mListView;
	private DirectoriesAdapter mAdapter;
	private EditText mEditFileName;
	private String mDefaultFileName, mFileName;
	private final DialogFragment mOverwriteComfirmFragment = new OverwriteComfirmFragment(),
			mSaveProgressFragment = new SaveProgressFragment();
	private SaveFileTask mSaveFileTask;

	private static InputFilter VALID_FILENAME_FILTER = new InputFilter() {

		@Override
		public CharSequence filter(final CharSequence source, final int start, final int end, final Spanned dest,
				final int dstart, final int dend) {
			for (int i = start; i < end; i++) {
				final char c = source.charAt(i);
				switch (c) {
					case '/':
					case '\\':
					case ':':
					case '"':
					case '*':
					case '?':
					case '<':
					case '>':
					case '|':
						return "";
				}

			}
			return null;
		}

	};

	@Override
	public void afterTextChanged(final Editable s) {

	}

	@Override
	public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

	}

	@Override
	public void onBackPressed() {
		if (mCurrentDirectory != null) {
			final File parent = mCurrentDirectory.getParentFile();
			if (parent != null) {
				mCurrentDirectory = parent;
				invalidateSupportOptionsMenu();
				getSupportLoaderManager().restartLoader(0, null, this);
				return;
			}
		}
		setResult(RESULT_CANCELED);
		finish();
	}

	@Override
	public void onClick(final DialogInterface dialog, final int id) {
		switch (id) {
			case DialogInterface.BUTTON_POSITIVE: {
				if (mSaveFileTask != null) {
					mSaveFileTask.cancel(true);
				}
				mSaveFileTask = new SaveFileTask(mSourceFile, new File(mFileName));
				mSaveFileTask.execute();
				break;
			}
			case DialogInterface.BUTTON_NEGATIVE: {
				break;
			}
		}

	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		final String action = getIntent().getAction();
		mCurrentDirectory = getExternalStorageDirectory();
		final String source_filename = getIntent().getStringExtra(INTENT_KEY_FILE_SOURCE);
		if (!INTENT_ACTION_SAVE_FILE.equals(action) || mCurrentDirectory == null || source_filename == null) {
			finish();
			return;
		}
		mSourceFile = new File(source_filename);
		if (!mSourceFile.exists()) {
			finish();
			return;
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.file_save_as);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mAdapter = new DirectoriesAdapter(this);
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(this);
		invalidateSupportOptionsMenu();
		mEditFileName = (EditText) findViewById(R.id.edit_file_name);
		mEditFileName.setFilters(new InputFilter[] { VALID_FILENAME_FILTER });
		mEditFileName.addTextChangedListener(this);
		mDefaultFileName = getIntent().getStringExtra(INTENT_KEY_FILENAME);
		mFileName = savedInstanceState != null ? savedInstanceState.getString(INTENT_KEY_FILENAME) : mDefaultFileName;
		mEditFileName.setText(mFileName);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<List<File>> onCreateLoader(final int id, final Bundle args) {
		return new DirectoriesLoader(this, mCurrentDirectory);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		getMenuInflater().inflate(R.menu.menu_file_save_as, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
		mCurrentDirectory = mAdapter.getItem(position);
		invalidateSupportOptionsMenu();
		getSupportLoaderManager().restartLoader(0, null, this);
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
			case MENU_SAVE: {
				if (mCurrentDirectory != null && mCurrentDirectory.isDirectory() && mCurrentDirectory.canWrite()) {
					mFileName = mEditFileName != null ? mEditFileName.getText().toString() : null;
					final int filename_length = mFileName != null ? mFileName.trim().length() : 0;
					final int default_filename_length = mDefaultFileName != null ? mDefaultFileName.trim().length() : 0;
					if (filename_length <= 0 && default_filename_length <= 0) return false;
					if (filename_length <= 0 && default_filename_length >= 0) {
						mFileName = mDefaultFileName;
					}
					final File destFile = new File(mCurrentDirectory, mFileName);
					if (!destFile.isDirectory() && destFile.exists()) {
						mOverwriteComfirmFragment.show(getSupportFragmentManager(), null);
					} else {
						if (mSaveFileTask != null) {
							mSaveFileTask.cancel(true);
						}
						mSaveFileTask = new SaveFileTask(mSourceFile, destFile);
						mSaveFileTask.execute();
					}
				}
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
	public boolean onPrepareOptionsMenu(final Menu menu) {
		final MenuItem item = menu.findItem(MENU_SAVE);
		if (item != null) {
			final int filename_length = mEditFileName != null ? mEditFileName.getText().toString().trim().length() : 0;
			final int default_filename_length = mDefaultFileName != null ? mDefaultFileName.trim().length() : 0;
			item.setEnabled(mCurrentDirectory.canWrite() && (default_filename_length > 0 || filename_length > 0));
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
		invalidateSupportOptionsMenu();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		mFileName = mEditFileName != null ? mEditFileName.getText().toString() : null;
		outState.putString(INTENT_KEY_FILENAME, mFileName);
		super.onSaveInstanceState(outState);
	}

	public static class OverwriteComfirmFragment extends BaseDialogFragment {

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final FileSaveAsActivity activity = (FileSaveAsActivity) getActivity();
			final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(R.string.overwrite_confirm);
			builder.setPositiveButton(android.R.string.ok, activity);
			builder.setNegativeButton(android.R.string.cancel, activity);
			return builder.create();
		}

	}

	public static class SaveProgressFragment extends BaseDialogFragment {

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final FileSaveAsActivity activity = (FileSaveAsActivity) getActivity();
			final ProgressDialog dialog = new ProgressDialog(activity);
			dialog.setCancelable(false);
			dialog.setMessage(getString(R.string.please_wait));
			return dialog;
		}

	}

	static class DirectoriesAdapter extends BaseAdapter {

		private final LayoutInflater mInflater;
		private final Context mContext;

		private final List<File> mData = new ArrayList<File>();

		public DirectoriesAdapter(final Context context) {
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
			text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_folder, 0, 0, 0);
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

	static class DirectoriesLoader extends AsyncTaskLoader<List<File>> {

		private final File path;

		private static final Comparator<File> NAME_COMPARATOR = new Comparator<File>() {
			@Override
			public int compare(final File file1, final File file2) {
				return file1.getName().compareTo(file2.getName());
			}
		};

		public DirectoriesLoader(final Context context, final File path) {
			super(context);
			this.path = path;
		}

		@Override
		public List<File> loadInBackground() {
			final List<File> directories_list = new ArrayList<File>();
			if (path != null && path.isDirectory()) {
				final File[] files = path.listFiles();
				if (files != null) {
					for (final File file : files) {
						if (file.isDirectory() && file.canRead()) {
							directories_list.add(file);
						}
					}
				}
				Collections.sort(directories_list, NAME_COMPARATOR);
			}
			return directories_list;
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

	class SaveFileTask extends AsyncTask<Void, Void, Boolean> {

		private final File src, dst;

		public SaveFileTask(final File src, final File dst) {
			this.src = src;
			this.dst = dst;
		}

		@Override
		protected Boolean doInBackground(final Void... args) {
			if (src == null || dst == null) return false;
			try {
				if (dst.isDirectory()) {
					FileUtils.copyFileToDirectory(src, dst);
				} else {
					FileUtils.copyFile(src, dst);
				}
			} catch (final IOException e) {
				return false;
			} catch (final Exception e) {
				return null;
			}
			return true;
		}

		@Override
		protected void onCancelled() {
			mSaveProgressFragment.dismiss();
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(final Boolean result) {
			mSaveProgressFragment.dismiss();
			super.onPostExecute(result);
			if (result != null) {

				if (result) {
					Toast.makeText(FileSaveAsActivity.this, getString(R.string.file_saved_to, dst.getPath()),
							Toast.LENGTH_SHORT).show();
					finish();
				}
			}
		}

		@Override
		protected void onPreExecute() {
			mSaveProgressFragment.show(getSupportFragmentManager(), null);
			super.onPreExecute();
		}

	}

}
