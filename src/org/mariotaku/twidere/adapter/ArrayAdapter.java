/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ArrayAdapter<T> extends BaseAdapter {

	private final LayoutInflater mInflater;
	private final int mLayoutRes;

	private final ArrayList<T> mData = new ArrayList<T>();

	public ArrayAdapter(final Context context, final int layoutRes) {
		mInflater = LayoutInflater.from(context);
		mLayoutRes = layoutRes;
	}

	public final void add(final T item) {
		if (item == null) return;
		mData.add(item);
		notifyDataSetChanged();
	}

	public final void addAll(final Collection<? extends T> collection) {
		mData.addAll(collection);
		notifyDataSetChanged();
	}

	public final void clear() {
		mData.clear();
		Log.w("ArrayAdapter", new Exception());
		notifyDataSetChanged();
	}

	public final T findItem(final long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return getItem(i);
		}
		return null;
	}

	public final int findItemPosition(final long id) {
		final int count = getCount();
		for (int i = 0; i < count; i++) {
			if (getItemId(i) == id) return i;
		}
		return -1;
	}

	@Override
	public int getCount() {
		return mData.size();
	}

	@Override
	public final T getItem(final int position) {
		return mData.get(position);
	}

	@Override
	public long getItemId(final int position) {
		return position;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		return convertView != null ? convertView : mInflater.inflate(mLayoutRes, null);
	}

	public final boolean remove(final int position) {
		final boolean ret = mData.remove(position) != null;
		notifyDataSetChanged();
		return ret;
	}

	public final void sort(final Comparator<? super T> comparator) {
		Collections.sort(mData, comparator);
		notifyDataSetChanged();
	}

}
