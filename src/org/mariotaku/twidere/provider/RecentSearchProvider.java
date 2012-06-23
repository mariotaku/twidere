package org.mariotaku.twidere.provider;

import android.content.SearchRecentSuggestionsProvider;

public class RecentSearchProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "org.mariotaku.twidere.provider.SearchRecentSuggestions";
	public final static int MODE = DATABASE_MODE_QUERIES;

	public RecentSearchProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}
}