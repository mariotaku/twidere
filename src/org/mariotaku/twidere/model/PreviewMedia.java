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

package org.mariotaku.twidere.model;

public class PreviewMedia {

	private static final PreviewMedia EMPTY_INSTANCE = new PreviewMedia(null, null);

	public final String url, original;

	public PreviewMedia(final String full, final String original) {
		url = full;
		this.original = original;
	}

	@Override
	public String toString() {
		return "PreviewMedia{url=" + url + ", original=" + original + "}";
	}

	public static PreviewMedia getEmpty() {
		return EMPTY_INSTANCE;
	}
}
