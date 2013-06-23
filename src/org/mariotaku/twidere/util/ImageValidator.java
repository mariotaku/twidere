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

import static org.mariotaku.twidere.util.Utils.closeSilently;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import android.graphics.BitmapFactory;
import android.net.Uri;

public class ImageValidator {

	private static final byte[] PNG_HEAD = new byte[] { 0xFFFFFF89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };
	private static final byte[] PNG_TAIL = new byte[] { 0x49, 0x45, 0x4E, 0x44, 0xFFFFFFAE, 0x42, 0x60, 0xFFFFFF82 };

	private static final byte[] JPEG_HEAD = new byte[] { 0xFFFFFFFF, 0xFFFFFFD8 };
	private static final byte[] JPEG_TAIL = new byte[] { 0xFFFFFFFF, 0xFFFFFFD9 };

	public static boolean checkImageValidity(final File file) {
		if (file == null) return false;
		return checkImageValidity(file.getPath());
	}

	public static boolean checkImageValidity(final String file) {
		final BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file, opts);
		final String type = opts.outMimeType;
		if (type == null) return false;
		if ("image/jpeg".equalsIgnoreCase(type))
			return checkJPEGValidity(file);
		else if ("image/png".equalsIgnoreCase(type)) return checkPNGValidity(file);
		return opts.outWidth > 0 && opts.outHeight > 0;
	}

	public static boolean checkImageValidity(final Uri uri) {
		if (uri == null) return false;
		return checkImageValidity(uri.getPath());
	}

	public static boolean checkJPEGValidity(final String file) {
		return checkHeadTailValidity(file, JPEG_HEAD, JPEG_TAIL);
	}

	public static boolean checkPNGValidity(final String file) {
		return checkHeadTailValidity(file, PNG_HEAD, PNG_TAIL);
	}

	private static boolean checkHeadTailValidity(final String file, final byte[] head, final byte[] tail) {
		if (file == null) return false;
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(file, "r");
			final long length = raf.length();
			// The file has 0-length, so it can't be a PNG file.
			if (length == 0) return false;
			byte[] buffer;
			// Read head.
			buffer = new byte[head.length];
			raf.seek(0);
			if (raf.read(buffer) != buffer.length || !Arrays.equals(buffer, head)) return false;
			// Read tail.
			buffer = new byte[tail.length];
			raf.seek(length - buffer.length);
			if (raf.read(buffer) != buffer.length || !Arrays.equals(buffer, tail)) return false;
		} catch (final IOException e) {
			return false;
		} finally {
			closeSilently(raf);
		}
		return true;
	}
}
