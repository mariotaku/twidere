/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask.Status;
import android.util.SparseArray;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AsyncTaskManager {

	private SparseArray<ManagedAsyncTask> mTasks = new SparseArray<ManagedAsyncTask>();

	private static AsyncTaskManager sInstance;

	public <T> int add(ManagedAsyncTask task, boolean exec, T... params) {
		final int hashCode = task.hashCode();
		mTasks.put(hashCode, task);
		if (exec) {
			execute(hashCode);
		}
		return hashCode;
	}

	public boolean cancel(int hashCode) {
		return cancel(hashCode, true);
	}

	public boolean cancel(int hashCode, boolean mayInterruptIfRunning) {
		final ManagedAsyncTask task = mTasks.get(hashCode);
		if (task != null) {
			task.cancel(mayInterruptIfRunning);
			mTasks.remove(hashCode);
			return true;
		}
		return false;
	}

	/**
	 * Cancel all tasks added, then clear all tasks.
	 */
	public void cancelAll() {
		for (int i = 0; i < mTasks.size(); i++) {
			cancel(mTasks.keyAt(i));
		}
		mTasks.clear();
	}

	public <T> boolean execute(int hashCode, T... params) {
		final ManagedAsyncTask task = mTasks.get(hashCode);
		if (task != null) {
			task.execute(params == null || params.length == 0 ? null : params);
			return true;
		}
		return false;
	}

	public List<ManagedAsyncTask<?, ?, ?>> getTaskList() {
		final List<ManagedAsyncTask<?, ?, ?>> list = new ArrayList<ManagedAsyncTask<?, ?, ?>>();

		for (int i = 0; i < mTasks.size(); i++) {
			final ManagedAsyncTask task = mTasks.valueAt(i);
			if (task != null) {
				list.add(task);
			}
		}
		return list;
	}

	public boolean hasActivatedTask() {
		return mTasks.size() > 0;
	}

	public boolean isExcuting(int hashCode) {
		final ManagedAsyncTask task = mTasks.get(hashCode);
		if (task != null && task.getStatus() == Status.RUNNING) return true;
		return false;
	}

	public void remove(int hashCode) {
		mTasks.remove(hashCode);
	}

	public static AsyncTaskManager getInstance() {
		if (sInstance == null) {
			sInstance = new AsyncTaskManager();
		}
		return sInstance;
	}
}
