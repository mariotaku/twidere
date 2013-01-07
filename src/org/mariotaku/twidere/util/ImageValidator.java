package org.mariotaku.twidere.util;
import java.io.RandomAccessFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import android.graphics.BitmapFactory;

public class ImageValidator {

	private static final byte[] PNG_HEAD = new byte[] {0xFFFFFF89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
	private static final byte[] PNG_TAIL = new byte[] {0x49, 0x45, 0x4E, 0x44, 0xFFFFFFAE, 0x42, 0x60, 0xFFFFFF82};
	
	private static final byte[] JPEG_HEAD = new byte[] {0xFFFFFFFF, 0xFFFFFFD8};
	private static final byte[] JPEG_TAIL = new byte[] {0xFFFFFFFF, 0xFFFFFFD9};

	public static boolean checkJPEGValidity(String file) {
		return checkHeadTailValidity(file, JPEG_HEAD, JPEG_TAIL);
	}
	
	public static boolean checkPNGValidity(String file) {
		return checkHeadTailValidity(file, PNG_HEAD, PNG_TAIL);
	}
	

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
		else if ("image/png".equalsIgnoreCase(type))
			return checkPNGValidity(file);
		return opts.outWidth > 0 && opts.outHeight > 0;
	}

	private static boolean checkHeadTailValidity(final String file, final byte[] head, final byte[] tail) {
		if (file == null) return false;
		try {
			final RandomAccessFile raf = new RandomAccessFile(file, "r");
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
		} catch (IOException e) {
			return false;
		}
		return true;
	}
}
