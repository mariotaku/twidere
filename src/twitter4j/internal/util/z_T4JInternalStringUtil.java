/*
 * Copyright 2007 Yusuke Yamamoto
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

package twitter4j.internal.util;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yusuke Yamamoto - yusuke at mac.com
 * @since Twitter4J 2.1.4
 */
public class z_T4JInternalStringUtil {
	private z_T4JInternalStringUtil() {
		throw new AssertionError();
	}

	public static String join(final int[] follows) {
		final StringBuffer buf = new StringBuffer(11 * follows.length);
		for (final int follow : follows) {
			if (0 != buf.length()) {
				buf.append(",");
			}
			buf.append(follow);
		}
		return buf.toString();
	}

	// for JDK1.4 compatibility

	public static String join(final long[] follows) {
		final StringBuffer buf = new StringBuffer(11 * follows.length);
		for (final long follow : follows) {
			if (0 != buf.length()) {
				buf.append(",");
			}
			buf.append(follow);
		}
		return buf.toString();
	}

	public static String join(final String[] track) {
		final StringBuffer buf = new StringBuffer(11 * track.length);
		for (final String str : track) {
			if (0 != buf.length()) {
				buf.append(",");
			}
			buf.append(str);
		}
		return buf.toString();
	}

	public static String maskString(final String str) {
		final StringBuffer buf = new StringBuffer(str.length());
		for (int i = 0; i < str.length(); i++) {
			buf.append("*");
		}
		return buf.toString();
	}

	public static String replaceLast(final String text, final String regex, final String replacement) {
		if (text == null || regex == null || replacement == null) return text;
		return text.replaceFirst("(?s)" + regex + "(?!.*?" + regex + ")", replacement);
	}

	public static String[] split(final String str, final String separator) {
		String[] returnValue;
		int index = str.indexOf(separator);
		if (index == -1) {
			returnValue = new String[] { str };
		} else {
			final List<String> strList = new ArrayList<String>();
			int oldIndex = 0;
			while (index != -1) {
				final String subStr = str.substring(oldIndex, index);
				strList.add(subStr);
				oldIndex = index + separator.length();
				index = str.indexOf(separator, oldIndex);
			}
			if (oldIndex != str.length()) {
				strList.add(str.substring(oldIndex));
			}
			returnValue = strList.toArray(new String[strList.size()]);
		}

		return returnValue;
	}
}
