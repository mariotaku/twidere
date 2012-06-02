package org.mariotaku.twidere.activity;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseFragment;
import org.mariotaku.twidere.fragment.DraftsFragment;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.fragment.UserProfileFragment;
import org.mariotaku.twidere.fragment.ViewConversationFragment;
import org.mariotaku.twidere.fragment.ViewStatusFragment;

import android.content.UriMatcher;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.Window;

public class LinkHandlerActivity extends BaseActivity {

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int CODE_STATUS = 1;
	private static final int CODE_USER = 2;
	private static final int CODE_CONVERSATION = 3;
	private static final int CODE_SEARCH = 4;
	private static final int CODE_DRAFTS = 5;

	static {
		URI_MATCHER.addURI(AUTHORITY_STATUS, null, CODE_STATUS);
		URI_MATCHER.addURI(AUTHORITY_USER, null, CODE_USER);
		URI_MATCHER.addURI(AUTHORITY_CONVERSATION, null, CODE_CONVERSATION);
		URI_MATCHER.addURI(AUTHORITY_SEARCH, null, CODE_SEARCH);
		URI_MATCHER.addURI(AUTHORITY_DRAFTS, null, CODE_DRAFTS);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Uri data = getIntent().getData();
		setWindowFeatureAndUiOptions(data);
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		if (data != null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			setTitle(data);
			ft.replace(android.R.id.content, getFragment(data));
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

	private Fragment getFragment(Uri uri) {
		Bundle bundle = null;
		Fragment fragment = new BaseFragment();
		if (uri != null) {
			switch (URI_MATCHER.match(uri)) {
				case CODE_STATUS: {
					Bundle extras = getIntent().getExtras();
					fragment = new ViewStatusFragment();
					String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
					bundle = extras != null ? new Bundle(extras) : new Bundle();
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
					break;
				}
				case CODE_USER: {
					fragment = new UserProfileFragment();
					String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
					bundle = new Bundle();
					bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
					break;
				}
				case CODE_CONVERSATION: {
					fragment = new ViewConversationFragment();
					String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
					bundle = new Bundle();
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
					break;
				}
				case CODE_SEARCH: {
					String type = uri.getQueryParameter(QUERY_PARAM_TYPE);
					if (QUERY_PARAM_VALUE_TWEETS.equals(type)) {
						fragment = new SearchTweetsFragment();
					} else if (QUERY_PARAM_VALUE_USERS.equals(type)) {
						fragment = new SearchUsersFragment();
					}
					String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
					String query = uri.getQueryParameter(QUERY_PARAM_QUERY);
					bundle = new Bundle();
					bundle.putString(INTENT_KEY_QUERY, query);
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
					break;
				}
				case CODE_DRAFTS: {
					fragment = new DraftsFragment();
				}
				default:
			}
			fragment.setArguments(bundle);
		}
		return fragment;
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

	private void setTitle(Uri uri) {
		if (uri == null) return;
		switch (URI_MATCHER.match(uri)) {
			case CODE_STATUS: {
				setTitle(R.string.view_status);
				break;
			}
			case CODE_USER: {
				setTitle(R.string.view_user_profile);
				break;
			}
			case CODE_CONVERSATION: {
				setTitle(R.string.view_conversation);
				break;
			}
			case CODE_SEARCH: {
				setTitle(R.string.search);
				break;
			}
			case CODE_DRAFTS: {
				setTitle(R.string.drafts);
				break;
			}
			default:
		}
	}

	private void setWindowFeatureAndUiOptions(Uri uri) {
		if (uri == null) return;
		switch (URI_MATCHER.match(uri)) {
			case CODE_STATUS: {
				getWindow().setUiOptions(ActivityInfo.UIOPTION_SPLIT_ACTION_BAR_WHEN_NARROW);
				break;
			}
			case CODE_USER: {
				requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
				break;
			}
			case CODE_CONVERSATION: {
				requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
				break;
			}
			case CODE_SEARCH: {
				requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
				break;
			}
			case CODE_DRAFTS: {
				break;
			}
			default:
		}
	}
}
