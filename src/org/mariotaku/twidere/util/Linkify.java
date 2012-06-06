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

import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  Linkify take a piece of text and a regular expression and turns all of the
 *  regex matches in the text into clickable links.  This is particularly
 *  useful for matching things like email addresses, web urls, etc. and making
 *  them actionable.
 *
 *  Alone with the pattern that is to be matched, a url scheme prefix is also
 *  required.  Any pattern match that does not begin with the supplied scheme
 *  will have the scheme prepended to the matched text when the clickable url
 *  is created.  For instance, if you are matching web urls you would supply
 *  the scheme <code>http://</code>.  If the pattern matches example.com, which
 *  does not have a url scheme prefix, the supplied scheme will be prepended to
 *  create <code>http://example.com</code> when the clickable url link is
 *  created.
 */

public class Linkify {

    private static final void addLinkMovementMethod(TextView t) {
        MovementMethod m = t.getMovementMethod();

        if ((m == null) || !(m instanceof LinkMovementMethod)) {
            if (t.getLinksClickable()) {
                t.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }


    /**
     *  Applies a regex to the text of a TextView turning the matches into
     *  links.  If links are found then UrlSpans are applied to the link
     *  text match areas, and the movement method for the text is changed
     *  to LinkMovementMethod.
     *
     *  @param text         TextView whose text is to be marked-up with links
     *  @param pattern      Regex pattern to be used for finding links
     *  @param scheme       Url scheme string (eg <code>http://</code> to be
     *                      prepended to the url of links that do not have
     *                      a scheme specified in the link text
     */
    public static final void addLinks(TextView text, Pattern p, String scheme) {
        SpannableString s = SpannableString.valueOf(text.getText());

        if (addLinks(s, p, scheme)) {
            text.setText(s);
            addLinkMovementMethod(text);
        }
    }

    /**
     *  Applies a regex to a Spannable turning the matches into
     *  links.
     *
     *  @param text         Spannable whose text is to be marked-up with
     *                      links
     *  @param pattern      Regex pattern to be used for finding links
     *  @param scheme       Url scheme string (eg <code>http://</code> to be
     *                      prepended to the url of links that do not have
     *                      a scheme specified in the link text
     */
    private static final boolean addLinks(Spannable s, Pattern p,
            String scheme) {
        boolean hasMatches = false;
        String prefix = (scheme == null) ? "" : scheme.toLowerCase();
        Matcher m = p.matcher(s);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            boolean allowed = true;

            if (allowed) {
                String url = makeUrl(m.group(0), new String[] { prefix });

                applyLink(url, start, end, s);
                hasMatches = true;
            }
        }

        return hasMatches;
    }

    private static final void applyLink(String url, int start, int end, Spannable text) {
        LinkSpan span = new LinkSpan(url);

        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private static final String makeUrl(String url, String[] prefixes) {

        boolean hasPrefix = false;
        
        for (int i = 0; i < prefixes.length; i++) {
            if (url.regionMatches(true, 0, prefixes[i], 0,
                                  prefixes[i].length())) {
                hasPrefix = true;

                // Fix capitalization if necessary
                if (!url.regionMatches(false, 0, prefixes[i], 0,
                                       prefixes[i].length())) {
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

    private static class LinkSpan extends URLSpan {

		public LinkSpan(String url) {
			super(url);
		}

		@Override
		public void onClick(View widget) {
			Log.d("Linkify", "view " + widget + " clicked, url = " + getURL());
		}
    	
    }
}