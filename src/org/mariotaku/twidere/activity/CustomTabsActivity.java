package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getTabIcon;
import static org.mariotaku.twidere.util.Utils.getTabTypeName;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseDialogFragment;
import org.mariotaku.twidere.provider.TweetStore.Tabs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class CustomTabsActivity extends BaseActivity {
	
	private ListView mListView;
	private CustomTabsAdapter mAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAdapter = new CustomTabsAdapter(this);
		setContentView(R.layout.base_list);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public void onContentChanged() {
		super.onContentChanged();
		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setAdapter(mAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_custom_tabs, menu);
		return super.onCreateOptionsMenu(menu);
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
	
	public static abstract class CustomTabsDialogFragment extends BaseDialogFragment implements OnClickListener {

		public abstract String getTitle();
		
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
				case DialogInterface.BUTTON_POSITIVE: {
					onOKClicked();
					break;
				}
			}
			
		}

		public abstract void onOKClicked();
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(getTitle());
			builder.setPositiveButton(android.R.string.ok, this);
			return builder.create();
		}
		
	}
 	
	public static class CustomTabsAdapter extends SimpleCursorAdapter {

		private int mTypeNameIdx = -1, mIconIdx = -1;
		
		public CustomTabsAdapter(Context context) {
			super(context, R.layout.two_line_with_icon_list_item, null, new String[]{Tabs.NAME}, new int[]{android.R.id.text1}, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);
			final TextView text2 = (TextView) view.findViewById(android.R.id.text2);
			final ImageView icon = (ImageView) view.findViewById(android.R.id.icon);
			if (mTypeNameIdx != -1) {
				text2.setText(getTabTypeName(context, cursor.getString(mTypeNameIdx)));
			}
			if (mIconIdx != -1) {
				icon.setImageResource(getTabIcon(cursor.getString(mIconIdx)));
			}
		}

		@Override
		public void changeCursor(Cursor cursor) {
			super.changeCursor(cursor);
			if (cursor != null) {
				mTypeNameIdx = cursor.getColumnIndex(Tabs.TYPE);
				mIconIdx = cursor.getColumnIndex(Tabs.ICON);
			}
		}
		
	}
}
