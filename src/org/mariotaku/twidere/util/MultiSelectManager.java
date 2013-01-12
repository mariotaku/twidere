package org.mariotaku.twidere.util;

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

	public NoDuplicatesLinkedList<Object> getSelectedItems() {
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
	class ItemsList extends NoDuplicatesLinkedList<Object> {

		@Override
		public boolean add(final Object object) {
			if (object instanceof ParcelableStatus) {
				mSelectedStatusIds.add(((ParcelableStatus) object).status_id);
			} else if (object instanceof ParcelableUser) {
				mSelectedUserIds.add(((ParcelableUser) object).user_id);
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
				mSelectedStatusIds.remove(((ParcelableStatus) object).status_id);
			} else if (object instanceof ParcelableUser) {
				mSelectedUserIds.remove(((ParcelableUser) object).user_id);
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
