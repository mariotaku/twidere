package org.mariotaku.twidere.util.net;

import twitter4j.http.HttpClient;
import twitter4j.http.HttpClientConfiguration;
import twitter4j.http.HttpClientFactory;

public class ApacheHttpClientFactory implements HttpClientFactory {

	@Override
	public HttpClient getInstance(final HttpClientConfiguration conf) {
		return new HttpClientImpl(conf);
	}

}
