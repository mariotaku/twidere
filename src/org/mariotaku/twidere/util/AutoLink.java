/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mariotaku.twidere.util;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

/**
 * Linkify take a piece of text and a regular expression and turns all of the
 * regex matches in the text into clickable links. This is particularly useful
 * for matching things like email addresses, web urls, etc. and making them
 * actionable.
 * 
 * Alone with the pattern that is to be matched, a url scheme prefix is also
 * required. Any pattern match that does not begin with the supplied scheme will
 * have the scheme prepended to the matched text when the clickable url is
 * created. For instance, if you are matching web urls you would supply the
 * scheme <code>http://</code>. If the pattern matches example.com, which does
 * not have a url scheme prefix, the supplied scheme will be prepended to create
 * <code>http://example.com</code> when the clickable url link is created.
 */

public class AutoLink {

	public static final int LINK_TYPE_MENTIONS = 1;
	public static final int LINK_TYPE_HASHTAGS = 2;
	public static final int LINK_TYPE_IMAGES = 3;
	public static final int LINK_TYPE_LINKS = 4;

	private static final Pattern PATTERN_MENTIONS = Pattern.compile("@([A-Za-z0-9_]+)");
	private static final Pattern PATTERN_HASHTAGS = Pattern.compile("#([A-Za-z0-9_]+)");
	private static final Pattern PATTERN_IMAGES = Pattern.compile("http(s?):\\/\\/.+?(?i)(png|jpeg|jpg|gif|bmp)");

	private final TextView view;

	private OnLinkClickListener mOnLinkClickListener;

	/**
	 * Filters out web URL matches that occur after an at-sign (@). This is to
	 * prevent turning the domain name in an email address into a web link.
	 */
	public static final MatchFilter sUrlMatchFilter = new MatchFilter() {
		@Override
		public final boolean acceptMatch(CharSequence s, int start, int end) {
			if (start == 0) return true;

			if (s.charAt(start - 1) == '@') return false;

			return true;
		}
	};

	public AutoLink(TextView view) {
		this.view = view;
	}

	/**
	 * Applies a regex to the text of a TextView turning the matches into links.
	 * If links are found then UrlSpans are applied to the link text match
	 * areas, and the movement method for the text is changed to
	 * LinkMovementMethod.
	 * 
	 * @param text TextView whose text is to be marked-up with links
	 * @param pattern Regex pattern to be used for finding links
	 * @param scheme Url scheme string (eg <code>http://</code> to be prepended
	 *            to the url of links that do not have a scheme specified in the
	 *            link text
	 */
	public final void addLinks(int type) {
		final SpannableString string = SpannableString.valueOf(view.getText());
		switch (type) {
			case LINK_TYPE_MENTIONS: {
				addLinks(string, PATTERN_MENTIONS, type);
				break;
			}
			case LINK_TYPE_HASHTAGS: {
				addLinks(string, PATTERN_HASHTAGS, type);
				break;
			}
			case LINK_TYPE_IMAGES: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (URLSpan span : spans) {
					int start = string.getSpanStart(span);
					int end = string.getSpanEnd(span);
					String url = span.getURL();
					if (url.matches(PATTERN_IMAGES.pattern())) {
						string.removeSpan(span);
					}
					applyLink(url, start, end, string, type);
				}
				break;
			}
			case LINK_TYPE_LINKS: {
				ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();
				gatherLinks(links, string, Patterns.WEB_URL, new String[] { "http://", "https://", "rtsp://" },
						sUrlMatchFilter, null);
				for (LinkSpec link : links) {
					URLSpan[] spans = string.getSpans(link.start, link.end, URLSpan.class);
					if (spans == null || spans.length <= 0) {
						applyLink(link.url, link.start, link.end, string, type);
					}
				}
			}
			default: {
				return;
			}

		}

