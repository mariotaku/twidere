package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getColorPreviewBitmap;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.ColorPickerDialogFragment;
import org.mariotaku.twidere.fragment.ColorPickerDialogFragment.OnColorSelectedListener;
import org.mariotaku.twidere.provider.TweetStore.Accounts;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class SetColorActivity extends BaseDialogActivity implements OnItemClickListener, OnColorSelectedListener {

	private GridView mColorsGrid;

	private int mCustomizedColor = Color.WHITE;

	List<Integer> mColors = new ArrayList<Integer>();
	ColorPickerDialogFragment mFragment = new ColorPickerDialogFragment();

	@Override
	public void onColorSelected(int color) {
		mCustomizedColor = color;
		mColorsGrid.invalidateViews();
		finishSelecting(color);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.set_color);
		mColorsGrid = (GridView) findViewById(R.id.colors_grid);

		Bundle bundle = savedInstanceState != null ? savedInstanceState : getIntent().getExtras();

		mCustomizedColor = bundle != null ? bundle.getInt(Accounts.USER_COLOR, Color.WHITE) : Color.WHITE;

		Resources res = getResources();
		mColors.add(res.getColor(R.color.holo_red_light));
		mColors.add(res.getColor(R.color.holo_orange_light));
		mColors.add(res.getColor(R.color.holo_green_light));
		mColors.add(res.getColor(R.color.holo_blue_light));
		mColors.add(res.getColor(R.color.holo_purple));
		mColors.add(Color.TRANSPARENT);
		if (mColors.contains(mCustomizedColor)) {

		}
		mColorsGrid.setAdapter(new ColorsAdapter(this, mColors));
		mColorsGrid.setOnItemClickListener(this);

	}

	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		if (position == adapter.getCount() - 1) {
			showDialog();
		} else {
			finishSelecting(mColors.get(position));
		}

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(Accounts.USER_COLOR, mCustomizedColor);
		super.onSaveInstanceState(outState);
	}

	private void finishSelecting(int color) {
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putInt(Accounts.USER_COLOR, color);
		intent.putExtras(bundle);
		setResult(RESULT_OK, intent);
		finish();
	}

	private void showDialog() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mFragment.setInitialColor(mCustomizedColor);
		mFragment.show(ft, "dialog");
	}

	private class ColorsAdapter extends ArrayAdapter<Integer> {

		private Context mContext;

		public ColorsAdapter(Context context, List<Integer> objects) {
			super(context, 0, objects);
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = getLayoutInflater().inflate(R.layout.color_grid_item, parent, false);
			ImageView color = (ImageView) view.findViewById(R.id.color);
			color.setImageBitmap(getColorPreviewBitmap(mContext, getItem(position)));
			if (position == getCount() - 1) {
				view.findViewById(R.id.text).setVisibility(View.VISIBLE);
				color.setImageBitmap(getColorPreviewBitmap(mContext, mCustomizedColor));
			}
			return view;
		}

	}

}
