package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import static org.mariotaku.twidere.util.Utils.makeCachedUserContentValues;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.model.SingleResponse;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;

public final class ParcelableUserLoader extends AsyncTaskLoader<SingleResponse<ParcelableUser>> implements Constants {

	private final Twitter twitter;
	private final ContentResolver resolver;
	private final boolean omit_intent_extra, hires_profile_image, load_from_cache;
	private final Bundle extras;
	private final long account_id, user_id;
	private final String screen_name;

	public ParcelableUserLoader(final Context context, final long account_id, final long user_id,
			final String screen_name, final Bundle extras, final boolean omit_intent_extra,
			final boolean load_from_cache) {
		super(context);
		resolver = context.getContentResolver();
		twitter = getTwitterInstance(context, account_id, true);
		hires_profile_image = context.getResources().getBoolean(R.bool.hires_profile_image);
		this.omit_intent_extra = omit_intent_extra;
		this.load_from_cache = load_from_cache;
		this.extras = extras;
		this.account_id = account_id;
		this.user_id = user_id;
		this.screen_name = screen_name;
	}

	@Override
	public SingleResponse<ParcelableUser> loadInBackground() {
		if (!omit_intent_extra && extras != null) {
			final ParcelableUser user = extras.getParcelable(INTENT_KEY_USER);
			if (user != null) {
				final ContentValues values = ParcelableUser.makeCachedUserContentValues(user);
				resolver.delete(CachedUsers.CONTENT_URI, CachedUsers.USER_ID + " = " + user.user_id, null);
				resolver.insert(CachedUsers.CONTENT_URI, values);
				return new SingleResponse<ParcelableUser>(user, null);
			}
		}
		if (twitter == null) return new SingleResponse<ParcelableUser>(null, null);
		if (load_from_cache) {
			final String where = CachedUsers.USER_ID + " = " + user_id + " OR " + CachedUsers.SCREEN_NAME + " = '"
					+ screen_name + "'";
			final Cursor cur = resolver.query(CachedUsers.CONTENT_URI, CachedUsers.COLUMNS, where, null, null);
			final int count = cur.getCount();
			try {
				if (count > 0) {
					cur.moveToFirst();
					return new SingleResponse<ParcelableUser>(new ParcelableUser(cur, account_id), null);
				}
			} finally {
				cur.close();
			}
		}
		try {
			final User user;
			if (user_id != -1) {
				user = twitter.showUser(user_id);
			} else if (screen_name != null) {
				user = twitter.showUser(screen_name);
			} else
				return new SingleResponse<ParcelableUser>(null, null);
			final ContentValues values = makeCachedUserContentValues(user, hires_profile_image);
			resolver.delete(CachedUsers.CONTENT_URI, CachedUsers.USER_ID + " = " + user.getId(), null);
			resolver.insert(CachedUsers.CONTENT_URI, values);
			return new SingleResponse<ParcelableUser>(new ParcelableUser(user, account_id, hires_profile_image), null);
		} catch (final TwitterException e) {
			return new SingleResponse<ParcelableUser>(null, e);
		}
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

}