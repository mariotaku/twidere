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

import org.mariotaku.twidere.util.BitmapDecodeHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

public class ImageLoader extends AbsImageLoader {

	public ImageLoader(final Context context, final DownloadListener listener, final Uri uri) {
		super(context, listener, uri);
	}

	@Override
	protected Result decodeImage(final File file) {
		final BitmapFactory.Options o = new BitmapFactory.Options();
		final String path = file.getAbsolutePath();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, o);
		if (o.outHeight <= 0 || o.outWidth <= 0) {
			new Result(null, null, null);
		}
		Bitmap bitmap = null;
		o.inSampleSize = Math.round(Math.max(o.outWidth, o.outHeight) / 2048f);
		o.inJustDecodeBounds = false;
		while (bitmap == null) {
			try {
				bitmap = BitmapDecodeHelper.decode(path, o);
				if (bitmap == null) {
					break;
				}
			} catch (final OutOfMemoryError e) {
				o.inSampleSize++;
			}
		}
		return new Result(bitmap, mImageFile, null);
	}

}
