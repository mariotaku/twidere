package org.mariotaku.twidere.activity;

import static org.mariotaku.twidere.util.Utils.getDefaultAccountId;

import android.app.Activity;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.activity.support.ActivityPickerActivity;

import java.util.List;

public class TwitterLinkHandlerActivity extends Activity implements Constants {

	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

	private static final String AUTHORITY_TWITTER_COM = "twitter.com";

	private static final int URI_CODE_TWITTER_STATUS = 1;
	private static final int URI_CODE_TWITTER_USER = 2;
	private static final int URI_CODE_TWITTER_USER_FOLLOWING = 11;
	private static final int URI_CODE_TWITTER_USER_FOLLOWERS = 12;
	private static final int URI_CODE_TWITTER_USER_FAVORITES = 13;

	static {
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/status/#", URI_CODE_TWITTER_STATUS);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*", URI_CODE_TWITTER_USER);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/following", URI_CODE_TWITTER_USER_FOLLOWING);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/followers", URI_CODE_TWITTER_USER_FOLLOWERS);
		URI_MATCHER.addURI(AUTHORITY_TWITTER_COM, "/*/favorites", URI_CODE_TWITTER_USER_FAVORITES);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		final Uri data = intent.getData();
		if (data == null) {
			finish();
			return;
		}
		final Uri uri = data.buildUpon().scheme(AUTHORITY_TWITTER_COM).build();
		final Intent handledIntent;
		switch (URI_MATCHER.match(uri)) {
			case URI_CODE_TWITTER_STATUS: {
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_STATUS);
				builder.appendQueryParameter(QUERY_PARAM_STATUS_ID, uri.getLastPathSegment());
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			case URI_CODE_TWITTER_USER: {
				final String lastPathSegment = uri.getLastPathSegment();
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				if ("following".equals(lastPathSegment)) {
					builder.authority(AUTHORITY_USER_FRIENDS);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else if ("followers".equals(lastPathSegment)) {
					builder.authority(AUTHORITY_USER_FOLLOWERS);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else if ("favorites".equals(lastPathSegment)) {
					builder.authority(AUTHORITY_USER_FAVORITES);
					builder.appendQueryParameter(QUERY_PARAM_USER_ID, String.valueOf(getDefaultAccountId(this)));
				} else {
					builder.authority(AUTHORITY_USER);
					builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, uri.getLastPathSegment());
				}
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			case URI_CODE_TWITTER_USER_FOLLOWING: {
				final List<String> pathSegments = uri.getPathSegments();
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FRIENDS);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(pathSegments.size() - 2));
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			case URI_CODE_TWITTER_USER_FOLLOWERS: {
				final List<String> pathSegments = uri.getPathSegments();
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FOLLOWERS);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(pathSegments.size() - 2));
				handledIntent = new Intent(Intent.ACTION_VIEW, builder.build());
				break;
			}
			case URI_CODE_TWITTER_USER_FAVORITES: {
				final List<String> pathSegments = uri.getPathSegments();
				final Uri.Builder builder = new Uri.Builder();
				builder.scheme(SCHEME_TWIDERE);
				builder.authority(AUTHORITY_USER_FAVORITES);
				builder.appendQueryParameter(QUERY_PARAM_SCREEN_NAME, pathSegments.get(pathSegments.size() - 2));
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
			final Intent pickIntent = new Intent(INTENT_ACTION_PICK_ACTIVITY);
			pickIntent.setClass(this, ActivityPickerActivity.class);
			final Intent extraIntent = new Intent(Intent.ACTION_VIEW, data);
			final String[] blacklist = { getPackageName() };
			pickIntent.putExtra(EXTRA_INTENT, extraIntent);
			pickIntent.putExtra(EXTRA_BLACKLIST, blacklist);
			startActivity(pickIntent);
		}
		finish();
	}

}
