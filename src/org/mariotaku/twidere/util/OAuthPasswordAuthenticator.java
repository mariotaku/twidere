/*
 *				Twidere - Twitter client for Android
 * 
 * Copyright (C) 2012 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static android.text.TextUtils.isEmpty;

import org.ccil.cowan.tagsoup.HTMLSchema;
import org.ccil.cowan.tagsoup.Parser;
import org.mariotaku.twidere.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.http.HttpClient;
import twitter4j.http.HttpClientFactory;
import twitter4j.http.HttpParameter;
import twitter4j.http.HttpRequest;
import twitter4j.http.RequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class OAuthPasswordAuthenticator implements Constants {

	private static final String PATTERN_OAUTH_PIN = "^\\d+$";

	private final Twitter twitter;
	private final HttpClient client;

	private String authenticity_token, oauth_pin;

	private final ContentHandler mAuthenticityTokenHandler = new DummyContentHandler() {

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes atts) {
			if ("input".equalsIgnoreCase(localName) && "authenticity_token".equalsIgnoreCase(atts.getValue("", "name"))) {
				final String authenticity_token = atts.getValue("", "value");
				if (!isEmpty(authenticity_token)) {
					setAuthenticityToken(authenticity_token);
				}
			}
		}
	};

	private final ContentHandler mOAuthPINHandler = new DummyContentHandler() {

		private boolean start_div, start_code;

		@Override
		public void characters(final char[] ch, final int start, final int length) {
			if (start_code && ch != null) {
				final String value = new String(ch, start, length);
				if (value.matches(PATTERN_OAUTH_PIN)) {
					oauth_pin = value;
					start_div = false;
					start_code = false;
				}
			}
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) {
			if ("div".equalsIgnoreCase(localName)) {
				start_div = false;
			} else if ("code".equalsIgnoreCase(localName)) {
				start_code = false;
			}
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName, final Attributes atts) {
			if (!isEmpty(oauth_pin)) return;
			if ("div".equalsIgnoreCase(localName)) {
				start_div = "oauth_pin".equals(atts.getValue("", "id"));
			} else if ("code".equalsIgnoreCase(localName)) {
				if (start_div) {
					start_code = true;
				}
			}
		}

	};

	public OAuthPasswordAuthenticator(final Twitter twitter) {
		final Configuration conf = twitter.getConfiguration();
		this.twitter = twitter;
		client = HttpClientFactory.getInstance(conf);
	}

	public synchronized AccessToken getOAuthAccessToken(final String username, final String password)
			throws AuthenticationException {
		authenticity_token = null;
		oauth_pin = null;
		final RequestToken request_token;
		try {
			request_token = twitter.getOAuthRequestToken(OAUTH_CALLBACK_OOB);
		} catch (final TwitterException e) {
			if (e.isCausedByNetworkIssue()) throw new AuthenticationException(e);
			throw new AuthenticityTokenException();
		}
		try {
			final String oauth_token = request_token.getToken();
			readAuthenticityToken(getHTTPContent(request_token.getAuthorizationURL(), false, null));
			if (authenticity_token == null) throw new AuthenticityTokenException();
			final Configuration conf = twitter.getConfiguration();
			final HttpParameter[] params = new HttpParameter[4];
			params[0] = new HttpParameter("authenticity_token", authenticity_token);
			params[1] = new HttpParameter("oauth_token", oauth_token);
			params[2] = new HttpParameter("session[username_or_email]", username);
			params[3] = new HttpParameter("session[password]", password);
			readOAuthPIN(getHTTPContent(conf.getOAuthAuthorizationURL().toString(), true, params));
			if (isEmpty(oauth_pin)) throw new WrongUserPassException();
			return twitter.getOAuthAccessToken(request_token, oauth_pin);
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
			throws TwitterException {
		final RequestMethod method = post ? RequestMethod.POST : RequestMethod.GET;
		final HashMap<String, String> headers = new HashMap<String, String>();
		// headers.put("User-Agent", user_agent);
		final HttpRequest request = new HttpRequest(method, url_string, url_string, params, null, headers);
		return client.request(request).asStream();
	}

	private synchronized void readAuthenticityToken(final InputStream stream) throws SAXException, IOException {
		final InputSource source = new InputSource(stream);
		final Parser parser = new Parser();
		parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
		parser.setContentHandler(mAuthenticityTokenHandler);
		parser.parse(source);
	}

	private synchronized void readOAuthPIN(final InputStream stream) throws SAXException, IOException {
		final InputSource source = new InputSource(stream);
		final Parser parser = new Parser();
		parser.setProperty(Parser.schemaProperty, HtmlParser.schema);
		parser.setContentHandler(mOAuthPINHandler);
		parser.parse(source);
	}

	private synchronized void setAuthenticityToken(final String authenticity_token) {
		this.authenticity_token = authenticity_token;
	}

	public static class AuthenticationException extends Exception {

		private static final long serialVersionUID = -5629194721838256378L;

		AuthenticationException() {
		}

		AuthenticationException(final Exception cause) {
			super(cause);
		}

		AuthenticationException(final String message) {
			super(message);
		}
	}

	public static final class AuthenticityTokenException extends AuthenticationException {

		private static final long serialVersionUID = -1840298989316218380L;

	}

	public static final class WrongUserPassException extends AuthenticationException {

		private static final long serialVersionUID = -4880737459768513029L;

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

}
