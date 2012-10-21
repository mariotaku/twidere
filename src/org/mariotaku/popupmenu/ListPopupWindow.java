package org.mariotaku.popupmenu;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.PopupWindow;

public interface ListPopupWindow {

	/**
	 * The provided prompt view should appear above list content.
	 * 
	 * @see #setPromptPosition(int)
	 * @see #getPromptPosition()
	 * @see #setPromptView(View)
	 */
	public static final int POSITION_PROMPT_ABOVE = 0;

	/**
	 * The provided prompt view should appear below list content.
	 * 
	 * @see #setPromptPosition(int)
	 * @see #getPromptPosition()
	 * @see #setPromptView(View)
	 */
	public static final int POSITION_PROMPT_BELOW = 1;

	/**
	 * Alias for {@link ViewGroup.LayoutParams#MATCH_PARENT}. If used to specify
	 * a popup width, the popup will match the width of the anchor view. If used
	 * to specify a popup height, the popup will fill available space.
	 */
	public static final int MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT;

	/**
	 * Alias for {@link ViewGroup.LayoutParams#WRAP_CONTENT}. If used to specify
	 * a popup width, the popup will use the width of its content.
	 */
	public static final int WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT;

	/**
	 * Mode for {@link #setInputMethodMode(int)}: the requirements for the input
	 * method should be based on the focusability of the popup. That is if it is
	 * focusable than it needs to work with the input method, else it doesn't.
	 */
	public static final int INPUT_METHOD_FROM_FOCUSABLE = PopupWindow.INPUT_METHOD_FROM_FOCUSABLE;

	/**
	 * Mode for {@link #setInputMethodMode(int)}: this popup always needs to
	 * work with an input method, regardless of whether it is focusable. This
	 * means that it will always be displayed so that the user can also operate
	 * the input method while it is shown.
	 */
	public static final int INPUT_METHOD_NEEDED = PopupWindow.INPUT_METHOD_NEEDED;

	/**
	 * Mode for {@link #setInputMethodMode(int)}: this popup never needs to work
	 * with an input method, regardless of whether it is focusable. This means
	 * that it will always be displayed to use as much space on the screen as
	 * needed, regardless of whether this covers the input method.
	 */
	public static final int INPUT_METHOD_NOT_NEEDED = PopupWindow.INPUT_METHOD_NOT_NEEDED;

	public void clearListSelection();

	public void dismiss();

	public View getAnchorView();

	public int getAnimationStyle();

	public Drawable getBackground();

	public int getHeight();

	public int getHorizontalOffset();

	public int getInputMethodMode();

	public android.widget.ListView getListView();

	public int getPromptPosition();

	public java.lang.Object getSelectedItem();

	public long getSelectedItemId();

	public int getSelectedItemPosition();

	public android.view.View getSelectedView();

	public int getSoftInputMode();

	public int getVerticalOffset();

	public int getWidth();

	public boolean isInputMethodNotNeeded();

	public boolean isModal();

	public boolean isShowing();

	public boolean performItemClick(int position);

	public void postShow();

	public void setAdapter(ListAdapter adapter);

	public void setAnchorView(View anchor);

	public void setAnimationStyle(int animationStyle);

	public void setBackgroundDrawable(Drawable d);

	public void setContentWidth(int width);

	public void setHeight(int height);

	public void setHorizontalOffset(int offset);

	public void setInputMethodMode(int mode);

	public void setListSelector(Drawable selector);

	public void setModal(boolean modal);

	public void setOnDismissListener(PopupWindow.OnDismissListener listener);

	public void setOnItemClickListener(AdapterView.OnItemClickListener clickListener);

	public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener selectedListener);

	public void setPromptPosition(int position);

	public void setPromptView(View prompt);

	public void setSelection(int position);

	public void setSoftInputMode(int mode);

	public void setVerticalOffset(int offset);

	public void setWidth(int width);

	public void show();

	public static class InstanceHelper {
		public static ListPopupWindow getInstance(final Context context) {
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB ? new ListPopupWindowNative(context)
					: new ListPopupWindowCompat(context);
		}
	}

}
