package org.mariotaku.twidere.loader;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mariotaku.twidere.util.ParcelableStatus;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import android.content.Context;
import android.content.SharedPreferences;

public abstract class Twitter4JStatusLoader extends ParcelableStatusesLoader {

	private final long mMaxId;

	public static final Comparator<Status> TWITTER4J_STATUS_ID_COMPARATOR = new Comparator<Status>() {

		@Override
		public int compare(Status object1, Status object2) {
			final long diff = object2.getId() - object1.getId();
			if (diff > Integer.MAX_VALUE) return Integer.MAX_VALUE;
			if (diff < Integer.MIN_VALUE) return Integer.MIN_VALUE;
			return (int) diff;
		}
	};

	public Twitter4JStatusLoader(Context context, long account_id, long max_id, List<ParcelableStatus> data) {
		super(context, account_id, data);
		mMaxId = max_id;
	}

	public abstract ResponseList<Status> getStatuses(Paging paging) throws TwitterException;

	@Override
	public List<ParcelableStatus> loadInBackground() {
		final List<ParcelableStatus> data = getData();
		final long account_id = getAccountId();
		ResponseList<Status> statuses = null;
		try {
			final Paging paging = new Paging();
			final SharedPreferences prefs = getContext().getSharedPreferences(SHARED_PREFERENCES_NAME,
					Context.MODE_PRIVATE);
			final int load_item_limit = prefs
					.getInt(PREFERENCE_KEY_LOAD_ITEM_LIMIT, PREFERENCE_DEFAULT_LOAD_ITEM_LIMIT);
			paging.setCount(load_item_limit);
			if (mMaxId != -1) {
				paging.setMaxId(mMaxId);
			}
			statuses = getStatuses(paging);
		} catch (final TwitterException e) {
			e.printStackTrace();
		}
		if (statuses != null) {
			Collections.sort(statuses, TWITTER4J_STATUS_ID_COMPARATOR);
			boolean insert_gap = false;
			for (int i = 0; i < statuses.size(); i++) {
				final Status status = statuses.get(i);
				final boolean list_modified = deleteStatus(status.getId());
				if (!insert_gap) {
					insert_gap = list_modified;
				}
				// data.add(new ParcelableStatus(status, account_id, i ==
				// statuses.size() - 1 ? insert_gap : false));
				data.add(new ParcelableStatus(status, account_id, false));
			}
		}
		Collections.sort(data, ParcelableStatus.STATUS_ID_COMPARATOR);
		return data;
	}

}
