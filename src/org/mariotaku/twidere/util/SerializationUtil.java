package org.mariotaku.twidere.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;

public class SerializationUtil {


	public static final String FILE_MODE_RW = "rw";
	public static final String FILE_MODE_R = "r";

	public static void write(Object obj, String file) throws IOException {
		ObjectOutputStream os = null;
		try {
			final RandomAccessFile raf = new RandomAccessFile(file, FILE_MODE_RW);
			final FileOutputStream fos = new FileOutputStream(raf.getFD());
			os = new ObjectOutputStream(fos);
			os.writeObject(obj);
		} finally {
			if (os != null) {
				os.close();
			}
		}
    }

	public static Object read(String file) throws IOException, ClassNotFoundException {
		ObjectInputStream is = null;
		try {
			final RandomAccessFile randomAccessFile = new RandomAccessFile(file, FILE_MODE_R);
			final FileInputStream fis = new FileInputStream(randomAccessFile.getFD());
			is = new ObjectInputStream(fis);
			return is.readObject();
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
	
	
}
