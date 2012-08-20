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

import static org.mariotaku.twidere.util.ServiceUtils.bindToService;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.IImageUploader;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;

public final class ImageUploaderInterface implements Constants, IImageUploader {

	private IImageUploader mService;

	private final ServiceConnection mConntecion = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName service, IBinder obj) {
			mService = IImageUploader.Stub.asInterface(obj);
		}

		@Override
		public void onServiceDisconnected(ComponentName service) {
			mService = null;
		}
	};

	private ImageUploaderInterface(Context context, String uploader_name) {
		final Intent intent = new Intent(INTENT_ACTION_EXTENSION_UPLOAD_IMAGE);
		final ComponentName component = ComponentName.unflattenFromString(uploader_name);
		intent.setComponent(component);
		bindToService(context, intent, mConntecion);
	}
	
	public void waitForService() {
		while (mService == null) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public IBinder asBinder() {
		// Useless here
		return mService.asBinder();
	}

	@Override
	public Uri upload(Uri file_uri, String message) {
		if (mService == null) return null;
		try {
			return mService.upload(file_uri, message);
		} catch (final RemoteException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ImageUploaderInterface getInstance(Application application, String uploader_name) {
		if (uploader_name == null) return null;
		final Intent intent = new Intent(INTENT_ACTION_EXTENSION_UPLOAD_IMAGE);
		final ComponentName component = ComponentName.unflattenFromString(uploader_name);
		intent.setComponent(component);
		if (application.getPackageManager().queryIntentServices(intent, 0).size() != 1) return null;
		return new ImageUploaderInterface(application, uploader_name);
	}

	public static class ServiceToken {

		ContextWrapper wrapped_context;

		ServiceToken(ContextWrapper context) {

			wrapped_context = context;
		}
	}
}
