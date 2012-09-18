package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.Utils.getConnection;
import static org.mariotaku.twidere.util.Utils.getProxy;
import static org.mariotaku.twidere.util.Utils.isNullOrEmpty;
import static org.mariotaku.twidere.util.Utils.parseURL;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
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
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import org.mariotaku.twidere.R;
import java.io.FileNotFoundException;

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
		 final RequestToken request_token = twitter.getOAuthRequestToken(TwitterLoginActivity.DEFAULT_OAUTH_CALLBACK);
			final String oauth_token = request_token.getToken();
			readAuthenticityToken(getHTTPContent(request_token.getAuthorizationURL(), "GET"));
			if (authenticity_token == null) throw new IOException("Cannot get authenticity token.");
			final Configuration conf = twitter.getConfiguration();
			final Uri.Builder authorization_url_builder = Uri.parse(conf.getOAuthAuthorizationURL()).buildUpon();
			authorization_url_builder.appendQueryParameter("authenticity_token", authenticity_token);
			authorization_url_builder.appendQueryParameter("oauth_token", oauth_token);
			authorization_url_builder.appendQueryParameter("session[username_or_email]", username);
			authorization_url_builder.appendQueryParameter("session[password]", password);
			readCallbackURL(getHTTPContent(authorization_url_builder.build().toString(), "POST"));
			if (callback_url == null) throw new AuthenticationException(context.getString(R.string.cannot_get_callback_url));
			if (!callback_url.startsWith(TwitterLoginActivity.DEFAULT_OAUTH_CALLBACK))
				throw new IOException("Wrong OAuth callback URL " + callback_url);
			final String oauth_verifier = Uri.parse(callback_url).getQueryParameter(TwitterLoginActivity.OAUTH_VERIFIER);
			if (isNullOrEmpty(oauth_verifier)) throw new IOException("Cannot get OAuth verifier.");
			return twitter.getOAuthAccessToken(request_token, oauth_verifier);
		} catch (FileNotFoundException e) {
			//TODO handle this exception
			throw new AuthenticationException("Failed to sign in, I'm working on this problem.");
		} catch (IOException e) {
			throw new AuthenticationException(e);
		} catch (SAXException e) {		
			throw new AuthenticationException(e);
		} catch (TwitterException e) {
			throw new AuthenticationException(e);
		}
	}

	private InputStream getHTTPContent(String url_string, String method) throws IOException {
		final URL url = parseURL(url_string);
		final Proxy proxy = getProxy(context);
		final boolean ignore_ssl_error = preferences.getBoolean(TwitterLoginActivity.PREFERENCE_KEY_IGNORE_SSL_ERROR,
				false);
		final HttpURLConnection conn = getConnection(url, ignore_ssl_error, proxy, resolver);
		if (conn == null) return null;
		conn.addRequestProperty("User-Agent", user_agent);
		if (method != null) {
			conn.setRequestMethod(method);
		}
		conn.setDoInput(true);
		conn.setDoOutput(false);
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
	
	public static final class AuthenticationException extends IOException {
		
		public AuthenticationException() {
			super();
		}
		
		public AuthenticationException(String message) {
			super(message);
		}
		
		public AuthenticationException(Exception cause) {
			super(cause);
		}
	}
}
