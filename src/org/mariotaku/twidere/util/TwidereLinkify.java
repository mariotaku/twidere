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

import static org.mariotaku.twidere.util.Utils.getImglyImage;
import static org.mariotaku.twidere.util.Utils.getInstagramImage;
import static org.mariotaku.twidere.util.Utils.getLockerzAndPlixiImage;
import static org.mariotaku.twidere.util.Utils.getTwitpicImage;
import static org.mariotaku.twidere.util.Utils.getYfrogImage;
import static org.mariotaku.twidere.util.Utils.matcherEnd;
import static org.mariotaku.twidere.util.Utils.matcherGroup;
import static org.mariotaku.twidere.util.Utils.matcherStart;

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

import com.twitter.Regex;

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

public class TwidereLinkify {

	public static final int LINK_TYPE_MENTION = 1;
	public static final int LINK_TYPE_HASHTAG = 2;
	public static final int LINK_TYPE_IMAGE = 3;
	public static final int LINK_TYPE_LINK = 4;
	public static final int LINK_TYPE_INSTAGRAM = 5;
	public static final int LINK_TYPE_TWITPIC = 6;
	public static final int LINK_TYPE_LOCKERZ_AND_PLIXI = 7;
	public static final int LINK_TYPE_IMGLY = 8;
	public static final int LINK_TYPE_YFROG = 9;
	public static final int LINK_TYPE_LIST = 10;

	public static final int[] ALL_LINK_TYPES = new int[] { LINK_TYPE_MENTION, LINK_TYPE_HASHTAG, LINK_TYPE_IMAGE,
			LINK_TYPE_LINK, LINK_TYPE_INSTAGRAM, LINK_TYPE_TWITPIC, LINK_TYPE_LOCKERZ_AND_PLIXI, LINK_TYPE_IMGLY,
			LINK_TYPE_YFROG

	};

	public static final Pattern PATTERN_IMAGES = Pattern.compile("(https?:\\/\\/.+?(?i)(png|jpeg|jpg|gif|bmp))",
			Pattern.CASE_INSENSITIVE);

	public static final Pattern PATTERN_TWITTER_IMAGES = Pattern.compile(
			"(https?:\\/\\/p\\.twimg\\.com\\/([\\d\\w\\-_]+)\\.(png|jpeg|jpg|gif|bmp))", Pattern.CASE_INSENSITIVE);

	public static final String SINA_WEIBO_IMAGES_AVALIABLE_SIZES = "(large|thumbnail|bmiddle|mw600)";

	public static final Pattern PATTERN_SINA_WEIBO_IMAGES = Pattern.compile("(https?:\\/\\/[\\w\\d]+\\.sinaimg\\.cn\\/"
			+ SINA_WEIBO_IMAGES_AVALIABLE_SIZES + "\\/(([\\d\\w]+)\\.(png|jpeg|jpg|gif|bmp)))",
			Pattern.CASE_INSENSITIVE);

	public static final Pattern PATTERN_LOCKERZ_AND_PLIXI = Pattern
			.compile("https?:\\/\\/(plixi\\.com\\/p|lockerz\\.com\\/s)\\/(\\w+)\\/?");

	public static final Pattern PATTERN_INSTAGRAM = Pattern.compile(
			"(https?:\\/\\/(instagr\\.am|instagram\\.com)\\/p\\/([_\\-\\d\\w]+)\\/?)", Pattern.CASE_INSENSITIVE);

	public static final int INSTAGRAM_GROUP_ALL = 1;
	public static final int INSTAGRAM_GROUP_ID = 3;

	public static final Pattern PATTERN_TWITPIC = Pattern.compile("(https?:\\/\\/twitpic\\.com\\/([\\d\\w]+)\\/?)",
			Pattern.CASE_INSENSITIVE);

	public static final int TWITPIC_GROUP_ALL = 1;
	public static final int TWITPIC_GROUP_ID = 2;

	public static final Pattern PATTERN_IMGLY = Pattern.compile("(https?:\\/\\/img\\.ly\\/([\\w\\d]+)\\/?)",
			Pattern.CASE_INSENSITIVE);

	public static final int IMGLY_GROUP_ALL = 1;
	public static final int IMGLY_GROUP_ID = 2;

	public static final Pattern PATTERN_YFROG = Pattern.compile("(https?:\\/\\/yfrog\\.com\\/([\\w\\d]+)\\/?)",
			Pattern.CASE_INSENSITIVE);

	public static final int YFROG_GROUP_ALL = 1;
	public static final int YFROG_GROUP_ID = 2;

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

	public TwidereLinkify(TextView view) {
		this.view = view;
	}

	public final void addAllLinks() {
		for (final int type : ALL_LINK_TYPES) {
			addLinks(type);
		}
	}

