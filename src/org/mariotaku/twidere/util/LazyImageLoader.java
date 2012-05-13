package org.mariotaku.twidere.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;

/**
 * Lazy image loader for {@link ListView} and {@link GridView} etc.</br> </br>
 * Inspired by <a url="https://github.com/thest1/LazyList">LazyList</a>, this
 * class has extra features like image loading/caching image to
 * /mnt/sdcard/Android/data/[package name]/cache features.</br> </br> Requires
 * Android 2.2, you can modify {@link Context#getExternalCacheDir()} to other to
 * support Android 2.1 and below.
 * 
 * @author mariotaku
 * 
 */
public class LazyImageLoader {

	private final MemoryCache mMemoryCache = new MemoryCache();
	private final FileCache mFileCache;
	private final Map<ImageView, Object> mImageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, Object>());
	private final ExecutorService mExecutorService;
	private final int mFallbackRes;
	private final int mRequiredSize;

	public LazyImageLoader(Context context, int fallback, int required_size) {
		mFileCache = new FileCache(context);
		mExecutorService = Executors.newFixedThreadPool(5);
		mFallbackRes = fallback;
		mRequiredSize = required_size % 2 == 0 ? required_size : required_size + 1;
	}

	public void clearFileCache() {
		mFileCache.clear();
	}

	public void clearMemoryCache() {
		mMemoryCache.clear();
	}

	public void displayImage(File file, ImageView imageview) {
		if (imageview == null) return;
		if (file == null) {
			imageview.setImageResource(mFallbackRes);
			return;
		}
		mImageViews.put(imageview, file);
		Bitmap bitmap = mMemoryCache.get(file);
		if (bitmap != null) {
			imageview.setImageBitmap(bitmap);
		} else {
			queuePhoto(file, imageview);
			imageview.setImageResource(mFallbackRes);
		}
	}

	public void displayImage(URL url, ImageView imageview) {
		if (imageview == null) return;
		if (url == null) {
			imageview.setImageResource(mFallbackRes);
			return;
		}
		mImageViews.put(imageview, url);
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null) {
			imageview.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageview);
			imageview.setImageResource(mFallbackRes);
		}
	}

	private void copyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			int count = is.read(bytes, 0, buffer_size);
			while (count != -1) {
				os.write(bytes, 0, count);
				count = is.read(bytes, 0, buffer_size);
			}
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	// decodes image and scales it to reduce memory consumption
	private Bitmap decodeFile(File f) {
		try {
			// decode image size
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, options);

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = options.outWidth, height_tmp = options.outHeight;
			int scale = 1;
			while (width_tmp / 2 >= mRequiredSize || height_tmp / 2 >= mRequiredSize) {
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale / 2;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	private Bitmap getBitmapFromFile(File file, ImageView imageview) {
		if (file == null) return null;
		File f = mFileCache.getFile(file);

		// from SD cache
		Bitmap bitmap = decodeFile(f);

		if (bitmap != null) return bitmap;
		bitmap = decodeFile(file);
		if (bitmap == null) return null;
		try {
			FileOutputStream fos = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
			fos.flush();
			fos.close();
		} catch (FileNotFoundException e) {
			// Storage state may changed, so call FileCache.init() again.
			mFileCache.init();
		} catch (IOException e) {
			// e.printStackTrace();
		}

		return bitmap;

	}

	private Bitmap getBitmapFromWeb(URL url, ImageView imageview) {
		if (url == null) return null;
		File f = mFileCache.getFile(url);

		// from SD cache
		Bitmap b = decodeFile(f);
		if (b != null) return b;

		// from web
		try {
			Bitmap bitmap = null;
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			conn.setInstanceFollowRedirects(true);
			InputStream is = conn.getInputStream();
			OutputStream os = new FileOutputStream(f);
			copyStream(is, os);
			os.close();
			bitmap = decodeFile(f);
			return bitmap;
		} catch (FileNotFoundException e) {
			// Storage state may changed, so call FileCache.init() again.
			mFileCache.init();
		} catch (IOException e) {
			// e.printStackTrace();
		}
		return null;
	}

	private void queuePhoto(File file, ImageView imageview) {
		LocalImageToLoad p = new LocalImageToLoad(file, imageview);
		mExecutorService.submit(new LocalImageLoader(p));
	}

	private void queuePhoto(URL url, ImageView imageview) {
		WebImageToLoad p = new WebImageToLoad(url, imageview);
		mExecutorService.submit(new WebImageLoader(p));
	}

	boolean imageViewReused(LocalImageToLoad imagetoload) {
		Object tag = mImageViews.get(imagetoload.imageview);
		if (tag == null || !tag.equals(imagetoload.file)) return true;
		return false;
	}

	boolean imageViewReused(WebImageToLoad imagetoload) {
		Object tag = mImageViews.get(imagetoload.imageview);
		if (tag == null || !tag.equals(imagetoload.url)) return true;
		return false;
	}

	public class MemoryCache {

		private Map<Object, SoftReference<Bitmap>> mCache = Collections
				.synchronizedMap(new HashMap<Object, SoftReference<Bitmap>>());

		public void clear() {
			mCache.clear();
		}

		public Bitmap get(Object tag) {
			if (!mCache.containsKey(tag)) return null;
			SoftReference<Bitmap> ref = mCache.get(tag);
			return ref.get();
		}

		public void put(Object id, Bitmap bitmap) {
			mCache.put(id, new SoftReference<Bitmap>(bitmap));
		}
	}

	private class FileCache {

		private static final String CACHE_DIR_NAME = "thumbnails";

		private File mCacheDir;
		private Context mContext;

		public FileCache(Context context) {
			mContext = context;
			init();
		}

		public void clear() {
			if (mCacheDir == null) return;
			File[] files = mCacheDir.listFiles();
			if (files == null) return;
			for (File f : files) {
				f.delete();
			}
		}

		/**
		 * I identify images by hashcode. Not a perfect solution, good for the
		 * demo.
		 */
		public File getFile(Object tag) {
			if (mCacheDir == null) return null;
			if (tag instanceof File) {
				if (mCacheDir.equals(((File) tag).getParentFile())) return (File) tag;
			}
			String filename = Integer.toHexString(tag.hashCode());
			File f = new File(mCacheDir, filename);
			return f;
		}

		public void init() {
			/* Find the dir to save cached images. */
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				mCacheDir = new File(mContext.getExternalCacheDir(), CACHE_DIR_NAME);
			} else {
				mCacheDir = new File(mContext.getCacheDir(), CACHE_DIR_NAME);
			}
			if (mCacheDir != null && !mCacheDir.exists()) {
				mCacheDir.mkdirs();
			}
		}

	}

	// Used to display bitmap in the UI thread
	private class LocalBitmapDisplayer implements Runnable {

		Bitmap bitmap;
		LocalImageToLoad imagetoload;

		public LocalBitmapDisplayer(Bitmap b, LocalImageToLoad p) {
			bitmap = b;
			imagetoload = p;
		}

		@Override
		public void run() {
			if (imageViewReused(imagetoload)) return;
			if (bitmap != null) {
				imagetoload.imageview.setImageBitmap(bitmap);
			} else {
				imagetoload.imageview.setImageResource(mFallbackRes);
			}
		}
	}

	private class LocalImageLoader implements Runnable {

		LocalImageToLoad imagetoload;

		LocalImageLoader(LocalImageToLoad imagetoload) {
			this.imagetoload = imagetoload;
		}

		@Override
		public void run() {
			if (imageViewReused(imagetoload) || imagetoload.file == null) return;
			Bitmap bmp = getBitmapFromFile(imagetoload.file, imagetoload.imageview);
			mMemoryCache.put(imagetoload.file, bmp);
			if (imageViewReused(imagetoload)) return;
			LocalBitmapDisplayer bd = new LocalBitmapDisplayer(bmp, imagetoload);
			Activity a = (Activity) imagetoload.imageview.getContext();
			a.runOnUiThread(bd);
		}
	}

	private class LocalImageToLoad {

		public File file;
		public ImageView imageview;

		public LocalImageToLoad(File file, ImageView imageview) {
			this.file = file;
			this.imageview = imageview;
		}
	}

	// Used to display bitmap in the UI thread
	private class WebBitmapDisplayer implements Runnable {

		Bitmap bitmap;
		WebImageToLoad imagetoload;

		public WebBitmapDisplayer(Bitmap b, WebImageToLoad p) {
			bitmap = b;
			imagetoload = p;
		}

		@Override
		public void run() {
			if (imageViewReused(imagetoload)) return;
			if (bitmap != null) {
				imagetoload.imageview.setImageBitmap(bitmap);
			} else {
				imagetoload.imageview.setImageResource(mFallbackRes);
			}
		}
	}

	private class WebImageLoader implements Runnable {

		WebImageToLoad imagetoload;

		WebImageLoader(WebImageToLoad imagetoload) {
			this.imagetoload = imagetoload;
		}

		@Override
		public void run() {
			if (imageViewReused(imagetoload) || imagetoload.url == null) return;
			Bitmap bmp = getBitmapFromWeb(imagetoload.url, imagetoload.imageview);
			mMemoryCache.put(imagetoload.url, bmp);
			if (imageViewReused(imagetoload)) return;
			WebBitmapDisplayer bd = new WebBitmapDisplayer(bmp, imagetoload);
			Activity a = (Activity) imagetoload.imageview.getContext();
			a.runOnUiThread(bd);
		}
	}

	// Task for the queue
	private class WebImageToLoad {

		public URL url;
		public ImageView imageview;

		public WebImageToLoad(URL url, ImageView imageview) {
			this.url = url;
			this.imageview = imageview;
		}
	}

}
