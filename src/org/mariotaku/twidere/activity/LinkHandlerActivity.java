package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.BaseFragment;
import org.mariotaku.twidere.fragment.DraftsFragment;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserProfileFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.fragment.ViewConversationFragment;
import org.mariotaku.twidere.fragment.ViewStatusFragment;

import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MenuItem;
import android.view.Window;
import android.widget.FrameLayout;

public class LinkHandlerActivity extends BaseActivity {

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
	private static final int CODE_STATUS = 1;
	private static final int CODE_USER = 2;
	private static final int CODE_USER_TIMELINE = 3;
	private static final int CODE_USER_FAVORITES = 4;
	private static final int CODE_CONVERSATION = 5;
	private static final int CODE_SEARCH = 6;
	private static final int CODE_DRAFTS = 7;

	static {
		URI_MATCHER.addURI(AUTHORITY_STATUS, null, CODE_STATUS);
		URI_MATCHER.addURI(AUTHORITY_USER, null, CODE_USER);
		URI_MATCHER.addURI(AUTHORITY_USER_TIMELINE, null, CODE_USER_TIMELINE);
		URI_MATCHER.addURI(AUTHORITY_USER_FAVORITES, null, CODE_USER_FAVORITES);
		URI_MATCHER.addURI(AUTHORITY_CONVERSATION, null, CODE_CONVERSATION);
		URI_MATCHER.addURI(AUTHORITY_SEARCH, null, CODE_SEARCH);
		URI_MATCHER.addURI(AUTHORITY_DRAFTS, null, CODE_DRAFTS);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Uri data = getIntent().getData();
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(new FrameLayout(this));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
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
				onBackPressed();
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
					final Bundle extras = getIntent().getExtras();
					fragment = new ViewStatusFragment();
					final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					bundle = extras != null ? new Bundle(extras) : new Bundle();
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					break;
				}
				case CODE_USER: {
					fragment = new UserProfileFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					bundle = new Bundle();
					if (!isNullOrEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isNullOrEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case CODE_USER_TIMELINE: {
					fragment = new UserTimelineFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					bundle = new Bundle();
					if (!isNullOrEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isNullOrEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case CODE_USER_FAVORITES: {
					fragment = new UserFavoritesFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					bundle = new Bundle();
					if (!isNullOrEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isNullOrEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case CODE_CONVERSATION: {
					fragment = new ViewConversationFragment();
					final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					bundle = new Bundle();
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					break;
				}
				case CODE_SEARCH: {
					final String type = uri.getQueryParameter(QUERY_PARAM_TYPE);
					if (QUERY_PARAM_VALUE_TWEETS.equals(type)) {
						fragment = new SearchTweetsFragment();
					} else if (QUERY_PARAM_VALUE_USERS.equals(type)) {
						fragment = new SearchUsersFragment();
					}
					final String query = uri.getQueryParameter(QUERY_PARAM_QUERY);
					bundle = new Bundle();
					bundle.putString(INTENT_KEY_QUERY, query);
					break;
				}
				case CODE_DRAFTS: {
					fragment = new DraftsFragment();
				}
				default:
			}

			if (bundle != null) {
				final String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
				if (param_account_id != null) {
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
				} else {
					final String param_account_name = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_NAME);
					if (param_account_name != null) {
						bundle.putLong(INTENT_KEY_ACCOUNT_ID, getAccountId(this, param_account_name));
					}
				}
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
			case CODE_USER_TIMELINE: {
				setTitle(R.string.tweets);
				break;
			}
			case CODE_USER_FAVORITES: {
				setTitle(R.string.favorites);
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
}
