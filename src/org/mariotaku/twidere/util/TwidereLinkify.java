/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.util;

import static org.mariotaku.twidere.util.MediaPreviewUtils.AVAILABLE_IMAGE_SHUFFIX;
import static org.mariotaku.twidere.util.MediaPreviewUtils.PATTERN_ALL_AVAILABLE_IMAGES;
import static org.mariotaku.twidere.util.MediaPreviewUtils.PATTERN_IMAGES;
import static org.mariotaku.twidere.util.Utils.matcherEnd;
import static org.mariotaku.twidere.util.Utils.matcherGroup;
import static org.mariotaku.twidere.util.Utils.matcherStart;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.widget.TextView;

import com.twitter.Extractor;
import com.twitter.Extractor.Entity;
import com.twitter.Regex;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.model.PreviewMedia;
import org.mariotaku.twidere.text.TwidereURLSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Linkify take a piece of text and a regular expression and turns all of the
 * regex matches in the text into clickable links. This is particularly useful
 * for matching things like email addresses, web urls, etc. and making them
 * actionable. Alone with the pattern that is to be matched, a url scheme prefix
 * is also required. Any pattern match that does not begin with the supplied
 * scheme will have the scheme prepended to the matched text when the clickable
 * url is created. For instance, if you are matching web urls you would supply
 * the scheme <code>http://</code>. If the pattern matches example.com, which
 * does not have a url scheme prefix, the supplied scheme will be prepended to
 * create <code>http://example.com</code> when the clickable url link is
 * created.
 */

public final class TwidereLinkify implements Constants {

	public static final int LINK_TYPE_MENTION = 1;
	public static final int LINK_TYPE_HASHTAG = 2;
	public static final int LINK_TYPE_LINK_WITH_IMAGE_EXTENSION = 3;
	public static final int LINK_TYPE_LINK = 4;
	public static final int LINK_TYPE_ALL_AVAILABLE_IMAGE = 5;
	public static final int LINK_TYPE_LIST = 6;
	public static final int LINK_TYPE_CASHTAG = 7;
	public static final int LINK_TYPE_USER_ID = 8;
	public static final int LINK_TYPE_STATUS = 9;

	public static final int[] ALL_LINK_TYPES = new int[] { LINK_TYPE_LINK, LINK_TYPE_MENTION, LINK_TYPE_HASHTAG,
			LINK_TYPE_STATUS, LINK_TYPE_LINK_WITH_IMAGE_EXTENSION, LINK_TYPE_ALL_AVAILABLE_IMAGE, LINK_TYPE_CASHTAG };

	public static final String AVAILABLE_URL_SCHEME_PREFIX = "(https?:\\/\\/)?";

	public static final String TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES = "(bigger|normal|mini|reasonably_small)";
	private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME = "(twimg[\\d\\w\\-]+\\.akamaihd\\.net|[\\w\\d]+\\.twimg\\.com)\\/profile_images\\/([\\d\\w\\-_]+)\\/([\\d\\w\\-_]+)_"
			+ TWITTER_PROFILE_IMAGES_AVAILABLE_SIZES + "(\\.?" + AVAILABLE_IMAGE_SHUFFIX + ")?";
	private static final String STRING_PATTERN_TWITTER_PROFILE_IMAGES = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_TWITTER_PROFILE_IMAGES_NO_SCHEME;

	public static final Pattern PATTERN_TWITTER_PROFILE_IMAGES = Pattern.compile(STRING_PATTERN_TWITTER_PROFILE_IMAGES,
			Pattern.CASE_INSENSITIVE);

	private static final String STRING_PATTERN_TWITTER_STATUS_NO_SCHEME = "((mobile|www)\\.)?twitter\\.com\\/(?:#!\\/)?(\\w+)\\/status(es)?\\/(\\d+)(\\/photo\\/\\d)?\\/?";
	private static final String STRING_PATTERN_TWITTER_STATUS = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_TWITTER_STATUS_NO_SCHEME;
	public static final Pattern PATTERN_TWITTER_STATUS = Pattern.compile(STRING_PATTERN_TWITTER_STATUS,
			Pattern.CASE_INSENSITIVE);
	public static final int GROUP_ID_TWITTER_STATUS_SCREEN_NAME = 4;
	public static final int GROUP_ID_TWITTER_STATUS_STATUS_ID = 6;

