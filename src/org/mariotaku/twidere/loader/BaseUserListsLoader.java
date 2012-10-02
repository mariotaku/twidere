package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.model.ParcelableUserList;
import org.mariotaku.twidere.util.NoDuplicatesArrayList;

import twitter4j.CursorSupport;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class BaseUserListsLoader extends AsyncTaskLoader<List<ParcelableUserList>> {

	final NoDuplicatesArrayList<ParcelableUserList> mData = new NoDuplicatesArrayList<ParcelableUserList>();
	final Twitter mTwitter;
	private final long mAccountId, mCursor;

	private long mNextCursor, mPrevCursor;

	public BaseUserListsLoader(final Context context, final long account_id, final long cursor,
			final List<ParcelableUserList> data) {
		super(context);
		if (data != null) {
			mData.addAll(data);
		}
		mTwitter = getTwitterInstance(context, account_id, true);
		mCursor = cursor;
		mAccountId = account_id;
	}

	public long getCursor() {
		return mCursor;
	}

	public long getNextCursor() {
		return mNextCursor;
	}

	public long getPrevCursor() {
		return mPrevCursor;
	}

	public Twitter getTwitter() {
		return mTwitter;
	}

	public abstract List<UserList> getUserLists() throws TwitterException;;

	@Override
	public List<ParcelableUserList> loadInBackground() {
		List<UserList> list_loaded = null;
		try {
			list_loaded = getUserLists();
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (list_loaded != null) {
			final int list_size = list_loaded.size();
			if (list_loaded instanceof PagableResponseList) {
				mNextCursor = ((CursorSupport) list_loaded).getNextCursor();
				mPrevCursor = ((CursorSupport) list_loaded).getPreviousCursor();
				for (int i = 0; i < list_size; i++) {
					mData.add(new ParcelableUserList(list_loaded.get(i), mAccountId, (mCursor + 1) * 20 + i));
				}
			} else {
				for (int i = 0; i < list_size; i++) {
					mData.add(new ParcelableUserList(list_loaded.get(i), mAccountId, i));
				}
			}
		}
		Collections.sort(mData);
		return mData;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}
}