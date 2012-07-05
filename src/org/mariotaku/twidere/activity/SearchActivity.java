package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.parseLong;

import org.mariotaku.actionbarcompat.ActionBar;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.provider.RecentSearchProvider;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
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

public class SearchActivity extends BaseActivity implements OnItemSelectedListener {

	private ActionBar mActionBar;
	private ArrayAdapter<TabSpec> mAdapter;
	private Spinner mSpinner;
	private Uri mData;
	private final Bundle mArguments = new Bundle();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		mArguments.clear();
		final String query = Intent.ACTION_SEARCH.equals(intent.getAction()) ? intent
				.getStringExtra(SearchManager.QUERY) : mData != null ? mData.getQueryParameter(QUERY_PARAM_QUERY)
				: null;
		if (query == null) {
			finish();
			return;
		}
		if (savedInstanceState == null) {
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
					RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
			suggestions.saveRecentQuery(query, null);
		}
		mArguments.putString(INTENT_KEY_QUERY, query);
		final String param_account_id = mData != null ? mData.getQueryParameter(QUERY_PARAM_ACCOUNT_ID) : null;
		if (param_account_id != null) {
			mArguments.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
		} else {
			final String param_account_name = mData != null ? mData.getQueryParameter(QUERY_PARAM_ACCOUNT_NAME) : null;
			if (param_account_name != null) {
				mArguments.putLong(INTENT_KEY_ACCOUNT_ID, getAccountId(this, param_account_name));
			} else {
				final long account_id = getDefaultAccountId(this);
				if (isMyAccount(this, account_id)) {
					mArguments.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
				} else {
					finish();
					return;
				}
			}
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
		mAdapter.add(new TabSpec(SearchTweetsFragment.class, getString(R.string.search_tweets)));
		mAdapter.add(new TabSpec(SearchUsersFragment.class, getString(R.string.search_users)));
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
