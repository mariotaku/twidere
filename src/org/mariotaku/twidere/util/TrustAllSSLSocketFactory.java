package org.mariotaku.twidere.util;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public final class TrustAllSSLSocketFactory extends SSLSocketFactory {

	final SSLContext sslContext;

	TrustAllSSLSocketFactory() throws KeyManagementException, NoSuchAlgorithmException {
		super();

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
		sslContext = SSLContext.getInstance("TLS");
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
			final SSLSocketFactory factory = new TrustAllSSLSocketFactory();
			factory.setHostnameVerifier(ALLOW_ALL_HOSTNAME_VERIFIER);
			return factory;
		} catch (final KeyStoreException e) {
		} catch (final KeyManagementException e) {
		} catch (final NoSuchAlgorithmException e) {
		} catch (final CertificateException e) {
		} catch (final IOException e) {
		}
		return null;
	}
}
