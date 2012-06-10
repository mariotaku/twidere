package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.ArrayList;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.ParcelableStatus;

import twitter4j.Twitter;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class ParcelableStatusesLoader extends AsyncTaskLoader<List<ParcelableStatus>> implements Constants {

	private final Twitter mTwitter;
	private final long mAccountId;
	private final List<ParcelableStatus> mData;

	public ParcelableStatusesLoader(Context context, long account_id, List<ParcelableStatus> data) {
		super(context);
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
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

	public long getAccountId() {
		return mAccountId;
	}

	public List<ParcelableStatus> getData() {
		return mData;
	}

	public Twitter getTwitter() {
		return mTwitter;
	}

	@Override
	public abstract List<ParcelableStatus> loadInBackground();

	@Override
	public void onStartLoading() {
		forceLoad();
	}

}