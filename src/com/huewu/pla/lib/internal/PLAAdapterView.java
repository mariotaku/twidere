/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huewu.pla.lib.internal;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewDebug;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.Spinner;

/**
 * An AdapterView is a view whose children are determined by an {@link Adapter}.
 * 
 * <p>
 * See {@link ListView}, {@link GridView}, {@link Spinner} for commonly used
 * subclasses of AdapterView.
 */
public abstract class PLAAdapterView<T extends Adapter> extends AdapterView<T> {

	/**
	 * The item view type returned by {@link Adapter#getItemViewType(int)} when
	 * the adapter does not want the item's view recycled.
	 */
	public static final int ITEM_VIEW_TYPE_IGNORE = -1;

	/**
	 * The item view type returned by {@link Adapter#getItemViewType(int)} when
	 * the item is a header or footer.
	 */
	public static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;

	/**
	 * The position of the first child displayed
	 */
	@ViewDebug.ExportedProperty
	int mFirstPosition = 0;

	/**
	 * The offset in pixels from the top of the AdapterView to the top of the
	 * view to select during the next layout.
	 */
	int mSpecificTop;

	/**
	 * Position from which to start looking for mSyncRowId
	 */
	int mSyncPosition;

	/**
	 * Row id to look for when data has changed
	 */
	long mSyncRowId = INVALID_ROW_ID;

	/**
	 * Height of the view when mSyncPosition and mSyncRowId where set
	 */
	long mSyncHeight;

	/**
	 * True if we need to sync to mSyncRowId
	 */
	boolean mNeedSync = false;

	/**
	 * Indicates whether to sync based on the selection or position. Possible
	 * values are {@link #SYNC_SELECTED_POSITION} or
	 * {@link #SYNC_FIRST_POSITION}.
	 */
	int mSyncMode;

	/**
	 * Our height after the last layout
	 */
	private int mLayoutHeight;

	/**
	 * Sync based on the selected child
	 */
	static final int SYNC_SELECTED_POSITION = 0;

	/**
	 * Sync based on the first child displayed
	 */
	static final int SYNC_FIRST_POSITION = 1;

	/**
	 * Maximum amount of time to spend in {@link #findSyncPositionPLA()}
	 */
	static final int SYNC_MAX_DURATION_MILLIS = 100;

	/**
	 * Indicates that this view is currently being laid out.
	 */
	boolean mInLayout = false;

	/**
	 * True if the data has changed since the last layout
	 */
	boolean mDataChanged;

	/**
	 * View to show if there are no items to show.
	 */
	private View mEmptyView;

	/**
	 * The number of items in the current adapter.
	 */
	@ViewDebug.ExportedProperty
	int mItemCount;

	/**
	 * The number of items in the adapter before a data changed event occured.
	 */
	int mOldItemCount;

	/**
	 * The last selected position we used when notifying
	 */
	int mOldSelectedPosition = INVALID_POSITION;

	/**
	 * The id of the last selected position we used when notifying
	 */
	long mOldSelectedRowId = INVALID_ROW_ID;

	/**
	 * Indicates what focusable state is requested when calling setFocusable().
	 * In addition to this, this view has other criteria for actually
	 * determining the focusable state (such as whether its empty or the text
	 * filter is shown).
	 * 
	 * @see #setFocusable(boolean)
	 * @see #checkFocusPLA()
	 */
	private boolean mDesiredFocusableState;
	private boolean mDesiredFocusableInTouchModeState;

	/**
	 * When set to true, calls to requestLayout() will not propagate up the
	 * parent hierarchy. This is used to layout the children during a layout
	 * pass.
	 */
	boolean mBlockLayoutRequests = false;

	public PLAAdapterView(final Context context) {
		super(context);
	}

