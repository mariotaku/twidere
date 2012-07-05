package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.isMyAccount;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.DirectMessagesInboxFragment;
import org.mariotaku.twidere.fragment.DirectMessagesOutboxFragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;

public class DirectMessagesActivity extends BaseActivity implements OnItemSelectedListener {

		private ActionBar mActionBar;
		private ArrayAdapter<TabSpec> mAdapter;
		private Spinner mSpinner;
		private final Bundle mArguments = new Bundle();

		@Override
		public void onCreate(Bundle savedInstanceState) {
			requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
			super.onCreate(savedInstanceState);
			final Intent intent = getIntent();
			final Bundle extras = intent.getExtras();
			final long account_id = extras != null ? extras.getLong(INTENT_KEY_ACCOUNT_ID, -1) : intent.getLongExtra(INTENT_KEY_ACCOUNT_ID, -1);
			mArguments.clear();
			if (isMyAccount(this, account_id)) {
				mArguments.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
			} else {
				finish();
				return;
			}
			setContentView(new FrameLayout(this));
			mActionBar = getSupportActionBar();
			mActionBar.setDisplayShowTitleEnabled(false);
			mActionBar.setDisplayHomeAsUpEnabled(true);
			mActionBar.setDisplayShowCustomEnabled(true);
			mActionBar.setCustomView(R.layout.actionbar_spinner_navigation);
			final View view = mActionBar.getCustomView();
			mSpinner = (Spinner) view.findViewById(R.id.navigate);
			mAdapter = new ArrayAdapter<TabSpec>(this, R.layout.spinner_item);
			mAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
			mAdapter.add(new TabSpec(DirectMessagesInboxFragment.class, getString(R.string.inbox)));
			mAdapter.add(new TabSpec(DirectMessagesOutboxFragment.class, getString(R.string.outbox)));
			mSpinner.setAdapter(mAdapter);
			mSpinner.setOnItemSelectedListener(this);
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.menu_filter, menu);
			return super.onCreateOptionsMenu(menu);
		}

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			final Fragment fragment = Fragment.instantiate(this, mAdapter.getItem(position).cls.getName());
			fragment.setArguments(mArguments);
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(android.R.id.content, fragment);
			ft.commit();

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}

		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
				case MENU_HOME:
					finish();
					break;
				case MENU_ADD:
					return false;
			}
			return super.onOptionsItemSelected(item);
		}

		private static class TabSpec {
			public final Class<? extends Fragment> cls;
			public final String name;

			public TabSpec(Class<? extends Fragment> cls, String name) {
				this.cls = cls;
				this.name = name;
			}

			@Override
			public String toString() {
				return name;
			}
		}

	}
