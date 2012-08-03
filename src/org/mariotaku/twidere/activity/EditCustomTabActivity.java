package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.CUSTOM_TABS_TYPE_NAME_MAP;
import static org.mariotaku.twidere.util.Utils.getTabTypeName;

import java.util.ArrayList;
import java.util.Set;

import org.mariotaku.twidere.Constants;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class EditCustomTabActivity extends BasePreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		final PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_HOME:
				finish();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	class TabTypePreference extends Preference implements Constants, OnPreferenceClickListener,
			OnClickListener {

		private final TabTypeAdapter mAdapter;

		private AlertDialog mDialog;

		public TabTypePreference(Context context) {
			this(context, null);
		}

		public TabTypePreference(Context context, AttributeSet attrs) {
			this(context, attrs, android.R.attr.preferenceStyle);
		}

		public TabTypePreference(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			setOnPreferenceClickListener(this);
			mAdapter = new TabTypeAdapter(context);
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			final String item = mAdapter.getItem(which);
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
		}
		
		class TabTypeAdapter extends BaseAdapter {

			final ArrayList<String> TYPES = new ArrayList<String>();
			final LayoutInflater mInflater;
			final Context mContext;
			public TabTypeAdapter(Context context) {
				final Set<String> keys = CUSTOM_TABS_TYPE_NAME_MAP.keySet();
				mContext = context;
				mInflater = LayoutInflater.from(context);
				for (String key : keys) {
					TYPES.add(key);
				}
			}
			
			@Override
			public int getCount() {
				return TYPES.size();
			}

			@Override
			public String getItem(int position) {
				return TYPES.get(position);
			}

			@Override
			public long getItemId(int position) {
				return getItem(position).hashCode();
			}

			@Override
			public View getView(int position, View view, ViewGroup root) {
				final View convertView = view == null ? mInflater.inflate(android.R.layout.simple_list_item_1, null) : view;
				final TextView text = (TextView) convertView.findViewById(android.R.id.text1);
				text.setText(getTabTypeName(mContext, getItem(position)));
				return null;
			}
			
		}

		@Override
		public boolean onPreferenceClick(Preference preference) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}
			final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
			builder.setAdapter(mAdapter, this);
			mDialog = builder.show();
			return true;
		}
	}
}
