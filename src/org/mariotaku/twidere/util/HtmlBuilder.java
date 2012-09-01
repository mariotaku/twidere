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

import static org.mariotaku.twidere.util.HtmlEscapeHelper.escape;
import static org.mariotaku.twidere.util.HtmlEscapeHelper.unescape;

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
		if (string == null) throw new NullPointerException();
		this.string = string;
		this.strict = strict;
		string_length = string.length();
	}

	public boolean addLink(String link, String display, int start, int end) {
		// if (start >= end) {
		// if (strict) throw new
		// IllegalArgumentException("start must lesser than end!");
		// return;
		// }
		if (start < 0 || end < 0 || start > end || end > string_length) {
			if (strict)
				throw new StringIndexOutOfBoundsException("String length = " + string_length + ", start = " + start
						+ ", end = " + end);
			return false;
		}
		for (final LinkSpec spec : links) {
			if (start >= spec.start && start <= spec.end || end >= spec.start && end <= spec.end) {
				if (strict) throw new IllegalArgumentException("link already added in this range!");
				return false;
			}
		}
		return links.add(new LinkSpec(link, display, start, end));
	}

	public String build(boolean unescape) {
		if (links.size() == 0) return escape(unescape ? unescape(string) : string);
		Collections.sort(links, LinkSpec.COMPARATOR);
		final StringBuilder builder = new StringBuilder();
		final int links_size = links.size();
		for (int i = 0; i < links_size; i++) {
			final LinkSpec spec = links.get(i);
			if (spec == null) {
				continue;
			}
			final int start = spec.start, end = spec.end;
			if (i == 0) {
				if (start >= 0 && start <= string_length) {
					builder.append(escape(unescape ? unescape(string.substring(0, start)) : string.substring(0, start)));
				}
			} else if (i > 0) {
				final int last_end = links.get(i - 1).end;
				if (last_end >= 0 && last_end <= start && start <= string_length) {
					builder.append(escape(unescape ? unescape(string.substring(last_end, start)) : string.substring(
							last_end, start)));
				}
			}
			builder.append("<a href=\"" + spec.link + "\">");
			if (start >= 0 && start <= end && end <= string_length) {
				builder.append(spec.display != null ? spec.display : escape(unescape ? unescape(string.substring(start,
						end)) : string.substring(start, end)));
			}
			builder.append("</a>");
			if (i == links.size() - 1 && end >= 0 && end <= string_length) {
				builder.append(escape(unescape ? unescape(string.substring(end, string_length)) : string.substring(end,
						string_length)));
			}
		}
		return builder.toString();
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
