package org.mariotaku.twidere.util;

import android.content.Intent;
import java.util.ArrayList;
import org.mariotaku.twidere.model.ParcelableStatus;
import org.mariotaku.twidere.model.ParcelableUser;
import org.mariotaku.twidere.Constants;
import java.lang.ref.WeakReference;

public class MultiSelectManager implements Constants {

	private final ItemsList mSelectedItems = new ItemsList();
	private final ArrayList<Long> mSelectedStatusIds = new ArrayList<Long>();
	private final ArrayList<Long> mSelectedUserIds = new ArrayList<Long>();
	private final ArrayList<WeakReference<Callback>> mCallbacks = new ArrayList<WeakReference<Callback>>();

	public NoDuplicatesLinkedList<Object> getSelectedItems() {
		return mSelectedItems;
	}

	public boolean unselectItem(Object item) {
		return mSelectedItems.remove(item);
	}

	public boolean selectItem(Object item) {
		return mSelectedItems.add(item);
	}

	public void clearSelectedItems() {
		mSelectedItems.clear();
	}

	public boolean isStatusSelected(long status_id) {
		return mSelectedStatusIds.contains(status_id);
	}

	public boolean isUserSelected(long user_id) {
		return mSelectedUserIds.contains(user_id);
	}

	private void onItemSelected(Object object) {
		for (WeakReference<Callback> ref : mCallbacks) {
			if (ref.get() == null) continue;
			ref.get().onItemSelected(object);
		}
	}

	private void onItemUnselected(Object object) {
		for (WeakReference<Callback> ref : mCallbacks) {
			if (ref.get() == null) continue;
			ref.get().onItemUnselected(object);
		}
	}

	private void onItemsCleared() {
		for (WeakReference<Callback> ref : mCallbacks) {
			if (ref.get() == null) continue;
			ref.get().onItemsCleared();
		}
	}
	
	public void register(Callback callback) {
		final ArrayList<WeakReference<Callback>> unused = new ArrayList<WeakReference<Callback>>();
		for (WeakReference<Callback> ref : mCallbacks) {
			if (ref.get() != null) continue;
			unused.add(ref);
		}
		mCallbacks.removeAll(unused);
		if (callback == null) return;
		mCallbacks.add(new WeakReference<Callback>(callback));
	}
	
	public boolean isActive() {
		return !mSelectedItems.isEmpty();
	}
	
	public boolean isSelected(Object object) {
		return mSelectedItems.contains(object);
	}
	
	public int getCount() {
		return mSelectedItems.size();
	}
	
	@SuppressWarnings("serial")
	class ItemsList extends NoDuplicatesLinkedList<Object> {

		@Override
		public boolean add(final Object object) {
			if (object instanceof ParcelableStatus) {
				mSelectedStatusIds.add(((ParcelableStatus) object).status_id);
			} else if (object instanceof ParcelableUser) {
				mSelectedUserIds.add(((ParcelableUser) object).user_id);
			} else {
				return false;
			}
			final boolean ret = super.add(object);
			if (ret) onItemSelected(object);
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
			if (ret){
				if (isEmpty()) {
					onItemsCleared();
				} else {
					onItemUnselected(object);
				}
			}
			return ret;
		}

	}
	
	public static interface Callback {

		public void onItemsCleared();

		public void onItemSelected(Object item);

		public void onItemUnselected(Object item);
		
	}
}
