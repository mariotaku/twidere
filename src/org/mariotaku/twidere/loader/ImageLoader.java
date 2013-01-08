package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.getImageLoaderHttpClient;
import static org.mariotaku.twidere.util.Utils.getRedirectedHttpResponse;
import static org.mariotaku.twidere.util.Utils.parseString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.twidere.twitter4j.http.HttpClientWrapper;
import org.mariotaku.twidere.twitter4j.http.HttpResponse;
import org.mariotaku.twidere.util.BitmapDecodeHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;
import java.io.FileDescriptor;
import java.io.FileInputStream;

public class ImageLoader extends AbstractImageLoader {

	public ImageLoader(final Context context, final DownloadListener listener, final Uri uri) {
		super(context, listener, uri);
	}
	
	protected Result decodeImage(FileDescriptor fd) {
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
