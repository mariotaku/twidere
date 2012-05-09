package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.fragment.ViewConversationFragment;
import org.mariotaku.twidere.fragment.ViewStatusFragment;

import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

public class LinkHandlerActivity extends BaseActivity {

	private Fragment mFragment;

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int CODE_STATUS = 1;
	private static final int CODE_USER = 2;
	private static final int CODE_CONVERSATION = 3;
	private static final int CODE_SEARCH = 4;

	static {
		URI_MATCHER.addURI(AUTHORITY_STATUS, null, CODE_STATUS);
		URI_MATCHER.addURI(AUTHORITY_USER, null, CODE_USER);
		URI_MATCHER.addURI(AUTHORITY_CONVERSATION, null, CODE_CONVERSATION);
		URI_MATCHER.addURI(AUTHORITY_SEARCH, null, CODE_SEARCH);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Uri data = getIntent().getData();
		if (data != null) {
			Bundle bundle = null;
			switch (URI_MATCHER.match(data)) {
				case CODE_STATUS: {
					setTitle(R.string.view_status);
					mFragment = new ViewStatusFragment();
					String param_status_id = data.getQueryParameter(QUERY_PARAM_STATUS_ID);
					String param_account_id = data.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
					bundle = new Bundle();
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
					break;
				}
				case CODE_USER: {
					setTitle(R.string.view_user_profile);
					mFragment = new UserTimelineFragment();
					String param_screen_name = data.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					String param_account_id = data.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
					bundle = new Bundle();
					bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
					break;
				}
				case CODE_CONVERSATION: {
					setTitle(R.string.view_conversation);
					mFragment = new ViewConversationFragment();
					String param_status_id = data.getQueryParameter(QUERY_PARAM_STATUS_ID);
					String param_account_id = data.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
					bundle = new Bundle();
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
					break;
				}
				case CODE_SEARCH: {
					break;
				}
				default:
					finish();
					return;
			}
			mFragment.setArguments(bundle);
			ft.replace(android.R.id.content, mFragment);
			ft.commit();
		} else {
			finish();
		}
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

	private long parseLong(String source) {
		if (source == null) return -1;
		try {
			return Long.parseLong(source);
		} catch (NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}
}
