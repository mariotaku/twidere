package org.mariotaku.twidere.util;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.provider.RecentSearchProvider;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.SearchRecentSuggestions;

public class OnPreferenceClickHandler implements OnPreferenceClickListener, Constants {

	private final Activity activity;

	public OnPreferenceClickHandler(Activity activity) {
		this.activity = activity;
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		if (PREFERENCE_KEY_CLEAR_DATABASES.equals(preference.getKey())) {
			final ContentResolver resolver = activity.getContentResolver();
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(activity,
					RecentSearchProvider.AUTHORITY, RecentSearchProvider.MODE);
			resolver.delete(Statuses.CONTENT_URI, null, null);
			resolver.delete(Mentions.CONTENT_URI, null, null);
			resolver.delete(CachedUsers.CONTENT_URI, null, null);
			resolver.delete(DirectMessages.Inbox.CONTENT_URI, null, null);
			resolver.delete(DirectMessages.Outbox.CONTENT_URI, null, null);
			resolver.delete(CachedTrends.Daily.CONTENT_URI, null, null);
			resolver.delete(CachedTrends.Weekly.CONTENT_URI, null, null);
			resolver.delete(CachedTrends.Local.CONTENT_URI, null, null);
			suggestions.clearHistory();
		} else if (PREFERENCE_KEY_CLEAR_CACHE.equals(preference.getKey())) {
			final Application app = activity.getApplication();
			if (app instanceof TwidereApplication) {
				((TwidereApplication) app).clearCache();
			}
		}
		return true;
	}

}