	private static final String STRING_PATTERN_TWITTER_LIST_NO_SCHEME = "((mobile|www)\\.)?twitter\\.com\\/(?:#!\\/)?(\\w+)\\/lists\\/(.+)\\/?";
	private static final String STRING_PATTERN_TWITTER_LIST = AVAILABLE_URL_SCHEME_PREFIX
			+ STRING_PATTERN_TWITTER_LIST_NO_SCHEME;
	public static final Pattern PATTERN_TWITTER_LIST = Pattern.compile(STRING_PATTERN_TWITTER_LIST,
			Pattern.CASE_INSENSITIVE);

	public static final int GROUP_ID_TWITTER_LIST_SCREEN_NAME = 4;
	public static final int GROUP_ID_TWITTER_LIST_LIST_NAME = 5;

	private final OnLinkClickListener mOnLinkClickListener;
	private final Extractor mExtractor = new Extractor();
	private int mHighlightOption, mHighlightColor;

	public TwidereLinkify(final OnLinkClickListener listener) {
		this(listener, VALUE_LINK_HIGHLIGHT_OPTION_CODE_BOTH, 0);
	}

	public TwidereLinkify(final OnLinkClickListener listener, final int highlightOption, final int highlightColor) {
		mOnLinkClickListener = listener;
		setHighlightOption(highlightOption);
		setLinkTextColor(highlightColor);
	}

	public final void applyAllLinks(final TextView view, final long account_id, final boolean sensitive) {
		applyAllLinks(view, account_id, sensitive, mOnLinkClickListener, mHighlightOption, mHighlightColor);
	}

	public final void applyAllLinks(final TextView view, final long account_id, final boolean sensitive,
			final OnLinkClickListener listener, final int highlightOption, final int highlightColor) {
		view.setMovementMethod(LinkMovementMethod.getInstance());
		final SpannableString string = SpannableString.valueOf(view.getText());
		for (final int type : ALL_LINK_TYPES) {
			addLinks(string, account_id, type, sensitive, listener, highlightOption, highlightColor);
		}
		view.setText(string);
		addLinkMovementMethod(view);
	}

	public final void applyUserProfileLink(final TextView view, final long account_id, final long user_id,
			final String screen_name) {
		applyUserProfileLink(view, account_id, user_id, screen_name, mOnLinkClickListener, mHighlightOption,
				mHighlightColor);
	}

	public final void applyUserProfileLink(final TextView view, final long account_id, final long user_id,
			final String screen_name, final OnLinkClickListener listener, final int highlightOption,
			final int highlightColor) {
		view.setMovementMethod(LinkMovementMethod.getInstance());
		final SpannableString string = SpannableString.valueOf(view.getText());
		final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
		for (final URLSpan span : spans) {
			string.removeSpan(span);
		}
		if (user_id > 0) {
			applyLink(String.valueOf(user_id), 0, string.length(), string, account_id, LINK_TYPE_USER_ID, false,
					listener, highlightOption, highlightColor);
		} else if (screen_name != null) {
			applyLink(screen_name, 0, string.length(), string, account_id, LINK_TYPE_MENTION, false, listener,
					highlightOption, highlightColor);
		}
		view.setText(string);
		addLinkMovementMethod(view);
	}

	public final void applyUserProfileLinkNoHighlight(final TextView view, final long account_id, final long user_id,
			final String screen_name) {
		applyUserProfileLink(view, account_id, user_id, screen_name, mOnLinkClickListener,
				VALUE_LINK_HIGHLIGHT_OPTION_CODE_NONE, mHighlightColor);
	}

	public void setHighlightOption(final int style) {
		mHighlightOption = style;
	}

	public void setLinkTextColor(final int color) {
		mHighlightColor = color;
	}

