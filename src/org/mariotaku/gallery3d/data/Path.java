/*
 * Copyright (C) 2010 The Android Open Source Project
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

package org.mariotaku.gallery3d.data;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.mariotaku.gallery3d.common.Utils;
import org.mariotaku.gallery3d.util.IdentityCache;

import android.util.Log;

public class Path {
	private static final String TAG = "Path";
	private static Path sRoot = new Path(null, "ROOT");

	private final Path mParent;
	private final String mSegment;
	private WeakReference<MediaObject> mObject;
	private IdentityCache<String, Path> mChildren;

	private Path(final Path parent, final String segment) {
		mParent = parent;
		mSegment = segment;
	}

	public boolean equalsIgnoreCase(final String p) {
		final String path = toString();
		return path.equalsIgnoreCase(p);
	}

	public Path getChild(final int segment) {
		return getChild(String.valueOf(segment));
	}

	public Path getChild(final long segment) {
		return getChild(String.valueOf(segment));
	}

	public Path getChild(final String segment) {
		synchronized (Path.class) {
			if (mChildren == null) {
				mChildren = new IdentityCache<String, Path>();
			} else {
				final Path p = mChildren.get(segment);
				if (p != null) return p;
			}

			final Path p = new Path(this, segment);
			mChildren.put(segment, p);
			return p;
		}
	}

	public Path getParent() {
		synchronized (Path.class) {
			return mParent;
		}
	}

	public String getPrefix() {
		if (this == sRoot) return "";
		return getPrefixPath().mSegment;
	}

	public Path getPrefixPath() {
		synchronized (Path.class) {
			Path current = this;
			if (current == sRoot) throw new IllegalStateException();
			while (current.mParent != sRoot) {
				current = current.mParent;
			}
			return current;
		}
	}

	public String getSuffix() {
		// We don't need lock because mSegment is final.
		return mSegment;
	}

	public void setObject(final MediaObject object) {
		synchronized (Path.class) {
			Utils.assertTrue(mObject == null || mObject.get() == null);
			mObject = new WeakReference<MediaObject>(object);
		}
	}

	public String[] split() {
		synchronized (Path.class) {
			int n = 0;
			for (Path p = this; p != sRoot; p = p.mParent) {
				n++;
			}
			final String[] segments = new String[n];
			int i = n - 1;
			for (Path p = this; p != sRoot; p = p.mParent) {
				segments[i--] = p.mSegment;
			}
			return segments;
		}
	}

	@Override
	// TODO: toString() should be more efficient, will fix it later
	public String toString() {
		synchronized (Path.class) {
			final StringBuilder sb = new StringBuilder();
			final String[] segments = split();
			for (final String segment : segments) {
				sb.append("/");
				sb.append(segment);
			}
			return sb.toString();
		}
	}

	MediaObject getObject() {
		synchronized (Path.class) {
			return mObject == null ? null : mObject.get();
		}
	}

	public static Path fromString(final String s) {
		synchronized (Path.class) {
			final String[] segments = split(s);
			Path current = sRoot;
			for (final String segment : segments) {
				current = current.getChild(segment);
			}
			return current;
		}
	}

	public static String[] split(final String s) {
		final int n = s.length();
		if (n == 0) return new String[0];
		if (s.charAt(0) != '/') throw new RuntimeException("malformed path:" + s);
		final ArrayList<String> segments = new ArrayList<String>();
		int i = 1;
		while (i < n) {
			int brace = 0;
			int j;
			for (j = i; j < n; j++) {
				final char c = s.charAt(j);
				if (c == '{') {
					++brace;
				} else if (c == '}') {
					--brace;
				} else if (brace == 0 && c == '/') {
					break;
				}
			}
			if (brace != 0) throw new RuntimeException("unbalanced brace in path:" + s);
			segments.add(s.substring(i, j));
			i = j + 1;
		}
		final String[] result = new String[segments.size()];
		segments.toArray(result);
		return result;
	}

	// Splits a string to an array of strings.
	// For example, "{foo,bar,baz}" -> {"foo","bar","baz"}.
	public static String[] splitSequence(final String s) {
		final int n = s.length();
		if (s.charAt(0) != '{' || s.charAt(n - 1) != '}') throw new RuntimeException("bad sequence: " + s);
		final ArrayList<String> segments = new ArrayList<String>();
		int i = 1;
		while (i < n - 1) {
			int brace = 0;
			int j;
			for (j = i; j < n - 1; j++) {
				final char c = s.charAt(j);
				if (c == '{') {
					++brace;
				} else if (c == '}') {
					--brace;
				} else if (brace == 0 && c == ',') {
					break;
				}
			}
			if (brace != 0) throw new RuntimeException("unbalanced brace in path:" + s);
			segments.add(s.substring(i, j));
			i = j + 1;
		}
		final String[] result = new String[segments.size()];
		segments.toArray(result);
		return result;
	}

	// Below are for testing/debugging only
	static void clearAll() {
		synchronized (Path.class) {
			sRoot = new Path(null, "");
		}
	}

	static void dumpAll() {
		dumpAll(sRoot, "", "");
	}

	static void dumpAll(final Path p, final String prefix1, final String prefix2) {
		synchronized (Path.class) {
			final MediaObject obj = p.getObject();
			Log.d(TAG, prefix1 + p.mSegment + ":" + (obj == null ? "null" : obj.getClass().getSimpleName()));
			if (p.mChildren != null) {
				final ArrayList<String> childrenKeys = p.mChildren.keys();
				int i = 0;
				final int n = childrenKeys.size();
				for (final String key : childrenKeys) {
					final Path child = p.mChildren.get(key);
					if (child == null) {
						++i;
						continue;
					}
					Log.d(TAG, prefix2 + "|");
					if (++i < n) {
						dumpAll(child, prefix2 + "+-- ", prefix2 + "|   ");
					} else {
						dumpAll(child, prefix2 + "+-- ", prefix2 + "    ");
					}
				}
			}
		}
	}
}
