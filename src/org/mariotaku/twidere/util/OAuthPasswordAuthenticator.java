package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.getConnection;
import static org.mariotaku.twidere.util.Utils.getProxy;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.mariotaku.twidere.R;
import org.mariotaku.twidere.activity.TwitterLoginActivity;
import org.mariotaku.twidere.app.TwidereApplication;
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
import twitter4j.internal.http.HttpParameter;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class OAuthPasswordAuthenticator {

	private final SharedPreferences preferences;
	private final HostAddressResolver resolver;

	private final Context context;
	private final Twitter twitter;

	private final String user_agent;

	private String authenticity_token, callback_url;

	private final ContentHandler mAuthenticityTokenHandler = new DummyContentHandler() {

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) {
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
		public void startElement(String uri, String localName, String qName, Attributes atts) {
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

	public OAuthPasswordAuthenticator(Context context, String user_agent, Twitter twitter) {
		this.context = context;
		preferences = context.getSharedPreferences(TwitterLoginActivity.SHARED_PREFERENCES_NAME,
				TwitterLoginActivity.MODE_PRIVATE);
		resolver = TwidereApplication.getInstance(context).getHostAddressResolver();
		this.user_agent = user_agent;
		this.twitter = twitter;
	}

	public AccessToken getOAuthAccessToken(String username, String password) throws AuthenticationException {
		authenticity_token = null;
		callback_url = null;
		try {
			final RequestToken request_token = twitter
					.getOAuthRequestToken(TwitterLoginActivity.DEFAULT_OAUTH_CALLBACK);
			final String oauth_token = request_token.getToken();
			readAuthenticityToken(getHTTPContent(request_token.getAuthorizationURL(), false, null));
			if (authenticity_token == null) throw new IOException("Cannot get authenticity token.");
			final Configuration conf = twitter.getConfiguration();
			final HttpParameter[] params = new HttpParameter[4];
			params[0] = new HttpParameter("authenticity_token", authenticity_token);
			params[1] = new HttpParameter("oauth_token", oauth_token);
			params[2] = new HttpParameter("session[username_or_email]", username);
			params[3] = new HttpParameter("session[password]", password);
			readCallbackURL(getHTTPContent(conf.getOAuthAuthorizationURL().toString(), true, params));
			if (callback_url == null)
				throw new AuthenticationException(context.getString(R.string.cannot_get_callback_url));
			if (!callback_url.startsWith(TwitterLoginActivity.DEFAULT_OAUTH_CALLBACK))
				throw new IOException("Wrong OAuth callback URL " + callback_url);
			final String oauth_verifier = Uri.parse(callback_url)
					.getQueryParameter(TwitterLoginActivity.OAUTH_VERIFIER);
			if (isNullOrEmpty(oauth_verifier)) throw new IOException("Cannot get OAuth verifier.");
			return twitter.getOAuthAccessToken(request_token, oauth_verifier);
		} catch (final IOException e) {
			throw new AuthenticationException(e);
		} catch (final SAXException e) {
			throw new AuthenticationException(e);
		} catch (final TwitterException e) {
			throw new AuthenticationException(e);
		}
	}

	private InputStream getHTTPContent(String url_string, boolean post, HttpParameter[] params) throws IOException {
		final URL url = parseURL(url_string);
		final Proxy proxy = getProxy(context);
		final boolean ignore_ssl_error = preferences.getBoolean(TwitterLoginActivity.PREFERENCE_KEY_IGNORE_SSL_ERROR,
				false);
		final HttpURLConnection conn = getConnection(url, ignore_ssl_error, proxy, resolver);
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

	private void readAuthenticityToken(InputStream stream) throws SAXException, IOException {
		final InputSource source = new InputSource(stream);
		final Parser parser = new Parser();
		parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
		parser.setContentHandler(mAuthenticityTokenHandler);
		parser.parse(source);
	}

	private void readCallbackURL(InputStream stream) throws SAXException, IOException {
		final InputSource source = new InputSource(stream);
		final Parser parser = new Parser();
		parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
		parser.setContentHandler(mCallbackURLHandler);
		parser.parse(source);
	}

	private void setAuthenticityToken(String authenticity_token) {
		this.authenticity_token = authenticity_token;
	}

	public static final class AuthenticationException extends Exception {

		private static final long serialVersionUID = -5629194721838256378L;

		public AuthenticationException() {
			super();
		}

		public AuthenticationException(Exception cause) {
			super(cause);
		}

		public AuthenticationException(String message) {
			super(message);
		}
	}

	static class DummyContentHandler implements ContentHandler {
		@Override
		public void characters(char[] ch, int start, int length) {
		}

		@Override
		public void endDocument() {
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
		}

		@Override
		public void endPrefixMapping(String prefix) {
		}

		@Override
		public void ignorableWhitespace(char[] ch, int start, int length) {
		}

		@Override
		public void processingInstruction(String target, String data) {
		}

		@Override
		public void setDocumentLocator(Locator locator) {
		}

		@Override
		public void skippedEntity(String name) {
		}

		@Override
		public void startDocument() {

		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) {
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) {
		}
	}

	/**
	 * Lazy initialization holder for HTML parser. This class will a) be
	 * preloaded by the zygote, or b) not loaded until absolutely necessary.
	 */
	static final class HtmlParser {
		private static final HTMLSchema schema = new HTMLSchema();
	}
}
