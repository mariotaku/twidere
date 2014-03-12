package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import org.mariotaku.twidere.Constants;

import java.util.List;

public class TwitterLinkHandlerActivity extends Activity implements Constants {

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	private static final String AUTHORITY_TWITTER_COM = "twitter.com";

	private static final int URI_CODE_TWITTER_STATUS = 1;
	private static final int URI_CODE_TWITTER_USER = 2;
	private static final int URI_CODE_TWITTER_USER_FOLLOWING = 11;
	private static final int URI_CODE_TWITTER_USER_FOLLOWERS = 12;
	private static final int URI_CODE_TWITTER_USER_FAVORITES = 13;
	private static final int URI_CODE_TWITTER_REDIRECT = 101;

	static {
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/status/#", URI_CODE_TWITTER_STATUS);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/status/#/photo/#", URI_CODE_TWITTER_STATUS);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*", URI_CODE_TWITTER_USER);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/following", URI_CODE_TWITTER_USER_FOLLOWING);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/followers", URI_CODE_TWITTER_USER_FOLLOWERS);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/favorites", URI_CODE_TWITTER_USER_FAVORITES);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/i/redirect", URI_CODE_TWITTER_REDIRECT);
	}

	private SharedPreferences mPreferences;
	private PackageManager mPackageManager;

	@Override
	protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		switch (requestCode) {
			case REQUEST_PICK_ACTIVITY: {
				if (resultCode != RESULT_OK || data == null || !data.hasExtra(EXTRA_DATA)
						|| !data.hasExtra(EXTRA_INTENT)) {
					finish();
					return;
				}
				final ResolveInfo resolveInfo = data.getParcelableExtra(EXTRA_DATA);
				final Intent extraIntent = data.getParcelableExtra(EXTRA_INTENT);
				final ActivityInfo activityInfo = resolveInfo.activityInfo;
				if (activityInfo == null) {
					finish();
					return;
				}
				final SharedPreferences.Editor editor = mPreferences.edit();
				editor.putString(KEY_FALLBACK_TWITTER_LINK_HANDLER, activityInfo.packageName);
				editor.apply();
				final Intent intent = new Intent(Intent.ACTION_VIEW, extraIntent.getData());
				intent.setClassName(activityInfo.packageName, activityInfo.name);
				startActivity(intent);
				finish();
				return;
			}
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPackageManager = getPackageManager();
		mPreferences = getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		if (data == null) {
			finish();
			return;
		}
		final Uri uri = data.buildUpon().authority(AUTHORITY_TWITTER_COM).build();
		final Intent handledIntent;
		final List<String> pathSegments = uri.getPathSegments();
		switch (URI_MATCHER.match(uri)) {
			case URI_CODE_TWITTER_STATUS: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_STATUS);
				builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, pathSegments.get(2));
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			case URI_CODE_TWITTER_USER: {
				final String pathSegment = pathSegments.get(0);
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				if ("following".equals(pathSegment)) {
					builder.authority(AUTHORITY_USER_FRIENDS);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else if ("followers".equals(pathSegment)) {
					builder.authority(AUTHORITY_USER_FOLLOWERS);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else if ("favorites".equals(pathSegment)) {
					builder.authority(AUTHORITY_USER_FAVORITES);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else {
					builder.authority(AUTHORITY_USER);
					builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegment);
				}
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			case URI_CODE_TWITTER_USER_FOLLOWING: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FRIENDS);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(0));
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			case URI_CODE_TWITTER_USER_FOLLOWERS: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FOLLOWERS);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(0));
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			case URI_CODE_TWITTER_USER_FAVORITES: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FAVORITES);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(0));
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			default: {
				handledIntent = null;
				break;
			}
		}
		if (handledIntent != null) {
			startActivity(handledIntent);
		} else {
			final String packageName = mPreferences.getString(KEY_FALLBACK_TWITTER_LINK_HANDLER, null);
			final Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, uri);
			fallbackIntent.setPackage(packageName);
			if (TextUtils.isEmpty(packageName) || mPackageManager.queryIntentActivities(fallbackIntent, 0).isEmpty()) {
				final Intent pickIntent = new Intent(INTENT_ACTION_PICK_ACTIVITY);
				pickIntent.putExtra(EXTRA_INTENT, new Intent(Intent.ACTION_VIEW, uri));
				pickIntent.putExtra(EXTRA_BLACKLIST, new String[] { getPackageName() });
				startActivityForResult(pickIntent, REQUEST_PICK_ACTIVITY);
				return;
			} else {
				startActivity(fallbackIntent);
			}
		}
		finish();
	}

}
