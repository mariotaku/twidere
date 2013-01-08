package org.mariotaku.twidere.loader;

import java.io.FileDescriptor;
import java.io.FileInputStream;

import org.mariotaku.twidere.util.BitmapDecodeHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class ImageLoader extends AbstractImageLoader {

	public ImageLoader(final Context context, final DownloadListener listener, final Uri uri) {
		super(context, listener, uri);
	}

	@Override
	protected Result decodeImage(final FileDescriptor fd) {
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fd, null, o);
		if (o.outHeight <= 0) return null;
		final BitmapFactory.Options o1 = new BitmapFactory.Options();
		Bitmap bitmap = null;
		while (bitmap == null) {
			try {
				final BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = o1.inSampleSize;
				bitmap = BitmapDecodeHelper.decode(new FileInputStream(fd), o2);
			} catch (final OutOfMemoryError e) {
				o1.inSampleSize++;
				continue;
			}
			if (bitmap == null) {
				break;
			}
			return new Result(bitmap, mImageFile, null);
		}
		return new Result(null, null, null);
	}

}
