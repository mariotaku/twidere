package org.mariotaku.twidere.util;

import java.io.IOException;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;

public class BitmapDecodeHelper {

	public static Bitmap decode(final String path, final BitmapFactory.Options opts) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) return DecodeMethodEclair.decode(path, opts);
		return BitmapFactory.decodeFile(path, opts);
	}

	@TargetApi(Build.VERSION_CODES.ECLAIR)
	static class DecodeMethodEclair {
		private static Bitmap decode(final String path, final BitmapFactory.Options opts) {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ECLAIR) return BitmapFactory.decodeFile(path, opts);
			if (path == null || opts == null) return null;
			try {
				final Bitmap bm = BitmapFactory.decodeFile(path, opts);
				final ExifInterface exif = new ExifInterface(path);
				final Matrix m = new Matrix();
				switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
					case ExifInterface.ORIENTATION_ROTATE_180: {
						m.postRotate(180);
						m.postScale(bm.getWidth(), bm.getHeight());
						return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
					}
					case ExifInterface.ORIENTATION_ROTATE_90: {
						m.postRotate(90);
						return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
					}
					case ExifInterface.ORIENTATION_ROTATE_270: {
						m.postRotate(270);
						return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
					}
				}
				return bm;
			} catch (final IOException e) {
				return null;
			}
		}
	}

}