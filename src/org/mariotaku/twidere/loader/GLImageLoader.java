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

package org.mariotaku.twidere.loader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

import org.mariotaku.gallery3d.util.BitmapUtils;
import org.mariotaku.twidere.util.Exif;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;
import android.net.Uri;
import android.util.DisplayMetrics;

public class GLImageLoader extends AbstractImageLoader {

	private final float mBackupSize;

	public GLImageLoader(final Context context, final DownloadListener listener, final Uri uri) {
		super(context, listener, uri);
		final Resources res = context.getResources();
		final DisplayMetrics dm = res.getDisplayMetrics();
		mBackupSize = Math.max(dm.heightPixels, dm.widthPixels);
	}

	@Override
	protected AbstractImageLoader.Result decodeImage(final FileDescriptor fd) {
		try {
			final BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(fd, false);
			final int width = decoder.getWidth();
			final int height = decoder.getHeight();
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = BitmapUtils.computeSampleSize(mBackupSize / Math.max(width, height));
			final Bitmap bitmap = decoder.decodeRegion(new Rect(0, 0, width, height), options);
			return new GLImageResult(decoder, bitmap, Exif.getOrientation(fd), mImageFile);
		} catch (final IOException e) {
			final BitmapFactory.Options o1 = new BitmapFactory.Options();
			o1.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fd, null, o1);
			final int width = o1.outWidth;
			final int height = o1.outHeight;
			if (width == 0 || height == 0) return new AbstractImageLoader.Result(null, mImageFile, e);
			final BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = BitmapUtils.computeSampleSize(mBackupSize / Math.max(width, height));
			final Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fd, null, o2);
			return new GLImageResult(null, bitmap, Exif.getOrientation(fd), mImageFile);
		}
	}

	public static class GLImageResult extends Result {

		public final BitmapRegionDecoder decoder;
		public final int orientation;

		GLImageResult(final BitmapRegionDecoder decoder, final Bitmap bitmap, final int orientation, final File file) {
			super(bitmap, file, null);
			this.decoder = decoder;
			this.orientation = orientation;
		}
	}
}
