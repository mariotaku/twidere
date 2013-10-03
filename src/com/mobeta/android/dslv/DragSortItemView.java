package com.mobeta.android.dslv;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * Lightweight ViewGroup that wraps list items obtained from user's ListAdapter.
 * ItemView expects a single child that has a definite height (i.e. the child's
 * layout height is not MATCH_PARENT). The width of ItemView will always match
 * the width of its child (that is, the width MeasureSpec given to ItemView is
 * passed directly to the child, and the ItemView measured width is set to the
 * child's measured width). The height of ItemView can be anything; the
 * 
 * 
 * The purpose of this class is to optimize slide shuffle animations.
 */
public class DragSortItemView extends ViewGroup {

	private int mGravity = Gravity.TOP;

	public DragSortItemView(final Context context) {
		super(context);

		// always init with standard ListView layout params
		setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		// setClipChildren(true);
	}

	public int getGravity() {
		return mGravity;
	}

	public void setGravity(final int gravity) {
		mGravity = gravity;
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		final View child = getChildAt(0);

		if (child == null) return;

		if (mGravity == Gravity.TOP) {
			child.layout(0, 0, getMeasuredWidth(), child.getMeasuredHeight());
		} else {
			child.layout(0, getMeasuredHeight() - child.getMeasuredHeight(), getMeasuredWidth(), getMeasuredHeight());
		}
	}

	/**
     * 
     */
	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

		int height = MeasureSpec.getSize(heightMeasureSpec);
		final int width = MeasureSpec.getSize(widthMeasureSpec);

		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);

		final View child = getChildAt(0);
		if (child == null) {
			setMeasuredDimension(0, width);
			return;
		}

		if (child.isLayoutRequested()) {
			// Always let child be as tall as it wants.
			measureChild(child, widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		}

		if (heightMode == MeasureSpec.UNSPECIFIED) {
			final ViewGroup.LayoutParams lp = getLayoutParams();

			if (lp.height > 0) {
				height = lp.height;
			} else {
				height = child.getMeasuredHeight();
			}
		}

		setMeasuredDimension(width, height);
	}

}
