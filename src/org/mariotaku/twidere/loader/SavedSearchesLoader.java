package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;
import twitter4j.ResponseList;
import twitter4j.SavedSearch;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class SavedSearchesLoader extends AsyncTaskLoader<ResponseList<SavedSearch>> {

	private final Twitter twitter;
	
	public SavedSearchesLoader(Context context, long account_id) {
		super(context);
		twitter = getTwitterInstance(context, account_id, false);
	}

	@Override
	public ResponseList<SavedSearch> loadInBackground() {
		if (twitter == null) return null;
		try {
			return twitter.getSavedSearches();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

}
