package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.mariotaku.twidere.model.ParcelableUserList;

import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.UserList;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class BaseUserListsLoader extends AsyncTaskLoader<List<ParcelableUserList>> {

	final List<ParcelableUserList> mData = new ArrayList<ParcelableUserList>();
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

	public abstract PagableResponseList<UserList> getUserLists() throws TwitterException;;

	@Override
	public List<ParcelableUserList> loadInBackground() {
		PagableResponseList<UserList> list_loaded = null;
		try {
			list_loaded = getUserLists();
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (list_loaded != null) {
			mNextCursor = list_loaded.getNextCursor();
			mPrevCursor = list_loaded.getPreviousCursor();
			final int list_size = list_loaded.size();
			for (int i = 0; i < list_size; i++) {
				final UserList user = list_loaded.get(i);
				if (!hasId(user.getId())) {
					mData.add(new ParcelableUserList(user, mAccountId, (mCursor + 1) * 20 + i));
				}
			}
		}
		Collections.sort(mData, ParcelableUserList.POSITION_COMPARATOR);
		return mData;
	}

	@Override
	public void onStartLoading() {
		forceLoad();
	}

	private boolean hasId(final int id) {
		for (final ParcelableUserList user : mData) {
			if (user.list_id == id) return true;
		}
		return false;
	}
}
