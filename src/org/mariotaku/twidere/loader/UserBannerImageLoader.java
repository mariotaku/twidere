package org.mariotaku.twidere.loader;

import static org.mariotaku.twidere.util.Utils.copyStream;
import static org.mariotaku.twidere.util.Utils.createAlphaGradientBanner;
import static org.mariotaku.twidere.util.Utils.getBestBannerType;
import static org.mariotaku.twidere.util.Utils.getBestCacheDir;
import static org.mariotaku.twidere.util.Utils.getHttpClient;
import static org.mariotaku.twidere.util.Utils.getProxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.mariotaku.twidere.app.TwidereApplication;
import org.mariotaku.twidere.fragment.UserProfileFragment;
import org.mariotaku.twidere.model.ParcelableUser;

import twitter4j.TwitterException;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HttpClientWrapper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.AsyncTaskLoader;

public class UserBannerImageLoader extends AsyncTaskLoader<Bitmap> {

	private static final String CACHE_DIR = "cached_images";

	private final ParcelableUser user;
	private final Context context;
	private final HostAddressResolver resolver;
	private final int width;
	private final int connection_timeout;
	private final boolean gradient_effect;

	public UserBannerImageLoader(final Context context, final ParcelableUser user, final int width,
			final boolean gradient_effect) {
		super(context);
		this.context = context;
		this.user = user;
		this.width = width;
		this.gradient_effect = gradient_effect;
		resolver = TwidereApplication.getInstance(context).getHostAddressResolver();
		connection_timeout = context.getSharedPreferences(UserProfileFragment.SHARED_PREFERENCES_NAME,
				Context.MODE_PRIVATE).getInt(UserProfileFragment.PREFERENCE_KEY_CONNECTION_TIMEOUT, 10) * 1000;
	}

	@Override
	public Bitmap loadInBackground() {
		if (user == null || user.profile_banner_url == null) return null;
		try {
			final String url = user.profile_banner_url + "/" + getBestBannerType(width);
			final File cache_dir = getImageCacheDir();
			final File cache_file = cache_dir != null && cache_dir.isDirectory() ? new File(cache_dir,
					getURLFilename(url)) : null;
			if (cache_file != null && cache_file.isFile()) {
				final BitmapFactory.Options o = new BitmapFactory.Options();
				// o.inSampleSize = scale_down ? 2 : 1;
				final Bitmap cache_bitmap = BitmapFactory.decodeFile(cache_file.getPath(), o);
				if (cache_bitmap != null)
					return gradient_effect ? createAlphaGradientBanner(cache_bitmap) : cache_bitmap;
			}
			final HttpClientWrapper client = getHttpClient(connection_timeout, true, getProxy(context), resolver, null);
			if (cache_file != null) {
				final FileOutputStream fos = new FileOutputStream(cache_file);
				final InputStream is = client.get(url, null).asStream();
				copyStream(is, fos);
				final BitmapFactory.Options o = new BitmapFactory.Options();
				// o.inSampleSize = scale_down ? 2 : 1;
				final Bitmap bitmap = BitmapFactory.decodeFile(cache_file.getPath(), o);
				return gradient_effect ? createAlphaGradientBanner(bitmap) : bitmap;
			} else {
				final Bitmap bitmap = BitmapFactory.decodeStream(client.get(url, null).asStream());
				return gradient_effect ? createAlphaGradientBanner(bitmap) : bitmap;
			}
		} catch (final IOException e) {
			return null;
		} catch (final TwitterException e) {
			return null;
		}
	}

	@Override
	protected void onStartLoading() {
		forceLoad();
	}

	private File getImageCacheDir() {
		final File cache_dir = getBestCacheDir(context, CACHE_DIR);
		if (cache_dir != null && !cache_dir.exists()) {
			cache_dir.mkdirs();
		}
		return cache_dir;
	}

	private String getURLFilename(final String url) {
		if (url == null) return null;
		return url.replaceFirst("https?:\\/\\/", "").replaceAll("[^a-zA-Z0-9]", "_");
	}

}