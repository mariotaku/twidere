/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.preference;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.provider.RecentSearchProvider;
import org.mariotaku.twidere.provider.TweetStore.CachedHashtags;
import org.mariotaku.twidere.provider.TweetStore.CachedStatuses;
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
			resolver.delete(DirectMessages.Inbox.CONTENT_URI, null, null);
			resolver.delete(DirectMessages.Outbox.CONTENT_URI, null, null);
			resolver.delete(CachedHashtags.CONTENT_URI, null, null);
			resolver.delete(CachedStatuses.CONTENT_URI, null, null);
			resolver.delete(CachedUsers.CONTENT_URI, null, null);
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
