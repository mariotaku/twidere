package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Filters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

public abstract class FiltersFragment extends BaseListFragment implements LoaderCallbacks<Cursor>,
		OnItemLongClickListener {

	private FilterListAdapter mAdapter;

	private AddItemFragment mFragment = new AddItemFragment(this);

	private ContentResolver mResolver;

	public abstract String[] getContentColumns();

	public abstract Uri getContentUri();

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		mResolver = getContentResolver();
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mAdapter = new FilterListAdapter(getActivity());
		setListAdapter(mAdapter);
		getListView().setOnItemLongClickListener(this);
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		final String[] cols = getContentColumns();
		final Uri uri = getContentUri();
		return new CursorLoader(getActivity(), uri, cols, null, null, null);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
		final String where = Filters._ID + "=" + id;
		mResolver.delete(getContentUri(), where, null);
		getLoaderManager().restartLoader(0, null, this);
		return true;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Toast.makeText(getActivity(), R.string.longclick_to_delete, Toast.LENGTH_SHORT).show();
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD:
				mFragment.show(getFragmentManager(), "add_rule");
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class FilteredKeywordsFragment extends FiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Keywords.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Keywords.CONTENT_URI;
		}

	}

	public static class FilteredSourcesFragment extends FiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Sources.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Sources.CONTENT_URI;
		}

	}

	public static class FilteredUsersFragment extends FiltersFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Users.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Users.CONTENT_URI;
		}

	}

	public static class FilterListAdapter extends SimpleCursorAdapter {

		private static final String[] from = new String[] { Filters.TEXT };

		private static final int[] to = new int[] { android.R.id.text1 };

		public FilterListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1, null, from, to, 0);
		}

	}

	private static class AddItemFragment extends BaseDialogFragment implements OnClickListener {

		private FiltersFragment mFragment;
		private EditText mEditText;

		public AddItemFragment(FiltersFragment fragment) {
			mFragment = fragment;
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					final ContentValues values = new ContentValues();
					if (mEditText.length() <= 0) return;
					final String text = mEditText.getText().toString();
					values.put(Filters.TEXT, text);
					getContentResolver().insert(mFragment.getContentUri(), values);
					mFragment.getLoaderManager().restartLoader(0, null, mFragment);
					break;
			}

		}

		@SuppressWarnings("deprecation")
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final FrameLayout layout = new FrameLayout(getActivity());
			mEditText = new EditText(getActivity());
			layout.addView(mEditText, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT) {

				{
					final int margin = (int) (getResources().getDisplayMetrics().density * 16);
					bottomMargin = margin;
					leftMargin = margin;
					rightMargin = margin;
					topMargin = margin;
					gravity = Gravity.CENTER;
				}
			});
			builder.setTitle(R.string.add_rule);
			builder.setView(layout);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}
	}
}
