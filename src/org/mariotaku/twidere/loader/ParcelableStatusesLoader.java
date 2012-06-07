package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;

public abstract class ParcelableStatusesLoader extends AsyncTaskLoader<List<ParcelableStatus>> implements Constants {

	private final Twitter mTwitter;
	private final long mAccountId, mMaxId;
	private final List<ParcelableStatus> mData;

	public static final Comparator<ParcelableStatus> TIMESTAMP_COMPARATOR = new Comparator<ParcelableStatus>() {

		@Override
		public int compare(ParcelableStatus object1, ParcelableStatus object2) {
			long diff = object2.status_timestamp - object1.status_timestamp;
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public ParcelableStatusesLoader(Context context, long account_id, long max_id,
			List<ParcelableStatus> data) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
		mMaxId = max_id;
		mData = data != null ? data : new ArrayList<ParcelableStatus>();
	}

	public boolean containsStatus(long status_id) {
		for (ParcelableStatus status : mData) {
			if (status.status_id == status_id) return true;
		}
		return false;
	}

	public boolean deleteStatus(long status_id) {
		for (ParcelableStatus status : mData) {
			if (status.status_id == status_id) return mData.remove(status);
		}
		return false;
	}

	@Override
	public List<ParcelableStatus> loadInBackground() {
		ResponseList<Status> statuses = null;
		try {
			Paging paging = new Paging();
			final SharedPreferences prefs = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			final int load_item_limit = prefs
					.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT) + 1;
			paging.setCount(load_item_limit);
			if (mMaxId != -1) {
				paging.setMaxId(mMaxId);
			}
				statuses = getStatuses(paging);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if (statuses != null) {
			for (Status status : statuses) {
				deleteStatus(status.getId());
				mData.add(new ParcelableStatus(status, mAccountId));
			}
		}
		Collections.sort(mData, TIMESTAMP_COMPARATOR);
		return mData;
	}

	public abstract ResponseList<Status> getStatuses(Paging paging) throws TwitterException;
	
	public Twitter getTwitter() {
		return mTwitter;
	}
	
	@Override
	public void onStartLoading() {
		forceLoad();
	}

}