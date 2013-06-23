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
