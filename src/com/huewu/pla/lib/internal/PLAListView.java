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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Checkable;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

import org.mariotaku.twidere.util.LongSparseArrayUtils;

import java.util.ArrayList;

/*
 * Implementation Notes:
 *
 * Some terminology:
 *
 *     index    - index of the items that are currently visible
 *     position - index of the items in the cursor
 */

/**
 * A view that shows items in a vertically scrolling list. The items come from
 * the {@link ListAdapter} associated with this view.
 * 
 * <p>
 * See the <a href="{@docRoot}
 * resources/tutorials/views/hello-listview.html">List View tutorial</a>.
 * </p>
 * 
 * @attr ref android.R.styleable#ListView_entries
 * @attr ref android.R.styleable#ListView_divider
 * @attr ref android.R.styleable#ListView_dividerHeight
 * @attr ref android.R.styleable#ListView_choiceMode
 * @attr ref android.R.styleable#ListView_headerDividersEnabled
 * @attr ref android.R.styleable#ListView_footerDividersEnabled
 */
public class PLAListView extends PLAAbsListView {
	/**
	 * Used to indicate a no preference for a position type.
	 */
	static final int NO_POSITION = -1;
	/**
	 * Normal list that does not indicate choices
	 */
	public static final int CHOICE_MODE_NONE = 0;

	/**
	 * The list allows up to one choice
	 */
	public static final int CHOICE_MODE_SINGLE = 1;

	/**
	 * The list allows multiple choices
	 */
	public static final int CHOICE_MODE_MULTIPLE = 2;

	/**
	 * When arrow scrolling, ListView will never scroll more than this factor
	 * times the height of the list.
	 */
	private static final float MAX_SCROLL_FACTOR = 0.33f;

	// TODO Not Supproted Features
	// Entry from XML.
	// Choice Mode & Item Selection.
	// Filter
	// Handle Key Event & Arrow Scrolling..
	// Can't find Footer & Header findBy methods...

	private final ArrayList<FixedViewInfo> mHeaderViewInfos = new ArrayList<PLAListView.FixedViewInfo>();

	private final ArrayList<FixedViewInfo> mFooterViewInfos = new ArrayList<PLAListView.FixedViewInfo>();

	Drawable mDivider;
	int mDividerHeight;

	Drawable mOverScrollHeader;
	Drawable mOverScrollFooter;

	private boolean mIsCacheColorOpaque;
	private boolean mDividerIsOpaque;

	private boolean mClipDivider;
	private boolean mHeaderDividersEnabled;
	private boolean mFooterDividersEnabled;

	private boolean mAreAllItemsSelectable = true;
	private boolean mItemsCanFocus = false;

	private int mChoiceMode = CHOICE_MODE_NONE;

	private SparseBooleanArray mCheckStates;

	private LongSparseArray<Boolean> mCheckedIdStates;

	// used for temporary calculations.
	private final Rect mTempRect = new Rect();
	private Paint mDividerPaint;

	public PLAListView(final Context context) {
		this(context, null);
	}

