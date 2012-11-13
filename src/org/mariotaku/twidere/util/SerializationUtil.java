/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

public class SerializationUtil {

	public static final String FILE_MODE_RW = "rw";
	public static final String FILE_MODE_R = "r";
	public static final String SERIALIZATION_CACHE_DIR = "serialization_cache";

	public static String getSerializationFilePath(final Context context, final Object... args) {
		if (context == null || args == null || args.length == 0) return null;
		final File cache_dir;
		if (getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			cache_dir = new File(
					Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? GetExternalCacheDirAccessor.getExternalCacheDir(context)
							: new File(getExternalStorageDirectory().getPath() + "/Android/data/"
									+ context.getPackageName() + "/cache/"), SERIALIZATION_CACHE_DIR);
		} else {
			cache_dir = new File(context.getCacheDir(), SERIALIZATION_CACHE_DIR);
		}
		if (!cache_dir.exists()) {
			cache_dir.mkdirs();
		}
		String filename = null;
		try {
			filename = URLEncoder.encode(ArrayUtils.toString(args, '.', false), "UTF-8");
		} catch (final UnsupportedEncodingException e) {
			return null;
		}
		final File cache_file = new File(cache_dir, filename);
		return cache_file.getPath();
	}

	@SuppressWarnings("unchecked")
	public static <T> T read(final String path) throws IOException, ClassNotFoundException {
		if (path == null) return null;
		ObjectInputStream is = null;
		try {
			// final RandomAccessFile raf = new RandomAccessFile(path,
			// FILE_MODE_RW);
			final FileInputStream fis = new FileInputStream(new File(path));
			is = new ObjectInputStream(fis);
			return (T) is.readObject();
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public static void write(final Object object, final String path) throws IOException {
		if (object == null || path == null) return;
		ObjectOutputStream os = null;
		try {
			final RandomAccessFile raf = new RandomAccessFile(path, FILE_MODE_RW);
			final FileOutputStream fos = new FileOutputStream(raf.getFD());
			os = new ObjectOutputStream(fos);
			os.writeObject(object);
		} finally {
			if (os != null) {
				os.close();
			}
		}
	}

}
