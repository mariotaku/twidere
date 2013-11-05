/*******************************************************************************
 * Copyright 2012 huewu.yang <hueuw.yang@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package com.huewu.pla.lib;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;

import com.huewu.pla.lib.internal.PLAListView;

import org.mariotaku.twidere.R;

/**
 * @author huewu.ynag
 * @date 2012-11-06
 */
public class MultiColumnListView extends PLAListView {

	@SuppressWarnings("unused")
	private static final String TAG = "MultiColumnListView";

	private static final int DEFAULT_COLUMN_NUMBER = 2;

	private int mColumnNumber = 2;
	private Column[] mColumns = null;
	private Column mFixedColumn = null; // column for footers & headers.
	private final SparseIntArray mItems = new SparseIntArray();

	private int mColumnPaddingLeft = 0;
	private int mColumnPaddingRight = 0;

	private final Rect mFrameRect = new Rect();

	public MultiColumnListView(final Context context) {
		super(context);
		init(null);
	}

	public MultiColumnListView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public MultiColumnListView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	@Override
	protected int getFillChildBottom() {
		// return smallest bottom value.
		// in order to determine fill down or not... (calculate below space)
		int result = Integer.MAX_VALUE;
		for (final Column c : mColumns) {
			final int bottom = c.getBottom();
			result = result > bottom ? bottom : result;
		}
		return result;
	}

	@Override
	protected int getFillChildTop() {
		// find largest column.
		int result = Integer.MIN_VALUE;
		for (final Column c : mColumns) {
			final int top = c.getTop();
			result = result < top ? top : result;
		}
		return result;
	}

	@Override
	protected int getItemBottom(final int pos) {
		if (isHeaderOrFooterPosition(pos)) return mFixedColumn.getTop();

		final int colIndex = mItems.get(pos, -1);
		if (colIndex == -1) return getFillChildTop();

		return mColumns[colIndex].getTop();
	}

	@Override
	protected int getItemLeft(final int pos) {

		if (isHeaderOrFooterPosition(pos)) return mFixedColumn.getColumnLeft();

		return getColumnLeft(pos);
	}

	@Override
	protected int getItemTop(final int pos) {
		// footer view should be place below the last column item.
		if (isHeaderOrFooterPosition(pos)) return mFixedColumn.getBottom();

		final int colIndex = mItems.get(pos, -1);
		if (colIndex == -1) return getFillChildBottom();

		return mColumns[colIndex].getBottom();
	}

	@Override
	protected int getScrollChildBottom() {
		// return largest bottom value.
		// for checking scrolling region...
		int result = Integer.MIN_VALUE;
		for (final Column c : mColumns) {
			final int bottom = c.getBottom();
			result = result < bottom ? bottom : result;
		}
		return result;
	}

	@Override
	protected int getScrollChildTop() {
		// find largest column.
		int result = Integer.MAX_VALUE;
		for (final Column c : mColumns) {
			final int top = c.getTop();
			result = result > top ? top : result;
		}
		return result;
	}

	@Override
	protected int modifyFlingInitialVelocity(final int initialVelocity) {
		return (int) (initialVelocity / (mColumnNumber / 1.5f));
	}

	@Override
	protected void onAdjustChildViews(final boolean down) {
		final int firstItem = getFirstVisiblePosition();
		if (down == false && firstItem == 0) {
			final int firstColumnTop = mColumns[0].getTop();
			for (final Column c : mColumns) {
				final int top = c.getTop();
				// align all column's top to 0's column.
				c.offsetTopAndBottom(firstColumnTop - top);
			}
		}
		super.onAdjustChildViews(down);
	}

	@Override
	protected void onItemAddedToList(final int position, final boolean flow) {
		super.onItemAddedToList(position, flow);

		if (isHeaderOrFooterPosition(position) == false) {
			final Column col = getNextColumn(flow, position);
			mItems.append(position, col.getIndex());
		}
	}