	public PLAListView(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.listViewStyle);
	}

	public PLAListView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);

		final TypedArray a = context.obtainStyledAttributes(attrs, styleable.ListView, defStyle, 0);

		// final Drawable d =
		// a.getDrawable(android.R.drawable.divider_horizontal_bright);
		// if (d != null) {
		// // If a divider is specified use its intrinsic height for divider
		// height
		// setDivider(d);
		// }

		final Drawable osHeader = a.getDrawable(styleable.ListView_overScrollHeader);
		if (osHeader != null) {
			setOverscrollHeader(osHeader);
		}

		final Drawable osFooter = a.getDrawable(styleable.ListView_overScrollFooter);
		if (osFooter != null) {
			setOverscrollFooter(osFooter);
		}

		// Use the height specified, zero being the default
		final int dividerHeight = a.getDimensionPixelSize(styleable.ListView_dividerHeight, 0);
		if (dividerHeight != 0) {
			setDividerHeight(dividerHeight);
		}

		mHeaderDividersEnabled = a.getBoolean(styleable.ListView_headerDividersEnabled, true);
		mFooterDividersEnabled = a.getBoolean(styleable.ListView_footerDividersEnabled, true);

		setChoiceMode(a.getInt(styleable.ListView_choiceMode, CHOICE_MODE_NONE));

		a.recycle();
	}

	/**
	 * Add a fixed view to appear at the bottom of the list. If addFooterView is
	 * called more than once, the views will appear in the order they were
	 * added. Views added using this call can take focus if they want.
	 * <p>
	 * NOTE: Call this before calling setAdapter. This is so ListView can wrap
	 * the supplied cursor with one that will also account for header and footer
	 * views.
	 * 
	 * 
	 * @param v The view to add.
	 */
	public void addFooterView(final View v) {
		addFooterView(v, null, true);
	}

	/**
	 * Add a fixed view to appear at the bottom of the list. If addFooterView is
	 * called more than once, the views will appear in the order they were
	 * added. Views added using this call can take focus if they want.
	 * <p>
	 * NOTE: Call this before calling setAdapter. This is so ListView can wrap
	 * the supplied cursor with one that will also account for header and footer
	 * views.
	 * 
	 * @param v The view to add.
	 * @param data Data to associate with this view
	 * @param isSelectable true if the footer view can be selected
	 */
	public void addFooterView(final View v, final Object data, final boolean isSelectable) {
		final FixedViewInfo info = new FixedViewInfo();
		info.view = v;
		info.data = data;
		info.isSelectable = isSelectable;
		mFooterViewInfos.add(info);

		// in the case of re-adding a footer view, or adding one later on,
		// we need to notify the observer
		if (mDataSetObserver != null) {
			mDataSetObserver.onChanged();
		}
	}

	/**
	 * Add a fixed view to appear at the top of the list. If addHeaderView is
	 * called more than once, the views will appear in the order they were
	 * added. Views added using this call can take focus if they want.
	 * <p>
	 * NOTE: Call this before calling setAdapter. This is so ListView can wrap
	 * the supplied cursor with one that will also account for header and footer
	 * views.
	 * 
	 * @param v The view to add.
	 */
	public void addHeaderView(final View v) {
		addHeaderView(v, null, true);
	}

	/**
	 * Add a fixed view to appear at the top of the list. If addHeaderView is
	 * called more than once, the views will appear in the order they were
	 * added. Views added using this call can take focus if they want.
	 * <p>
	 * NOTE: Call this before calling setAdapter. This is so ListView can wrap
	 * the supplied cursor with one that will also account for header and footer
	 * views.
	 * 
	 * @param v The view to add.
	 * @param data Data to associate with this view
	 * @param isSelectable whether the item is selectable
	 */
	public void addHeaderView(final View v, final Object data, final boolean isSelectable) {

		if (mAdapter != null)
			throw new IllegalStateException("Cannot add header view to list -- setAdapter has already been called.");

		final FixedViewInfo info = new FixedViewInfo();
		info.view = v;
		info.data = data;
		info.isSelectable = isSelectable;
		mHeaderViewInfos.add(info);
	}

	/**
	 * Clear any choices previously set
	 */
	public void clearChoices() {
		if (mCheckStates != null) {
			mCheckStates.clear();
		}
		if (mCheckedIdStates != null) {
			mCheckedIdStates.clear();
		}
	}

	@Override
	public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
		final boolean populated = super.dispatchPopulateAccessibilityEvent(event);

		// If the item count is less than 15 then subtract disabled items from
		// the count and
		// position. Otherwise ignore disabled items.
		if (!populated) {
			int itemCount = 0;
			int currentItemIndex = getSelectedItemPosition();

			final ListAdapter adapter = getAdapter();
			if (adapter != null) {
				final int count = adapter.getCount();
				if (count < 15) {
					for (int i = 0; i < count; i++) {
						if (adapter.isEnabled(i)) {
							itemCount++;
						} else if (i <= currentItemIndex) {
							currentItemIndex--;
						}
					}
				} else {
					itemCount = count;
				}
			}

			event.setItemCount(itemCount);
			event.setCurrentItemIndex(currentItemIndex);
		}

		return populated;
	}

	/**
	 * Go to the last or first item if possible (not worrying about panning
	 * across or navigating within the internal focus of the currently selected
	 * item.)
	 * 
	 * @param direction either {@link View#FOCUS_UP} or {@link View#FOCUS_DOWN}
	 * 
	 * @return whether selection was moved
	 */
	public boolean fullScroll(final int direction) {
		boolean moved = false;
		if (direction == FOCUS_UP) {
			final int position = lookForSelectablePositionPLA(0, true);
			if (position >= 0) {
				mLayoutMode = LAYOUT_FORCE_TOP;
				invokeOnItemScrollListener();
				moved = true;
			}
		} else if (direction == FOCUS_DOWN) {
			final int position = lookForSelectablePositionPLA(mItemCount - 1, true);
			if (position >= 0) {
				mLayoutMode = LAYOUT_FORCE_BOTTOM;
				invokeOnItemScrollListener();
			}
			moved = true;
		}

		if (moved && !awakenScrollBars()) {
			awakenScrollBars();
			invalidate();
		}

		return moved;
	}

	/**
	 * Returns the adapter currently in use in this ListView. The returned
	 * adapter might not be the same adapter passed to
	 * {@link #setAdapter(ListAdapter)} but might be a
	 * {@link WrapperListAdapter}.
	 * 
	 * @return The adapter currently used to display data in this ListView.
	 * 
	 * @see #setAdapter(ListAdapter)
	 */
	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	/**
	 * Returns the set of checked items ids. The result is only valid if the
	 * choice mode has not been set to {@link #CHOICE_MODE_NONE} and the adapter
	 * has stable IDs. ({@link ListAdapter#hasStableIds()} == {@code true})
	 * 
	 * @return A new array which contains the id of each checked item in the
	 *         list.
	 */
	public long[] getCheckedItemIds() {
		if (mChoiceMode == CHOICE_MODE_NONE || mCheckedIdStates == null || mAdapter == null) return new long[0];

		final LongSparseArray<Boolean> idStates = mCheckedIdStates;
		final int count = idStates.size();
		final long[] ids = new long[count];

		for (int i = 0; i < count; i++) {
			ids[i] = idStates.keyAt(i);
		}

		return ids;
	}

	/**
	 * Returns the currently checked item. The result is only valid if the
	 * choice mode has been set to {@link #CHOICE_MODE_SINGLE}.
	 * 
	 * @return The position of the currently checked item or
	 *         {@link #INVALID_POSITION} if nothing is selected
	 * 
	 * @see #setChoiceMode(int)
	 */
	public int getCheckedItemPosition() {
		if (mChoiceMode == CHOICE_MODE_SINGLE && mCheckStates != null && mCheckStates.size() == 1)
			return mCheckStates.keyAt(0);

		return INVALID_POSITION;
	}

	/**
	 * Returns the set of checked items in the list. The result is only valid if
	 * the choice mode has not been set to {@link #CHOICE_MODE_NONE}.
	 * 
	 * @return A SparseBooleanArray which will return true for each call to
	 *         get(int position) where position is a position in the list, or
	 *         <code>null</code> if the choice mode is set to
	 *         {@link #CHOICE_MODE_NONE}.
	 */
	public SparseBooleanArray getCheckedItemPositions() {
		if (mChoiceMode != CHOICE_MODE_NONE) return mCheckStates;
		return null;
	}

	/**
	 * Returns the set of checked items ids. The result is only valid if the
	 * choice mode has not been set to {@link #CHOICE_MODE_NONE}.
	 * 
	 * @return A new array which contains the id of each checked item in the
	 *         list.
	 * 
	 * @deprecated Use {@link #getCheckedItemIds()} instead.
	 */
	@Deprecated
	public long[] getCheckItemIds() {
		// Use new behavior that correctly handles stable ID mapping.
		if (mAdapter != null && mAdapter.hasStableIds()) return getCheckedItemIds();

		// Old behavior was buggy, but would sort of work for adapters without
		// stable IDs.
		// Fall back to it to support legacy apps.
		if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null && mAdapter != null) {
			final SparseBooleanArray states = mCheckStates;
			final int count = states.size();
			final long[] ids = new long[count];
			final ListAdapter adapter = mAdapter;

			int checkedCount = 0;
			for (int i = 0; i < count; i++) {
				if (states.valueAt(i)) {
					ids[checkedCount++] = adapter.getItemId(states.keyAt(i));
				}
			}

			// Trim array if needed. mCheckStates may contain false values
			// resulting in checkedCount being smaller than count.
			if (checkedCount == count)
				return ids;
			else {
				final long[] result = new long[checkedCount];
				System.arraycopy(ids, 0, result, 0, checkedCount);

				return result;
			}
		}
		return new long[0];
	}

	/**
	 * @see #setChoiceMode(int)
	 * 
	 * @return The current choice mode
	 */
	public int getChoiceMode() {
		return mChoiceMode;
	}

	/**
	 * Returns the drawable that will be drawn between each item in the list.
	 * 
	 * @return the current drawable drawn between list elements
	 */
	public Drawable getDivider() {
		return mDivider;
	}

	/**
	 * @return Returns the height of the divider that will be drawn between each
	 *         item in the list.
	 */
	public int getDividerHeight() {
		return mDividerHeight;
	}

	@Override
	public int getFirstVisiblePosition() {
		return Math.max(0, mFirstPosition - getHeaderViewsCount());
	}

	@Override
	public int getFooterViewsCount() {
		return mFooterViewInfos.size();
	}

	@Override
	public int getHeaderViewsCount() {
		return mHeaderViewInfos.size();
	}

	/**
	 * @return Whether the views created by the ListAdapter can contain
	 *         focusable items.
	 */
	public boolean getItemsCanFocus() {
		return mItemsCanFocus;
	}

	@Override
	public int getLastVisiblePosition() {
		return Math.min(mFirstPosition + getChildCount() - 1, mAdapter.getCount() - 1);
	}

	/**
	 * @return The maximum amount a list view will scroll in response to an
	 *         arrow event.
	 */
	public int getMaxScrollAmount() {
		// return (int) (MAX_SCROLL_FACTOR * (mBottom - mTop));
		return (int) (MAX_SCROLL_FACTOR * (getBottom() - getTop()));
	}

	/**
	 * @return The drawable that will be drawn below all other list content
	 */
	public Drawable getOverscrollFooter() {
		return mOverScrollFooter;
	}

	/**
	 * @return The drawable that will be drawn above all other list content
	 */
	public Drawable getOverscrollHeader() {
		return mOverScrollHeader;
	}

	/**
	 * check this view is fixed view(ex>Header & Footer) or not.
	 * 
	 * @param v
	 * @return true if this is fixed view.
	 */
	public boolean isFixedView(final View v) {

		{
			// check header view.
			final ArrayList<FixedViewInfo> where = mHeaderViewInfos;
			final int len = where.size();
			for (int i = 0; i < len; ++i) {
				final FixedViewInfo info = where.get(i);
				if (info.view == v) return true;
			}
		}

		{
			// check footer view.
			final ArrayList<FixedViewInfo> where = mFooterViewInfos;
			final int len = where.size();
			for (int i = 0; i < len; ++i) {
				final FixedViewInfo info = where.get(i);
				if (info.view == v) return true;
			}
		}

		return false;
	}

	/**
	 * Returns the checked state of the specified position. The result is only
	 * valid if the choice mode has been set to {@link #CHOICE_MODE_SINGLE} or
	 * {@link #CHOICE_MODE_MULTIPLE}.
	 * 
	 * @param position The item whose checked state to return
	 * @return The item's checked state or <code>false</code> if choice mode is
	 *         invalid
	 * 
	 * @see #setChoiceMode(int)
	 */
	public boolean isItemChecked(final int position) {
		if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) return mCheckStates.get(position);

		return false;
	}

	/**
	 * @hide Pending API council approval.
	 */
	@Override
	public boolean isOpaque() {
		// return (mCachingStarted && mIsCacheColorOpaque && mDividerIsOpaque &&
		// hasOpaqueScrollbars()) || super.isOpaque();
		// we can ignore scrollbar...
		return mCachingStarted && mIsCacheColorOpaque && mDividerIsOpaque || super.isOpaque();
	}

	@Override
	public void onRestoreInstanceState(final Parcelable state) {
		final SavedState ss = (SavedState) state;

		super.onRestoreInstanceState(ss.getSuperState());

		if (ss.checkState != null) {
			mCheckStates = ss.checkState;
		}

		if (ss.checkIdState != null) {
			mCheckedIdStates = ss.checkIdState;
		}
	}

	@Override
	public Parcelable onSaveInstanceState() {
		final Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState, mCheckStates, mCheckedIdStates);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent ev) {
		if (mItemsCanFocus && ev.getAction() == MotionEvent.ACTION_DOWN && ev.getEdgeFlags() != 0) // Don't
																									// handle
																									// edge
																									// touches
																									// immediately
																									// --
																									// they
																									// may
																									// actually
																									// belong
																									// to
																									// one
																									// of
																									// our
			// descendants.
			return false;
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean performItemClick(final View view, final int position, final long id) {
		boolean handled = false;

		if (mChoiceMode != CHOICE_MODE_NONE) {
			handled = true;
			if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
				final boolean newValue = !mCheckStates.get(position, false);
				mCheckStates.put(position, newValue);
				if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
					if (newValue) {
						mCheckedIdStates.put(mAdapter.getItemId(position), Boolean.TRUE);
					} else {
						mCheckedIdStates.delete(mAdapter.getItemId(position));
					}
				}
			} else {
				final boolean newValue = !mCheckStates.get(position, false);
				if (newValue) {
					mCheckStates.clear();
					mCheckStates.put(position, true);
					if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
						mCheckedIdStates.clear();
						mCheckedIdStates.put(mAdapter.getItemId(position), Boolean.TRUE);
					}
				}
			}

			mDataChanged = true;
			rememberSyncStatePLA();
			requestLayout();
		}

		handled |= super.performItemClick(view, position, id);

		return handled;
	}

	/**
	 * Removes a previously-added footer view.
	 * 
	 * @param v The view to remove
	 * @return true if the view was removed, false if the view was not a footer
	 *         view
	 */
	public boolean removeFooterView(final View v) {
		if (mFooterViewInfos.size() > 0) {
			boolean result = false;
			if (((PLAHeaderViewListAdapter) mAdapter).removeFooter(v)) {
				mDataSetObserver.onChanged();
				result = true;
			}
			removeFixedViewInfo(v, mFooterViewInfos);
			return result;
		}
		return false;
	}

	/**
	 * Removes a previously-added header view.
	 * 
	 * @param v The view to remove
	 * @return true if the view was removed, false if the view was not a header
	 *         view
	 */
	public boolean removeHeaderView(final View v) {
		if (mHeaderViewInfos.size() > 0) {
			boolean result = false;
			if (((PLAHeaderViewListAdapter) mAdapter).removeHeader(v)) {
				mDataSetObserver.onChanged();
				result = true;
			}
			removeFixedViewInfo(v, mHeaderViewInfos);
			return result;
		}
		return false;
	}

	@Override
	public boolean requestChildRectangleOnScreen(final View child, final Rect rect, final boolean immediate) {

		final int rectTopWithinChild = rect.top;

		// offset so rect is in coordinates of the this view
		rect.offset(child.getLeft(), child.getTop());
		rect.offset(-child.getScrollX(), -child.getScrollY());

		final int height = getHeight();
		int listUnfadedTop = getScrollY();
		int listUnfadedBottom = listUnfadedTop + height;
		final int fadingEdge = getVerticalFadingEdgeLength();

		if (showingTopFadingEdge()) {
			// leave room for top fading edge as long as rect isn't at very top
			if (rectTopWithinChild > fadingEdge) {
				listUnfadedTop += fadingEdge;
			}
		}

		final int childCount = getChildCount();
		final int bottomOfBottomChild = getChildAt(childCount - 1).getBottom();

		if (showingBottomFadingEdge()) {
			// leave room for bottom fading edge as long as rect isn't at very
			// bottom
			if (rect.bottom < bottomOfBottomChild - fadingEdge) {
				listUnfadedBottom -= fadingEdge;
			}
		}

		int scrollYDelta = 0;

		if (rect.bottom > listUnfadedBottom && rect.top > listUnfadedTop) {
			// need to MOVE DOWN to get it in view: move down just enough so
			// that the entire rectangle is in view (or at least the first
			// screen size chunk).

			if (rect.height() > height) {
				// just enough to get screen size chunk on
				scrollYDelta += rect.top - listUnfadedTop;
			} else {
				// get entire rect at bottom of screen
				scrollYDelta += rect.bottom - listUnfadedBottom;
			}

			// make sure we aren't scrolling beyond the end of our children
			final int distanceToBottom = bottomOfBottomChild - listUnfadedBottom;
			scrollYDelta = Math.min(scrollYDelta, distanceToBottom);
		} else if (rect.top < listUnfadedTop && rect.bottom < listUnfadedBottom) {
			// need to MOVE UP to get it in view: move up just enough so that
			// entire rectangle is in view (or at least the first screen
			// size chunk of it).

			if (rect.height() > height) {
				// screen size chunk
				scrollYDelta -= listUnfadedBottom - rect.bottom;
			} else {
				// entire rect at top
				scrollYDelta -= listUnfadedTop - rect.top;
			}

			// make sure we aren't scrolling any further than the top our
			// children
			final int top = getChildAt(0).getTop();
			final int deltaToTop = top - listUnfadedTop;
			scrollYDelta = Math.max(scrollYDelta, deltaToTop);
		}

		final boolean scroll = scrollYDelta != 0;
		if (scroll) {
			scrollListItemsBy(-scrollYDelta);
			positionSelector(child);
			mSelectedTop = child.getTop();
			invalidate();
		}
		return scroll;
	}

	/**
	 * Sets the data behind this ListView.
	 * 
	 * The adapter passed to this method may be wrapped by a
	 * {@link WrapperListAdapter}, depending on the ListView features currently
	 * in use. For instance, adding headers and/or footers will cause the
	 * adapter to be wrapped.
	 * 
	 * @param adapter The ListAdapter which is responsible for maintaining the
	 *            data backing this list and for producing a view to represent
	 *            an item in that data set.
	 * 
	 * @see #getAdapter()
	 */
	@Override
	public void setAdapter(final ListAdapter adapter) {
		if (null != mAdapter) {
			mAdapter.unregisterDataSetObserver(mDataSetObserver);
		}

		resetList();
		mRecycler.clear();

		if (mHeaderViewInfos.size() > 0 || mFooterViewInfos.size() > 0) {
			mAdapter = new PLAHeaderViewListAdapter(mHeaderViewInfos, mFooterViewInfos, adapter);
		} else {
			mAdapter = adapter;
		}

		mOldSelectedPosition = INVALID_POSITION;
		mOldSelectedRowId = INVALID_ROW_ID;
		if (mAdapter != null) {
			mAreAllItemsSelectable = mAdapter.areAllItemsEnabled();
			mOldItemCount = mItemCount;
			mItemCount = mAdapter.getCount();
			checkFocusPLA();

			mDataSetObserver = new AdapterDataSetObserver();
			mAdapter.registerDataSetObserver(mDataSetObserver);

			mRecycler.setViewTypeCount(mAdapter.getViewTypeCount());

		} else {
			mAreAllItemsSelectable = true;
			checkFocusPLA();
			// Nothing selected
		}

		if (mCheckStates != null) {
			mCheckStates.clear();
		}

		if (mCheckedIdStates != null) {
			mCheckedIdStates.clear();
		}

		requestLayout();
	}

	@Override
	public void setCacheColorHint(final int color) {
		final boolean opaque = color >>> 24 == 0xFF;
		mIsCacheColorOpaque = opaque;
		if (opaque) {
			if (mDividerPaint == null) {
				mDividerPaint = new Paint();
			}
			mDividerPaint.setColor(color);
		}
		super.setCacheColorHint(color);
	}

	/**
	 * Defines the choice behavior for the List. By default, Lists do not have
	 * any choice behavior ({@link #CHOICE_MODE_NONE}). By setting the
	 * choiceMode to {@link #CHOICE_MODE_SINGLE}, the List allows up to one item
	 * to be in a chosen state. By setting the choiceMode to
	 * {@link #CHOICE_MODE_MULTIPLE}, the list allows any number of items to be
	 * chosen.
	 * 
	 * @param choiceMode One of {@link #CHOICE_MODE_NONE},
	 *            {@link #CHOICE_MODE_SINGLE}, or {@link #CHOICE_MODE_MULTIPLE}
	 */
	public void setChoiceMode(final int choiceMode) {
		mChoiceMode = choiceMode;
		if (mChoiceMode != CHOICE_MODE_NONE) {
			if (mCheckStates == null) {
				mCheckStates = new SparseBooleanArray();
			}
			if (mCheckedIdStates == null && mAdapter != null && mAdapter.hasStableIds()) {
				mCheckedIdStates = new LongSparseArray<Boolean>();
			}
		}
	}

	/**
	 * Sets the drawable that will be drawn between each item in the list. If
	 * the drawable does not have an intrinsic height, you should also call
	 * {@link #setDividerHeight(int)}
	 * 
	 * @param divider The drawable to use.
	 */
	public void setDivider(final Drawable divider) {
		if (divider != null) {
			mDividerHeight = divider.getIntrinsicHeight();
			mClipDivider = divider instanceof ColorDrawable;
		} else {
			mDividerHeight = 0;
			mClipDivider = false;
		}
		mDivider = divider;
		mDividerIsOpaque = divider == null || divider.getOpacity() == PixelFormat.OPAQUE;
		requestLayoutIfNecessary();
	}

	/**
	 * Sets the height of the divider that will be drawn between each item in
	 * the list. Calling this will override the intrinsic height as set by
	 * {@link #setDivider(Drawable)}
	 * 
	 * @param height The new height of the divider in pixels.
	 */
	public void setDividerHeight(final int height) {
		mDividerHeight = height;
		requestLayoutIfNecessary();
	}

	/**
	 * Enables or disables the drawing of the divider for footer views.
	 * 
	 * @param footerDividersEnabled True to draw the footers, false otherwise.
	 * 
	 * @see #setHeaderDividersEnabled(boolean)
	 * @see #addFooterView(android.view.View)
	 */
	public void setFooterDividersEnabled(final boolean footerDividersEnabled) {
		mFooterDividersEnabled = footerDividersEnabled;
		invalidate();
	}

	/**
	 * Enables or disables the drawing of the divider for header views.
	 * 
	 * @param headerDividersEnabled True to draw the headers, false otherwise.
	 * 
	 * @see #setFooterDividersEnabled(boolean)
	 * @see #addHeaderView(android.view.View)
	 */
	public void setHeaderDividersEnabled(final boolean headerDividersEnabled) {
		mHeaderDividersEnabled = headerDividersEnabled;
		invalidate();
	}

	/**
	 * Sets the checked state of the specified position. The is only valid if
	 * the choice mode has been set to {@link #CHOICE_MODE_SINGLE} or
	 * {@link #CHOICE_MODE_MULTIPLE}.
	 * 
	 * @param position The item whose checked state is to be checked
	 * @param value The new checked state for the item
	 */
	public void setItemChecked(final int position, final boolean value) {
		if (mChoiceMode == CHOICE_MODE_NONE) return;

		if (mChoiceMode == CHOICE_MODE_MULTIPLE) {
			mCheckStates.put(position, value);
			if (mCheckedIdStates != null && mAdapter.hasStableIds()) {
				if (value) {
					mCheckedIdStates.put(mAdapter.getItemId(position), Boolean.TRUE);
				} else {
					mCheckedIdStates.delete(mAdapter.getItemId(position));
				}
			}
		} else {
			final boolean updateIds = mCheckedIdStates != null && mAdapter.hasStableIds();
			// Clear all values if we're checking something, or unchecking the
			// currently
			// selected item
			if (value || isItemChecked(position)) {
				mCheckStates.clear();
				if (updateIds) {
					mCheckedIdStates.clear();
				}
			}
			// this may end up selecting the value we just cleared but this way
			// we ensure length of mCheckStates is 1, a fact
			// getCheckedItemPosition relies on
			if (value) {
				mCheckStates.put(position, true);
				if (updateIds) {
					mCheckedIdStates.put(mAdapter.getItemId(position), Boolean.TRUE);
				}
			}
		}

		// Do not generate a data change while we are in the layout phase
		if (!mInLayout && !mBlockLayoutRequests) {
			mDataChanged = true;
			rememberSyncStatePLA();
			requestLayout();
		}
	}

	/**
	 * Indicates that the views created by the ListAdapter can contain focusable
	 * items.
	 * 
	 * @param itemsCanFocus true if items can get focus, false otherwise
	 */
	public void setItemsCanFocus(final boolean itemsCanFocus) {
		mItemsCanFocus = itemsCanFocus;
		if (!itemsCanFocus) {
			setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		}
	}

	/**
	 * Sets the drawable that will be drawn below all other list content. This
	 * area can become visible when the user overscrolls the list, or when the
	 * list's content does not fully fill the container area.
	 * 
	 * @param footer The drawable to use
	 */
	public void setOverscrollFooter(final Drawable footer) {
		mOverScrollFooter = footer;
		invalidate();
	}

	/**
	 * Sets the drawable that will be drawn above all other list content. This
	 * area can become visible when the user overscrolls the list.
	 * 
	 * @param header The drawable to use
	 */
	public void setOverscrollHeader(final Drawable header) {
		mOverScrollHeader = header;
		// if (mScrollY < 0) {
		// invalidate();
		// }

		if (getScrollY() < 0) {
			invalidate();
		}
	}

	/**
	 * Sets the currently selected item. If in touch mode, the item will not be
	 * selected but it will still be positioned appropriately. If the specified
	 * selection position is less than 0, then the item at position 0 will be
	 * selected.
	 * 
	 * @param position Index (starting at 0) of the data item to be selected.
	 */
	@Override
	public void setSelection(final int position) {
	}

	@Override
	protected boolean canAnimate() {
		return super.canAnimate() && mItemCount > 0;
	}

	@Override
	protected void dispatchDraw(final Canvas canvas) {
		// Draw the dividers
		final int dividerHeight = mDividerHeight;
		final Drawable overscrollHeader = mOverScrollHeader;
		final Drawable overscrollFooter = mOverScrollFooter;
		final boolean drawOverscrollHeader = overscrollHeader != null;
		final boolean drawOverscrollFooter = overscrollFooter != null;
		final boolean drawDividers = dividerHeight > 0 && mDivider != null;

		if (drawDividers || drawOverscrollHeader || drawOverscrollFooter) {
			// Only modify the top and bottom in the loop, we set the left and
			// right here
			final Rect bounds = mTempRect;
			// bounds.left = mPaddingLeft;
			// bounds.right = mRight - mLeft - mPaddingRight;
			bounds.left = getPaddingLeft();
			bounds.right = getRight() - getLeft() - getPaddingRight();

			final int count = getChildCount();
			final int headerCount = mHeaderViewInfos.size();
			final int itemCount = mItemCount;
			final int footerLimit = itemCount - mFooterViewInfos.size() - 1;
			final boolean headerDividers = mHeaderDividersEnabled;
			final boolean footerDividers = mFooterDividersEnabled;
			final int first = mFirstPosition;
			final boolean areAllItemsSelectable = mAreAllItemsSelectable;
			final ListAdapter adapter = mAdapter;
			// If the list is opaque *and* the background is not, we want to
			// fill a rect where the dividers would be for non-selectable items
			// If the list is opaque and the background is also opaque, we don't
			// need to draw anything since the background will do it for us
			final boolean fillForMissingDividers = drawDividers && isOpaque() && !super.isOpaque();

			if (fillForMissingDividers && mDividerPaint == null && mIsCacheColorOpaque) {
				mDividerPaint = new Paint();
				mDividerPaint.setColor(getCacheColorHint());
			}
			final Paint paint = mDividerPaint;

			// final int listBottom = mBottom - mTop - mListPadding.bottom +
			// mScrollY;
			final int listBottom = getBottom() - getTop() - mListPadding.bottom + getScrollY();
			if (!mStackFromBottom) {
				int bottom = 0;

				// Draw top divider or header for overscroll
				// final int scrollY = mScrollY;
				final int scrollY = getScrollY();
				if (count > 0 && scrollY < 0) {
					if (drawOverscrollHeader) {
						bounds.bottom = 0;
						bounds.top = scrollY;
						drawOverscrollHeader(canvas, overscrollHeader, bounds);
					} else if (drawDividers) {
						bounds.bottom = 0;
						bounds.top = -dividerHeight;
						drawDivider(canvas, bounds, -1);
					}
				}

				for (int i = 0; i < count; i++) {
					if ((headerDividers || first + i >= headerCount) && (footerDividers || first + i < footerLimit)) {
						final View child = getChildAt(i);
						bottom = child.getBottom();
						// Don't draw dividers next to items that are not
						// enabled
						if (drawDividers && bottom < listBottom && !(drawOverscrollFooter && i == count - 1)) {
							if (areAllItemsSelectable || adapter.isEnabled(first + i)
									&& (i == count - 1 || adapter.isEnabled(first + i + 1))) {
								bounds.top = bottom;
								bounds.bottom = bottom + dividerHeight;
								drawDivider(canvas, bounds, i);
							} else if (fillForMissingDividers) {
								bounds.top = bottom;
								bounds.bottom = bottom + dividerHeight;
								canvas.drawRect(bounds, paint);
							}
						}
					}
				}

				// final int overFooterBottom = mBottom + mScrollY;
				final int overFooterBottom = getBottom() + getScrollY();
				if (drawOverscrollFooter && first + count == itemCount && overFooterBottom > bottom) {
					bounds.top = bottom;
					bounds.bottom = overFooterBottom;
					drawOverscrollFooter(canvas, overscrollFooter, bounds);
				}
			} else {
				int top;
				final int listTop = mListPadding.top;

				// final int scrollY = mScrollY;
				final int scrollY = getScrollY();

				if (count > 0 && drawOverscrollHeader) {
					bounds.top = scrollY;
					bounds.bottom = getChildAt(0).getTop();
					drawOverscrollHeader(canvas, overscrollHeader, bounds);
				}

				final int start = drawOverscrollHeader ? 1 : 0;
				for (int i = start; i < count; i++) {
					if ((headerDividers || first + i >= headerCount) && (footerDividers || first + i < footerLimit)) {
						final View child = getChildAt(i);
						top = child.getTop();
						// Don't draw dividers next to items that are not
						// enabled
						if (drawDividers && top > listTop) {
							if (areAllItemsSelectable || adapter.isEnabled(first + i)
									&& (i == count - 1 || adapter.isEnabled(first + i + 1))) {
								bounds.top = top - dividerHeight;
								bounds.bottom = top;
								// Give the method the child ABOVE the divider,
								// so we
								// subtract one from our child
								// position. Give -1 when there is no child
								// above the
								// divider.
								drawDivider(canvas, bounds, i - 1);
							} else if (fillForMissingDividers) {
								bounds.top = top - dividerHeight;
								bounds.bottom = top;
								canvas.drawRect(bounds, paint);
							}
						}
					}
				}

				if (count > 0 && scrollY > 0) {
					if (drawOverscrollFooter) {
						// final int absListBottom = mBottom;
						final int absListBottom = getBottom();
						bounds.top = absListBottom;
						bounds.bottom = absListBottom + scrollY;
						drawOverscrollFooter(canvas, overscrollFooter, bounds);
					} else if (drawDividers) {
						bounds.top = listBottom;
						bounds.bottom = listBottom + dividerHeight;
						drawDivider(canvas, bounds, -1);
					}
				}
			}
		}

		// Draw the indicators (these should be drawn above the dividers) and
		// children
		super.dispatchDraw(canvas);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void fillGap(final boolean down) {
		final int count = getChildCount();
		if (down) {
			fillDown(mFirstPosition + count, getItemTop(mFirstPosition + count));
			onAdjustChildViews(down);
		} else {
			fillUp(mFirstPosition - 1, getItemBottom(mFirstPosition - 1));
			onAdjustChildViews(down);
		}
	}

	/**
	 * override this method to manipulate the position of each item in list
	 * view. return item's bottom position. (item will be added in up direction)
	 * 
	 * @param pos
	 * @return value of pos's item bottom.
	 */
	protected int getItemBottom(final int pos) {
		final int count = getChildCount();
		return count > 0 ? getChildAt(0).getTop() - mDividerHeight : getHeight() - getListPaddingBottom();
	}

	/**
	 * override this method to manipulate the position of each item in list
	 * view. return item left position.
	 * 
	 * @param pos
	 * @return pos's item left position.
	 */
	protected int getItemLeft(final int pos) {
		return mListPadding.left;
	}

	/**
	 * override this method to manipulate the position of each item in list
	 * view. return item's top position. (item will be added in down direction)
	 * 
	 * @param pos
	 * @return value of pos's item top.
	 */
	protected int getItemTop(final int pos) {
		// just return the last itme's bottom position..
		final int count = getChildCount();
		return count > 0 ? getChildAt(count - 1).getBottom() + mDividerHeight : getListPaddingTop();
	}

	protected View getLastChild() {
		final int numChildren = getChildCount();
		return getChildAt(numChildren - 1);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void layoutChildren() {
		final boolean blockLayoutRequests = mBlockLayoutRequests;
		if (!blockLayoutRequests) {
			mBlockLayoutRequests = true;
		} else
			return;

		try {
			super.layoutChildren();
			invalidate();
			if (mAdapter == null) {
				resetList();
				invokeOnItemScrollListener();
				return;
			}

			final int childrenTop = mListPadding.top;
			// int childrenBottom = mBottom - mTop - mListPadding.bottom;
			final int childrenBottom = getBottom() - getTop() - mListPadding.bottom;

			final int childCount = getChildCount();
			final int index = 0;

			View oldFirst = null;
			View focusLayoutRestoreView = null;

			// Remember stuff we will need down below
			switch (mLayoutMode) {
				case LAYOUT_FORCE_TOP:
				case LAYOUT_FORCE_BOTTOM:
				case LAYOUT_SPECIFIC:
				case LAYOUT_SYNC:
					break;
				default:
					// Remember the previous first child
					oldFirst = getChildAt(0);
			}

			final boolean dataChanged = mDataChanged;
			if (dataChanged) {
				handleDataChangedPLA();
			}

			// Handle the empty set by removing all views that are visible
			// and calling it a day
			if (mItemCount == 0) {
				resetList();
				invokeOnItemScrollListener();
				return;
			} else if (mItemCount != mAdapter.getCount())
				throw new IllegalStateException("The content of the adapter has changed but "
						+ "ListView did not receive a notification. Make sure the content of "
						+ "your adapter is not modified from a background thread, but only "
						+ "from the UI thread. [in ListView(" + getId() + ", " + getClass() + ") with Adapter("
						+ mAdapter.getClass() + ")]");

			// Pull all children into the RecycleBin.
			// These views will be reused if possible
			final int firstPosition = mFirstPosition;
			final RecycleBin recycleBin = mRecycler;

			// reset the focus restoration

			// Don't put header or footer views into the Recycler. Those are
			// already cached in mHeaderViews;
			if (dataChanged) {
				for (int i = 0; i < childCount; i++) {
					recycleBin.addScrapView(getChildAt(i));
					if (ViewDebug.TRACE_RECYCLER) {
						ViewDebug.trace(getChildAt(i), ViewDebug.RecyclerTraceType.MOVE_TO_SCRAP_HEAP, index, i);
					}
				}
			} else {
				recycleBin.fillActiveViews(childCount, firstPosition);
			}

			// take focus back to us temporarily to avoid the eventual
			// call to clear focus when removing the focused child below
			// from messing things up when ViewRoot assigns focus back
			// to someone else
			final View focusedChild = getFocusedChild();
			if (focusedChild != null) {
				// TODO: in some cases focusedChild.getParent() == null
				// we can remember the focused view to restore after relayout if
				// the
				// data hasn't changed, or if the focused position is a header
				// or footer
				if (!dataChanged || isDirectChildHeaderOrFooter(focusedChild)) {
					// remember the specific view that had focus
					focusLayoutRestoreView = findFocus();
					if (focusLayoutRestoreView != null) {
						// tell it we are going to mess with it
						focusLayoutRestoreView.onStartTemporaryDetach();
					}
				}
				requestFocus();
			}

			switch (mLayoutMode) {
				case LAYOUT_SYNC:
					onLayoutSync(mSyncPosition);
					// Clear out old views
					detachAllViewsFromParent();
					fillSpecific(mSyncPosition, mSpecificTop);
					onLayoutSyncFinished(mSyncPosition);
					break;
				case LAYOUT_FORCE_BOTTOM:
					detachAllViewsFromParent();
					fillUp(mItemCount - 1, childrenBottom);
					adjustViewsUpOrDown();
					break;
				case LAYOUT_FORCE_TOP:
					detachAllViewsFromParent();
					mFirstPosition = 0;
					fillFromTop(childrenTop);
					adjustViewsUpOrDown();
					break;
				default:
					if (childCount == 0) {
						detachAllViewsFromParent();
						if (!mStackFromBottom) {
							fillFromTop(childrenTop);
						} else {
							fillUp(mItemCount - 1, childrenBottom);
						}
					} else {
						if (mFirstPosition < mItemCount) {
							onLayoutSync(mFirstPosition);
							detachAllViewsFromParent();
							fillSpecific(mFirstPosition, oldFirst == null ? childrenTop : oldFirst.getTop());
							onLayoutSyncFinished(mFirstPosition);
						} else {
							onLayoutSync(0);
							detachAllViewsFromParent();
							fillSpecific(0, childrenTop);
							onLayoutSyncFinished(0);
						}
					}
					break;
			}

			// Flush any cached views that did not get reused above
			recycleBin.scrapActiveViews();

			if (mTouchMode > TOUCH_MODE_DOWN && mTouchMode < TOUCH_MODE_SCROLL) {
				final View child = getChildAt(mMotionPosition - mFirstPosition);
				if (child != null) {
					positionSelector(child);
				}
			} else {
				mSelectedTop = 0;
				mSelectorRect.setEmpty();
			}

			// even if there is not selected position, we may need to restore
			// focus (i.e. something focusable in touch mode)
			if (hasFocus() && focusLayoutRestoreView != null) {
				focusLayoutRestoreView.requestFocus();
			}

			// tell focus view we are done mucking with it, if it is still in
			// our view hierarchy.
			if (focusLayoutRestoreView != null && focusLayoutRestoreView.getWindowToken() != null) {
				focusLayoutRestoreView.onFinishTemporaryDetach();
			}

			mLayoutMode = LAYOUT_NORMAL;
			mDataChanged = false;
			mNeedSync = false;

			invokeOnItemScrollListener();
		} finally {
			if (!blockLayoutRequests) {
				mBlockLayoutRequests = false;
			}
		}
	}

	/**
	 * this method is called to adjust child view's up & down.
	 * 
	 * @param down
	 */
	protected void onAdjustChildViews(final boolean down) {
		if (down) {
			correctTooHigh(getChildCount());
		} else {
			correctTooLow(getChildCount());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * Children specified in XML are assumed to be header views. After we have
	 * parsed them move them out of the children list and into mHeaderViews.
	 */
	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();

		final int count = getChildCount();
		if (count > 0) {
			for (int i = 0; i < count; ++i) {
				addHeaderView(getChildAt(i));
			}
			removeAllViews();
		}
	}

	@Override
	protected void onFocusChanged(final boolean gainFocus, final int direction, final Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);

		if (DEBUG) {
			Log.v("PLA_ListView", "onFocusChanged");
		}

		int closetChildIndex = -1;
		if (gainFocus && previouslyFocusedRect != null) {
			// previouslyFocusedRect.offset(mScrollX, mScrollY);
			previouslyFocusedRect.offset(getScrollX(), getScrollY());

			final ListAdapter adapter = mAdapter;
			// Don't cache the result of getChildCount or mFirstPosition here,
			// it could change in layoutChildren.
			if (adapter.getCount() < getChildCount() + mFirstPosition) {
				mLayoutMode = LAYOUT_NORMAL;
				layoutChildren();
			}

			// figure out which item should be selected based on previously
			// focused rect
			final Rect otherRect = mTempRect;
			int minDistance = Integer.MAX_VALUE;
			final int childCount = getChildCount();
			final int firstPosition = mFirstPosition;

			for (int i = 0; i < childCount; i++) {
				// only consider selectable views
				if (!adapter.isEnabled(firstPosition + i)) {
					continue;
				}

				final View other = getChildAt(i);
				other.getDrawingRect(otherRect);
				offsetDescendantRectToMyCoords(other, otherRect);
				final int distance = getDistance(previouslyFocusedRect, otherRect, direction);

				if (distance < minDistance) {
					minDistance = distance;
					closetChildIndex = i;
				}
			}
		}

		if (closetChildIndex >= 0) {
			setSelection(closetChildIndex + mFirstPosition);
		} else {
			requestLayout();
		}
	}

	/**
	 * @param position position of newly adde ditem.
	 * @param flow If flow is true, align top edge to y. If false, align bottom
	 *            edge to y.
	 */
	protected void onItemAddedToList(final int position, final boolean flow) {
	}

	protected void onLayoutChild(final View child, final int position, final int l, final int t, final int r,
			final int b) {
		child.layout(l, t, r, b);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		// Sets up mListPadding
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		int childWidth = 0;
		int childHeight = 0;

		mItemCount = mAdapter == null ? 0 : mAdapter.getCount();
		if (mItemCount > 0 && (widthMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.UNSPECIFIED)) {
			final View child = obtainView(0, mIsScrap);

			measureScrapChild(child, 0, widthMeasureSpec);

			childWidth = child.getMeasuredWidth();
			childHeight = child.getMeasuredHeight();

			if (recycleOnMeasure()
					&& mRecycler.shouldRecycleViewType(((LayoutParams) child.getLayoutParams()).viewType)) {
				mRecycler.addScrapView(child);
			}
		}

		if (widthMode == MeasureSpec.UNSPECIFIED) {
			widthSize = mListPadding.left + mListPadding.right + childWidth + getVerticalScrollbarWidth();
		}

		if (heightMode == MeasureSpec.UNSPECIFIED) {
			heightSize = mListPadding.top + mListPadding.bottom + childHeight + getVerticalFadingEdgeLength() * 2;
		}

		if (heightMode == MeasureSpec.AT_MOST) {
			// TODO: after first layout we should maybe start at the first
			// visible position, not 0
			heightSize = measureHeightOfChildren(widthMeasureSpec, 0, NO_POSITION, heightSize, -1);
		}

		setMeasuredDimension(widthSize, heightSize);
		mWidthMeasureSpec = widthMeasureSpec;
	}

	/**
	 * this method is called every time a new child is mesaure.
	 * 
	 * @param child
	 * @param widthMeasureSpec
	 * @param heightMeasureSpec
	 */
	protected void onMeasureChild(final View child, final int position, final int widthMeasureSpec,
			final int heightMeasureSpec) {
		child.measure(widthMeasureSpec, heightMeasureSpec);
	}

	protected void onOffsetChild(final View child, final int position, final int offsetLeft, final int offsetTop) {
		child.offsetLeftAndRight(offsetLeft);
		child.offsetTopAndBottom(offsetTop);
	}

	/**
	 * @return True to recycle the views used to measure this ListView in
	 *         UNSPECIFIED/AT_MOST modes, false otherwise.
	 * @hide
	 */
	@ViewDebug.ExportedProperty(category = "list")
	protected boolean recycleOnMeasure() {
		return true;
	}

	private View addViewAbove(final View theView, final int position) {
		final int abovePosition = position - 1;
		final View view = obtainView(abovePosition, mIsScrap);
		final int edgeOfNewChild = theView.getTop() - mDividerHeight;
		setupChild(view, abovePosition, edgeOfNewChild, false, mListPadding.left, false, mIsScrap[0]);
		return view;
	}

	private View addViewBelow(final View theView, final int position) {
		final int belowPosition = position + 1;
		final View view = obtainView(belowPosition, mIsScrap);
		final int edgeOfNewChild = theView.getBottom() + mDividerHeight;
		setupChild(view, belowPosition, edgeOfNewChild, true, mListPadding.left, false, mIsScrap[0]);
		return view;
	}

	/**
	 * Make sure views are touching the top or bottom edge, as appropriate for
	 * our gravity
	 */
	private void adjustViewsUpOrDown() {
		final int childCount = getChildCount();
		int delta;

		if (childCount > 0) {
			// View child;
			if (!mStackFromBottom) {
				// Uh-oh -- we came up short. Slide all views up to make them
				// align with the top
				final int firstTop = getScrollChildTop();
				// child = getChildAt(0);
				// delta = child.getTop() - mListPadding.top;
				delta = firstTop - mListPadding.top;
				if (mFirstPosition != 0) {
					// It's OK to have some space above the first item if it is
					// part of the vertical spacing
					delta -= mDividerHeight;
				}
				if (delta < 0) {
					// We only are looking to see if we are too low, not too
					// high
					delta = 0;
				}
			} else {
				// we are too high, slide all views down to align with bottom
				// child = getChildAt(childCount - 1);
				// delta = child.getBottom() - (getHeight() -
				// mListPadding.bottom);
				final int lastBottom = getScrollChildBottom();
				delta = lastBottom - (getHeight() - mListPadding.bottom);

				if (mFirstPosition + childCount < mItemCount) {
					// It's OK to have some space below the last item if it is
					// part of the vertical spacing
					delta += mDividerHeight;
				}

				if (delta > 0) {
					delta = 0;
				}
			}

			if (delta != 0) {
				// offsetChildrenTopAndBottom(-delta);
				tryOffsetChildrenTopAndBottom(-delta);
			}
		}
	}

	private void clearRecycledState(final ArrayList<FixedViewInfo> infos) {
		if (infos != null) {
			final int count = infos.size();

			for (int i = 0; i < count; i++) {
				final View child = infos.get(i).view;
				final LayoutParams p = (LayoutParams) child.getLayoutParams();
				if (p != null) {
					p.recycledHeaderFooter = false;
				}
			}
		}
	}

	/**
	 * Check if we have dragged the bottom of the list too high (we have pushed
	 * the top element off the top of the screen when we did not need to).
	 * Correct by sliding everything back down.
	 * 
	 * @param childCount Number of children
	 */
	private void correctTooHigh(final int childCount) {
		// First see if the last item is visible. If it is not, it is OK for the
		// top of the list to be pushed up.
		final int lastPosition = mFirstPosition + childCount - 1;
		if (lastPosition == mItemCount - 1 && childCount > 0) {

			// Get the last child ...
			// final View lastChild = getChildAt(childCount - 1);

			// ... and its bottom edge
			// final int lastBottom = lastChild.getBottom();
			final int lastBottom = getScrollChildBottom();

			// This is bottom of our drawable area
			// final int end = (mBottom - mTop) - mListPadding.bottom;
			final int end = getBottom() - getTop() - mListPadding.bottom;

			// This is how far the bottom edge of the last view is from the
			// bottom of the drawable area
			int bottomOffset = end - lastBottom;

			// View firstChild = getChildAt(0);
			// final int firstTop = firstChild.getTop();
			final int firstTop = getScrollChildTop();

			// Make sure we are 1) Too high, and 2) Either there are more rows
			// above the
			// first row or the first row is scrolled off the top of the
			// drawable area
			if (bottomOffset > 0 && (mFirstPosition > 0 || firstTop < mListPadding.top)) {
				if (mFirstPosition == 0) {
					// Don't pull the top too far down
					bottomOffset = Math.min(bottomOffset, mListPadding.top - firstTop);
				}
				// Move everything down
				// offsetChildrenTopAndBottom(bottomOffset);
				tryOffsetChildrenTopAndBottom(bottomOffset);
				if (mFirstPosition > 0) {
					// Fill the gap that was opened above mFirstPosition with
					// more rows, if
					// possible
					final int newFirstTop = getScrollChildTop();
					fillUp(mFirstPosition - 1, newFirstTop - mDividerHeight);
					// Close up the remaining gap
					adjustViewsUpOrDown();
				}

			}
		}
	}

	/**
	 * Check if we have dragged the bottom of the list too low (we have pushed
	 * the bottom element off the bottom of the screen when we did not need to).
	 * Correct by sliding everything back up.
	 * 
	 * @param childCount Number of children
	 */
	private void correctTooLow(final int childCount) {
		// First see if the first item is visible. If it is not, it is OK for
		// the
		// bottom of the list to be pushed down.
		if (mFirstPosition == 0 && childCount > 0) {

			// Get the first child and its top edge
			final int firstTop = getScrollChildTop();

			// This is top of our drawable area
			final int start = mListPadding.top;

			// This is bottom of our drawable area
			final int end = getBottom() - getTop() - mListPadding.bottom;

			// This is how far the top edge of the first view is from the top of
			// the drawable area
			int topOffset = firstTop - start;
			final int lastBottom = getScrollChildBottom();

			final int lastPosition = mFirstPosition + childCount - 1;

			// Make sure we are 1) Too low, and 2) Either there are more rows
			// below the
			// last row or the last row is scrolled off the bottom of the
			// drawable area
			if (topOffset > 0) {
				if (lastPosition < mItemCount - 1 || lastBottom > end) {
					if (lastPosition == mItemCount - 1) {
						// Don't pull the bottom too far up
						topOffset = Math.min(topOffset, lastBottom - end);
					}
					// Move everything up
					tryOffsetChildrenTopAndBottom(-topOffset);
					if (lastPosition < mItemCount - 1) {
						// Fill the gap that was opened below the last position
						// with more rows, if
						// possible
						fillDown(lastPosition + 1, getFillChildTop() + mDividerHeight);
						// Close up the remaining gap
						adjustViewsUpOrDown();
					}
				} else if (lastPosition == mItemCount - 1) {
					adjustViewsUpOrDown();
				}
			}
		}
	}

	/**
	 * Fills the list from pos down to the end of the list view.
	 * 
	 * @param pos The first position to put in the list
	 * 
	 * @param nextTop The location where the top of the item associated with pos
	 *            should be drawn
	 * 
	 * @return The view that is currently selected, if it happens to be in the
	 *         range that we draw.
	 */
	private View fillDown(int pos, final int top) {

		// int end = (mBottom - mTop) - mListPadding.bottom;
		final int end = getBottom() - getTop() - mListPadding.bottom;
		int childTop = getFillChildBottom() + mDividerHeight;

		while (childTop < end && pos < mItemCount) {
			// is this the selected item?
			makeAndAddView(pos, getItemTop(pos), true, false);
			pos++;
			childTop = getFillChildBottom() + mDividerHeight;
		}

		return null;
	}

	/**
	 * Fills the list from top to bottom, starting with mFirstPosition
	 * 
	 * @param nextTop The location where the top of the first item should be
	 *            drawn
	 * 
	 * @return The view that is currently selected
	 */
	private View fillFromTop(final int nextTop) {
		mFirstPosition = Math.min(mFirstPosition, -1);
		mFirstPosition = Math.min(mFirstPosition, mItemCount - 1);
		if (mFirstPosition < 0) {
			mFirstPosition = 0;
		}
		return fillDown(mFirstPosition, nextTop);
	}

	/**
	 * Put a specific item at a specific location on the screen and then build
	 * up and down from there.
	 * 
	 * @param position The reference view to use as the starting point
	 * @param top Pixel offset from the top of this view to the top of the
	 *            reference view.
	 * 
	 * @return The selected view, or null if the selected view is outside the
	 *         visible area.
	 */
	private View fillSpecific(final int position, final int top) {

		if (DEBUG) {
			Log.d("PLA_ListView", "FillSpecific: " + position + ":" + top);
		}

		final View temp = makeAndAddView(position, top, true, false);

		// Possibly changed again in fillUp if we add rows above this one.

		mFirstPosition = position;
		final int dividerHeight = mDividerHeight;
		if (!mStackFromBottom) {
			fillUp(position - 1, temp.getTop() - dividerHeight);
			// This will correct for the top of the first view not touching the
			// top of the list
			adjustViewsUpOrDown();
			fillDown(position + 1, temp.getBottom() + dividerHeight);
			final int childCount = getChildCount();
			if (childCount > 0) {
				correctTooHigh(childCount);
			}
		} else {
			fillDown(position + 1, temp.getBottom() + dividerHeight);
			// This will correct for the bottom of the last view not touching
			// the bottom of the list
			adjustViewsUpOrDown();
			fillUp(position - 1, temp.getTop() - dividerHeight);
			final int childCount = getChildCount();
			if (childCount > 0) {
				correctTooLow(childCount);
			}
		}

		return null;
	}

	/**
	 * Fills the list from pos up to the top of the list view.
	 * 
	 * @param pos The first position to put in the list
	 * 
	 * @param nextBottom The location where the bottom of the item associated
	 *            with pos should be drawn
	 * 
	 * @return The view that is currently selected
	 */
	private View fillUp(int pos, final int bottom) {
		final int end = mListPadding.top;
		int childBottom = getFillChildTop();
		while (childBottom > end && pos >= 0) {
			// is this the selected item?
			makeAndAddView(pos, getItemBottom(pos), false, false);
			// nextBottom = child.getTop() - mDividerHeight;
			pos--;
			childBottom = getItemBottom(pos);
		}

		mFirstPosition = pos + 1;

		return null;
	}

	/**
	 * @param child a direct child of this list.
	 * @return Whether child is a header or footer view.
	 */
	private boolean isDirectChildHeaderOrFooter(final View child) {

		final ArrayList<FixedViewInfo> headers = mHeaderViewInfos;
		final int numHeaders = headers.size();
		for (int i = 0; i < numHeaders; i++) {
			if (child == headers.get(i).view) return true;
		}
		final ArrayList<FixedViewInfo> footers = mFooterViewInfos;
		final int numFooters = footers.size();
		for (int i = 0; i < numFooters; i++) {
			if (child == footers.get(i).view) return true;
		}
		return false;
	}

	/**
	 * Obtain the view and add it to our list of children. The view can be made
	 * fresh, converted from an unused view, or used as is if it was in the
	 * recycle bin.
	 * 
	 * @param position Logical position in the list
	 * @param childrenBottomOrTop Top or bottom edge of the view to add
	 * @param flow If flow is true, align top edge to y. If false, align bottom
	 *            edge to y.
	 * @param childrenLeft Left edge where children should be positioned
	 * @param selected Is this position selected?
	 * @return View that was added
	 */
	@SuppressWarnings("deprecation")
	private View makeAndAddView(final int position, final int childrenBottomOrTop, final boolean flow,
			final boolean selected) {
		View child;

		int childrenLeft;
		if (!mDataChanged) {
			// Try to use an exsiting view for this position
			child = mRecycler.getActiveView(position);
			if (child != null) {

				if (ViewDebug.TRACE_RECYCLER) {
					ViewDebug.trace(child, ViewDebug.RecyclerTraceType.RECYCLE_FROM_ACTIVE_HEAP, position,
							getChildCount());
				}

				// Found it -- we're using an existing child
				// This just needs to be positioned
				childrenLeft = getItemLeft(position);
				setupChild(child, position, childrenBottomOrTop, flow, childrenLeft, selected, true);
				return child;
			}
		}

		// Notify new item is added to view.

		onItemAddedToList(position, flow);
		childrenLeft = getItemLeft(position);

		// Make a new view for this position, or convert an unused view if
		// possible
		child = obtainView(position, mIsScrap);

		// This needs to be positioned and measured
		setupChild(child, position, childrenBottomOrTop, flow, childrenLeft, selected, mIsScrap[0]);

		return child;
	}

	private void measureScrapChild(final View child, final int position, final int widthMeasureSpec) {
		LayoutParams p = (LayoutParams) child.getLayoutParams();
		if (p == null) {
			p = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0);
			child.setLayoutParams(p);
		}
		p.viewType = mAdapter.getItemViewType(position);
		p.forceAdd = true;

		final int childWidthSpec = ViewGroup.getChildMeasureSpec(widthMeasureSpec, mListPadding.left
				+ mListPadding.right, p.width);
		final int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	private void removeFixedViewInfo(final View v, final ArrayList<FixedViewInfo> where) {
		final int len = where.size();
		for (int i = 0; i < len; ++i) {
			final FixedViewInfo info = where.get(i);
			if (info.view == v) {
				where.remove(i);
				break;
			}
		}
	}

	/**
	 * Scroll the children by amount, adding a view at the end and removing
	 * views that fall off as necessary.
	 * 
	 * @param amount The amount (positive or negative) to scroll.
	 */
	private void scrollListItemsBy(final int amount) {
		// offsetChildrenTopAndBottom(amount);
		tryOffsetChildrenTopAndBottom(amount);

		final int listBottom = getHeight() - mListPadding.bottom;
		final int listTop = mListPadding.top;
		final PLAAbsListView.RecycleBin recycleBin = mRecycler;

		if (amount < 0) {
			// shifted items up

			// may need to pan views into the bottom space
			View last = getLastChild();
			int numChildren = getChildCount();
			// View last = getChildAt(numChildren - 1);

			while (last.getBottom() < listBottom) {
				final int lastVisiblePosition = mFirstPosition + numChildren - 1;
				if (lastVisiblePosition < mItemCount - 1) {
					addViewBelow(last, lastVisiblePosition);
					last = getLastChild();
					numChildren++;
				} else {
					break;
				}
			}

			// may have brought in the last child of the list that is skinnier
			// than the fading edge, thereby leaving space at the end. need
			// to shift back
			if (last.getBottom() < listBottom) {
				// offsetChildrenTopAndBottom(listBottom - last.getBottom());
				tryOffsetChildrenTopAndBottom(listBottom - last.getBottom());
			}

			// top views may be panned off screen
			View first = getChildAt(0);
			while (first.getBottom() < listTop) {
				final PLAAbsListView.LayoutParams layoutParams = (LayoutParams) first.getLayoutParams();
				if (recycleBin.shouldRecycleViewType(layoutParams.viewType)) {
					detachViewFromParent(first);
					recycleBin.addScrapView(first);
				} else {
					removeViewInLayout(first);
				}
				first = getChildAt(0);
				mFirstPosition++;
			}
		} else {
			// shifted items down
			View first = getChildAt(0);

			// may need to pan views into top
			while (first.getTop() > listTop && mFirstPosition > 0) {
				first = addViewAbove(first, mFirstPosition);
				mFirstPosition--;
			}

			// may have brought the very first child of the list in too far and
			// need to shift it back
			if (first.getTop() > listTop) {
				// offsetChildrenTopAndBottom(listTop - first.getTop());
				tryOffsetChildrenTopAndBottom(listTop - first.getTop());
			}

			int lastIndex = getChildCount() - 1;
			View last = getChildAt(lastIndex);

			// bottom view may be panned off screen
			while (last.getTop() > listBottom) {
				final PLAAbsListView.LayoutParams layoutParams = (LayoutParams) last.getLayoutParams();
				if (recycleBin.shouldRecycleViewType(layoutParams.viewType)) {
					detachViewFromParent(last);
					recycleBin.addScrapView(last);
				} else {
					removeViewInLayout(last);
				}
				last = getChildAt(--lastIndex);
			}
		}
	}

	/**
	 * Add a view as a child and make sure it is measured (if necessary) and
	 * positioned properly.
	 * 
	 * @param child The view to add
	 * @param position The position of this child
	 * @param y The y position relative to which this view will be positioned
	 * @param flowDown If true, align top edge to y. If false, align bottom edge
	 *            to y.
	 * @param childrenLeft Left edge where children should be positioned
	 * @param selected Is this position selected?
	 * @param recycled Has this view been pulled from the recycle bin? If so it
	 *            does not need to be remeasured.
	 */
	private void setupChild(final View child, final int position, final int y, final boolean flowDown,
			final int childrenLeft, final boolean selected, final boolean recycled) {

		final boolean isSelected = selected && shouldShowSelector();
		final boolean updateChildSelected = isSelected != child.isSelected();
		final int mode = mTouchMode;
		final boolean isPressed = mode > TOUCH_MODE_DOWN && mode < TOUCH_MODE_SCROLL && mMotionPosition == position;
		final boolean updateChildPressed = isPressed != child.isPressed();
		final boolean needToMeasure = !recycled || updateChildSelected || child.isLayoutRequested();

		// Respect layout params that are already in the view. Otherwise make
		// some up...
		// noinspection unchecked
		PLAAbsListView.LayoutParams p = (PLAAbsListView.LayoutParams) child.getLayoutParams();
		if (p == null) {
			p = new PLAAbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT, 0);
		}
		p.viewType = mAdapter.getItemViewType(position);

		if (recycled && !p.forceAdd || p.recycledHeaderFooter
				&& p.viewType == PLAAdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
			attachViewToParent(child, flowDown ? -1 : 0, p);
		} else {
			p.forceAdd = false;
			if (p.viewType == PLAAdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER) {
				p.recycledHeaderFooter = true;
			}
			addViewInLayout(child, flowDown ? -1 : 0, p, true);
		}

		if (updateChildSelected) {
			child.setSelected(isSelected);
		}

		if (updateChildPressed) {
			child.setPressed(isPressed);
		}

		if (mChoiceMode != CHOICE_MODE_NONE && mCheckStates != null) {
			final boolean useActivated = getContext().getApplicationInfo().targetSdkVersion >= android.os.Build.VERSION_CODES.HONEYCOMB;
			if (child instanceof Checkable) {
				((Checkable) child).setChecked(mCheckStates.get(position));
			} else if (useActivated) {
				child.setActivated(mCheckStates.get(position));
			}
		}

		if (needToMeasure) {
			final int childWidthSpec = ViewGroup.getChildMeasureSpec(mWidthMeasureSpec, mListPadding.left
					+ mListPadding.right, p.width);
			final int lpHeight = p.height;
			int childHeightSpec;
			if (lpHeight > 0) {
				childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
			} else {
				childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			}

			onMeasureChild(child, position, childWidthSpec, childHeightSpec);
			// child.measure(childWidthSpec, childHeightSpec);
		} else {
			cleanupLayoutState(child);
		}

		final int w = child.getMeasuredWidth();
		final int h = child.getMeasuredHeight();
		final int childTop = flowDown ? y : y - h;

		if (needToMeasure) {
			final int childRight = childrenLeft + w;
			final int childBottom = childTop + h;
			// child.layout(childrenLeft, childTop, childRight, childBottom);
			onLayoutChild(child, position, childrenLeft, childTop, childRight, childBottom);
		} else {
			final int offsetLeft = childrenLeft - child.getLeft();
			final int offsetTop = childTop - child.getTop();
			onOffsetChild(child, position, offsetLeft, offsetTop);
		}

		if (mCachingStarted && !child.isDrawingCacheEnabled()) {
			child.setDrawingCacheEnabled(true);
		}
	}

	/**
	 * @return Whether the list needs to show the bottom fading edge
	 */
	private boolean showingBottomFadingEdge() {
		final int childCount = getChildCount();
		final int bottomOfBottomChild = getChildAt(childCount - 1).getBottom();
		final int lastVisiblePosition = mFirstPosition + childCount - 1;

		// final int listBottom = mScrollY + getHeight() - mListPadding.bottom;
		final int listBottom = getScrollY() + getHeight() - mListPadding.bottom;

		return lastVisiblePosition < mItemCount - 1 || bottomOfBottomChild < listBottom;
	}

	/**
	 * @return Whether the list needs to show the top fading edge
	 */
	private boolean showingTopFadingEdge() {
		// final int listTop = mScrollY + mListPadding.top;
		final int listTop = getScrollY() + mListPadding.top;
		return mFirstPosition > 0 || getChildAt(0).getTop() > listTop;
	}

	/**
	 * Draws a divider for the given child in the given bounds.
	 * 
	 * @param canvas The canvas to draw to.
	 * @param bounds The bounds of the divider.
	 * @param childIndex The index of child (of the View) above the divider.
	 *            This will be -1 if there is no child above the divider to be
	 *            drawn.
	 */
	void drawDivider(final Canvas canvas, final Rect bounds, final int childIndex) {
		// This widget draws the same divider for all children
		final Drawable divider = mDivider;
		final boolean clipDivider = mClipDivider;

		if (!clipDivider) {
			divider.setBounds(bounds);
		} else {
			canvas.save();
			canvas.clipRect(bounds);
		}

		divider.draw(canvas);

		if (clipDivider) {
			canvas.restore();
		}
	}

	void drawOverscrollFooter(final Canvas canvas, final Drawable drawable, final Rect bounds) {
		final int height = drawable.getMinimumHeight();

		canvas.save();
		canvas.clipRect(bounds);

		final int span = bounds.bottom - bounds.top;
		if (span < height) {
			bounds.bottom = bounds.top + height;
		}

		drawable.setBounds(bounds);
		drawable.draw(canvas);

		canvas.restore();
	}

	void drawOverscrollHeader(final Canvas canvas, final Drawable drawable, final Rect bounds) {
		final int height = drawable.getMinimumHeight();

		canvas.save();
		canvas.clipRect(bounds);

		final int span = bounds.bottom - bounds.top;
		if (span < height) {
			bounds.top = bounds.bottom - height;
		}

		drawable.setBounds(bounds);
		drawable.draw(canvas);

		canvas.restore();
	}

	@Override
	int findMotionRow(final int y) {
		final int childCount = getChildCount();
		if (childCount > 0) {
			if (!mStackFromBottom) {
				for (int i = 0; i < childCount; i++) {
					final View v = getChildAt(i);
					if (y <= v.getBottom()) return mFirstPosition + i;
				}
			} else {
				for (int i = childCount - 1; i >= 0; i--) {
					final View v = getChildAt(i);
					if (y >= v.getTop()) return mFirstPosition + i;
				}
			}
		}
		return INVALID_POSITION;
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
	@Override
	int lookForSelectablePositionPLA(int position, final boolean lookDown) {
		final ListAdapter adapter = mAdapter;
		if (adapter == null || isInTouchMode()) return INVALID_POSITION;

		final int count = adapter.getCount();
		if (!mAreAllItemsSelectable) {
			if (lookDown) {
				position = Math.max(0, position);
				while (position < count && !adapter.isEnabled(position)) {
					position++;
				}
			} else {
				position = Math.min(position, count - 1);
				while (position >= 0 && !adapter.isEnabled(position)) {
					position--;
				}
			}

			if (position < 0 || position >= count) return INVALID_POSITION;
			return position;
		} else {
			if (position < 0 || position >= count) return INVALID_POSITION;
			return position;
		}
	}

	/**
	 * Measures the height of the given range of children (inclusive) and
	 * returns the height with this ListView's padding and divider heights
	 * included. If maxHeight is provided, the measuring will stop when the
	 * current height reaches maxHeight.
	 * 
	 * @param widthMeasureSpec The width measure spec to be given to a child's
	 *            {@link View#measure(int, int)}.
	 * @param startPosition The position of the first child to be shown.
	 * @param endPosition The (inclusive) position of the last child to be
	 *            shown. Specify {@link #NO_POSITION} if the last child should
	 *            be the last available child from the adapter.
	 * @param maxHeight The maximum height that will be returned (if all the
	 *            children don't fit in this value, this value will be
	 *            returned).
	 * @param disallowPartialChildPosition In general, whether the returned
	 *            height should only contain entire children. This is more
	 *            powerful--it is the first inclusive position at which partial
	 *            children will not be allowed. Example: it looks nice to have
	 *            at least 3 completely visible children, and in portrait this
	 *            will most likely fit; but in landscape there could be times
	 *            when even 2 children can not be completely shown, so a value
	 *            of 2 (remember, inclusive) would be good (assuming
	 *            startPosition is 0).
	 * @return The height of this ListView with the given children.
	 */
	final int measureHeightOfChildren(final int widthMeasureSpec, final int startPosition, int endPosition,
			final int maxHeight, final int disallowPartialChildPosition) {

		final ListAdapter adapter = mAdapter;
		if (adapter == null) return mListPadding.top + mListPadding.bottom;

		// Include the padding of the list
		int returnedHeight = mListPadding.top + mListPadding.bottom;
		final int dividerHeight = mDividerHeight > 0 && mDivider != null ? mDividerHeight : 0;
		// The previous height value that was less than maxHeight and contained
		// no partial children
		int prevHeightWithoutPartialChild = 0;
		int i;
		View child;

		// mItemCount - 1 since endPosition parameter is inclusive
		endPosition = endPosition == NO_POSITION ? adapter.getCount() - 1 : endPosition;
		final PLAAbsListView.RecycleBin recycleBin = mRecycler;
		final boolean recyle = recycleOnMeasure();
		final boolean[] isScrap = mIsScrap;

		for (i = startPosition; i <= endPosition; ++i) {
			child = obtainView(i, isScrap);

			measureScrapChild(child, i, widthMeasureSpec);

			if (i > 0) {
				// Count the divider for all but one child
				returnedHeight += dividerHeight;
			}

			// Recycle the view before we possibly return from the method
			if (recyle && recycleBin.shouldRecycleViewType(((LayoutParams) child.getLayoutParams()).viewType)) {
				recycleBin.addScrapView(child);
			}

			returnedHeight += child.getMeasuredHeight();

			if (returnedHeight >= maxHeight) // We went over, figure out which
												// height to return. If
												// returnedHeight > maxHeight,
				// then the i'th position did not fit completely.
				return disallowPartialChildPosition >= 0 // Disallowing is
															// enabled (> -1)
						&& i > disallowPartialChildPosition // We've past the
															// min pos
						&& prevHeightWithoutPartialChild > 0 // We have a prev
																// height
						&& returnedHeight != maxHeight // i'th child did not fit
														// completely
				? prevHeightWithoutPartialChild : maxHeight;

			if (disallowPartialChildPosition >= 0 && i >= disallowPartialChildPosition) {
				prevHeightWithoutPartialChild = returnedHeight;
			}
		}

		// At this point, we went through the range of children, and they each
		// completely fit, so return the returnedHeight
		return returnedHeight;
	}

	/**
	 * The list is empty. Clear everything out.
	 */
	@Override
	void resetList() {
		// The parent's resetList() will remove all views from the layout so we
		// need to
		// cleanup the state of our footers and headers
		clearRecycledState(mHeaderViewInfos);
		clearRecycledState(mFooterViewInfos);

		super.resetList();

		mLayoutMode = LAYOUT_NORMAL;
	}

	/**
	 * A class that represents a fixed view in a list, for example a header at
	 * the top or a footer at the bottom.
	 */
	public class FixedViewInfo {
		/** The view to add to the list */
		public View view;
		/**
		 * The data backing the view. This is returned from
		 * {@link ListAdapter#getItem(int)}.
		 */
		public Object data;
		/** <code>true</code> if the fixed view should be selectable in the list */
		public boolean isSelectable;
	}

	private static class styleable {
		public static final int[] ListView = { android.R.attr.dividerHeight, android.R.attr.footerDividersEnabled,
				android.R.attr.headerDividersEnabled, android.R.attr.overScrollFooter, android.R.attr.overScrollHeader,
				android.R.attr.choiceMode };
		public static final int ListView_dividerHeight = 0;
		public static final int ListView_footerDividersEnabled = 1;
		public static final int ListView_headerDividersEnabled = 2;
		public static final int ListView_overScrollFooter = 3;
		public static final int ListView_overScrollHeader = 4;
		public static final int ListView_choiceMode = 5;
	}

	static class SavedState extends BaseSavedState {
		SparseBooleanArray checkState;
		LongSparseArray<Boolean> checkIdState;

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(final Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(final int size) {
				return new SavedState[size];
			}
		};

		/**
		 * Constructor called from {@link #CREATOR}
		 */
		private SavedState(final Parcel in) {
			super(in);
			checkState = in.readSparseBooleanArray();
			final long[] idState = in.createLongArray();

			if (idState.length > 0) {
				checkIdState = new LongSparseArray<Boolean>();
				LongSparseArrayUtils.setValues(checkIdState, idState, Boolean.TRUE);
			}
		}

		/**
		 * Constructor called from {@link ListView#onSaveInstanceState()}
		 */
		SavedState(final Parcelable superState, final SparseBooleanArray checkState,
				final LongSparseArray<Boolean> checkIdState) {
			super(superState);
			this.checkState = checkState;
			this.checkIdState = checkIdState;
		}

		@Override
		public String toString() {
			return "ListView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " checkState="
					+ checkState + "}";
		}

		@Override
		public void writeToParcel(final Parcel out, final int flags) {
			super.writeToParcel(out, flags);
			out.writeSparseBooleanArray(checkState);
			out.writeLongArray(checkIdState != null ? LongSparseArrayUtils.getKeys(checkIdState) : new long[0]);
		}
	}

}// end of class
