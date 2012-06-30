package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getAccountId;
import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;
import static org.mariotaku.twidere.util.Utils.isMyAccount;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;

import org.mariotaku.twidere.R;
import org.mariotaku.twidere.fragment.WebMapFragment;
import org.mariotaku.twidere.fragment.SearchTweetsFragment;
import org.mariotaku.twidere.fragment.SearchUsersFragment;
import org.mariotaku.twidere.fragment.UserBlocksFragment;
import org.mariotaku.twidere.fragment.UserFavoritesFragment;
import org.mariotaku.twidere.fragment.UserFollowersFragment;
import org.mariotaku.twidere.fragment.UserFriendsFragment;
import org.mariotaku.twidere.fragment.UserProfileFragment;
import org.mariotaku.twidere.fragment.UserTimelineFragment;
import org.mariotaku.twidere.fragment.ViewConversationFragment;
import org.mariotaku.twidere.fragment.ViewStatusFragment;
import org.mariotaku.twidere.provider.RecentSearchProvider;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.DialogFragment;
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
	private static final int CODE_USER_FOLLOWERS = 5;
	private static final int CODE_USER_FOLLOWING = 6;
	private static final int CODE_USER_BLOCKS = 7;
	private static final int CODE_CONVERSATION = 8;
	private static final int CODE_SEARCH = 9;
	private static final int CODE_MAP = 10;

	static {
		URI_MATCHER.addURI(AUTHORITY_STATUS, null, CODE_STATUS);
		URI_MATCHER.addURI(AUTHORITY_USER, null, CODE_USER);
		URI_MATCHER.addURI(AUTHORITY_USER_TIMELINE, null, CODE_USER_TIMELINE);
		URI_MATCHER.addURI(AUTHORITY_USER_FOLLOWERS, null, CODE_USER_FOLLOWERS);
		URI_MATCHER.addURI(AUTHORITY_USER_FOLLOWING, null, CODE_USER_FOLLOWING);
		URI_MATCHER.addURI(AUTHORITY_USER_FAVORITES, null, CODE_USER_FAVORITES);
		URI_MATCHER.addURI(AUTHORITY_USER_BLOCKS, null, CODE_USER_BLOCKS);
		URI_MATCHER.addURI(AUTHORITY_CONVERSATION, null, CODE_CONVERSATION);
		URI_MATCHER.addURI(AUTHORITY_SEARCH, null, CODE_SEARCH);
		URI_MATCHER.addURI(AUTHORITY_MAP, null, CODE_MAP);
	}

	private Fragment mFragment;
	private String mSearchType;

	private final DialogFragment mSearchTypeFragment = new SearchTypeFragment();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestSupportWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		setContentView(new FrameLayout(this));
		setSupportProgressBarIndeterminateVisibility(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState != null) {
			mSearchType = savedInstanceState.getString(INTENT_KEY_QUERY_TYPE);
		}
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		final String action = intent.getAction();
		if (Intent.ACTION_SEARCH.equals(action)) {
			final long account_id = getDefaultAccountId(this);
			if (isMyAccount(this, account_id)) {
				final String query = intent.getStringExtra(SearchManager.QUERY);
				final Bundle args = new Bundle();
				args.putString(INTENT_KEY_QUERY, query);
				args.putLong(INTENT_KEY_ACCOUNT_ID, account_id);

				if (QUERY_PARAM_VALUE_TWEETS.equals(mSearchType)) {
					setTitle(getString(R.string.search_tweets) + " | " + query);
					mFragment = new SearchTweetsFragment();
				} else if (QUERY_PARAM_VALUE_USERS.equals(mSearchType)) {
					setTitle(getString(R.string.search_users) + " | " + query);
					mFragment = new SearchUsersFragment();
				} else {
					final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
							RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
					suggestions.saveRecentQuery(query, null);
					mSearchTypeFragment.setArguments(args);
					// if (mSearchTypeFragment.isHidden()) {
					mSearchTypeFragment.show(getSupportFragmentManager(), null);
					// }
					setTitle(getString(android.R.string.search_go) + " | " + query);
					return;
				}
				mFragment.setArguments(args);
				final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
				ft.replace(android.R.id.content, mFragment);
				ft.commit();
			}
		} else if (data != null) {
			if (setFragment(data)) {
				if (mFragment != null) {
					final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
					ft.replace(android.R.id.content, mFragment);
					ft.commit();
					return;
				} else {
					finish();
				}
			}
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

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(INTENT_KEY_QUERY_TYPE, mSearchType);
		super.onSaveInstanceState(outState);
	}

	private long parseLong(String source) {
		if (source == null) return -1;
		try {
			return Long.parseLong(source);
		} catch (final NumberFormatException e) {
			// Wrong number format? Ignore them.
		}
		return -1;
	}

	private boolean setFragment(Uri uri) {
		final Bundle extras = getIntent().getExtras();
		Fragment fragment = null;
		if (uri != null) {
			final Bundle bundle = new Bundle();
			switch (URI_MATCHER.match(uri)) {
				case CODE_STATUS: {
					setTitle(R.string.view_status);
					fragment = new ViewStatusFragment();
					final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					if (extras != null) {
						bundle.putAll(extras);
					}
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					break;
				}
				case CODE_USER: {
					setTitle(R.string.view_user_profile);
					fragment = new UserProfileFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isNullOrEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isNullOrEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case CODE_USER_TIMELINE: {
					setTitle(R.string.tweets);
					fragment = new UserTimelineFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isNullOrEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isNullOrEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case CODE_USER_FAVORITES: {
					setTitle(R.string.favorites);
					fragment = new UserFavoritesFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isNullOrEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isNullOrEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case CODE_USER_FOLLOWERS: {
					setTitle(R.string.followers);
					fragment = new UserFollowersFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isNullOrEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isNullOrEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case CODE_USER_FOLLOWING: {
					setTitle(R.string.following);
					fragment = new UserFriendsFragment();
					final String param_screen_name = uri.getQueryParameter(QUERY_PARAM_SCREEN_NAME);
					final String param_user_id = uri.getQueryParameter(QUERY_PARAM_USER_ID);
					if (!isNullOrEmpty(param_screen_name)) {
						bundle.putString(INTENT_KEY_SCREEN_NAME, param_screen_name);
					}
					if (!isNullOrEmpty(param_user_id)) {
						bundle.putLong(INTENT_KEY_USER_ID, parseLong(param_user_id));
					}
					break;
				}
				case CODE_USER_BLOCKS: {
					setTitle(R.string.blocked_users);
					fragment = new UserBlocksFragment();
					break;
				}
				case CODE_CONVERSATION: {
					setTitle(R.string.view_conversation);
					fragment = new ViewConversationFragment();
					final String param_status_id = uri.getQueryParameter(QUERY_PARAM_STATUS_ID);
					bundle.putLong(INTENT_KEY_STATUS_ID, parseLong(param_status_id));
					break;
				}
				case CODE_SEARCH: {
					if (mSearchType == null) {
						mSearchType = uri.getQueryParameter(QUERY_PARAM_TYPE);
					}
					final String query = uri.getQueryParameter(QUERY_PARAM_QUERY);
					if (query == null) {
						finish();
						return false;
					}
					bundle.putString(INTENT_KEY_QUERY, query);
					final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
							RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
					suggestions.saveRecentQuery(query, null);
					if (QUERY_PARAM_VALUE_TWEETS.equals(mSearchType)) {
						setTitle(getString(R.string.search_tweets) + " | " + query);
						fragment = new SearchTweetsFragment();
					} else if (QUERY_PARAM_VALUE_USERS.equals(mSearchType)) {
						setTitle(getString(R.string.search_users) + " | " + query);
						fragment = new SearchUsersFragment();
					} else {
						setTitle(getString(android.R.string.search_go) + " | " + query);
						mSearchTypeFragment.setArguments(bundle);
						mSearchTypeFragment.show(getSupportFragmentManager(), null);
						return false;
					}
					break;
				}
				case CODE_MAP: {
					setTitle(R.string.view_map);
					final String param_lat = uri.getQueryParameter(QUERY_PARAM_LAT);
					final String param_lng = uri.getQueryParameter(QUERY_PARAM_LNG);
					if (param_lat == null || param_lng == null) {
						finish();
						return false;
					}
					try {
						bundle.putDouble(INTENT_KEY_LATITUDE, Double.valueOf(param_lat));
						bundle.putDouble(INTENT_KEY_LONGITUDE, Double.valueOf(param_lng));
					} catch (final NumberFormatException e) {
						finish();
						return false;
					}
					fragment = new WebMapFragment();
					fragment.setArguments(bundle);
					mFragment = fragment;
					return true;
				}
				default: {
					break;
				}
			}
			final String param_account_id = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_ID);
			if (param_account_id != null) {
				bundle.putLong(INTENT_KEY_ACCOUNT_ID, parseLong(param_account_id));
			} else {
				final String param_account_name = uri.getQueryParameter(QUERY_PARAM_ACCOUNT_NAME);
				if (param_account_name != null) {
					bundle.putLong(INTENT_KEY_ACCOUNT_ID, getAccountId(this, param_account_name));
				} else {
					final long account_id = getDefaultAccountId(this);
					if (isMyAccount(this, account_id)) {
						bundle.putLong(INTENT_KEY_ACCOUNT_ID, account_id);
					} else {
						finish();
						return false;
					}
				}
			}
			if (fragment != null) {
				fragment.setArguments(bundle);
			}
		}
		mFragment = fragment;
		return true;
	}

	public static class SearchTypeFragment extends DialogFragment {

		private static final int ITEM_TYPE_TWEETS = 0;
		private static final int ITEM_TYPE_USERS = 1;
		private LinkHandlerActivity mActivity;

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			mActivity = (LinkHandlerActivity) getActivity();
			super.onActivityCreated(savedInstanceState);
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			mActivity.finish();
			super.onCancel(dialog);
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final CharSequence[] items = new CharSequence[] { getString(R.string.search_tweets),
					getString(R.string.search_users) };
			final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			final Bundle args = new Bundle(getArguments());
			final String query = args.getString(INTENT_KEY_QUERY);
			builder.setTitle(getString(android.R.string.search_go) + " " + query);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int item) {

					Fragment fragment = null;
					switch (item) {
						case ITEM_TYPE_TWEETS: {
							getActivity().setTitle(getString(R.string.search_tweets) + " | " + query);
							fragment = new SearchTweetsFragment();
							mActivity.mSearchType = QUERY_PARAM_VALUE_TWEETS;
							break;
						}
						case ITEM_TYPE_USERS: {
							getActivity().setTitle(getString(R.string.search_users) + " | " + query);
							fragment = new SearchUsersFragment();
							mActivity.mSearchType = QUERY_PARAM_VALUE_USERS;
							break;
						}
					}
					if (fragment != null) {
						mActivity.mFragment = fragment;
						fragment.setArguments(args);
						final FragmentTransaction ft = getFragmentManager().beginTransaction();
						ft.replace(android.R.id.content, fragment);
						ft.commit();
					}
				}
			});
			return builder.create();
		}

	}
}