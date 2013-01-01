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
import static org.mariotaku.twidere.util.HtmlEscapeHelper.toHtml;

import java.util.ArrayList;
import java.util.Collections;

import android.util.Log;

public class HtmlBuilder {

	private static final String LOGTAG = "HtmlBuilder";

	private final String string;
	private final int string_length;
	private final boolean strict, orig_need_escape, display_need_escape;

	private final ArrayList<LinkSpec> links = new ArrayList<LinkSpec>();

	@Deprecated
	public HtmlBuilder(final String string) {
		this(string, true);
	}

	@Deprecated
	public HtmlBuilder(final String string, final boolean strict) {
		this(string, false, true, true);
	}

	public HtmlBuilder(final String string, final boolean strict, final boolean need_escape) {
		this(string, true, need_escape, need_escape);
	}

	public HtmlBuilder(final String string, final boolean strict, final boolean orig_need_escape,
			final boolean display_need_escape) {
		if (string == null) throw new NullPointerException();
		this.string = string;
		this.strict = strict;
		this.orig_need_escape = orig_need_escape;
		this.display_need_escape = display_need_escape;
		string_length = string.length();
	}

	public boolean addLink(final String link, final String display, final int start, final int end) {
		if (start < 0 || end < 0 || start > end || end > string_length) {
			final String message = "String length = " + string_length + ", start = " + start + ", end = " + end;
			if (strict) throw new StringIndexOutOfBoundsException(message);
			Log.e(LOGTAG, message);
			return false;
		}
		for (final LinkSpec spec : links) {
			if (start >= spec.start && start <= spec.end || end >= spec.start && end <= spec.end) {
				final String message = "link already added in this range!";
				if (strict) throw new IllegalArgumentException(message);
				Log.e(LOGTAG, message);
				return false;
			}
		}
		return links.add(new LinkSpec(link, display, start, end));
	}

	public String build() {
		if (links.size() == 0) return origToHtmlIfNeeded(string);
		Collections.sort(links);
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
					builder.append(origToHtmlIfNeeded(string.substring(0, start)));
				}
			} else if (i > 0) {
				final int last_end = links.get(i - 1).end;
				if (last_end >= 0 && last_end <= start && start <= string_length) {
					builder.append(origToHtmlIfNeeded(string.substring(last_end, start)));
				}
			}
			builder.append("<a href=\"" + spec.link + "\">");
			if (start >= 0 && start <= end && end <= string_length) {
				builder.append(!isEmpty(spec.display) ? displayToHtmlIfNeeded(spec.display) : spec.link);
			}
			builder.append("</a>");
			if (i == links.size() - 1 && end >= 0 && end <= string_length) {
				builder.append(origToHtmlIfNeeded(string.substring(end, string_length)));
			}
		}
		return builder.toString();
	}

	private String displayToHtmlIfNeeded(final String string) {
		return display_need_escape ? toHtml(string) : string;
	}

	private String origToHtmlIfNeeded(final String string) {
		return orig_need_escape ? toHtml(string) : string;
	}

	static final class LinkSpec implements Comparable<LinkSpec> {

		final String link, display;
		final int start, end;

		LinkSpec(final String link, final String display, final int start, final int end) {
			this.link = link;
			this.display = display;
			this.start = start;
			this.end = end;
		}

		@Override
		public int compareTo(final LinkSpec that) {
			return start - that.start;
		}

		@Override
		public String toString() {
			return "LinkSpec(" + link + ", " + start + ", " + end + ")";
		}
	}

}
