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
