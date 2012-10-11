//package org.mariotaku.popupmenu;
//
//import org.mariotaku.internal.menu.MenuAdapter;
//import org.mariotaku.internal.menu.MenuImpl;
//import org.mariotaku.twidere.R;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Rect;
//import android.util.TypedValue;
//import android.view.Display;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.MenuItem;
//import android.view.MotionEvent;
//import android.view.View;
//import android.view.View.MeasureSpec;
//import android.view.View.OnTouchListener;
//import android.view.ViewGroup.LayoutParams;
//import android.view.WindowManager;
//import android.widget.AdapterView;
//import android.widget.AdapterView.OnItemClickListener;
//import android.widget.FrameLayout;
//import android.widget.ListView;
//import android.widget.PopupWindow;
//import android.widget.PopupWindow.OnDismissListener;
//
//class PopupMenuCompat implements OnDismissListener, OnItemClickListener, OnTouchListener {
//
//	private FrameLayout mRootView;
//	private ListView mListView;
//
//	private OnMenuItemClickListener mItemClickListener;
//	private OnDismissListener mDismissListener;
//
//	private Menu mMenu;
//	private final Context context;
//	private final Resources res;
//	private final View mAnchorView;
//	private final PopupWindow mWindow;
//	private final WindowManager mWindowManager;
//
//	private boolean mDidAction;
//
//	private int rootWidth = 0, mPosX, mPosY;
//
//	private final MenuAdapter mAdapter;
//
//	private final OnTouchListener mViewTouchListener = new OnTouchListener() {
//
//		@Override
//		public boolean onTouch(final View v, final MotionEvent event) {
//			return true;
//		}
//
//	};
//
//	private int mGravity = Gravity.NO_GRAVITY;
//
//	/**
//	 * Constructor for default vertical layout
//	 * 
//	 * @param context Context
//	 */
//	PopupMenuCompat(final Context context, final View view) {
//		this.context = context;
//		res = context.getResources();
//		mAnchorView = view;
//		mWindow = new PopupWindow(context);
//		mWindow.setTouchInterceptor(this);
//		mWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
//		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//		mAdapter = new MenuAdapter(context);
//		mMenu = new MenuImpl(context, mAdapter);
//		setView();
//
//	}
//
//	/**
//	 * Dismiss the popup window.
//	 */
//	@Override
//	public void dismiss() {
//		if (isPopupWindowShowing()) {
//			mWindow.dismiss();
//		}
//	}
//
//	@Override
//	public Menu getMenu() {
//		return mMenu;
//	}
//
//	public MenuInflater getMenuInflater() {
//		return new MenuInflater(context);
//	}
//
//	@Override
//	public void inflate(final int menuRes) {
//		new MenuInflater(context).inflate(menuRes, mMenu);
//	}
//
//	@Override
//	public void onDismiss() {
//		if (!mDidAction && mDismissListener != null) {
//			mDismissListener.onDismiss(this);
//		}
//	}
//
//	@Override
//	public void onItemClick(final AdapterView<?> adapter, final View view, final int position, final long id) {
//		mDidAction = true;
//		dismiss();
//
//		final MenuItem item = mAdapter.getItem(position);
//		if (item.hasSubMenu()) {
//			showMenu(item.getSubMenu(), false);
//		} else {
//			if (mItemClickListener != null) {
//				mItemClickListener.onMenuItemClick(item);
//			}
//		}
//	}
//
//	@Override
//	public boolean onTouch(final View v, final MotionEvent event) {
//		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
//			mWindow.dismiss();
//
//			return true;
//		}
//
//		return false;
//	}
//
//	public void setAnchorByTouch(final boolean enabled) {
//		mAnchorView.setOnTouchListener(enabled ? mViewTouchListener : null);
//	}
//
//	@Override
//	public void setMenu(final Menu menu) {
//		mMenu = menu;
//	}
//
//	/**
//	 * Set listener for window dismissed. This listener will only be fired if
//	 * the quickaction dialog is dismissed by clicking outside the dialog or
//	 * clicking on sticky item.
//	 */
//	@Override
//	public void setOnDismissListener(final PopupMenu.OnDismissListener listener) {
//		mWindow.setOnDismissListener(listener != null ? this : null);
//
//		mDismissListener = listener;
//	}
//
//	/**
//	 * Set listener for action item clicked.
//	 * 
//	 * @param listener Listener
//	 */
//	@Override
//	public void setOnMenuItemClickListener(final OnMenuItemClickListener listener) {
//		mItemClickListener = listener;
//	}
//
//	@Override
//	public void show() {
//		if (isPopupWindowShowing()) {
//			dismiss();
//		}
//		showMenu(getMenu(), true);
//	}
//
//	private boolean isPopupWindowShowing() {
//		if (mWindow == null) return false;
//		return mWindow.isShowing();
//	}
//
//	/**
//	 * On pre show
//	 */
//	private void preShow() {
//		if (mListView == null)
//			throw new IllegalStateException("setContentView was not called with a view to display.");
//
//		final TypedValue value = new TypedValue();
//		context.getTheme().resolveAttribute(R.attr.popupBackground, value, true);
//
//		mWindow.setBackgroundDrawable(res.getDrawable(value.resourceId));
//
//		mWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
//		mWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
//		mWindow.setTouchable(true);
//		mWindow.setFocusable(true);
//		mWindow.setOutsideTouchable(true);
//	}
//
//	@SuppressWarnings("deprecation")
//	private void setAnchor(final View anchor) {
//
//		final int horizontal_gravity = Gravity.LEFT;
//		int vertical_gravity = 0;
//
//		preShow();
//
//		mDidAction = false;
//
//		// final Rect anchorRect = new Rect(location[0], location[1],
//		// location[0] + anchor.getWidth(), location[1]
//		// + anchor.getHeight());
//
//		final int[] location = new int[2];
//		anchor.getLocationOnScreen(location);
//		final Rect visibleRect = new Rect();
//		anchor.getGlobalVisibleRect(visibleRect);
//
//		final int widthSpec = MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY);
//		final int heightSpec = MeasureSpec.makeMeasureSpec(LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY);
//
//		mRootView.measure(widthSpec, heightSpec);
//
//		final int rootHeight = mRootView.getMeasuredHeight();
//
//		if (rootWidth == 0) {
//			rootWidth = mRootView.getMeasuredWidth();
//		}
//
//		final Display disp = mWindowManager.getDefaultDisplay();
//		final int screenWidth = disp.getWidth(), screenHeight = disp.getHeight();
//
//		// automatically get X coord of popup (top left)
//		if (visibleRect.left + rootWidth > screenWidth) {
//			mPosX = visibleRect.left - (rootWidth - anchor.getWidth());
//			mPosX = mPosX < 0 ? 0 : mPosX;
//
//		} else {
//			if (anchor.getWidth() > rootWidth) {
//				mPosX = visibleRect.centerX() - rootWidth / 2;
//			} else {
//				mPosX = visibleRect.left;
//			}
//
//		}
//
//		final int dyBottom = screenHeight - visibleRect.bottom;
//
//		final boolean dropDown = rootHeight < dyBottom;
//
//		if (dropDown) {
//			vertical_gravity = Gravity.TOP;
//			mPosY = visibleRect.bottom;
//		} else {
//
//			vertical_gravity = Gravity.BOTTOM;
//			mPosY = screenHeight - visibleRect.top;
//		}
//
//		mGravity = horizontal_gravity | vertical_gravity;
//
//		setAnimationStyle(screenWidth, visibleRect.centerX(), !dropDown);
//
//	}
//
//	/**
//	 * Set animation style
//	 * 
//	 * @param screenWidth screen width
//	 * @param requestedX distance from left edge
//	 * @param onTop flag to indicate where the popup should be displayed. Set
//	 *            TRUE if displayed on top of anchor view and vice versa
//	 */
//	private void setAnimationStyle(final int screenWidth, final int requestedX, final boolean popUp) {
//
//		if (requestedX <= screenWidth / 4) {
//			mWindow.setAnimationStyle(popUp ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
//		} else if (requestedX > screenWidth / 4 && requestedX < 3 * (screenWidth / 4)) {
//			mWindow.setAnimationStyle(popUp ? R.style.Animations_PopUpMenu_Center
//					: R.style.Animations_PopDownMenu_Center);
//		} else {
//			mWindow.setAnimationStyle(popUp ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
//		}
//	}
//
//	/**
//	 * Set root view.
//	 * 
//	 */
//	private void setView() {
//
//		mRootView = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.popup_list, null);
//		mRootView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//		mListView = (ListView) mRootView.findViewById(android.R.id.list);
//		mListView.setAdapter(mAdapter);
//		mListView.setOnItemClickListener(this);
//		mWindow.setContentView(mRootView);
//	}
//
//	private void showMenu(final Menu menu, final boolean set_anchor) {
//		mAdapter.setMenu(menu);
//		if (set_anchor) {
//			setAnchor(mAnchorView);
//		}
//		mWindow.showAtLocation(mAnchorView, mGravity, mPosX, mPosY);
//	}
//
//}
