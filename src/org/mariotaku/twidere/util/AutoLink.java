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

	private static final Pattern PATTERN_MENTIONS = Pattern.compile("@([A-Za-z0-9_]+)");
	private static final Pattern PATTERN_HASHTAGS = Pattern.compile("#([A-Za-z0-9_]+)");

	private final TextView view;

	private OnLinkClickListener mOnLinkClickListener;

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
		SpannableString string = SpannableString.valueOf(view.getText());

		Pattern pattern = null;
		switch (type) {
			case LINK_TYPE_MENTIONS: {
				pattern = PATTERN_MENTIONS;
				break;
			}
			case LINK_TYPE_HASHTAGS: {
				pattern = PATTERN_HASHTAGS;
				break;
			}
			default: {
				return;
			}

		}

		if (addLinks(string, pattern, type)) {
			view.setText(string);
			addLinkMovementMethod(view);
		}
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

	public interface OnLinkClickListener {
		public void onLinkClick(String link, int type);
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