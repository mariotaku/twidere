package org.mariotaku.twidere.util;

import org.mariotaku.twidere.Constants;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public abstract class ManagedAsyncTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> implements Constants {

	private AsyncTaskManager manager;
	private Context context;

	public ManagedAsyncTask(Context context, AsyncTaskManager manager) {
		this.manager = manager;
		this.context = context;
	}

	@Override
	protected void onCancelled() {
		manager.remove(hashCode());
		context.sendBroadcast(new Intent(BROADCAST_REFRESHSTATE_CHANGED));
		super.onCancelled();
	}

	@Override
	protected void onCancelled(Result result) {
		manager.remove(hashCode());
		context.sendBroadcast(new Intent(BROADCAST_REFRESHSTATE_CHANGED));
		super.onCancelled(result);
	}

	@Override
	protected void onPostExecute(Result result) {
		manager.remove(hashCode());
		context.sendBroadcast(new Intent(BROADCAST_REFRESHSTATE_CHANGED));
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		context.sendBroadcast(new Intent(BROADCAST_REFRESHSTATE_CHANGED));
		super.onPreExecute();
	}

}
