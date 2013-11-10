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

package org.mariotaku.twidere.util.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ContentLengthInputStream extends InputStream {

	private final InputStream stream;
	private final int length;
	private int available;
	private ReadListener readListener;

	public ContentLengthInputStream(final File file) throws FileNotFoundException {
		this(new FileInputStream(file), file.length());
	}

	public ContentLengthInputStream(final InputStream stream, final long length) {
		this.stream = stream;
		this.length = available = (int) length;
	}

	@Override
	public synchronized int available() {
		return available;
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	public int length() {
		return length;
	}

	@Override
	public int read() throws IOException {
		available--;
		if (readListener != null) {
			readListener.onRead(length, available);
		}
		return stream.read();
	}

	public void setReadListener(final ReadListener readListener) {
		this.readListener = readListener;
	}

	public interface ReadListener {
		void onRead(int length, int available);
	}

}