	private final boolean addCashtagLinks(final Spannable spannable, final long account_id,
			final OnLinkClickListener listener, final int highlightOption, final int highlightColor) {
		boolean hasMatches = false;
		for (final Entity entity : mExtractor.extractCashtagsWithIndices(spannable.toString())) {
			final int start = entity.getStart();
			final int end = entity.getEnd();
			applyLink(entity.getValue(), start, end, spannable, account_id, LINK_TYPE_HASHTAG, false, listener,
					highlightOption, highlightColor);
			hasMatches = true;
		}
		return hasMatches;
	}

	private final boolean addHashtagLinks(final Spannable spannable, final long account_id,
			final OnLinkClickListener listener, final int highlightOption, final int highlightColor) {
		boolean hasMatches = false;
		final Matcher matcher = Regex.VALID_HASHTAG.matcher(spannable);
		for (final Entity entity : mExtractor.extractHashtagsWithIndices(spannable.toString())) {
			final int start = entity.getStart();
			final int end = entity.getEnd();
			applyLink(entity.getValue(), start, end, spannable, account_id, LINK_TYPE_HASHTAG, false, listener,
					highlightOption, highlightColor);
			hasMatches = true;
		}
		return hasMatches;
	}

	/**
	 * Applies a regex to the text of a TextView turning the matches into links.
	 * If links are found then UrlSpans are applied to the link text match
	 * areas, and the movement method for the text is changed to
	 * LinkMovementMethod.
	 * 
	 * @param highlightColor
	 * @param highlightOption
	 * @param listener
	 * 
	 * @param description TextView whose text is to be marked-up with links
	 * @param pattern Regex pattern to be used for finding links
	 * @param scheme Url scheme string (eg <code>http://</code> to be prepended
	 *            to the url of links that do not have a scheme specified in the
	 *            link text
	 */
	private final void addLinks(final SpannableString string, final long account_id, final int type,
			final boolean sensitive, final OnLinkClickListener listener, final int highlightOption,
			final int highlightColor) {
		switch (type) {
			case LINK_TYPE_MENTION: {
				addMentionOrListLinks(string, account_id, listener, highlightOption, highlightColor);
				break;
			}
			case LINK_TYPE_HASHTAG: {
				addHashtagLinks(string, account_id, listener, highlightOption, highlightColor);
				break;
			}
			case LINK_TYPE_LINK_WITH_IMAGE_EXTENSION: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final int start = string.getSpanStart(span);
					final int end = string.getSpanEnd(span);
					final String url = span.getURL();
					if (PATTERN_IMAGES.matcher(url).matches()) {
						string.removeSpan(span);
						applyLink(url, start, end, string, account_id, LINK_TYPE_LINK_WITH_IMAGE_EXTENSION, sensitive,
								listener, highlightOption, highlightColor);
					}
				}
				break;
			}
			case LINK_TYPE_LINK: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final int start = string.getSpanStart(span);
					final int end = string.getSpanEnd(span);
					if (start < 0 || end > string.length() || start > end) {
						continue;
					}
					string.removeSpan(span);
					applyLink(span.getURL(), start, end, string, account_id, LINK_TYPE_LINK, sensitive, listener,
							highlightOption, highlightColor);
				}
				// for (final Extractor.Entity entity :
				// mExtractor.extractURLsWithIndices(ParseUtils.parseString(string)))
				// {
				// final int start = entity.getStart(), end = entity.getEnd();
				// if (entity.getType() != Extractor.Entity.Type.URL
				// || string.getSpans(start, end, URLSpan.class).length > 0) {
				// continue;
				// }
				// applyLink(entity.getValue(), start, end, string, account_id,
				// LINK_TYPE_LINK, sensitive, listener,
				// highlightOption, highlightColor);
				// }
				break;
			}
			case LINK_TYPE_ALL_AVAILABLE_IMAGE: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final Matcher matcher = PATTERN_ALL_AVAILABLE_IMAGES.matcher(span.getURL());
					if (matcher.matches()) {
						final PreviewMedia spec = MediaPreviewUtils.getAllAvailableImage(matcher.group(), true);
						final int start = string.getSpanStart(span);
						final int end = string.getSpanEnd(span);
						if (spec == null || start < 0 || end > string.length() || start > end) {
							continue;
						}
						string.removeSpan(span);
						applyLink(spec.url, spec.original, start, end, string, account_id,
								LINK_TYPE_LINK_WITH_IMAGE_EXTENSION, sensitive, listener, highlightOption,
								highlightColor);
					}
				}
				break;
			}
			case LINK_TYPE_STATUS: {
				final URLSpan[] spans = string.getSpans(0, string.length(), URLSpan.class);
				for (final URLSpan span : spans) {
					final Matcher matcher = PATTERN_TWITTER_STATUS.matcher(span.getURL());
					if (matcher.matches()) {
						final int start = string.getSpanStart(span);
						final int end = string.getSpanEnd(span);
						final String url = matcherGroup(matcher, GROUP_ID_TWITTER_STATUS_STATUS_ID);
						string.removeSpan(span);
						applyLink(url, start, end, string, account_id, LINK_TYPE_STATUS, sensitive, listener,
								highlightOption, highlightColor);
					}
				}
				break;
			}
			case LINK_TYPE_CASHTAG: {
				addCashtagLinks(string, account_id, listener, highlightOption, highlightColor);
				break;
			}
			default: {
				return;
			}

		}
	}

	private final boolean addMentionOrListLinks(final Spannable spannable, final long account_id,
			final OnLinkClickListener listener, final int highlightOption, final int highlightColor) {
		boolean hasMatches = false;
		final Matcher matcher = Regex.VALID_MENTION_OR_LIST.matcher(spannable);

		while (matcher.find()) {
			final int start = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_AT);
			final int username_end = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
			final int list_start = matcherStart(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			final int list_end = matcherEnd(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			final String mention = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_USERNAME);
			final String list = matcherGroup(matcher, Regex.VALID_MENTION_OR_LIST_GROUP_LIST);
			applyLink(mention, start, username_end, spannable, account_id, LINK_TYPE_MENTION, false, listener,
					highlightOption, highlightColor);
			if (list_start >= 0 && list_end >= 0) {
				applyLink(mention + "/" + list, list_start, list_end, spannable, account_id, LINK_TYPE_LIST, false,
						listener, highlightOption, highlightColor);
			}
			hasMatches = true;
		}
		final URLSpan[] spans = spannable.getSpans(0, spannable.length(), URLSpan.class);
		for (final URLSpan span : spans) {
			final Matcher m = PATTERN_TWITTER_LIST.matcher(span.getURL());
			if (m.matches()) {
				final int start = spannable.getSpanStart(span);
				final int end = spannable.getSpanEnd(span);
				final String screen_name = matcherGroup(m, GROUP_ID_TWITTER_LIST_SCREEN_NAME);
				final String list_name = matcherGroup(m, GROUP_ID_TWITTER_LIST_LIST_NAME);
				spannable.removeSpan(span);
				applyLink(screen_name + "/" + list_name, start, end, spannable, account_id, LINK_TYPE_LIST, false,
						listener, highlightOption, highlightColor);
			}
		}
		return hasMatches;
	}

	private final void applyLink(final String url, final int start, final int end, final Spannable text,
			final long accountId, final int type, final boolean sensitive, final OnLinkClickListener listener,
			final int highlightOption, final int highlightColor) {
		applyLink(url, null, start, end, text, accountId, type, sensitive, listener, highlightOption, highlightColor);
	}

	private final void applyLink(final String url, final String orig, final int start, final int end,
			final Spannable text, final long accountId, final int type, final boolean sensitive,
			final OnLinkClickListener listener, final int highlightOption, final int highlightColor) {
		final TwidereURLSpan span = new TwidereURLSpan(url, orig, accountId, type, sensitive, listener,
				highlightOption, highlightColor);
		text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
	}

	private static final void addLinkMovementMethod(final TextView t) {
		final MovementMethod m = t.getMovementMethod();
		if (m == null || !(m instanceof LinkMovementMethod)) {
			if (t.getLinksClickable()) {
				t.setMovementMethod(LinkMovementMethod.getInstance());
			}
		}
	}

	public interface OnLinkClickListener {
		public void onLinkClick(String link, String orig, long account_id, int type, boolean sensitive);
	}
}
