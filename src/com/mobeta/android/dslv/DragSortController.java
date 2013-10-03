package com.mobeta.android.dslv;

import android.graphics.Point;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;

/**
 * Class that starts and stops item drags on a {@link DragSortListView} based on
 * touch gestures. This class also inherits from {@link SimpleFloatViewManager},
 * which provides basic float View creation.
 * 
 * An instance of this class is meant to be passed to the methods
 * {@link DragSortListView#setTouchListener()} and
 * {@link DragSortListView#setFloatViewManager()} of your
 * {@link DragSortListView} instance.
 */
public class DragSortController extends SimpleFloatViewManager implements View.OnTouchListener,
		GestureDetector.OnGestureListener {

	/**
	 * Drag init mode enum.
	 */
	public static final int ON_DOWN = 0;
	public static final int ON_DRAG = 1;
	public static final int ON_LONG_PRESS = 2;

	private int mDragInitMode = ON_DOWN;

	private boolean mSortEnabled = true;

	/**
	 * Remove mode enum.
	 */
	public static final int CLICK_REMOVE = 0;
	public static final int FLING_REMOVE = 1;

	/**
	 * The current remove mode.
	 */
	private int mRemoveMode;

	private boolean mRemoveEnabled = false;
	private boolean mIsRemoving = false;

	private final GestureDetector mDetector;

	private final GestureDetector mFlingRemoveDetector;

	private final int mTouchSlop;

	public static final int MISS = -1;

	private int mHitPos = MISS;
	private int mFlingHitPos = MISS;

	private int mClickRemoveHitPos = MISS;

	private final int[] mTempLoc = new int[2];

	private int mItemX;
	private int mItemY;

	private int mCurrX;
	private int mCurrY;

	private boolean mDragging = false;

	private final float mFlingSpeed = 500f;

	private int mDragHandleId;

	private int mClickRemoveId;

	private int mFlingHandleId;
	private boolean mCanDrag;

	private final DragSortListView mDslv;
	private int mPositionX;

	private final GestureDetector.OnGestureListener mFlingRemoveListener = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public final boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
				final float velocityY) {
			// Log.d("mobeta", "on fling remove called");
			if (mRemoveEnabled && mIsRemoving) {
				final int w = mDslv.getWidth();
				final int minPos = w / 5;
				if (velocityX > mFlingSpeed) {
					if (mPositionX > -minPos) {
						mDslv.stopDragWithVelocity(true, velocityX);
					}
				} else if (velocityX < -mFlingSpeed) {
					if (mPositionX < minPos) {
						mDslv.stopDragWithVelocity(true, velocityX);
					}
				}
				mIsRemoving = false;
			}
			return false;
		}
	};

	/**
	 * Calls {@link #DragSortController(DragSortListView, int)} with a 0 drag
	 * handle id, FLING_RIGHT_REMOVE remove mode, and ON_DOWN drag init. By
	 * default, sorting is enabled, and removal is disabled.
	 * 
	 * @param dslv The DSLV instance
	 */
	public DragSortController(final DragSortListView dslv) {
		this(dslv, 0, ON_DOWN, FLING_REMOVE);
	}

	public DragSortController(final DragSortListView dslv, final int dragHandleId, final int dragInitMode,
			final int removeMode) {
		this(dslv, dragHandleId, dragInitMode, removeMode, 0);
	}

	public DragSortController(final DragSortListView dslv, final int dragHandleId, final int dragInitMode,
			final int removeMode, final int clickRemoveId) {
		this(dslv, dragHandleId, dragInitMode, removeMode, clickRemoveId, 0);
	}

	/**
	 * By default, sorting is enabled, and removal is disabled.
	 * 
	 * @param dslv The DSLV instance
	 * @param dragHandleId The resource id of the View that represents the drag
	 *            handle in a list item.
	 */
	public DragSortController(final DragSortListView dslv, final int dragHandleId, final int dragInitMode,
			final int removeMode, final int clickRemoveId, final int flingHandleId) {
		super(dslv);
		mDslv = dslv;
		mDetector = new GestureDetector(dslv.getContext(), this);
		mFlingRemoveDetector = new GestureDetector(dslv.getContext(), mFlingRemoveListener);
		mFlingRemoveDetector.setIsLongpressEnabled(false);
		mTouchSlop = ViewConfiguration.get(dslv.getContext()).getScaledTouchSlop();
		mDragHandleId = dragHandleId;
		mClickRemoveId = clickRemoveId;
		mFlingHandleId = flingHandleId;
		setRemoveMode(removeMode);
		setDragInitMode(dragInitMode);
	}

	/**
	 * Checks for the touch of an item's drag handle (specified by
	 * {@link #setDragHandleId(int)}), and returns that item's position if a
	 * drag handle touch was detected.
	 * 
	 * @param ev The ACTION_DOWN MotionEvent.
	 * 
	 * @return The list position of the item whose drag handle was touched; MISS
	 *         if unsuccessful.
	 */
	public int dragHandleHitPosition(final MotionEvent ev) {
		return viewIdHitPosition(ev, mDragHandleId);
	}

	public int flingHandleHitPosition(final MotionEvent ev) {
		return viewIdHitPosition(ev, mFlingHandleId);
	}

	public int getDragInitMode() {
		return mDragInitMode;
	}

	public int getRemoveMode() {
		return mRemoveMode;
	}

	public boolean isRemoveEnabled() {
		return mRemoveEnabled;
	}

	public boolean isSortEnabled() {
		return mSortEnabled;
	}

	@Override
	public boolean onDown(final MotionEvent ev) {
		if (mRemoveEnabled && mRemoveMode == CLICK_REMOVE) {
			mClickRemoveHitPos = viewIdHitPosition(ev, mClickRemoveId);
		}

		mHitPos = startDragPosition(ev);
		if (mHitPos != MISS && mDragInitMode == ON_DOWN) {
			startDrag(mHitPos, (int) ev.getX() - mItemX, (int) ev.getY() - mItemY);
		}

		mIsRemoving = false;
		mCanDrag = true;
		mPositionX = 0;
		mFlingHitPos = startFlingPosition(ev);

		return true;
	}

	/**
	 * Overrides to provide fading when slide removal is enabled.
	 */
	@Override
	public void onDragFloatView(final View floatView, final Point position, final Point touch) {

		if (mRemoveEnabled && mIsRemoving) {
			mPositionX = position.x;
		}
	}

	// complete the OnGestureListener interface
	@Override
	public final boolean onFling(final MotionEvent e1, final MotionEvent e2, final float velocityX,
			final float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(final MotionEvent e) {
		// Log.d("mobeta", "lift listener long pressed");
		if (mHitPos != MISS && mDragInitMode == ON_LONG_PRESS) {
			mDslv.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
			startDrag(mHitPos, mCurrX - mItemX, mCurrY - mItemY);
		}
	}

	@Override
	public boolean onScroll(final MotionEvent e1, final MotionEvent e2, final float distanceX, final float distanceY) {

		final int x1 = (int) e1.getX();
		final int y1 = (int) e1.getY();
		final int x2 = (int) e2.getX();
		final int y2 = (int) e2.getY();
		final int deltaX = x2 - mItemX;
		final int deltaY = y2 - mItemY;

		if (mCanDrag && !mDragging && (mHitPos != MISS || mFlingHitPos != MISS)) {
			if (mHitPos != MISS) {
				if (mDragInitMode == ON_DRAG && Math.abs(y2 - y1) > mTouchSlop && mSortEnabled) {
					startDrag(mHitPos, deltaX, deltaY);
				} else if (mDragInitMode != ON_DOWN && Math.abs(x2 - x1) > mTouchSlop && mRemoveEnabled) {
					mIsRemoving = true;
					startDrag(mFlingHitPos, deltaX, deltaY);
				}
			} else if (mFlingHitPos != MISS) {
				if (Math.abs(x2 - x1) > mTouchSlop && mRemoveEnabled) {
					mIsRemoving = true;
					startDrag(mFlingHitPos, deltaX, deltaY);
				} else if (Math.abs(y2 - y1) > mTouchSlop) {
					mCanDrag = false; // if started to scroll the list then
										// don't allow sorting nor
										// fling-removing
				}
			}
		}
		// return whatever
		return false;
	}

	// complete the OnGestureListener interface
	@Override
	public void onShowPress(final MotionEvent ev) {
		// do nothing
	}

	// complete the OnGestureListener interface
	@Override
	public boolean onSingleTapUp(final MotionEvent ev) {
		if (mRemoveEnabled && mRemoveMode == CLICK_REMOVE) {
			if (mClickRemoveHitPos != MISS) {
				mDslv.removeItem(mClickRemoveHitPos - mDslv.getHeaderViewsCount());
			}
		}
		return true;
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent ev) {
		if (!mDslv.isDragEnabled() || mDslv.listViewIntercepted()) return false;

		mDetector.onTouchEvent(ev);
		if (mRemoveEnabled && mDragging && mRemoveMode == FLING_REMOVE) {
			mFlingRemoveDetector.onTouchEvent(ev);
		}

		final int action = ev.getAction() & MotionEvent.ACTION_MASK;
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mCurrX = (int) ev.getX();
				mCurrY = (int) ev.getY();
				break;
			case MotionEvent.ACTION_UP:
				if (mRemoveEnabled && mIsRemoving) {
					final int x = mPositionX >= 0 ? mPositionX : -mPositionX;
					final int removePoint = mDslv.getWidth() / 2;
					if (x > removePoint) {
						mDslv.stopDragWithVelocity(true, 0);
					}
				}
			case MotionEvent.ACTION_CANCEL:
				mIsRemoving = false;
				mDragging = false;
				break;
		}

		return false;
	}

	/**
	 * Set the resource id for the View that represents click removal button.
	 * 
	 * @param id An android resource id.
	 */
	public void setClickRemoveId(final int id) {
		mClickRemoveId = id;
	}

	/**
	 * Set the resource id for the View that represents the drag handle in a
	 * list item.
	 * 
	 * @param id An android resource id.
	 */
	public void setDragHandleId(final int id) {
		mDragHandleId = id;
	}

	/**
	 * Set how a drag is initiated. Needs to be one of {@link ON_DOWN},
	 * {@link ON_DRAG}, or {@link ON_LONG_PRESS}.
	 * 
	 * @param mode The drag init mode.
	 */
	public void setDragInitMode(final int mode) {
		mDragInitMode = mode;
	}

	/**
	 * Set the resource id for the View that represents the fling handle in a
	 * list item.
	 * 
	 * @param id An android resource id.
	 */
	public void setFlingHandleId(final int id) {
		mFlingHandleId = id;
	}

	/**
	 * Enable/Disable item removal without affecting remove mode.
	 */
	public void setRemoveEnabled(final boolean enabled) {
		mRemoveEnabled = enabled;
	}

	/**
	 * One of {@link CLICK_REMOVE}, {@link FLING_RIGHT_REMOVE},
	 * {@link FLING_LEFT_REMOVE}, {@link SLIDE_RIGHT_REMOVE}, or
	 * {@link SLIDE_LEFT_REMOVE}.
	 */
	public void setRemoveMode(final int mode) {
		mRemoveMode = mode;
	}

	/**
	 * Enable/Disable list item sorting. Disabling is useful if only item
	 * removal is desired. Prevents drags in the vertical direction.
	 * 
	 * @param enabled Set <code>true</code> to enable list item sorting.
	 */
	public void setSortEnabled(final boolean enabled) {
		mSortEnabled = enabled;
	}

	/**
	 * Sets flags to restrict certain motions of the floating View based on
	 * DragSortController settings (such as remove mode). Starts the drag on the
	 * DragSortListView.
	 * 
	 * @param position The list item position (includes headers).
	 * @param deltaX Touch x-coord minus left edge of floating View.
	 * @param deltaY Touch y-coord minus top edge of floating View.
	 * 
	 * @return True if drag started, false otherwise.
	 */
	public boolean startDrag(final int position, final int deltaX, final int deltaY) {

		int dragFlags = 0;
		if (mSortEnabled && !mIsRemoving) {
			dragFlags |= DragSortListView.DRAG_POS_Y | DragSortListView.DRAG_NEG_Y;
		}
		if (mRemoveEnabled && mIsRemoving) {
			dragFlags |= DragSortListView.DRAG_POS_X;
			dragFlags |= DragSortListView.DRAG_NEG_X;
		}

		mDragging = mDslv.startDrag(position - mDslv.getHeaderViewsCount(), dragFlags, deltaX, deltaY);
		return mDragging;
	}

	/**
	 * Get the position to start dragging based on the ACTION_DOWN MotionEvent.
	 * This function simply calls {@link #dragHandleHitPosition(MotionEvent)}.
	 * Override to change drag handle behavior; this function is called
	 * internally when an ACTION_DOWN event is detected.
	 * 
	 * @param ev The ACTION_DOWN MotionEvent.
	 * 
	 * @return The list position to drag if a drag-init gesture is detected;
	 *         MISS if unsuccessful.
	 */
	public int startDragPosition(final MotionEvent ev) {
		return dragHandleHitPosition(ev);
	}

	public int startFlingPosition(final MotionEvent ev) {
		return mRemoveMode == FLING_REMOVE ? flingHandleHitPosition(ev) : MISS;
	}

	public int viewIdHitPosition(final MotionEvent ev, final int id) {
		final int x = (int) ev.getX();
		final int y = (int) ev.getY();

		final int touchPos = mDslv.pointToPosition(x, y); // includes
															// headers/footers

		final int numHeaders = mDslv.getHeaderViewsCount();
		final int numFooters = mDslv.getFooterViewsCount();
		final int count = mDslv.getCount();

		// Log.d("mobeta", "touch down on position " + itemnum);
		// We're only interested if the touch was on an
		// item that's not a header or footer.
		if (touchPos != AdapterView.INVALID_POSITION && touchPos >= numHeaders && touchPos < count - numFooters) {
			final View item = mDslv.getChildAt(touchPos - mDslv.getFirstVisiblePosition());
			final int rawX = (int) ev.getRawX();
			final int rawY = (int) ev.getRawY();

			final View dragBox = id == 0 ? item : (View) item.findViewById(id);
			if (dragBox != null) {
				dragBox.getLocationOnScreen(mTempLoc);

				if (rawX > mTempLoc[0] && rawY > mTempLoc[1] && rawX < mTempLoc[0] + dragBox.getWidth()
						&& rawY < mTempLoc[1] + dragBox.getHeight()) {

					mItemX = item.getLeft();
					mItemY = item.getTop();

					return touchPos;
				}
			}
		}

		return MISS;
	}

}
