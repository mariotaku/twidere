package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.widget.TabsAdapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnShowListener;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.ExtendedViewPager;
import com.viewpagerindicator.TabPageIndicator;

public class FilterFragment extends BaseFragment {

	private ExtendedViewPager mViewPager;

	private TabsAdapter mAdapter;

	private TabPageIndicator mIndicator;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		View view = getView();
		mViewPager = (ExtendedViewPager) view.findViewById(R.id.pager);
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		mAdapter = new TabsAdapter(getSherlockActivity(), getFragmentManager());
		mAdapter.addTab(FilteredUsersFragment.class, getString(R.string.users), null);
		mAdapter.addTab(FilteredKeywordsFragment.class, getString(R.string.keywords), null);
		mAdapter.addTab(FilteredSourcesFragment.class, getString(R.string.sources), null);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_filter, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.filters_list, container, false);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD:
				return false;
			case MENU_TOGGLE:
				SharedPreferences prefs = getSherlockActivity().getSharedPreferences(PREFERENCE_NAME,
						Context.MODE_PRIVATE);
				boolean filter_enabled = prefs.getBoolean(PREFERENCE_KEY_ENABLE_FILTER, false);
				prefs.edit().putBoolean(PREFERENCE_KEY_ENABLE_FILTER, !filter_enabled).commit();
				getSherlockActivity().invalidateOptionsMenu();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		SharedPreferences prefs = getSherlockActivity().getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
		boolean filter_enabled = prefs.getBoolean(PREFERENCE_KEY_ENABLE_FILTER, false);
		getSherlockActivity().getSupportActionBar().setSubtitle(
				filter_enabled ? R.string.filter_enabled : R.string.filter_disabled);
		menu.findItem(MENU_TOGGLE).setTitle(filter_enabled ? R.string.disable : R.string.enable);
		super.onPrepareOptionsMenu(menu);
	}

	public static abstract class BaseFilterListFragment extends BaseListFragment implements LoaderCallbacks<Cursor>,
			OnItemLongClickListener {

		private FilterListAdapter mAdapter;

		private AddItemFragment mFragment = new AddItemFragment(this);

		private ContentResolver mResolver;

		public abstract String[] getContentColumns();

		public abstract Uri getContentUri();

		public abstract FilterListAdapter getFilterListAdapter();

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			mResolver = getSherlockActivity().getContentResolver();
			super.onActivityCreated(savedInstanceState);
			setHasOptionsMenu(true);
			mAdapter = getFilterListAdapter();
			setListAdapter(mAdapter);
			getListView().setOnItemLongClickListener(this);
			getLoaderManager().initLoader(0, null, this);
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			String[] cols = getContentColumns();
			Uri uri = getContentUri();
			return new CursorLoader(getSherlockActivity(), uri, cols, null, null, null);
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> adapter, View view, int position, long id) {
			String where = Filters._ID + "=" + id;
			mResolver.delete(getContentUri(), where, null);
			getLoaderManager().restartLoader(0, null, this);
			return true;
		}

		@Override
		public void onListItemClick(ListView l, View v, int position, long id) {
			Toast.makeText(getSherlockActivity(), R.string.longclick_to_delete, Toast.LENGTH_SHORT).show();
			super.onListItemClick(l, v, position, id);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			mAdapter.changeCursor(null);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
			mAdapter.changeCursor(data);
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

		public static class FilterListAdapter extends SimpleCursorAdapter {

			public FilterListAdapter(Context context) {
				super(context, android.R.layout.simple_list_item_1, null, from, to, 0);
			}

			private static final String[] from = new String[] { Filters.TEXT };

			private static final int[] to = new int[] { android.R.id.text1 };

		}

		private static class AddItemFragment extends BaseDialogFragment implements OnClickListener, TextWatcher,
				OnShowListener {

			private BaseFilterListFragment mFragment;
			private EditText mEditText;

			public AddItemFragment(BaseFilterListFragment fragment) {
				mFragment = fragment;
			}

			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						ContentValues values = new ContentValues();
						String text = mEditText.getText().toString();
						if (mFragment instanceof FilteredKeywordsFragment) {
							final CharSequence TAG_START = "<p>";
							final CharSequence TAG_END = "</p>";
							SpannableString tmp = new SpannableString(text);
							String formatted = Html.toHtml(tmp);
							if (formatted != null && formatted.contains(TAG_START) && formatted.contains(TAG_END)) {
								int start = formatted.indexOf(TAG_START.toString()) + TAG_START.length();
								int end = formatted.lastIndexOf(TAG_END.toString());
								text = formatted.substring(start, end);
							}
						}
						values.put(Filters.TEXT, text);
						getSherlockActivity().getContentResolver().insert(mFragment.getContentUri(), values);
						mFragment.getLoaderManager().restartLoader(0, null, mFragment);
						break;
				}

			}

			@Override
			public Dialog onCreateDialog(Bundle savedInstanceState) {
				AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
				FrameLayout layout = new FrameLayout(getSherlockActivity());
				mEditText = new EditText(getSherlockActivity());
				mEditText.addTextChangedListener(this);
				layout.addView(mEditText, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT) {

					{
						int margin = (int) (getResources().getDisplayMetrics().density * 16);
						bottomMargin = margin;
						leftMargin = margin;
						rightMargin = margin;
						topMargin = margin;
					}
				});
				builder.setTitle(R.string.add_rule);
				builder.setView(layout);
				builder.setPositiveButton(android.R.string.ok, this);
				builder.setNegativeButton(android.R.string.cancel, this);
				AlertDialog dialog = builder.create();
				dialog.setOnShowListener(this);
				return dialog;
			}

			@Override
			public void onShow(DialogInterface dialog) {
				setOKButton(mEditText.length());
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				int length = s.toString().trim().length();
				setOKButton(length);
			}

			private void setOKButton(int length) {
				if (getDialog() instanceof AlertDialog) {
					Button button = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
					if (button != null) {
						button.setVisibility(length > 0 ? View.VISIBLE : View.INVISIBLE);
					}
				}
			}
		}
	}

	public static class FilteredKeywordsFragment extends BaseFilterListFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Keywords.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Keywords.CONTENT_URI;
		}

		@Override
		public FilterListAdapter getFilterListAdapter() {
			return new FilteredKeywordsAdapter(getSherlockActivity());
		}

		private static class FilteredKeywordsAdapter extends FilterListAdapter {

			private int mTextIdx;

			public FilteredKeywordsAdapter(Context context) {
				super(context);
			}

			@Override
			public void bindView(View view, Context context, Cursor cursor) {
				String text = cursor.getString(mTextIdx);
				TextView tv = (TextView) view.findViewById(android.R.id.text1);
				tv.setText(Html.fromHtml(text).toString());
			}

			@Override
			public void changeCursor(Cursor cursor) {
				super.changeCursor(cursor);
				if (cursor != null) {
					mTextIdx = cursor.getColumnIndexOrThrow(Filters.Keywords.TEXT);
				}
			}

		}

	}

	public static class FilteredSourcesFragment extends BaseFilterListFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Sources.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Sources.CONTENT_URI;
		}

		@Override
		public FilterListAdapter getFilterListAdapter() {
			return new FilterListAdapter(getSherlockActivity());
		}

	}

	public static class FilteredUsersFragment extends BaseFilterListFragment {

		@Override
		public String[] getContentColumns() {
			return Filters.Users.COLUMNS;
		}

		@Override
		public Uri getContentUri() {
			return Filters.Users.CONTENT_URI;
		}

		@Override
		public FilterListAdapter getFilterListAdapter() {
			return new FilterListAdapter(getSherlockActivity());
		}

	}

}
