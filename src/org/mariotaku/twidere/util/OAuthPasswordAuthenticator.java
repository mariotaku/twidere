package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.getConnection;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.mariotaku.twidere.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import twitter4j.HostAddressResolver;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;

public class OAuthPasswordAuthenticator implements Constants {

	private final boolean ignore_ssl_error;
	private final HostAddressResolver resolver;
	private final Twitter twitter;
	private final String user_agent;
	private final Proxy proxy;
	private final int connection_timeout;

	private String authenticity_token, callback_url;

	private final ContentHandler mAuthenticityTokenHandler = new DummyContentHandler() {

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes atts) {
			if ("input".equalsIgnoreCase(localName) && "authenticity_token".equalsIgnoreCase(atts.getValue("", "name"))) {
				final String authenticity_token = atts.getValue("", "value");
				if (!isNullOrEmpty(authenticity_token)) {
					setAuthenticityToken(authenticity_token);
				}
			}
		}
	};

	private final ContentHandler mCallbackURLHandler = new DummyContentHandler() {

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes atts) {
			if ("meta".equalsIgnoreCase(localName) && "refresh".equalsIgnoreCase(atts.getValue("", "http-equiv"))) {
				final String content = atts.getValue("", "content");
				final String url_prefix = "url=";
				final int idx = content.indexOf(url_prefix);
				if (!isNullOrEmpty(content) && idx != -1) {
					final String url = content.substring(idx + url_prefix.length());
					if (!isNullOrEmpty(url)) {
						callback_url = url;
					}
				}
			}
		}
	};

	public OAuthPasswordAuthenticator(final Twitter twitter, final Proxy proxy, final String user_agent) {
		final Configuration conf = twitter.getConfiguration();
		this.twitter = twitter;
		this.user_agent = user_agent;
		this.proxy = proxy;
		resolver = conf.getHostAddressResolver();
		ignore_ssl_error = conf.isSSLErrorIgnored();
		connection_timeout = conf.getHttpConnectionTimeout();
	}

	public AccessToken getOAuthAccessToken(final String username, final String password)
			throws AuthenticationException, OAuthPasswordAuthenticator.CallbackURLException {
		authenticity_token = null;
		callback_url = null;
		try {
			final RequestToken request_token = twitter.getOAuthRequestToken(DEFAULT_OAUTH_CALLBACK);
			final String oauth_token = request_token.getToken();
			readAuthenticityToken(getHTTPContent(request_token.getAuthorizationURL(), false, null));
			if (authenticity_token == null) throw new AuthenticationException("Cannot get authenticity token.");
			final Configuration conf = twitter.getConfiguration();
			final HttpParameter[] params = new HttpParameter[4];
			params[0] = new HttpParameter("authenticity_token", authenticity_token);
			params[1] = new HttpParameter("oauth_token", oauth_token);
			params[2] = new HttpParameter("session[username_or_email]", username);
			params[3] = new HttpParameter("session[password]", password);
			readCallbackURL(getHTTPContent(conf.getOAuthAuthorizationURL().toString(), true, params));
			if (callback_url == null) throw new CallbackURLException();
			if (!callback_url.startsWith(DEFAULT_OAUTH_CALLBACK))
				throw new IOException("Wrong OAuth callback URL " + callback_url);
			final String oauth_verifier = parseParameters(callback_url.substring(callback_url.indexOf("?") + 1)).get(
					INTENT_KEY_OAUTH_VERIFIER);
			if (isNullOrEmpty(oauth_verifier)) throw new AuthenticationException("Cannot get OAuth verifier.");
			return twitter.getOAuthAccessToken(request_token, oauth_verifier);
		} catch (final IOException e) {
			throw new AuthenticationException(e);
		} catch (final SAXException e) {
			throw new AuthenticationException(e);
		} catch (final TwitterException e) {
			throw new AuthenticationException(e);
		} catch (final NullPointerException e) {
			throw new AuthenticationException(e);
		}
	}

	private InputStream getHTTPContent(final String url_string, final boolean post, final HttpParameter[] params)
			throws IOException {
		final URL url = parseURL(url_string);
		final HttpURLConnection conn = getConnection(url, connection_timeout, ignore_ssl_error, proxy, resolver);
		if (conn == null) return null;
		conn.addRequestProperty("User-Agent", user_agent);
		conn.setRequestMethod(post ? "POST" : "GET");
		if (post && params != null) {
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			final String postParam = HttpParameter.encodeParameters(params);
			final byte[] bytes = postParam.getBytes("UTF-8");
			conn.setRequestProperty("Content-Length", Integer.toString(bytes.length));
			conn.setDoOutput(true);
			final OutputStream os = conn.getOutputStream();
			os.write(bytes);
			os.flush();
			os.close();
		}
		return conn.getInputStream();
	}

	private void readAuthenticityToken(final InputStream stream) throws SAXException, IOException {
		final InputSource source = new InputSource(stream);
		final Parser parser = new Parser();
		parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
		parser.setContentHandler(mAuthenticityTokenHandler);
		parser.parse(source);
	}

	private void readCallbackURL(final InputStream stream) throws SAXException, IOException {
		final InputSource source = new InputSource(stream);
		final Parser parser = new Parser();
		parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
		parser.setContentHandler(mCallbackURLHandler);
		parser.parse(source);
	}

	private void setAuthenticityToken(final String authenticity_token) {
		this.authenticity_token = authenticity_token;
	}

	private static Map<String, String> parseParameters(final String raw_params_string)
			throws UnsupportedEncodingException {
		if (raw_params_string == null) return Collections.emptyMap();
		final Map<String, String> params_map = new HashMap<String, String>();
		final String[] raw_params_array = raw_params_string.split("&");
		for (final String raw_param : raw_params_array) {
			final String[] raw_param_segment = raw_param.split("=");
			if (raw_param_segment.length != 2) {
				continue;
			}
			params_map.put(URLDecoder.decode(raw_param_segment[0], "UTF-8"),
					URLDecoder.decode(raw_param_segment[1], "UTF-8"));
		}
		return params_map;
	}

	public static class AuthenticationException extends Exception {

		private static final long serialVersionUID = -5629194721838256378L;

		AuthenticationException() {
			super();
		}

		AuthenticationException(final Exception cause) {
			super(cause);
		}

		AuthenticationException(final String message) {
			super(message);
		}
	}

	public static final class CallbackURLException extends AuthenticationException {

		private static final long serialVersionUID = 1735318863603574697L;

	}

	static class DummyContentHandler implements ContentHandler {
		@Override
		public void characters(final char[] ch, final int start, final int length) {
		}

		@Override
		public void endDocument() {
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) {
		}

		@Override
		public void endPrefixMapping(final String prefix) {
		}

		@Override
		public void ignorableWhitespace(final char[] ch, final int start, final int length) {
		}

		@Override
		public void processingInstruction(final String target, final String data) {
		}

		@Override
		public void setDocumentLocator(final Locator locator) {
		}

		@Override
		public void skippedEntity(final String name) {
		}

		@Override
		public void startDocument() {

		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes atts) {
		}

		@Override
		public void startPrefixMapping(final String prefix, final String uri) {
		}
	}

	/**
	 * Lazy initialization holder for HTML parser. This class will a) be
	 * preloaded by the zygote, or b) not loaded until absolutely necessary.
	 */
	static final class HtmlParser {
		private static final HTMLSchema schema = new HTMLSchema();
	}

	static final class HttpParameter {
		final String name;
		final String value;

		HttpParameter(final String name, final String value) {
			this.name = name;
			this.value = value;
		}

		static String encode(final String value) {
			String encoded = null;
			try {
				encoded = URLEncoder.encode(value, "UTF-8");
			} catch (final UnsupportedEncodingException ignore) {
			}
			final StringBuffer buf = new StringBuffer(encoded.length());
			char focus;
			for (int i = 0; i < encoded.length(); i++) {
				focus = encoded.charAt(i);
				if (focus == '*') {
					buf.append("%2A");
				} else if (focus == '+') {
					buf.append("%20");
				} else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7'
						&& encoded.charAt(i + 2) == 'E') {
					buf.append('~');
					i += 2;
				} else {
					buf.append(focus);
				}
			}
			return buf.toString();
		}

		static String encodeParameters(final HttpParameter[] httpParams) {
			if (null == httpParams) return "";
			final StringBuffer buf = new StringBuffer();
			for (int j = 0; j < httpParams.length; j++) {
				if (j != 0) {
					buf.append("&");
				}
				buf.append(encode(httpParams[j].name)).append("=").append(encode(httpParams[j].value));
			}
			return buf.toString();
		}

	}
}
