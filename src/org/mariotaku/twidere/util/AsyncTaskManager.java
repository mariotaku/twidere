package org.mariotaku.twidere.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AsyncTaskManager {

	public Map<Integer, ManagedAsyncTask> mTasks = new HashMap<Integer, ManagedAsyncTask>();

	public int add(ManagedAsyncTask task, boolean exec, Object... params) {
		int hashCode = task.hashCode();
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
		if (mTasks.containsKey(hashCode)) {
			ManagedAsyncTask task = mTasks.get(hashCode);
			if (task != null) {
				task.cancel(mayInterruptIfRunning);
				mTasks.remove(hashCode);
				return true;
			}
		}
		return false;
	}

	/**
	 * Cancel all tasks added, then clear all tasks.
	 */
	public void cancelAll() {
		Set<Integer> set = mTasks.keySet();
		for (Integer i : set) {
			cancel(i);
		}
		mTasks.clear();
	}

	public boolean execute(int hashCode, Object... params) {
		if (mTasks.containsKey(hashCode)) {
			ManagedAsyncTask task = mTasks.get(hashCode);
			if (task != null) {
				task.execute(params);
				return true;
			}
		}
		return false;
	}

	public boolean hasActivatedTask() {
		return mTasks.size() > 0;
	}

	public boolean isExcuting(int hashCode) {
		if (mTasks.containsKey(hashCode)) {
			ManagedAsyncTask task = mTasks.get(hashCode);
			if (task != null) return true;
		}
		return false;
	}

	public void remove(int hashCode) {
		if (mTasks.containsKey(hashCode)) {
			mTasks.remove(hashCode);
		}
	}
}
