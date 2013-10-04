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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;

import java.util.List;

public class SettingsActivity extends BasePreferenceActivity implements OnSharedPreferenceChangeListener {

	private SharedPreferences mPreferences;
	private HeaderAdapter mAdapter;

	public HeaderAdapter getHeaderAdapter() {
		if (mAdapter != null) return mAdapter;
		return mAdapter = new HeaderAdapter(this);
	}

	@Override
	public void onBuildHeaders(final List<Header> target) {
		loadHeadersFromResource(R.xml.settings_headers, target);
		final HeaderAdapter adapter = getHeaderAdapter();
		adapter.clear();
		adapter.addAll(target);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME: {
				onBackPressed();
				return true;
			}
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(final SharedPreferences preferences, final String key) {
	}

	@Override
	public void setListAdapter(final ListAdapter adapter) {
		if (adapter == null) {
			super.setListAdapter(null);
		} else {
			super.setListAdapter(getHeaderAdapter());
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		super.onCreate(savedInstanceState);
		setIntent(getIntent().addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected boolean shouldRestartWhenThemeChanged() {
		return !isMultiPane() && !getIntent().hasExtra(EXTRA_SHOW_FRAGMENT);
	}

	static class HeaderAdapter extends ArrayAdapter<Header> {

		static final int HEADER_TYPE_CATEGORY = 0;
		static final int HEADER_TYPE_NORMAL = 1;

		private final Context mContext;
		private final Resources mResources;

		public HeaderAdapter(final Context context) {
			super(context, R.layout.settings_list_item);
			mContext = context;
			mResources = context.getResources();
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public int getItemViewType(final int position) {
			final Header header = getItem(position);
			return getHeaderType(header);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final Header header = getItem(position);
			final int viewType = getHeaderType(header);
			final View view;
			switch (viewType) {
				case HEADER_TYPE_CATEGORY: {
					view = new TextView(mContext, null, android.R.attr.listSeparatorTextViewStyle);
					((TextView) view).setText(header.getTitle(mResources));
					break;
				}
				default: {
					final boolean is_switch_item = convertView != null
							&& convertView.findViewById(android.R.id.toggle) != null;
					final boolean should_create_new = convertView instanceof TextView || is_switch_item;
					view = super.getView(position, should_create_new ? null : convertView, parent);
					final ViewHolder holder;
					final Object tag = view.getTag();
					if (tag instanceof ViewHolder) {
						holder = (ViewHolder) tag;
					} else {
						holder = new ViewHolder(view);
						view.setTag(holder);
					}
					final CharSequence title = header.getTitle(mResources);
					holder.title.setText(title);
					final CharSequence summary = header.getSummary(mResources);
					if (!TextUtils.isEmpty(summary)) {
						holder.summary.setVisibility(View.VISIBLE);
						holder.summary.setText(summary);
					} else {
						holder.summary.setVisibility(View.GONE);
					}
					if (header.iconRes != 0) {
						holder.icon.setImageResource(header.iconRes);
					} else {
						holder.icon.setImageDrawable(null);
					}
					break;
				}
			}
			return view;
		}

		@Override
		public boolean isEnabled(final int position) {
			return getItemViewType(position) != HEADER_TYPE_CATEGORY;
		}

		static int getHeaderType(final Header header) {
			if (header.fragment == null && header.intent == null)
				return HEADER_TYPE_CATEGORY;
			else
				return HEADER_TYPE_NORMAL;
		}

		private static class ViewHolder {
			private final TextView title, summary;
			private final ImageView icon;

			ViewHolder(final View view) {
				title = (TextView) view.findViewById(android.R.id.title);
				summary = (TextView) view.findViewById(android.R.id.summary);
				icon = (ImageView) view.findViewById(android.R.id.icon);
			}
		}

	}

}
