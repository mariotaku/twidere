/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util.imageloader;

import static org.mariotaku.twidere.util.TwidereLinkify.PATTERN_TWITTER_PROFILE_IMAGES;
import static org.mariotaku.twidere.util.TwidereLinkify.TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES;
import static org.mariotaku.twidere.util.Utils.generateBrowserUserAgent;
import static org.mariotaku.twidere.util.Utils.getImageLoaderHttpClient;
import static org.mariotaku.twidere.util.Utils.getProxy;
import static org.mariotaku.twidere.util.Utils.getRedirectedHttpResponse;
import static org.mariotaku.twidere.util.Utils.replaceLast;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.nostra13.universalimageloader.core.download.ImageDownloader;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.model.PreviewMedia;
import org.mariotaku.twidere.util.MediaPreviewUtils;
import org.mariotaku.twidere.util.Utils;
import org.mariotaku.twidere.util.io.ContentLengthInputStream;

import twitter4j.TwitterException;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class TwidereImageDownloader implements ImageDownloader, Constants {

	private static final HostnameVerifier ALLOW_ALL_HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();
	private static final TrustManager[] TRUST_ALL_CERTS = new TrustManager[] { new TrustAllX509TrustManager() };
	private static final SSLSocketFactory IGNORE_ERROR_SSL_FACTORY;

	static {
		System.setProperty("http.keepAlive", "false");
		SSLSocketFactory factory = null;
		try {
			final SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, TRUST_ALL_CERTS, new SecureRandom());
			factory = sc.getSocketFactory();
		} catch (final KeyManagementException e) {
		} catch (final NoSuchAlgorithmException e) {
		}
		IGNORE_ERROR_SSL_FACTORY = factory;
	}

	private final Context mContext;
	private final ContentResolver mResolver;
	private HttpClientWrapper mClient;
	private Proxy mProxy;
	private boolean mFastImageLoading;
	private String mUserAgent;
	private final boolean mFullImage;
	private final String mTwitterProfileImageSize;

	public TwidereImageDownloader(final Context context, final boolean fullImage) {
		mContext = context;
		mResolver = context.getContentResolver();
		mFullImage = fullImage;
		mTwitterProfileImageSize = String.format("_%s", context.getString(R.string.profile_image_size));
		reloadConnectivitySettings();
	}

	@Override
	public InputStream getStream(final String uriString, final Object extras) throws IOException {
		if (uriString == null) return null;
		final Uri uri = Uri.parse(uriString);
		final String scheme = uri.getScheme();
		if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_CONTENT.equals(scheme)
				|| ContentResolver.SCHEME_FILE.equals(scheme)) return mResolver.openInputStream(uri);
		final PreviewMedia media = MediaPreviewUtils.getAllAvailableImage(uriString, mFullImage, mFullImage
				|| !mFastImageLoading ? mClient : null);
		try {
			final String mediaUrl = media != null ? media.url : uriString;
			if (PATTERN_TWITTER_PROFILE_IMAGES.matcher(uriString).matches())
				return getStream(replaceLast(mediaUrl, "_" + TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES,
						mTwitterProfileImageSize));
			else
				return getStream(mediaUrl);
		} catch (final TwitterException e) {
			final int statusCode = e.getStatusCode();
			if (statusCode != -1 && PATTERN_TWITTER_PROFILE_IMAGES.matcher(uriString).matches()
					&& !uriString.contains("_normal.")) {
				try {
					return getStream(Utils.getNormalTwitterProfileImage(uriString));
				} catch (final TwitterException e2) {

				}
			}
			throw new IOException(String.format(Locale.US, "Error downloading image %s, error code: %d", uriString,
					statusCode));
		}
	}

	public void reloadConnectivitySettings() {
		mClient = getImageLoaderHttpClient(mContext);
		mFastImageLoading = mContext.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(
				KEY_FAST_IMAGE_LOADING, true);
		mProxy = getProxy(mContext);
		mUserAgent = generateBrowserUserAgent();
	}

	private ContentLengthInputStream getStream(final String uri_string) throws IOException, TwitterException {
		if (mFastImageLoading) {
			final URL url = new URL(uri_string);
			final HttpURLConnection conn = (HttpURLConnection) (mProxy != null ? url.openConnection(mProxy) : url
					.openConnection());
			if (conn instanceof HttpsURLConnection) {
				((HttpsURLConnection) conn).setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);
				if (IGNORE_ERROR_SSL_FACTORY != null) {
					((HttpsURLConnection) conn).setSSLSocketFactory(IGNORE_ERROR_SSL_FACTORY);
				}
			}
			conn.setRequestProperty("User-Agent", mUserAgent);
			conn.setInstanceFollowRedirects(true);
			return new ContentLengthInputStream(conn.getInputStream(), conn.getContentLength());
		} else {
			final HttpResponse resp = getRedirectedHttpResponse(mClient, uri_string);
			return new ContentLengthInputStream(resp.asStream(), (int) resp.getContentLength());
		}
	}

	private static final class AllowAllHostnameVerifier implements HostnameVerifier {
		@Override
		public boolean verify(final String hostname, final SSLSession session) {
			return true;
		}
	}

	private static final class TrustAllX509TrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
		}

		@Override
		public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[] {};
		}
	}
}
