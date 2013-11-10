/*
 * Copyright 2007 Yusuke Yamamoto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.util.net;

import static android.text.TextUtils.isEmpty;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import twitter4j.TwitterException;
import twitter4j.http.HostAddressResolver;
import twitter4j.http.HttpClientConfiguration;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpResponseCode;
import twitter4j.http.RequestMethod;
import twitter4j.internal.logging.Logger;
import twitter4j.internal.util.InternalStringUtil;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * HttpClient implementation for Apache HttpClient 4.0.x
 * 
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.2
 */
public class HttpClientImpl implements twitter4j.http.HttpClient, HttpResponseCode {
	private static final Logger logger = Logger.getLogger(HttpClientImpl.class);
	private final HttpClientConfiguration conf;
	private final HttpClient client;

	private static final SSLSocketFactory TRUST_ALL_SSL_SOCKET_FACTORY = TrustAllSSLSocketFactory.getInstance();

	public HttpClientImpl(final HttpClientConfiguration conf) {
		this.conf = conf;
		final SchemeRegistry registry = new SchemeRegistry();
		final SSLSocketFactory factory = conf.isSSLErrorIgnored() ? TRUST_ALL_SSL_SOCKET_FACTORY : SSLSocketFactory
				.getSocketFactory();
		registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		registry.register(new Scheme("https", factory, 443));
		final HttpParams params = new BasicHttpParams();
		final ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, registry);
		final DefaultHttpClient client = new DefaultHttpClient(cm, params);
		final HttpParams client_params = client.getParams();
		HttpConnectionParams.setConnectionTimeout(client_params, conf.getHttpConnectionTimeout());
		HttpConnectionParams.setSoTimeout(client_params, conf.getHttpReadTimeout());

		if (conf.getHttpProxyHost() != null && !conf.getHttpProxyHost().equals("")) {
			final HttpHost proxy = new HttpHost(conf.getHttpProxyHost(), conf.getHttpProxyPort());
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

			if (conf.getHttpProxyUser() != null && !conf.getHttpProxyUser().equals("")) {
				if (logger.isDebugEnabled()) {
					logger.debug("Proxy AuthUser: " + conf.getHttpProxyUser());
					logger.debug("Proxy AuthPassword: " + InternalStringUtil.maskString(conf.getHttpProxyPassword()));
				}
				client.getCredentialsProvider().setCredentials(
						new AuthScope(conf.getHttpProxyHost(), conf.getHttpProxyPort()),
						new UsernamePasswordCredentials(conf.getHttpProxyUser(), conf.getHttpProxyPassword()));
			}
		}
		this.client = client;
	}

	@Override
	public twitter4j.http.HttpResponse request(final twitter4j.http.HttpRequest req) throws TwitterException {
		try {
			HttpRequestBase commonsRequest;

			final HostAddressResolver resolver = conf.getHostAddressResolver();
			final String url_string = req.getURL();
			final URI url_orig;
			try {
				url_orig = new URI(url_string);
			} catch (final URISyntaxException e) {
				throw new TwitterException(e);
			}
			final String host = url_orig.getHost(), authority = url_orig.getAuthority();
			final String resolved_host = resolver != null ? resolver.resolve(host) : null;
			final String resolved_url = !isEmpty(resolved_host) ? url_string.replace("://" + host, "://"
					+ resolved_host) : url_string;

			if (req.getMethod() == RequestMethod.GET) {
				commonsRequest = new HttpGet(resolved_url);
			} else if (req.getMethod() == RequestMethod.POST) {
				final HttpPost post = new HttpPost(resolved_url);
				// parameter has a file?
				boolean hasFile = false;
				final HttpParameter[] params = req.getParameters();
				if (params != null) {
					for (final HttpParameter parameter : params) {
						if (parameter.isFile()) {
							hasFile = true;
							break;
						}
					}
					if (!hasFile) {
						if (params.length > 0) {
							post.setEntity(new UrlEncodedFormEntity(params));
						}
					} else {
						final MultipartEntity me = new MultipartEntity();
						for (final HttpParameter parameter : params) {
							if (parameter.isFile()) {
								final ContentBody body;
								if (parameter.getFile() != null) {
									body = new FileBody(parameter.getFile(), parameter.getContentType());
								} else {
									body = new InputStreamBody(parameter.getFileBody(), parameter.getFileName(),
											parameter.getContentType());
								}
								me.addPart(parameter.getName(), body);
							} else {
								final ContentBody body = new StringBody(parameter.getValue(),
										"text/plain; charset=UTF-8", Charset.forName("UTF-8"));
								me.addPart(parameter.getName(), body);
							}
						}
						post.setEntity(me);
					}
				}
				post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
				commonsRequest = post;
			} else if (req.getMethod() == RequestMethod.DELETE) {
				commonsRequest = new HttpDelete(resolved_url);
			} else if (req.getMethod() == RequestMethod.HEAD) {
				commonsRequest = new HttpHead(resolved_url);
			} else if (req.getMethod() == RequestMethod.PUT) {
				commonsRequest = new HttpPut(resolved_url);
			} else
				throw new TwitterException("Unsupported request method " + req.getMethod());
			final Map<String, String> headers = req.getRequestHeaders();
			for (final String headerName : headers.keySet()) {
				commonsRequest.addHeader(headerName, headers.get(headerName));
			}
			String authorizationHeader;
			if (req.getAuthorization() != null
					&& (authorizationHeader = req.getAuthorization().getAuthorizationHeader(req)) != null) {
				commonsRequest.addHeader("Authorization", authorizationHeader);
			}
			if (!isEmpty(resolved_host) && !resolved_host.equals(host)) {
				commonsRequest.addHeader("Host", authority);
			}

			final ApacheHttpClientHttpResponseImpl res;
			try {
				res = new ApacheHttpClientHttpResponseImpl(client.execute(commonsRequest), conf);
			} catch (final IllegalStateException e) {
				throw new TwitterException("Please check your API settings.", e);
			} catch (final NullPointerException e) {
				// Bug http://code.google.com/p/android/issues/detail?id=5255
				throw new TwitterException("Please check your APN settings, make sure not to use WAP APNs.", e);
			} catch (final OutOfMemoryError e) {
				// I don't know why OOM thown, but it should be catched.
				System.gc();
				throw new TwitterException("Unknown error", e);
			}
			final int statusCode = res.getStatusCode();
			if (statusCode < OK || statusCode > ACCEPTED) throw new TwitterException(res.asString(), req, res);
			return res;
		} catch (final IOException e) {
			throw new TwitterException(e);
		}
	}

	@Override
	public void shutdown() {
		client.getConnectionManager().shutdown();
	}

	final static class TrustAllSSLSocketFactory extends SSLSocketFactory {
		final SSLContext sslContext = SSLContext.getInstance(TLS);

		TrustAllSSLSocketFactory(final KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException,
				KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			final TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
				}

				@Override
				public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}

		@Override
		public Socket createSocket(final Socket socket, final String host, final int port, final boolean autoClose)
				throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		public static SSLSocketFactory getInstance() {
			try {
				final KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				trustStore.load(null, null);
				final SSLSocketFactory factory = new TrustAllSSLSocketFactory(trustStore);
				factory.setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);
				return factory;
			} catch (final GeneralSecurityException e) {
				logger.error("Exception while creating SSLSocketFactory instance", e);
			} catch (final IOException e) {
				logger.error("Exception while creating SSLSocketFactory instance", e);
			}
			return null;
		}
	}
}
