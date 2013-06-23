/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import java.util.List;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;

public class MultiSelectManager implements Constants {

	private final ItemsList mSelectedItems = new ItemsList();
	private final NoDuplicatesArrayList<Long> mSelectedStatusIds = new NoDuplicatesArrayList<Long>();
	private final NoDuplicatesArrayList<Long> mSelectedUserIds = new NoDuplicatesArrayList<Long>();
	private final NoDuplicatesArrayList<Callback> mCallbacks = new NoDuplicatesArrayList<Callback>();

	public void clearSelectedItems() {
		mSelectedItems.clear();
	}

	public int getCount() {
		return mSelectedItems.size();
	}

	public List<Object> getSelectedItems() {
		return mSelectedItems;
	}

	public boolean isActive() {
		return !mSelectedItems.isEmpty();
	}

	public boolean isSelected(final Object object) {
		return mSelectedItems.contains(object);
	}

	public boolean isStatusSelected(final long status_id) {
		return mSelectedStatusIds.contains(status_id);
	}

	public boolean isUserSelected(final long user_id) {
		return mSelectedUserIds.contains(user_id);
	}

	public void registerCallback(final Callback callback) {
		if (callback == null) return;
		mCallbacks.add(callback);
	}

	public boolean selectItem(final Object item) {
		return mSelectedItems.add(item);
	}

	public void unregisterCallback(final Callback callback) {
		mCallbacks.remove(callback);
	}

	public boolean unselectItem(final Object item) {
		return mSelectedItems.remove(item);
	}

	private void onItemsCleared() {
		for (final Callback callback : mCallbacks) {
			callback.onItemsCleared();
		}
	}

	private void onItemSelected(final Object object) {
		for (final Callback callback : mCallbacks) {
			callback.onItemSelected(object);
		}
	}

	private void onItemUnselected(final Object object) {
		for (final Callback callback : mCallbacks) {
			callback.onItemUnselected(object);
		}
	}

	public static interface Callback {

		public void onItemsCleared();

		public void onItemSelected(Object item);

		public void onItemUnselected(Object item);

	}

	@SuppressWarnings("serial")
	class ItemsList extends NoDuplicatesArrayList<Object> {

		@Override
		public boolean add(final Object object) {
			if (object instanceof ParcelableStatus) {
				mSelectedStatusIds.add(((ParcelableStatus) object).id);
			} else if (object instanceof ParcelableUser) {
				mSelectedUserIds.add(((ParcelableUser) object).id);
			} else
				return false;
			final boolean ret = super.add(object);
			onItemSelected(object);
			return ret;
		}

		@Override
		public void clear() {
			super.clear();
			mSelectedStatusIds.clear();
			mSelectedUserIds.clear();
			onItemsCleared();
		}

		@Override
		public boolean remove(final Object object) {
			final boolean ret = super.remove(object);
			if (object instanceof ParcelableStatus) {
				mSelectedStatusIds.remove(((ParcelableStatus) object).id);
			} else if (object instanceof ParcelableUser) {
				mSelectedUserIds.remove(((ParcelableUser) object).id);
			}
			if (ret) {
				if (isEmpty()) {
					onItemsCleared();
				} else {
					onItemUnselected(object);
				}
			}
			return ret;
		}

	}
}
