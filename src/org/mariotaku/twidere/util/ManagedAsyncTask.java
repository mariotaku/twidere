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

import org.mariotaku.twidere.Constants;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

public abstract class ManagedAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> implements
		Constants {

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
