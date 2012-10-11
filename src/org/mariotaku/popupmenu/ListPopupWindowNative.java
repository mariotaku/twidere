package org.mariotaku.popupmenu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.PopupWindow;

public class ListPopupWindowNative extends android.widget.ListPopupWindow implements ListPopupWindow {


	/**
	 * Create a new, empty popup window capable of displaying items from a
	 * ListAdapter. Backgrounds should be set using
	 * {@link #setBackgroundDrawable(Drawable)}.
	 * 
	 * @param context
	 *            Context used for contained views.
	 */
	public ListPopupWindowNative(Context context) {
		super(context);
	}

	/**
	 * Create a new, empty popup window capable of displaying items from a
	 * ListAdapter. Backgrounds should be set using
	 * {@link #setBackgroundDrawable(Drawable)}.
	 * 
	 * @param context
	 *            Context used for contained views.
	 * @param attrs
	 *            Attributes from inflating parent views used to style the
	 *            popup.
	 */
	public ListPopupWindowNative(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Create a new, empty popup window capable of displaying items from a
	 * ListAdapter. Backgrounds should be set using
	 * {@link #setBackgroundDrawable(Drawable)}.
	 * 
	 * @param context
	 *            Context used for contained views.
	 * @param attrs
	 *            Attributes from inflating parent views used to style the
	 *            popup.
	 * @param defStyleAttr
	 *            Default style attribute to use for popup content.
	 */
	public ListPopupWindowNative(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	/**
	 * Create a new, empty popup window capable of displaying items from a
	 * ListAdapter. Backgrounds should be set using
	 * {@link #setBackgroundDrawable(Drawable)}.
	 * 
	 * @param context
	 *            Context used for contained views.
	 * @param attrs
	 *            Attributes from inflating parent views used to style the
	 *            popup.
	 * @param defStyleAttr
	 *            Style attribute to read for default styling of popup content.
	 * @param defStyleRes
	 *            Style resource ID to use for default styling of popup content.
	 */
	public ListPopupWindowNative(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}



}
