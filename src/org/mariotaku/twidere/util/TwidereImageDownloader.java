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

import static org.mariotaku.twidere.util.Utils.getImageLoaderHttpClient;
import static org.mariotaku.twidere.util.Utils.getProxy;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.mariotaku.twidere.Constants;

import twitter4j.TwitterException;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.nostra13.universalimageloader.core.download.ImageDownloader;

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

	private final Context context;
	private final ContentResolver resolver;
	private HttpClientWrapper client;
	private Proxy proxy;
	private boolean fast_image_loading;

	public TwidereImageDownloader(final Context context) {
		this.context = context;
		resolver = context.getContentResolver();
		initHttpClient();
	}

	@Override
	public InputStream getStream(final String uri_string, final Object extras) throws IOException {
		final InputStream is;
		if (uri_string == null) return null;
		final Uri uri = Uri.parse(uri_string);
		final String scheme = uri.getScheme();
		try {
			if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_CONTENT.equals(scheme)
					|| ContentResolver.SCHEME_FILE.equals(scheme)) return resolver.openInputStream(uri);
			if (fast_image_loading) {
				final URL url = new URL(uri_string);
				final HttpURLConnection conn = (HttpURLConnection) (proxy != null ? url.openConnection(proxy) : url
						.openConnection());
				if (conn instanceof HttpsURLConnection) {
					((HttpsURLConnection) conn).setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);
					if (IGNORE_ERROR_SSL_FACTORY != null) {
						((HttpsURLConnection) conn).setSSLSocketFactory(IGNORE_ERROR_SSL_FACTORY);
					}
				}
				conn.setInstanceFollowRedirects(true);
				is = new ContentLengthInputStream(conn.getInputStream(), conn.getContentLength());
			} else {
				final HttpResponse resp = Utils.getRedirectedHttpResponse(client, uri_string);
				is = new ContentLengthInputStream(resp.asStream(), (int) resp.getContentLength());
			}
		} catch (final TwitterException e) {
			throw new IOException(e.getMessage());
		}
		return is;
	}

	public void initHttpClient() {
		client = getImageLoaderHttpClient(context);
		fast_image_loading = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getBoolean(
				PREFERENCE_KEY_FAST_IMAGE_LOADING, true);
		proxy = getProxy(context);
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
