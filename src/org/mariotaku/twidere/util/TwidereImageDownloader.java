package org.mariotaku.twidere.util;
import com.nostra13.universalimageloader.core.download.ImageDownloader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import android.content.Context;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;
import twitter4j.TwitterException;

public class TwidereImageDownloader extends ImageDownloader {

	private final Context context;
	private HttpClientWrapper client;

	public TwidereImageDownloader(final Context context) {
		this.context = context;
		initHttpClient();
	}
	
	public void initHttpClient() {
		client = Utils.getImageLoaderHttpClient(context);
	}

	protected InputStream getStreamFromNetwork(URI uri) throws IOException {
		final InputStream is;
		try {
			final HttpResponse resp = Utils.getRedirectedHttpResponse(client, uri.toString());
			is = resp.asStream();
		} catch (TwitterException e) {
			throw new IOException(e);
		}
		return is;
	}
	
}
