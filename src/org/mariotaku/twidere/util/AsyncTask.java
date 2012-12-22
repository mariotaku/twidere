package org.mariotaku.twidere.util;

import java.util.concurrent.ExecutorService;

import android.os.Handler;

public abstract class AsyncTask<Param, Progress, Result> {

	private Thread mThread;
	private final Handler mHandler;
	private final ExecutorService mExecutor;
	private final Runnable mRunnable;

	private boolean mCancelled;
	private Param[] mParams;
	private Status mStatus = Status.PENDING;

	public AsyncTask() {
		this(new Handler(), null);
	}

	public AsyncTask(final ExecutorService executor) {
		this(new Handler(), executor);
	}

	public AsyncTask(final Handler handler) {
		this(handler, null);
	}

	public AsyncTask(final Handler handler, final ExecutorService executor) {
		if (handler == null) throw new NullPointerException();
		mHandler = handler;
		mExecutor = executor;
		mRunnable = new BackgroundRunnable();
	}

	public void cancel(final boolean mayInterruptIfRunning) {
		mCancelled = true;
		if (mExecutor == null && mThread != null) {
			mThread.interrupt();
		}
		onCancelled();
		mStatus = Status.FINISHED;
	}

	public AsyncTask<Param, Progress, Result> execute(final Param... params) {
		if (mStatus != Status.PENDING) {
			switch (mStatus) {
				case RUNNING:
					throw new IllegalStateException("Cannot execute task:" + " the task is already running.");
				case FINISHED:
					throw new IllegalStateException("Cannot execute task:" + " the task has already been executed "
							+ "(a task can be executed only once)");
			}
		}

		mStatus = Status.RUNNING;
		onPreExecute();
		mParams = params;
		if (mExecutor != null) {
			mExecutor.execute(mRunnable);
		} else {
			mThread = new InternalThread();
			mThread.start();
		}

		return this;
	}

	public Status getStatus() {
		return mStatus;
	}

	public boolean isCancelled() {
		return mCancelled;
	}

	protected abstract Result doInBackground(Param... params);

	protected void onCancelled() {

	}

	protected void onCancelled(final Result result) {

	}

	protected void onPostExecute(final Result result) {

	}

	protected void onPreExecute() {

	}

	public enum Status {
		RUNNING, PENDING, FINISHED
	}

	private final class BackgroundRunnable implements Runnable {

		@Override
		public void run() {
			final Result result = doInBackground(mParams);
			mHandler.post(new OnPostExecuteRunnable(result));
		}
	}

	private final class InternalThread extends Thread {

		@Override
		public void run() {
			mRunnable.run();
		}
	}

	private final class OnPostExecuteRunnable implements Runnable {

		final Result mResult;

		public OnPostExecuteRunnable(final Result result) {
			mResult = result;
		}

		@Override
		public void run() {
			if (isCancelled()) {
				onCancelled(mResult);
			} else {
				onPostExecute(mResult);
			}
			mStatus = Status.FINISHED;
		}

	}
}
