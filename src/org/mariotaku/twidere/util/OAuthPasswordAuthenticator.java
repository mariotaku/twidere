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

import android.text.TextUtils;
import android.util.Xml;

import org.mariotaku.twidere.Constants;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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
import java.io.Reader;
import java.util.HashMap;

public class OAuthPasswordAuthenticator implements Constants {

	private final Twitter twitter;
	private final HttpClient client;

	public OAuthPasswordAuthenticator(final Twitter twitter) {
		final Configuration conf = twitter.getConfiguration();
		this.twitter = twitter;
		client = HttpClientFactory.getInstance(conf);
	}

	public synchronized AccessToken getOAuthAccessToken(final String username, final String password)
			throws AuthenticationException {
		final RequestToken request_token;
		try {
			request_token = twitter.getOAuthRequestToken(OAUTH_CALLBACK_OOB);
		} catch (final TwitterException e) {
			if (e.isCausedByNetworkIssue()) throw new AuthenticationException(e);
			throw new AuthenticityTokenException();
		}
		try {
			final String oauth_token = request_token.getToken();
			final String authenticity_token = readAuthenticityTokenFromHtml(getHTTPContent(
					request_token.getAuthorizationURL(), false, null));
			if (authenticity_token == null) throw new AuthenticityTokenException();
			final Configuration conf = twitter.getConfiguration();
			final HttpParameter[] params = new HttpParameter[4];
			params[0] = new HttpParameter("authenticity_token", authenticity_token);
			params[1] = new HttpParameter("oauth_token", oauth_token);
			params[2] = new HttpParameter("session[username_or_email]", username);
			params[3] = new HttpParameter("session[password]", password);
			final String oauth_pin = readOAuthPINFromHtml(getHTTPContent(conf.getOAuthAuthorizationURL().toString(),
					true, params));
			if (isEmpty(oauth_pin)) throw new WrongUserPassException();
			return twitter.getOAuthAccessToken(request_token, oauth_pin);
		} catch (final IOException e) {
			throw new AuthenticationException(e);
		} catch (final TwitterException e) {
			throw new AuthenticationException(e);
		} catch (final NullPointerException e) {
			throw new AuthenticationException(e);
		} catch (final XmlPullParserException e) {
			throw new AuthenticationException(e);
		}
	}

	private Reader getHTTPContent(final String url_string, final boolean post, final HttpParameter[] params)
			throws TwitterException {
		final RequestMethod method = post ? RequestMethod.POST : RequestMethod.GET;
		final HashMap<String, String> headers = new HashMap<String, String>();
		// headers.put("User-Agent", user_agent);
		final HttpRequest request = new HttpRequest(method, url_string, url_string, params, null, headers);
		return client.request(request).asReader();
	}

	public static String readAuthenticityTokenFromHtml(final Reader in) throws IOException, XmlPullParserException {
		final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
		final XmlPullParser parser = f.newPullParser();
		parser.setFeature(Xml.FEATURE_RELAXED, true);
		parser.setInput(in);
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			final String tag = parser.getName();
			switch (parser.getEventType()) {
				case XmlPullParser.START_TAG: {
					if ("input".equals(tag) && "authenticity_token".equals(parser.getAttributeValue(null, "name")))
						return parser.getAttributeValue(null, "value");
				}
			}
		}
		return null;
	}

	public static String readOAuthPINFromHtml(final Reader in) throws XmlPullParserException, IOException {
		boolean start_div = false, start_code = false;
		final XmlPullParserFactory f = XmlPullParserFactory.newInstance();
		final XmlPullParser parser = f.newPullParser();
		parser.setFeature(Xml.FEATURE_RELAXED, true);
		parser.setInput(in);
		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			final String tag = parser.getName();
			final int type = parser.getEventType();
			if (type == XmlPullParser.START_TAG) {
				if ("div".equalsIgnoreCase(tag)) {
					start_div = "oauth_pin".equals(parser.getAttributeValue(null, "id"));
				} else if ("code".equalsIgnoreCase(tag)) {
					if (start_div) {
						start_code = true;
					}
				}
			} else if (type == XmlPullParser.END_TAG) {
				if ("div".equalsIgnoreCase(tag)) {
					start_div = false;
				} else if ("code".equalsIgnoreCase(tag)) {
					start_code = false;
				}
			} else if (type == XmlPullParser.TEXT) {
				final String text = parser.getText();
				if (start_code && !TextUtils.isEmpty(text) && TextUtils.isDigitsOnly(text)) return text;
			}
		}
		return null;
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

}