	/**
	 * Applies a regex to the text of a TextView turning the matches into links.
	 * If links are found then UrlSpans are applied to the link text match
	 * areas, and the movement method for the text is changed to
	 * LinkMovementMethod.
	 * 
	 * @param description TextView whose text is to be marked-up with links
	 * @param pattern Regex pattern to be used for finding links
	 * @param scheme Url scheme string (eg <code>http://</code> to be prepended
	 *            to the url of links that do not have a scheme specified in the
	 *            link text
	 */
	public final void addLinks(int type) {
		final SpannableString string = SpannableString.valueOf(view.getText());
		switch (type) {
			case LINK_TYPE_MENTION: {
				addMentionOrListLinks(string);
				break;
			}
			case LINK_TYPE_HASHTAG: {
				addHashtagLinks(string);
				break;
			}
			case LINK_TYPE_IMAGE: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final int start = string.getSpanStart(span);
					final int end = string.getSpanEnd(span);
					final String url = span.getURL();
					if (url.matches(PATTERN_IMAGES.pattern())) {
						string.removeSpan(span);
						applyLink(url, start, end, string, LINK_TYPE_IMAGE);
					}
				}
				break;
			}
			case LINK_TYPE_LINK: {
				final ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();
				gatherLinks(links, string, Patterns.WEB_URL, new String[] { "http://", "https://", "rtsp://" },
						sUrlMatchFilter, null);
				for (final LinkSpec link : links) {
					final URLSpan[] spans = string.getSpans(link.start, link.end, URLSpan.class);
					if (spans == null || spans.length <= 0) {
						applyLink(link.url, link.start, link.end, string, LINK_TYPE_LINK);
					}
				}
				break;
			}
			case LINK_TYPE_INSTAGRAM: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final Matcher matcher = PATTERN_INSTAGRAM.matcher(span.getURL());
					if (matcher.matches()) {
						final int start = string.getSpanStart(span);
						final int end = string.getSpanEnd(span);
						final String url = getInstagramImage(matcherGroup(matcher, INSTAGRAM_GROUP_ID)).image_link;
						string.removeSpan(span);
						applyLink(url, start, end, string, LINK_TYPE_IMAGE);
					}
				}
				break;
			}
			case LINK_TYPE_TWITPIC: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final Matcher matcher = PATTERN_TWITPIC.matcher(span.getURL());
					if (matcher.matches()) {
						final int start = string.getSpanStart(span);
						final int end = string.getSpanEnd(span);
						final String url = getTwitpicImage(matcherGroup(matcher, TWITPIC_GROUP_ID)).image_link;
						string.removeSpan(span);
						applyLink(url, start, end, string, LINK_TYPE_IMAGE);
					}
				}
				break;
			}
			case LINK_TYPE_LOCKERZ_AND_PLIXI: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final Matcher matcher = PATTERN_LOCKERZ_AND_PLIXI.matcher(span.getURL());
					if (matcher.matches()) {
						final int start = string.getSpanStart(span);
						final int end = string.getSpanEnd(span);
						final String url = getLockerzAndPlixiImage(matcher.group()).image_link;
						string.removeSpan(span);
						applyLink(url, start, end, string, LINK_TYPE_IMAGE);
					}
				}
				break;
			}
			case LINK_TYPE_IMGLY: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final Matcher matcher = PATTERN_IMGLY.matcher(span.getURL());
					if (matcher.matches()) {
						final int start = string.getSpanStart(span);
						final int end = string.getSpanEnd(span);
						final String url = getImglyImage(matcherGroup(matcher, IMGLY_GROUP_ID)).image_link;
						string.removeSpan(span);
						applyLink(url, start, end, string, LINK_TYPE_IMAGE);
					}
				}
				break;
			}
			case LINK_TYPE_YFROG: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final Matcher matcher = PATTERN_YFROG.matcher(span.getURL());
					if (matcher.matches()) {
						final int start = string.getSpanStart(span);
						final int end = string.getSpanEnd(span);
						final String url = getYfrogImage(matcherGroup(matcher, YFROG_GROUP_ID)).image_link;
						string.removeSpan(span);
						applyLink(url, start, end, string, LINK_TYPE_IMAGE);
					}
				}
				break;
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

	private final boolean addHashtagLinks(Spannable spannable) {
		boolean hasMatches = false;
		final Matcher matcher = Regex.VALID_HASHTAG.matcher(spannable);

		while (matcher.find()) {
			final int start = matcherStart(matcher, Regex.VALID_HASHTAG_GROUP_HASHTAG_FULL);
			final int end = matcherEnd(matcher, Regex.VALID_HASHTAG_GROUP_HASHTAG_FULL);
			final String url = matcherGroup(matcher, Regex.VALID_HASHTAG_GROUP_HASHTAG_FULL);

			applyLink(url, start, end, spannable, LINK_TYPE_HASHTAG);
			hasMatches = true;
		}

		return hasMatches;
	}

	private final boolean addMentionOrListLinks(Spannable spannable) {
		boolean hasMatches = false;
		final Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(spannable);

		while (matcher.find()) {
			final int start = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_AT);
			final int username_end = matcherEnd(matcher,Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
			final int list_start = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			final int list_end = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			final String mention = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
			final String list = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			applyLink(mention, start, username_end, spannable, LINK_TYPE_MENTION);
			if (list_start >= 0 && list_end >= 0) {
				applyLink(mention + "/" + list, list_start, list_end, spannable, LINK_TYPE_LIST);
			}
			hasMatches = true;
		}

		return hasMatches;
	}

	private final void applyLink(String url, int start, int end, Spannable text, int type) {
		final LinkSpan span = new LinkSpan(url, type, mOnLinkClickListener);

		text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private static final void addLinkMovementMethod(TextView t) {
		final MovementMethod m = t.getMovementMethod();

		if (m == null || !(m instanceof LinkMovementMethod)) {
			if (t.getLinksClickable()) {
				t.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

	private static final void gatherLinks(ArrayList<LinkSpec> links, Spannable s, Pattern pattern, String[] schemes,
			MatchFilter matchFilter, TransformFilter transformFilter) {
		final Matcher m = pattern.matcher(s);

		while (m.find()) {
			final int start = m.start();
			final int end = m.end();

			if (matchFilter == null || matchFilter.acceptMatch(s, start, end)) {
				final LinkSpec spec = new LinkSpec();
				final String url = makeUrl(m.group(0), schemes, m, transformFilter);

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
