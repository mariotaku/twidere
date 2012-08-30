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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class HtmlBuilder {

	private final String string;
	private final int string_length;
	private final boolean strict;

	private final List<LinkSpec> links = new ArrayList<LinkSpec>();

	public HtmlBuilder(String string) {
		this(string, false);
	}

	public HtmlBuilder(String string, boolean strict) {
		this.string = string;
		this.strict = strict;
		if (string == null) throw new NullPointerException();
		string_length = string.length();
	}

	public void addLink(String link, String display, int start, int end) {
		if (start >= end) {
			if (strict) throw new IllegalArgumentException("start must lesser than end!");
			return;
		}
		if (start < 0 || start >= string_length || end < 0 || end > string_length) {
			if (strict)
				throw new StringIndexOutOfBoundsException("String length = " + string_length + ", start = " + start
						+ ", end = " + end);
			return;
		}
		final int links_size = links.size();
		for (int i = 0; i < links_size; i++) {
			final LinkSpec spec = links.get(i);
			if (start >= spec.start && start <= spec.end || end >= spec.start && end <= spec.end) {
				if (strict) throw new IllegalArgumentException("link already added in this range!");
				return;
			}
		}
		links.add(new LinkSpec(link, display, start, end));
	}

	public String build() {
		if (links.size() == 0) return escapeHTMLString(string);
		Collections.sort(links, LinkSpec.COMPARATOR);
		final StringBuilder builder = new StringBuilder();
		final int links_size = links.size();
		for (int i = 0; i < links_size; i++) {
			final LinkSpec spec = links.get(i);
			if (i == 0) {
				try {
					builder.append(escapeHTMLString(string.substring(0, spec.start)));
				} catch (final StringIndexOutOfBoundsException e) {
					throw new StringIndexOutOfBoundsException("String = " + string + ", end = 0 , start = "
							+ spec.start);
				}
			}
			if (i > 0) {
				try {
					builder.append(escapeHTMLString(string.substring(links.get(i - 1).end, spec.start)));
				} catch (final StringIndexOutOfBoundsException e) {
					throw new StringIndexOutOfBoundsException("String = " + string + ", end = " + links.get(i - 1).end
							+ ", start = " + spec.start);
				}
			}
			builder.append("<a href=\"" + spec.link + "\">");
			builder.append(spec.display == null ? escapeHTMLString(string.substring(spec.start, spec.end))
					: spec.display);
			builder.append("</a>");
			if (i == links.size() - 1) {
				builder.append(escapeHTMLString(string.substring(spec.end, string.length())));
			}
		}
		return builder.toString();
	}

	private static String escapeHTMLString(String string) {
		final StringBuffer sb = new StringBuffer(string.length());
		// true if last char was blank
		boolean lastWasBlankChar = false;
		final int len = string.length();
		for (int i = 0; i < len; i++) {
			final char c = string.charAt(i);
			if (c == ' ') {
				// blank gets extra work,
				// this solves the problem you get if you replace all
				// blanks with &nbsp;, if you do that you loss
				// word breaking
				if (lastWasBlankChar) {
					lastWasBlankChar = false;
					sb.append("&nbsp;");
				} else {
					lastWasBlankChar = true;
					sb.append(' ');
				}
			} else {
				lastWasBlankChar = false;
				// HTML Special Chars
				switch (c) {
					case '"':
						sb.append("&quot;");
						break;
					case '&':
						sb.append("&amp;");
						break;
					case '<':
						sb.append("&lt;");
						break;
					case '>':
						sb.append("&gt;");
						break;
					case '\n':
						sb.append("<br/>");
						break;
					default:
						final int ci = 0xffff & c;
						if (ci < 160) {
							// nothing special only 7 Bit
							sb.append(c);
						} else {
							// Not 7 Bit use the unicode system
							sb.append("&#");
							sb.append(ci);
							sb.append(';');
						}
						break;

				}
			}
		}
		return sb.toString();
	}

	static class LinkSpec {

		private static final Comparator<LinkSpec> COMPARATOR = new Comparator<LinkSpec>() {

			@Override
			public int compare(LinkSpec lhs, LinkSpec rhs) {
				return lhs.start - rhs.start;
			}
		};

		final String link, display;
		final int start, end;

		LinkSpec(String link, String display, int start, int end) {
			this.link = link;
			this.display = display;
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return "LinkSpec(" + link + ", " + start + ", " + end + ")";
		}
	}

}
