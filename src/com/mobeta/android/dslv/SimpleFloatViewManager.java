package com.mobeta.android.dslv;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Simple implementation of the FloatViewManager class. Uses list items as they
 * appear in the ListView to create the floating View.
 */
public class SimpleFloatViewManager implements DragSortListView.FloatViewManager {

	private Bitmap mFloatBitmap;

	private ImageView mImageView;

	private int mFloatBGColor = Color.BLACK;

	private final ListView mListView;

	public SimpleFloatViewManager(final ListView lv) {
		mListView = lv;
	}

	/**
	 * This simple implementation creates a Bitmap copy of the list item
	 * currently shown at ListView <code>position</code>.
	 */
	@Override
	public View onCreateFloatView(final int position) {
		// Guaranteed that this will not be null? I think so. Nope, got
		// a NullPointerException once...
		final View v = mListView.getChildAt(position + mListView.getHeaderViewsCount()
				- mListView.getFirstVisiblePosition());

		if (v == null) return null;

		v.setPressed(false);

		// Create a copy of the drawing cache so that it does not get
		// recycled by the framework when the list tries to clean up memory
		// v.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		v.setDrawingCacheEnabled(true);
		mFloatBitmap = Bitmap.createBitmap(v.getDrawingCache());
		v.setDrawingCacheEnabled(false);

		if (mImageView == null) {
			mImageView = new ImageView(mListView.getContext());
		}
		mImageView.setBackgroundColor(mFloatBGColor);
		mImageView.setPadding(0, 0, 0, 0);
		mImageView.setImageBitmap(mFloatBitmap);
		mImageView.setLayoutParams(new ViewGroup.LayoutParams(v.getWidth(), v.getHeight()));

		return mImageView;
	}

	/**
	 * Removes the Bitmap from the ImageView created in onCreateFloatView() and
	 * tells the system to recycle it.
	 */
	@Override
	public void onDestroyFloatView(final View floatView) {
		((ImageView) floatView).setImageDrawable(null);

		mFloatBitmap.recycle();
		mFloatBitmap = null;
	}

	/**
	 * This does nothing
	 */
	@Override
	public void onDragFloatView(final View floatView, final Point position, final Point touch) {
		// do nothing
	}

	public void setBackgroundColor(final int color) {
		mFloatBGColor = color;
	}

}
