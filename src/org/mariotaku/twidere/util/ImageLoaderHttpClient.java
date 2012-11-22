package org.mariotaku.twidere.util;

import android.content.Context;
import twitter4j.http.HostAddressResolver;
import org.mariotaku.twidere.app.TwidereApplication;
import org.apache.http.HttpResponse;
import java.io.InputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;

public class ImageLoaderHttpClient {

	private final HostAddressResolver mResolver;

	public ImageLoaderHttpClient(final Context context) {
		mResolver = TwidereApplication.getInstance(context).getHostAddressResolver();
	}
	
	public InputStream getAsStream(final String urlString) throws IOException {
		final URL url = new URL(urlString);
		final HttpsURLConnection conn = url.openConnection();
		conn.setSSLSocketFactory();
	}
}
