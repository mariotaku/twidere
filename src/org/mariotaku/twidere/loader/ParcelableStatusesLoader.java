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

package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getTwitterInstance;

import java.util.ConcurrentModificationException;
import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.util.NoDuplicatesLinkedList;

import twitter4j.Twitter;
import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class ParcelableStatusesLoader extends AsyncTaskLoader<List<ParcelableStatus>> implements Constants {

	private final Twitter mTwitter;
	private final long mAccountId;
	private final String mClassName;
	private final List<ParcelableStatus> mData;
	private final boolean mFirstLoad, mIsHomeTab;

	public ParcelableStatusesLoader(Context context, long account_id, List<ParcelableStatus> data, String class_name,
			boolean is_home_tab) {
		super(context);
		mClassName = class_name;
		mTwitter = getTwitterInstance(context, account_id, true);
		mAccountId = account_id;
		mFirstLoad = data == null;
		mData = data != null ? data : new NoDuplicatesLinkedList<ParcelableStatus>();
		mIsHomeTab = is_home_tab;
	}

	protected boolean containsStatus(long status_id) {
		for (final ParcelableStatus status : mData) {
			if (status.status_id == status_id) return true;
		}
		return false;
	}

	protected synchronized boolean deleteStatus(long status_id) {
		try {
			final NoDuplicatesLinkedList<ParcelableStatus> data_to_remove = new NoDuplicatesLinkedList<ParcelableStatus>();
			for (final ParcelableStatus status : mData) {
				if (status.status_id == status_id) {
					data_to_remove.add(status);
				}
			}
			return mData.removeAll(data_to_remove);
		} catch (final ConcurrentModificationException e) {
			// This shouldn't happen.
		}
		return false;
	}

	protected long getAccountId() {
		return mAccountId;
	}

	protected String getClassName() {
		return mClassName;
	}

	protected List<ParcelableStatus> getData() {
		return mData;
	}

	protected Twitter getTwitter() {
		return mTwitter;
	}

	protected boolean isFirstLoad() {
		return mFirstLoad;
	}

	protected boolean isHomeTab() {
		return mIsHomeTab;
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

}
