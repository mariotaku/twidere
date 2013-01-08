package org.mariotaku.twidere.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import org.mariotaku.gallery3d.util.BitmapUtils;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import org.mariotaku.gallery3d.util.DecodeUtils;
import org.mariotaku.twidere.util.Exif;

public class GLImageLoader extends AbstractImageLoader {

	private final float mBackupSize;

	protected AbstractImageLoader.Result decodeImage(FileDescriptor fd) {
		try {
			final BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(fd, false);
			final int width = decoder.getWidth();
			final int height = decoder.getHeight();
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = BitmapUtils.computeSampleSize(mBackupSize / Math.max(width, height));
			final Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, width, height), options);
			return new GLImageResult(decoder, bitmap, Exif.getOrientation(fd), mImageFile);
		} catch (IOException e) {
			final BitmapFactory.Options o1 = new BitmapFactory.Options();
			o1.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, o1);
			final int width = o1.outWidth;
			final int height = o1.outHeight;
			final BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = BitmapUtils.computeSampleSize(mBackupSize / Math.max(width, height));
			final Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd, null, o2);
			return new GLImageResult(null, bitmap, Exif.getOrientation(fd), mImageFile);
		}
	}

	public GLImageLoader(final Context context, final DownloadListener listener, final Uri uri) {
		super(context, listener, uri);
		final Resources res = context.getResources();
		final DisplayMetrics dm = res.getDisplayMetrics();
		mBackupSize = Math.max(dm.heightPixels, dm.widthPixels);
	}
	
	public static class GLImageResult extends Result {
		
		public final BitmapRegionDecoder decoder;
		public final int orientation;
		
		GLImageResult(BitmapRegionDecoder decoder, Bitmap bitmap, int orientation, File file) {
			super(bitmap, file, null);
			this.decoder = decoder;
			this.orientation = orientation;
		}
	}
}