	@Override
	protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
		super.onLayout(changed, l, t, r, b);
		// TODO the adapter status may be changed. what should i do here...
	}

	@Override
	protected void onLayoutSync(final int syncPos) {
		for (final Column c : mColumns) {
			c.save();
		}
	}

	@Override
	protected void onLayoutSyncFinished(final int syncPos) {
		for (final Column c : mColumns) {
			c.clear();
		}
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		final int width = (getMeasuredWidth() - mListPadding.left - mListPadding.right - mColumnPaddingLeft - mColumnPaddingRight)
				/ mColumnNumber;

		for (int index = 0; index < mColumnNumber; ++index) {
			mColumns[index].mColumnWidth = width;
			mColumns[index].mColumnLeft = mListPadding.left + mColumnPaddingLeft + width * index;
		}

		mFixedColumn.mColumnLeft = mListPadding.left;
		mFixedColumn.mColumnWidth = getMeasuredWidth();
	}

	@Override
	protected void onMeasureChild(final View child, final int position, final int widthMeasureSpec,
			final int heightMeasureSpec) {
		if (isFixedView(child)) {
			child.measure(widthMeasureSpec, heightMeasureSpec);
		} else {
			child.measure(MeasureSpec.EXACTLY | getColumnWidth(position), heightMeasureSpec);
		}
	}

	private int getColumnLeft(final int pos) {
		final int colIndex = mItems.get(pos, -1);

		if (colIndex == -1) return 0;

		return mColumns[colIndex].getColumnLeft();
	}

	// ////////////////////////////////////////////////////////////////////////////
	// Private Methods...
	// ////////////////////////////////////////////////////////////////////////////

	private int getColumnWidth(final int pos) {
		final int colIndex = mItems.get(pos, -1);

		if (colIndex == -1) return 0;

		return mColumns[colIndex].getColumnWidth();
	}

	// flow If flow is true, align top edge to y. If false, align bottom edge to
	// y.
	private Column getNextColumn(final boolean flow, int position) {

		// we already have this item...
		final int colIndex = mItems.get(position, -1);
		if (colIndex != -1) return mColumns[colIndex];

		// adjust position (exclude headers...)
		position = Math.max(0, position - getHeaderViewsCount());

		final int lastVisiblePos = Math.max(0, position);
		if (lastVisiblePos < mColumnNumber) return mColumns[lastVisiblePos];

		if (flow) // find column which has the smallest bottom value.
			return gettBottomColumn();
		else
			// find column which has the smallest top value.
			return getTopColumn();
	}

	private Column gettBottomColumn() {
		Column result = mColumns[0];
		for (final Column c : mColumns) {
			result = result.getBottom() > c.getBottom() ? c : result;
		}

		if (DEBUG) {
			Log.d("Column", "get Shortest Bottom Column: " + result.getIndex());
		}
		return result;
	}

	private Column getTopColumn() {
		Column result = mColumns[0];
		for (final Column c : mColumns) {
			result = result.getTop() > c.getTop() ? c : result;
		}
		return result;
	}

	private void init(final AttributeSet attrs) {
		getWindowVisibleDisplayFrame(mFrameRect);

		if (attrs == null) {
			// default column number is 2.
			mColumnNumber = DEFAULT_COLUMN_NUMBER;
		} else {
			final TypedArray a = getContext().obtainStyledAttributes(attrs, styleable.PinterestLikeAdapterView);

			final int defColNumber = a.getInteger(styleable.PinterestLikeAdapterView_numColumns, -1);

			if (defColNumber != -1) {
				mColumnNumber = defColNumber;
			} else {
				mColumnNumber = DEFAULT_COLUMN_NUMBER;
			}

			mColumnPaddingLeft = a.getDimensionPixelSize(styleable.PinterestLikeAdapterView_plaColumnPaddingLeft, 0);
			mColumnPaddingRight = a.getDimensionPixelSize(styleable.PinterestLikeAdapterView_plaColumnPaddingRight, 0);
			a.recycle();
		}

		mColumns = new Column[mColumnNumber];
		for (int i = 0; i < mColumnNumber; ++i) {
			mColumns[i] = new Column(i);
		}

		mFixedColumn = new FixedColumn();
	}

	private boolean isHeaderOrFooterPosition(final int pos) {
		final int type = mAdapter.getItemViewType(pos);
		return type == ITEM_VIEW_TYPE_HEADER_OR_FOOTER;
	}

	private class Column {

		private final int mIndex;
		private int mColumnWidth;
		private int mColumnLeft;
		private int mSynchedTop = 0;
		private int mSynchedBottom = 0;

		// TODO is it ok to use item position info to identify item??

		public Column(final int index) {
			mIndex = index;
		}

		public void clear() {
			mSynchedTop = 0;
			mSynchedBottom = 0;
		}

		public int getBottom() {
			// find biggest value.
			int bottom = Integer.MIN_VALUE;
			final int childCount = getChildCount();

			for (int index = 0; index < childCount; ++index) {
				final View v = getChildAt(index);

				if (v.getLeft() != mColumnLeft && isFixedView(v) == false) {
					continue;
				}
				bottom = bottom < v.getBottom() ? v.getBottom() : bottom;
			}

			if (bottom == Integer.MIN_VALUE) return mSynchedBottom; // no child
																	// for this
																	// column..
			return bottom;
		}

		public int getColumnLeft() {
			return mColumnLeft;
		}

		public int getColumnWidth() {
			return mColumnWidth;
		}

		public int getIndex() {
			return mIndex;
		}

		public int getTop() {
			// find smallest value.
			int top = Integer.MAX_VALUE;
			final int childCount = getChildCount();
			for (int index = 0; index < childCount; ++index) {
				final View v = getChildAt(index);
				if (v.getLeft() != mColumnLeft && isFixedView(v) == false) {
					continue;
				}
				top = top > v.getTop() ? v.getTop() : top;
			}

			if (top == Integer.MAX_VALUE) return mSynchedTop; // no child for
																// this column.
																// just return
																// saved sync
																// top..
			return top;
		}

		public void offsetTopAndBottom(final int offset) {
			if (offset == 0) return;

			// find biggest value.
			final int childCount = getChildCount();

			for (int index = 0; index < childCount; ++index) {
				final View v = getChildAt(index);

				if (v.getLeft() != mColumnLeft && isFixedView(v) == false) {
					continue;
				}

				v.offsetTopAndBottom(offset);
			}
		}

		public void save() {
			mSynchedTop = 0;
			mSynchedBottom = getTop(); // getBottom();
		}
	}// end of inner class Column

	private class FixedColumn extends Column {

		public FixedColumn() {
			super(Integer.MAX_VALUE);
		}

		@Override
		public int getBottom() {
			return getScrollChildBottom();
		}

		@Override
		public int getTop() {
			return getScrollChildTop();
		}

	}

	static class styleable {
		public static final int[] PinterestLikeAdapterView = { android.R.attr.numColumns, R.attr.plaColumnPaddingLeft,
				R.attr.plaColumnPaddingRight };
		public static final int PinterestLikeAdapterView_numColumns = 0;
		public static final int PinterestLikeAdapterView_plaColumnPaddingLeft = 1;
		public static final int PinterestLikeAdapterView_plaColumnPaddingRight = 2;
	}

}
