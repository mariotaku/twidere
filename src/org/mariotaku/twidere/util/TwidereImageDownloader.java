package org.mariotaku.twidere.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.nostra13.universalimageloader.core.download.ImageDownloader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import twitter4j.TwitterException;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;

public class TwidereImageDownloader implements ImageDownloader {

	private final Context context;
	private final ContentResolver resolver;
	private HttpClientWrapper client;

	public TwidereImageDownloader(final Context context) {
		this.context = context;
		this.resolver = context.getContentResolver();
		initHttpClient();
	}

	@Override
	public InputStream getStream(final String uri_string, final Object extras) throws IOException {
		final InputStream is;
		if (uri_string == null) return null;
		final Uri uri = Uri.parse(uri_string);
		final String scheme = uri.getScheme();
		try {
			if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)
					|| ContentResolver.SCHEME_CONTENT.equals(scheme)
							|| ContentResolver.SCHEME_FILE.equals(scheme))
				return resolver.openInputStream(uri);
			final HttpResponse resp = Utils.getRedirectedHttpResponse(client, uri_string);
			is = new ContentLengthInputStream(resp.asStream(), (int) resp.getContentLength());
		} catch (final TwitterException e) {
			throw new IOException(e);
		}
		return is;
	}

	public void initHttpClient() {
		client = Utils.getImageLoaderHttpClient(context);
	}
	
	private static class ContentLengthInputStream extends InputStream {

		private final InputStream stream;
		private final int length;
		private int available;

		ContentLengthInputStream(final InputStream stream, final int length) {
			this.stream = stream;
			this.length = this.available = length;
		}
		
		@Override
		public synchronized int available() {
			return available;
		}
		
		@Override
		public void close() throws IOException {
			stream.close();
		}
		
		public int length() {
			return length;
		}
		
		@Override
		public int read() throws IOException {
			available--;
			return stream.read();
		}

	}

}
