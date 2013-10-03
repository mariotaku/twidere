/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.mobeta.android.dslv;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

// taken from v4 rev. 10 ResourceCursorAdapter.java

/**
 * Static library support version of the framework's
 * {@link android.widget.ResourceCursorAdapter}. Used to write apps that run on
 * platforms prior to Android 3.0. When running on Android 3.0 or above, this
 * implementation is still used; it does not try to switch to the framework's
 * implementation. See the framework SDK documentation for a class overview.
 */
public abstract class ResourceDragSortCursorAdapter extends DragSortCursorAdapter {
	private int mLayout;

	private int mDropDownLayout;

	private final LayoutInflater mInflater;

	/**
	 * Constructor the enables auto-requery.
	 * 
	 * @deprecated This option is discouraged, as it results in Cursor queries
	 *             being performed on the application's UI thread and thus can
	 *             cause poor responsiveness or even Application Not Responding
	 *             errors. As an alternative, use
	 *             {@link android.app.LoaderManager} with a
	 *             {@link android.content.CursorLoader}.
	 * 
	 * @param context The context where the ListView associated with this
	 *            adapter is running
	 * @param layout resource identifier of a layout file that defines the views
	 *            for this list item. Unless you override them later, this will
	 *            define both the item views and the drop down views.
	 */
	@Deprecated
	public ResourceDragSortCursorAdapter(final Context context, final int layout, final Cursor c) {
		super(context, c);
		mLayout = mDropDownLayout = layout;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Constructor with default behavior as per
	 * {@link CursorAdapter#CursorAdapter(Context, Cursor, boolean)}; it is
	 * recommended you not use this, but instead
	 * {@link #ResourceCursorAdapter(Context, int, Cursor, int)}. When using
	 * this constructor, {@link #FLAG_REGISTER_CONTENT_OBSERVER} will always be
	 * set.
	 * 
	 * @param context The context where the ListView associated with this
	 *            adapter is running
	 * @param layout resource identifier of a layout file that defines the views
	 *            for this list item. Unless you override them later, this will
	 *            define both the item views and the drop down views.
	 * @param c The cursor from which to get the data.
	 * @param autoRequery If true the adapter will call requery() on the cursor
	 *            whenever it changes so the most recent data is always
	 *            displayed. Using true here is discouraged.
	 */
	public ResourceDragSortCursorAdapter(final Context context, final int layout, final Cursor c,
			final boolean autoRequery) {
		super(context, c, autoRequery);
		mLayout = mDropDownLayout = layout;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	/**
	 * Standard constructor.
	 * 
	 * @param context The context where the ListView associated with this
	 *            adapter is running
	 * @param layout Resource identifier of a layout file that defines the views
	 *            for this list item. Unless you override them later, this will
	 *            define both the item views and the drop down views.
	 * @param c The cursor from which to get the data.
	 * @param flags Flags used to determine the behavior of the adapter, as per
	 *            {@link CursorAdapter#CursorAdapter(Context, Cursor, int)}.
	 */
	public ResourceDragSortCursorAdapter(final Context context, final int layout, final Cursor c, final int flags) {
		super(context, c, flags);
		mLayout = mDropDownLayout = layout;
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View newDropDownView(final Context context, final Cursor cursor, final ViewGroup parent) {
		return mInflater.inflate(mDropDownLayout, parent, false);
	}

	/**
	 * Inflates view(s) from the specified XML file.
	 * 
	 * @see android.widget.CursorAdapter#newView(android.content.Context,
	 *      android.database.Cursor, ViewGroup)
	 */
	@Override
	public View newView(final Context context, final Cursor cursor, final ViewGroup parent) {
		return mInflater.inflate(mLayout, parent, false);
	}

	/**
	 * <p>
	 * Sets the layout resource of the drop down views.
	 * </p>
	 * 
	 * @param dropDownLayout the layout resources used to create drop down views
	 */
	public void setDropDownViewResource(final int dropDownLayout) {
		mDropDownLayout = dropDownLayout;
	}

	/**
	 * <p>
	 * Sets the layout resource of the item views.
	 * </p>
	 * 
	 * @param layout the layout resources used to create item views
	 */
	public void setViewResource(final int layout) {
		mLayout = layout;
	}
}
