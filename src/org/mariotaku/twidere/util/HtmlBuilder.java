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

	public void addLink(String link, int start, int end) {
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
		for (int i = 0; i < links.size(); i++) {
			final LinkSpec spec = links.get(i);
			if (start >= spec.start && start <= spec.end || end >= spec.start && end <= spec.end) {
				if (strict) throw new IllegalArgumentException("link already added in this range!");
				return;
			}
		}
		links.add(new LinkSpec(link, start, end));
	}

	public String build() {
		if (links.size() == 0) return string;
		Collections.sort(links, LinkSpec.COMPARATOR);
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < links.size(); i++) {
			final LinkSpec spec = links.get(i);
			if (i == 0) {
				builder.append(string.substring(0, spec.start));
			}
			if (i > 0) {
				builder.append(string.substring(links.get(i - 1).end, spec.start));
			}
			builder.append("<a href=\"" + spec.link + "\">");
			builder.append(string.substring(spec.start, spec.end));
			builder.append("</a>");
			if (i == links.size() - 1) {
				builder.append(string.substring(spec.end, string.length()));
			}
		}
		return builder.toString();
	}

	private static class LinkSpec {

		private static final Comparator<LinkSpec> COMPARATOR = new Comparator<LinkSpec>() {

			@Override
			public int compare(LinkSpec lhs, LinkSpec rhs) {
				return lhs.start - rhs.start;
			}
		};

		final String link;
		final int start, end;

		LinkSpec(String link, int start, int end) {
			this.link = link;
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return "LinkSpec(" + link + ", " + start + ", " + end + ")";
		}
	}

}