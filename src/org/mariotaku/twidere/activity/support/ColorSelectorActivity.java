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

package org.mariotaku.twidere.activity.support;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.adapter.ArrayAdapter;
import org.mariotaku.twidere.fragment.support.ColorPickerDialogFragment;
import org.mariotaku.twidere.fragment.support.ColorPickerDialogFragment.OnColorSelectedListener;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import java.util.ArrayList;
import java.util.List;

public class ColorSelectorActivity extends BaseSupportDialogActivity implements OnItemClickListener,
		OnColorSelectedListener {

	private GridView mColorsGrid;

	private int mCustomizedColor = Color.WHITE;

	List<Integer> mColors = new ArrayList<Integer>();

	@Override
	public void onColorSelected(final int color) {
		mCustomizedColor = color;
		mColorsGrid.invalidateViews();
		finishSelecting(color);
	}

	@Override
	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
		if (position == adapter.getCount() - 1) {
			showDialog();
		} else {
			finishSelecting(mColors.get(position));
		}

	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_color);
		mColorsGrid = (GridView) findViewById(R.id.colors_grid);

		final Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();

		mCustomizedColor = bundle != null ? bundle.getInt(Accounts.USER_COLOR, Color.WHITE) : Color.WHITE;

		final Resources res = getResources();
		mColors.add(res.getColor(android.R.color.holo_red_dark));
		mColors.add(res.getColor(android.R.color.holo_red_light));
		mColors.add(res.getColor(android.R.color.holo_orange_dark));
		mColors.add(res.getColor(android.R.color.holo_orange_light));
		mColors.add(res.getColor(android.R.color.holo_green_light));
		mColors.add(res.getColor(android.R.color.holo_green_dark));
		mColors.add(res.getColor(android.R.color.holo_blue_bright));
		mColors.add(res.getColor(android.R.color.holo_blue_light));
		mColors.add(res.getColor(android.R.color.holo_blue_dark));
		mColors.add(res.getColor(android.R.color.holo_purple));
		mColors.add(res.getColor(android.R.color.white));
		mColors.add(Color.TRANSPARENT);
		if (mColors.contains(mCustomizedColor)) {

		}
		final ColorsAdapter adapter = new ColorsAdapter(this, mColors);
		adapter.setCustomizedColor(mCustomizedColor);
		mColorsGrid.setAdapter(adapter);
		mColorsGrid.setOnItemClickListener(this);

	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		outState.putInt(Accounts.USER_COLOR, mCustomizedColor);
		super.onSaveInstanceState(outState);
	}

	private void finishSelecting(final int color) {
		final Intent intent = new Intent();
		final Bundle bundle = new Bundle();
		bundle.putInt(Accounts.USER_COLOR, color);
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void showDialog() {
		final ColorPickerDialogFragment f = new ColorPickerDialogFragment();
		f.setInitialColor(mCustomizedColor);
		f.show(getSupportFragmentManager(), "color_picker_dialog");
	}

	static class ColorsAdapter extends ArrayAdapter<Integer> {

		private int mCustomizedColor;

		public ColorsAdapter(final Context context, final List<Integer> objects) {
			super(context, R.layout.color_grid_item, objects);
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			final View view = super.getView(position, convertView, parent);
			final ImageView color = (ImageView) view.findViewById(R.id.color);
			final boolean is_last = position == getCount() - 1;
			view.findViewById(R.id.text).setVisibility(is_last ? View.VISIBLE : View.GONE);
			color.setImageDrawable(new ColorDrawable(is_last ? mCustomizedColor : getItem(position)));
			return view;
		}

		public void setCustomizedColor(final int color) {
			if (mCustomizedColor == color) return;
			mCustomizedColor = color;
			notifyDataSetChanged();
		}
	}

}
