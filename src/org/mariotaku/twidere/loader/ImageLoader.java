package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.getImageLoaderHttpClient;
import static org.mariotaku.twidere.util.Utils.getRedirectedHttpResponse;
import static org.mariotaku.twidere.util.Utils.parseString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mariotaku.gallery3d.util.GalleryUtils;
import org.mariotaku.twidere.twitter4j.http.HttpClientWrapper;
import org.mariotaku.twidere.twitter4j.http.HttpResponse;
import org.mariotaku.twidere.util.BitmapDecodeHelper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.AsyncTaskLoader;

public class ImageLoader extends AsyncTaskLoader<ImageLoader.Result> {

	private static final String CACHE_DIR_NAME = "cached_images";

	private final Uri mUri;
	private final Context mContext;
	private final HttpClientWrapper mClient;
	private final Handler mHandler;
	private final ImageLoader.DownloadListener mListener;

	private File mCacheDir;

	public ImageLoader(final Context context, final ImageLoader.DownloadListener listener, final Uri uri) {
		super(context);
		mContext = context;
		mHandler = new Handler();
		mUri = uri;
		mClient = getImageLoaderHttpClient(context);
		mListener = listener;
		init();
	}

	@Override
	public ImageLoader.Result loadInBackground() {

		if (mUri == null) return new Result(null, null, null);
		final String scheme = mUri.getScheme();
		if ("http".equals(scheme) || "https".equals(scheme)) {
			final String url = parseString(mUri.toString());
			if (url == null) return new Result(null, null, null);
			if (mCacheDir == null || !mCacheDir.exists()) {
				init();
			}
			final File cache_file = new File(mCacheDir, getURLFilename(url));

			// from SD cache
			final Bitmap cached_bitmap = decodeFile(cache_file);
			if (cached_bitmap != null) return new Result(cached_bitmap, cache_file, null);
			// from web
			try {
				final HttpResponse resp = getRedirectedHttpResponse(mClient, url);
				if (resp == null) return null;
				final long length = resp.getContentLength();
				mHandler.post(new DownloadStartRunnable(mListener, length));
				final InputStream is = resp.asStream();
				final OutputStream os = new FileOutputStream(cache_file);
				try {
					dump(is, os);
					mHandler.post(new DownloadFinishRunnable(mListener));
				} finally {
					GalleryUtils.closeSilently(is);
					GalleryUtils.closeSilently(os);
				}
				final Bitmap bitmap = decodeFile(cache_file);
				if (bitmap == null) {
					// The file is corrupted, so we remove it from
					// cache.
					if (cache_file.isFile()) {
						cache_file.delete();
					}
				}
				return new Result(bitmap, cache_file, null);
			} catch (final FileNotFoundException e) {
				init();
			} catch (final Exception e) {
				mHandler.post(new DownloadErrorRunnable(mListener, e));
				return new Result(null, null, e);
			}
		} else if ("file".equals(scheme)) {
			final File file = new File(mUri.getPath());
			return new Result(decodeFile(file), file, null);
		}
		return new Result(null, null, null);
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	private Bitmap decodeFile(final File f) {
		if (f == null) return null;
		final BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(f.getPath(), o);
		if (o.outHeight <= 0) return null;
		final BitmapFactory.Options o1 = new BitmapFactory.Options();
		Bitmap bitmap = null;
		while (bitmap == null) {
			try {
				final BitmapFactory.Options o2 = new BitmapFactory.Options();
				o2.inSampleSize = o1.inSampleSize;
				bitmap = BitmapDecodeHelper.decode(f.getPath(), o2);
			} catch (final OutOfMemoryError e) {
				o1.inSampleSize++;
				continue;
			}
			if (bitmap == null) {
				break;
			}
			return bitmap;
		}
		return null;
	}

	private void dump(final InputStream is, final OutputStream os) throws IOException {
		final byte buffer[] = new byte[1024];
		int rc = is.read(buffer, 0, buffer.length);
		long downloaded = 0;
		while (rc > 0) {
			downloaded += rc;
			mHandler.post(new ProgressUpdateRunnable(mListener, downloaded));
			os.write(buffer, 0, rc);
			rc = is.read(buffer, 0, buffer.length);
		}
	}

	private String getURLFilename(final String url) {
		if (url == null) return null;
		return url.replaceFirst("https?:\\/\\/", "").replaceAll("[^\\w\\d_]", "_");
	}

	private void init() {
		/* Find the dir to save cached images. */
		mCacheDir = getBestCacheDir(mContext, CACHE_DIR_NAME);
		if (mCacheDir != null && !mCacheDir.exists()) {
			mCacheDir.mkdirs();
		}
	}

	public static interface DownloadListener {
		void onDownloadError(Throwable t);

		void onDownloadFinished();

		void onDownloadStart(long total);

		void onProgressUpdate(long downloaded);
	}

	public static class Result {
		public final Bitmap bitmap;
		public final File file;
		public final Exception exception;

		public Result(final Bitmap bitmap, final File file, final Exception exception) {
			this.bitmap = bitmap;
			this.file = file;
			this.exception = exception;
		}
	}

	private final static class DownloadErrorRunnable implements Runnable {

		private final ImageLoader.DownloadListener listener;
		private final Throwable t;

		DownloadErrorRunnable(final ImageLoader.DownloadListener listener, final Throwable t) {
			this.listener = listener;
			this.t = t;
		}

		@Override
		public void run() {
			if (listener == null) return;
			listener.onDownloadError(t);
		}
	}

	private final static class DownloadFinishRunnable implements Runnable {

		private final ImageLoader.DownloadListener listener;

		DownloadFinishRunnable(final ImageLoader.DownloadListener listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			if (listener == null) return;
			listener.onDownloadFinished();
		}
	}

	private final static class DownloadStartRunnable implements Runnable {

		private final ImageLoader.DownloadListener listener;
		private final long total;

		DownloadStartRunnable(final ImageLoader.DownloadListener listener, final long total) {
			this.listener = listener;
			this.total = total;
		}

		@Override
		public void run() {
			if (listener == null) return;
			listener.onDownloadStart(total);
		}
	}

	private final static class ProgressUpdateRunnable implements Runnable {

		private final ImageLoader.DownloadListener listener;
		private final long current;

		ProgressUpdateRunnable(final ImageLoader.DownloadListener listener, final long current) {
			this.listener = listener;
			this.current = current;
		}

		@Override
		public void run() {
			if (listener == null) return;
			listener.onProgressUpdate(current);
		}
	}
}