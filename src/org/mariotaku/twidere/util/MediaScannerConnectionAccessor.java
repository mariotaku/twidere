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

package org.mariotaku.twidere.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;

public class MediaScannerConnectionAccessor {

	public static void scanFile(final Context context, final String[] paths, final String[] mimeTypes,
			final OnMediaScanCompletedListener callback) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) return;
		MediaScannerConnectionAccessorFroyo.scanFile(context, paths, mimeTypes, callback);
	}

	public static interface OnMediaScanCompletedListener {
		public void onScanCompleted(String path, Uri uri);
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	static class MediaScannerConnectionAccessorFroyo {
		public static void scanFile(final Context context, final String[] paths, final String[] mimeTypes,
				final OnMediaScanCompletedListener callback) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) return;
			MediaScannerConnection.scanFile(context, paths, mimeTypes, new ListenerProxy(callback));
		}

		private static class ListenerProxy implements MediaScannerConnection.OnScanCompletedListener {
			private final OnMediaScanCompletedListener callback;

			ListenerProxy(final OnMediaScanCompletedListener callback) {
				this.callback = callback;
			}

			@Override
			public void onScanCompleted(final String path, final Uri uri) {
				if (callback != null) {
					callback.onScanCompleted(path, uri);
				}
			}
		}
	}
}
