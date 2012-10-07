package org.mariotaku.twidere.util;



import static android.os.Environment.getExternalStorageDirectory;
import static android.os.Environment.getExternalStorageState;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.net.URLEncoder;

public class SerializationUtil {

	public static final String FILE_MODE_RW = "rw";
	public static final String FILE_MODE_R = "r";
	public static final String SERIALIZATION_CACHE_DIR = "serialization_cache";

	public static void write(Object object, String path) throws IOException {
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

	public static Object read(String path) throws IOException, ClassNotFoundException {
		if (path == null) return null;
		ObjectInputStream is = null;
		try {
			//final RandomAccessFile raf = new RandomAccessFile(path, FILE_MODE_RW);
			final FileInputStream fis = new FileInputStream(new File(path));
			is = new ObjectInputStream(fis);
			return is.readObject();
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}
	
	public static String getSerializationFilePath(Context context, Object... args) {
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
		if (cache_dir == null) return null;
		if (!cache_dir.exists()) {
			cache_dir.mkdirs();
		}
		final String filename = URLEncoder.encode(ArrayUtils.toString(args, '.', false));
		final File cache_file = new File(cache_dir, filename);
		return cache_file.getPath();
	}
	
}