		view.setText(string);
		addLinkMovementMethod(view);
	}

	public OnLinkClickListener getmOnLinkClickListener() {
		return mOnLinkClickListener;
	}

	public void setOnLinkClickListener(OnLinkClickListener listener) {
		mOnLinkClickListener = listener;
	}

	/**
	 * Applies a regex to a Spannable turning the matches into links.
	 * 
	 * @param text Spannable whose text is to be marked-up with links
	 * @param pattern Regex pattern to be used for finding links
	 * @param scheme Url scheme string (eg <code>http://</code> to be prepended
	 *            to the url of links that do not have a scheme specified in the
	 *            link text
	 */
	private final boolean addLinks(Spannable spannable, Pattern pattern, int type) {
		boolean hasMatches = false;
		Matcher matcher = pattern.matcher(spannable);

		while (matcher.find()) {
			int start = matcher.start();
			int end = matcher.end();
			boolean allowed = true;

			if (allowed) {
				String url = matcher.group(0);

				applyLink(url, start, end, spannable, type);
				hasMatches = true;
			}
		}

		return hasMatches;
	}

	private final void applyLink(String url, int start, int end, Spannable text, int type) {
		LinkSpan span = new LinkSpan(url, type, mOnLinkClickListener);

		text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private static final void addLinkMovementMethod(TextView t) {
		MovementMethod m = t.getMovementMethod();

		if (m == null || !(m instanceof LinkMovementMethod)) {
			if (t.getLinksClickable()) {
				t.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

	private static final void gatherLinks(ArrayList<LinkSpec> links, Spannable s, Pattern pattern, String[] schemes,
			MatchFilter matchFilter, TransformFilter transformFilter) {
		Matcher m = pattern.matcher(s);

		while (m.find()) {
			int start = m.start();
			int end = m.end();

			if (matchFilter == null || matchFilter.acceptMatch(s, start, end)) {
				LinkSpec spec = new LinkSpec();
				String url = makeUrl(m.group(0), schemes, m, transformFilter);

				spec.url = url;
				spec.start = start;
				spec.end = end;

				links.add(spec);
			}
		}
	}

	private static final String makeUrl(String url, String[] prefixes, Matcher m, TransformFilter filter) {
		if (filter != null) {
			url = filter.transformUrl(m, url);
		}

		boolean hasPrefix = false;

		for (int i = 0; i < prefixes.length; i++) {
			if (url.regionMatches(true, 0, prefixes[i], 0, prefixes[i].length())) {
				hasPrefix = true;

				// Fix capitalization if necessary
				if (!url.regionMatches(false, 0, prefixes[i], 0, prefixes[i].length())) {
					url = prefixes[i] + url.substring(prefixes[i].length());
				}

				break;
			}
		}

		if (!hasPrefix) {
			url = prefixes[0] + url;
		}

		return url;
	}

	/**
	 * MatchFilter enables client code to have more control over what is allowed
	 * to match and become a link, and what is not.
	 * 
	 * For example: when matching web urls you would like things like
	 * http://www.example.com to match, as well as just example.com itelf.
	 * However, you would not want to match against the domain in
	 * support@example.com. So, when matching against a web url pattern you
	 * might also include a MatchFilter that disallows the match if it is
	 * immediately preceded by an at-sign (@).
	 */
	public interface MatchFilter {
		/**
		 * Examines the character span matched by the pattern and determines if
		 * the match should be turned into an actionable link.
		 * 
		 * @param s The body of text against which the pattern was matched
		 * @param start The index of the first character in s that was matched
		 *            by the pattern - inclusive
		 * @param end The index of the last character in s that was matched -
		 *            exclusive
		 * 
		 * @return Whether this match should be turned into a link
		 */
		boolean acceptMatch(CharSequence s, int start, int end);
	}

	public interface OnLinkClickListener {
		public void onLinkClick(String link, int type);
	}

	/**
	 * TransformFilter enables client code to have more control over how matched
	 * patterns are represented as URLs.
	 * 
	 * For example: when converting a phone number such as (919) 555-1212 into a
	 * tel: URL the parentheses, white space, and hyphen need to be removed to
	 * produce tel:9195551212.
	 */
	public interface TransformFilter {
		/**
		 * Examines the matched text and either passes it through or uses the
		 * data in the Matcher state to produce a replacement.
		 * 
		 * @param match The regex matcher state that found this URL text
		 * @param url The text that was matched
		 * 
		 * @return The transformed form of the URL
		 */
		String transformUrl(final Matcher match, String url);
	}

	private static class LinkSpan extends URLSpan {

		private final int type;
		private final OnLinkClickListener listener;

		public LinkSpan(String url, int type, OnLinkClickListener listener) {
			super(url);
			this.type = type;
			this.listener = listener;
		}

		@Override
		public void onClick(View widget) {
			if (listener != null) {
				listener.onLinkClick(getURL(), type);
			}
		}

	}
}

class LinkSpec {
	String url;
	int start;
	int end;
}