	public PLAAdapterView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public PLAAdapterView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public void addView(final View child) {
		throw new UnsupportedOperationException("addView(View) is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * @param index Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public void addView(final View child, final int index) {
		throw new UnsupportedOperationException("addView(View, int) is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * @param index Ignored.
	 * @param params Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public void addView(final View child, final int index, final LayoutParams params) {
		throw new UnsupportedOperationException("addView(View, int, LayoutParams) " + "is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * @param params Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public void addView(final View child, final LayoutParams params) {
		throw new UnsupportedOperationException("addView(View, LayoutParams) " + "is not supported in AdapterView");
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
		boolean populated = false;
		// This is an exceptional case which occurs when a window gets the
		// focus and sends a focus event via its focused child to announce
		// current focus/selection. AdapterView fires selection but not focus
		// events so we change the event type here.
		if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_FOCUSED) {
			event.setEventType(AccessibilityEvent.TYPE_VIEW_SELECTED);
		}

		// we send selection events only from AdapterView to avoid
		// generation of such event for each child
		final View selectedView = getSelectedView();
		if (selectedView != null) {
			populated = selectedView.dispatchPopulateAccessibilityEvent(event);
		}

		if (!populated) {
			if (selectedView != null) {
				event.setEnabled(selectedView.isEnabled());
			}
			event.setItemCount(getCount());
			event.setCurrentItemIndex(getSelectedItemPosition());
		}

		return populated;
	}

	/**
	 * Returns the adapter currently associated with this widget.
	 * 
	 * @return The adapter used to provide this view's content.
	 */
	@Override
	public abstract T getAdapter();

	/**
	 * @return The number of items owned by the Adapter associated with this
	 *         AdapterView. (This is the number of data items, which may be
	 *         larger than the number of visible view.)
	 */
	@Override
	@ViewDebug.CapturedViewProperty
	public int getCount() {
		return mItemCount;
	}

	/**
	 * When the current adapter is empty, the AdapterView can display a special
	 * view call the empty view. The empty view is used to provide feedback to
	 * the user that no data is available in this AdapterView.
	 * 
	 * @return The view to show if the adapter is empty.
	 */
	@Override
	public View getEmptyView() {
		return mEmptyView;
	}

	/**
	 * Returns the position within the adapter's data set for the first item
	 * displayed on screen.
	 * 
	 * @return The position within the adapter's data set
	 */
	@Override
	public int getFirstVisiblePosition() {
		return mFirstPosition;
	}

	/**
	 * Gets the data associated with the specified position in the list.
	 * 
	 * @param position Which data to get
	 * @return The data associated with the specified position in the list
	 */
	@Override
	public Object getItemAtPosition(final int position) {
		final T adapter = getAdapter();
		return adapter == null || position < 0 ? null : adapter.getItem(position);
	}

	@Override
	public long getItemIdAtPosition(final int position) {
		final T adapter = getAdapter();
		return adapter == null || position < 0 ? INVALID_ROW_ID : adapter.getItemId(position);
	}

	/**
	 * Returns the position within the adapter's data set for the last item
	 * displayed on screen.
	 * 
	 * @return The position within the adapter's data set
	 */
	@Override
	public int getLastVisiblePosition() {
		return mFirstPosition + getChildCount() - 1;
	}

	/**
	 * Get the position within the adapter's data set for the view, where view
	 * is a an adapter item or a descendant of an adapter item.
	 * 
	 * @param view an adapter item, or a descendant of an adapter item. This
	 *            must be visible in this AdapterView at the time of the call.
	 * @return the position within the adapter's data set of the view, or
	 *         {@link #INVALID_POSITION} if the view does not correspond to a
	 *         list item (or it is not currently visible).
	 */
	@Override
	public int getPositionForView(final View view) {
		View listItem = view;
		try {
			View v;
			while (!(v = (View) listItem.getParent()).equals(this)) {
				listItem = v;
			}
		} catch (final ClassCastException e) {
			// We made it up to the window without find this list view
			return INVALID_POSITION;
		}

		// Search the children for the list item
		final int childCount = getChildCount();
		for (int i = 0; i < childCount; i++) {
			if (getChildAt(i).equals(listItem)) return mFirstPosition + i;
		}

		// Child not found!
		return INVALID_POSITION;
	}

	/**
	 * @return The data corresponding to the currently selected item, or null if
	 *         there is nothing selected.
	 */
	@Override
	public Object getSelectedItem() {
		final T adapter = getAdapter();
		final int selection = getSelectedItemPosition();
		if (adapter != null && adapter.getCount() > 0 && selection >= 0)
			return adapter.getItem(selection);
		else
			return null;
	}

	/**
	 * @return The id corresponding to the currently selected item, or
	 *         {@link #INVALID_ROW_ID} if nothing is selected.
	 */
	@Override
	@ViewDebug.CapturedViewProperty
	public long getSelectedItemId() {
		return INVALID_ROW_ID;
	}

	/**
	 * Return the position of the currently selected item within the adapter's
	 * data set
	 * 
	 * @return int Position (starting at 0), or {@link #INVALID_POSITION} if
	 *         there is nothing selected.
	 */
	@Override
	@ViewDebug.CapturedViewProperty
	public int getSelectedItemPosition() {
		return INVALID_POSITION;
	}

	/**
	 * @return The view corresponding to the currently selected item, or null if
	 *         nothing is selected
	 */
	@Override
	public abstract View getSelectedView();

	/**
	 * Call the OnItemClickListener, if it is defined.
	 * 
	 * @param view The view within the AdapterView that was clicked.
	 * @param position The position of the view in the adapter.
	 * @param id The row id of the item that was clicked.
	 * @return True if there was an assigned OnItemClickListener that was
	 *         called, false otherwise is returned.
	 */
	@Override
	public boolean performItemClick(final View view, final int position, final long id) {
		final OnItemClickListener onItemClickListener = getOnItemClickListener();
		if (onItemClickListener != null) {
			playSoundEffect(SoundEffectConstants.CLICK);
			onItemClickListener.onItemClick(this, view, position, id);
			return true;
		}
		return false;
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public void removeAllViews() {
		throw new UnsupportedOperationException("removeAllViews() is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param child Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public void removeView(final View child) {
		throw new UnsupportedOperationException("removeView(View) is not supported in AdapterView");
	}

	/**
	 * This method is not supported and throws an UnsupportedOperationException
	 * when called.
	 * 
	 * @param index Ignored.
	 * 
	 * @throws UnsupportedOperationException Every time this method is invoked.
	 */
	@Override
	public void removeViewAt(final int index) {
		throw new UnsupportedOperationException("removeViewAt(int) is not supported in AdapterView");
	}

	/**
	 * Sets the adapter that provides the data and the views to represent the
	 * data in this widget.
	 * 
	 * @param adapter The adapter to use to create this view's content.
	 */
	@Override
	public abstract void setAdapter(T adapter);

	/**
	 * Sets the view to show if the adapter is empty
	 */
	@Override
	public void setEmptyView(final View emptyView) {
		mEmptyView = emptyView;

		final T adapter = getAdapter();
		final boolean empty = adapter == null || adapter.isEmpty();
		updateEmptyStatus(empty);
	}

	@Override
	public void setFocusable(final boolean focusable) {
		final T adapter = getAdapter();
		final boolean empty = adapter == null || adapter.getCount() == 0;

		mDesiredFocusableState = focusable;
		if (!focusable) {
			mDesiredFocusableInTouchModeState = false;
		}

		super.setFocusable(focusable && (!empty || isInFilterModePLA()));
	}

	@Override
	public void setFocusableInTouchMode(final boolean focusable) {
		final T adapter = getAdapter();
		final boolean empty = adapter == null || adapter.getCount() == 0;

		mDesiredFocusableInTouchModeState = focusable;
		if (focusable) {
			mDesiredFocusableState = true;
		}

		super.setFocusableInTouchMode(focusable && (!empty || isInFilterModePLA()));
	}

	@Override
	public void setOnClickListener(final OnClickListener l) {
		throw new RuntimeException("Don't call setOnClickListener for an AdapterView. "
				+ "You probably want setOnItemClickListener instead");
	}

	/**
	 * Sets the currently selected item. To support accessibility subclasses
	 * that override this method must invoke the overriden super method first.
	 * 
	 * @param position Index (starting at 0) of the data item to be selected.
	 */
	@Override
	public abstract void setSelection(int position);

	@Override
	protected boolean canAnimate() {
		return super.canAnimate() && mItemCount > 0;
	}

	/**
	 * Override to prevent thawing of any views created by the adapter.
	 */
	@Override
	protected void dispatchRestoreInstanceState(final SparseArray<Parcelable> container) {
		dispatchThawSelfOnly(container);
	}

	/**
	 * Override to prevent freezing of any views created by the adapter.
	 */
	@Override
	protected void dispatchSaveInstanceState(final SparseArray<Parcelable> container) {
		dispatchFreezeSelfOnly(container);
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		mLayoutHeight = getHeight();
	}

	/**
	 * Update the status of the list based on the empty parameter. If empty is
	 * true and we have an empty view, display it. In all the other cases, make
	 * sure that the listview is VISIBLE and that the empty view is GONE (if
	 * it's not null).
	 */
	private void updateEmptyStatus(boolean empty) {
		if (isInFilterModePLA()) {
			empty = false;
		}

		if (empty) {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(View.VISIBLE);
				setVisibility(View.GONE);
			} else {
				// If the caller just removed our empty view, make sure the list
				// view is visible
				setVisibility(View.VISIBLE);
			}

			// We are now GONE, so pending layouts will not be dispatched.
			// Force one here to make sure that the state of the list matches
			// the state of the adapter.
			if (mDataChanged) {
				this.onLayout(false, getLeft(), getTop(), getRight(), getBottom());
			}
		} else {
			if (mEmptyView != null) {
				mEmptyView.setVisibility(View.GONE);
			}
			setVisibility(View.VISIBLE);
		}
	}

	void checkFocusPLA() {
		final T adapter = getAdapter();
		final boolean empty = adapter == null || adapter.getCount() == 0;
		final boolean focusable = !empty || isInFilterModePLA();
		// The order in which we set focusable in touch mode/focusable may
		// matter
		// for the client, see View.setFocusableInTouchMode() comments for more
		// details
		super.setFocusableInTouchMode(focusable && mDesiredFocusableInTouchModeState);
		super.setFocusable(focusable && mDesiredFocusableState);
		if (mEmptyView != null) {
			updateEmptyStatus(adapter == null || adapter.isEmpty());
		}
	}

	/**
	 * Searches the adapter for a position matching mSyncRowId. The search
	 * starts at mSyncPosition and then alternates between moving up and moving
	 * down until 1) we find the right position, or 2) we run out of time, or 3)
	 * we have looked at every position
	 * 
	 * @return Position of the row that matches mSyncRowId, or
	 *         {@link #INVALID_POSITION} if it can't be found
	 */
	int findSyncPositionPLA() {
		final int count = mItemCount;

		if (count == 0) return INVALID_POSITION;

		final long idToMatch = mSyncRowId;
		int seed = mSyncPosition;

		// If there isn't a selection don't hunt for it
		if (idToMatch == INVALID_ROW_ID) return INVALID_POSITION;

		// Pin seed to reasonable values
		seed = Math.max(0, seed);
		seed = Math.min(count - 1, seed);

		final long endTime = SystemClock.uptimeMillis() + SYNC_MAX_DURATION_MILLIS;

		long rowId;

		// first position scanned so far
		int first = seed;

		// last position scanned so far
		int last = seed;

		// True if we should move down on the next iteration
		boolean next = false;

		// True when we have looked at the first item in the data
		boolean hitFirst;

		// True when we have looked at the last item in the data
		boolean hitLast;

		// Get the item ID locally (instead of getItemIdAtPosition), so
		// we need the adapter
		final T adapter = getAdapter();
		if (adapter == null) return INVALID_POSITION;

		while (SystemClock.uptimeMillis() <= endTime) {
			rowId = adapter.getItemId(seed);
			if (rowId == idToMatch) // Found it!
				return seed;

			hitLast = last == count - 1;
			hitFirst = first == 0;

			if (hitLast && hitFirst) {
				// Looked at everything
				break;
			}

			if (hitFirst || next && !hitLast) {
				// Either we hit the top, or we are trying to move down
				last++;
				seed = last;
				// Try going up next time
				next = false;
			} else if (hitLast || !next && !hitFirst) {
				// Either we hit the bottom, or we are trying to move up
				first--;
				seed = first;
				// Try going down next time
				next = true;
			}

		}

		return INVALID_POSITION;
	}

	void handleDataChangedPLA() {
		final int count = mItemCount;

		if (count > 0) {
			// Find the row we are supposed to sync to
			if (mNeedSync) {
				mNeedSync = false;
			}
		}
	}

	/**
	 * Indicates whether this view is in filter mode. Filter mode can for
	 * instance be enabled by a user when typing on the keyboard.
	 * 
	 * @return True if the view is in filter mode, false otherwise.
	 */
	boolean isInFilterModePLA() {
		return false;
	}

	/**
	 * Find a position that can be selected (i.e., is not a separator).
	 * 
	 * @param position The starting position to look at.
	 * @param lookDown Whether to look down for other positions.
	 * @return The next selectable position starting at position and then
	 *         searching either up or down. Returns {@link #INVALID_POSITION} if
	 *         nothing can be found.
	 */
	int lookForSelectablePositionPLA(final int position, final boolean lookDown) {
		return position;
	}

	/**
	 * Remember enough information to restore the screen state when the data has
	 * changed.
	 * 
	 */
	void rememberSyncStatePLA() {
		if (getChildCount() > 0) {
			mNeedSync = true;
			mSyncHeight = mLayoutHeight;
			// Sync the based on the offset of the first view
			final View v = getChildAt(0);
			final T adapter = getAdapter();
			if (mFirstPosition >= 0 && mFirstPosition < adapter.getCount()) {
				mSyncRowId = adapter.getItemId(mFirstPosition);
			} else {
				mSyncRowId = NO_ID;
			}
			mSyncPosition = mFirstPosition;
			if (v != null) {
				mSpecificTop = v.getTop();
			}
			mSyncMode = SYNC_FIRST_POSITION;
		}
	}

	class AdapterDataSetObserver extends DataSetObserver {

		private Parcelable mInstanceState = null;

		public void clearSavedState() {
			mInstanceState = null;
		}

		@Override
		public void onChanged() {
			mDataChanged = true;
			mOldItemCount = mItemCount;
			mItemCount = getAdapter().getCount();

			// Detect the case where a cursor that was previously invalidated
			// has
			// been repopulated with new data.
			if (PLAAdapterView.this.getAdapter().hasStableIds() && mInstanceState != null && mOldItemCount == 0
					&& mItemCount > 0) {
				onRestoreInstanceState(mInstanceState);
				mInstanceState = null;
			} else {
				rememberSyncStatePLA();
			}
			checkFocusPLA();
			requestLayout();
		}

		@Override
		public void onInvalidated() {
			mDataChanged = true;

			if (PLAAdapterView.this.getAdapter().hasStableIds()) {
				// Remember the current state for the case where our hosting
				// activity is being
				// stopped and later restarted
				mInstanceState = onSaveInstanceState();
			}

			// Data is invalid so we should reset our state
			mOldItemCount = mItemCount;
			mItemCount = 0;
			mNeedSync = false;

			checkFocusPLA();
			requestLayout();
		}
	}

}
