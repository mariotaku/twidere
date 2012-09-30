package org.mariotaku.twidere.preference;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.RecentSearchProvider;
import org.mariotaku.twidere.provider.TweetStore.CachedTrends;
import org.mariotaku.twidere.provider.TweetStore.CachedUsers;
import org.mariotaku.twidere.provider.TweetStore.DirectMessages;
import org.mariotaku.twidere.provider.TweetStore.Mentions;
import org.mariotaku.twidere.provider.TweetStore.Statuses;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.SearchRecentSuggestions;
import android.util.AttributeSet;

public class ClearDatabasesPreference extends Preference implements Constants, OnPreferenceClickListener {

	private ClearCacheTask mClearCacheTask;

	public ClearDatabasesPreference(final Context context) {
		this(context, null);
	}

	public ClearDatabasesPreference(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.preferenceStyle);
	}

	public ClearDatabasesPreference(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		setOnPreferenceClickListener(this);
	}

	@Override
	public boolean onPreferenceClick(final Preference preference) {
		if (mClearCacheTask == null || mClearCacheTask.getStatus() != Status.RUNNING) {
			mClearCacheTask = new ClearCacheTask(getContext());
			mClearCacheTask.execute();
		}
		return true;
	}

	static class ClearCacheTask extends AsyncTask<Void, Void, Void> {

		private final Context context;
		private final ProgressDialog mProgress;

		public ClearCacheTask(final Context context) {
			this.context = context;
			mProgress = new ProgressDialog(context);
		}

		@Override
		protected Void doInBackground(final Void... args) {
			if (context == null) return null;
			final ContentResolver resolver = context.getContentResolver();
			final SearchRecentSuggestions suggestions = new SearchRecentSuggestions(context,
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
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			super.onPostExecute(result);
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			if (mProgress != null && mProgress.isShowing()) {
				mProgress.dismiss();
			}
			mProgress.setMessage(context.getString(R.string.please_wait));
			mProgress.setCancelable(false);
			mProgress.show();
			super.onPreExecute();
		}

	}

}
