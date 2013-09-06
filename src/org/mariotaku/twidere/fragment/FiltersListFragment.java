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

package org.mariotaku.twidere.fragment;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.AutoCompleteAdapter;
import org.mariotaku.twidere.adapter.TabsAdapter;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredKeywordsFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredLinksFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredSourcesFragment;
import org.mariotaku.twidere.fragment.BaseFiltersFragment.FilteredUsersFragment;
import org.mariotaku.twidere.model.Panes;
import org.mariotaku.twidere.provider.TweetStore.Filters;
import org.mariotaku.twidere.util.ThemeUtils;
import org.mariotaku.twidere.util.ViewAccessor;
import org.mariotaku.twidere.view.TabPageIndicator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

public class FiltersListFragment extends BaseFragment implements Panes.Right {

	private static final String INTENT_KEY_AUTO_COMPLETE = "auto_complete";

	private ViewPager mViewPager;
	private TabPageIndicator mIndicator;
	private TabsAdapter mAdapter;

	@Override
	public void onActivityCreated(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);
		mAdapter = new TabsAdapter(getActivity(), getFragmentManager(), mIndicator);
		mAdapter.addTab(FilteredUsersFragment.class, null, getString(R.string.users), null, 0);
		mAdapter.addTab(FilteredKeywordsFragment.class, null, getString(R.string.keywords), null, 1);
		mAdapter.addTab(FilteredSourcesFragment.class, null, getString(R.string.sources), null, 2);
		mAdapter.addTab(FilteredLinksFragment.class, null, getString(R.string.links), null, 3);
		mViewPager.setAdapter(mAdapter);
		mIndicator.setViewPager(mViewPager);
		mIndicator.setDisplayLabel(true);
		mIndicator.setDisplayIcon(false);
	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
		inflater.inflate(R.menu.menu_filter, menu);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup parent, final Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.filters, null);
		mViewPager = (ViewPager) view.findViewById(R.id.pager);
		mIndicator = (TabPageIndicator) view.findViewById(android.R.id.tabs);
		ViewAccessor.setBackground(mIndicator, ThemeUtils.getActionBarBackground(getActivity()));
		return view;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ADD: {
				final Fragment filter = mAdapter.getItem(mViewPager.getCurrentItem());
				if (!(filter instanceof BaseFiltersFragment)) return true;
				final Bundle args = new Bundle();
				args.putBoolean(INTENT_KEY_AUTO_COMPLETE, filter instanceof FilteredUsersFragment);
				args.putParcelable(INTENT_KEY_URI, ((BaseFiltersFragment) filter).getContentUri());
				final AddItemFragment dialog = new AddItemFragment();
				dialog.setArguments(args);
				dialog.show(getFragmentManager(), "add_rule");
				return true;
			}
		}
		return false;
	}

	public static final class AddItemFragment extends BaseDialogFragment implements OnClickListener {

		private AutoCompleteTextView mEditText;

		private AutoCompleteAdapter mUserAutoCompleteAdapter;

		@Override
		public void onClick(final DialogInterface dialog, final int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (mEditText.length() <= 0) return;
					final ContentValues values = new ContentValues();
					final String text = mEditText.getText().toString();
					values.put(Filters.VALUE, text);
					final Bundle args = getArguments();
					final Uri uri = args.getParcelable(INTENT_KEY_URI);
					getContentResolver().insert(uri, values);
					break;
			}

		}

		@Override
		public Dialog onCreateDialog(final Bundle savedInstanceState) {
			final Context context = getActivity();
			final AlertDialog.Builder builder = new AlertDialog.Builder(context);
			final View view = LayoutInflater.from(context).inflate(R.layout.auto_complete_textview_default_style, null);
			builder.setView(view);
			mEditText = (AutoCompleteTextView) view.findViewById(R.id.edit_text);
			final Bundle args = getArguments();
			if (args != null && args.getBoolean(INTENT_KEY_AUTO_COMPLETE)) {
				mUserAutoCompleteAdapter = new AutoCompleteAdapter(getActivity());
				mEditText.setAdapter(mUserAutoCompleteAdapter);
				mEditText.setThreshold(1);
			}
			builder.setTitle(R.string.add_rule);
			builder.setPositiveButton(android.R.string.ok, this);
			builder.setNegativeButton(android.R.string.cancel, this);
			return builder.create();
		}

	}
}
