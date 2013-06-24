/*
 * 				Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2013 Mariotaku Lee <mariotaku.lee@gmail.com>
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

import java.io.IOException;
import java.io.InputStream;

import twitter4j.TwitterException;
import twitter4j.http.HttpClientWrapper;
import twitter4j.http.HttpResponse;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import com.nostra13.universalimageloader.core.download.ImageDownloader;

public class TwidereImageDownloader implements ImageDownloader {

	private final Context context;
	private final ContentResolver resolver;
	private HttpClientWrapper client;

	public TwidereImageDownloader(final Context context) {
		this.context = context;
		resolver = context.getContentResolver();
		initHttpClient();
	}

	@Override
	public InputStream getStream(final String uri_string, final Object extras) throws IOException {
		final InputStream is;
		if (uri_string == null) return null;
		final Uri uri = Uri.parse(uri_string);
		final String scheme = uri.getScheme();
		try {
			if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme) || ContentResolver.SCHEME_CONTENT.equals(scheme)
					|| ContentResolver.SCHEME_FILE.equals(scheme)) return resolver.openInputStream(uri);
			final HttpResponse resp = Utils.getRedirectedHttpResponse(client, uri_string);
			is = new ContentLengthInputStream(resp.asStream(), (int) resp.getContentLength());
		} catch (final TwitterException e) {
			throw new IOException(e.getMessage());
		}
		return is;
	}

	public void initHttpClient() {
		client = Utils.getImageLoaderHttpClient(context);
	}

}
