package org.mariotaku.twidere.util;

import android.text.TextUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

public class SystemHostsParser {

	private final Map<String, String> mHosts = new HashMap<String, String>();

	public boolean contains(final String host) {
		return mHosts.containsKey(host);
	}

	public String getAddress(final String host) {
		return mHosts.get(host);
	}

	public void init() {
		if (mHosts.isEmpty()) return;
		reload();
	}

	public boolean reload() {
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader("/etc/hosts"));
			mHosts.clear();
			String line = null;
			while ((line = reader.readLine()) != null) {
				final String trimmed = line.trim();
				// Skip if this line is empty or commented out
				if (trimmed.length() == 0 || trimmed.startsWith("#")) {
					continue;
				}
				final String[] segments = trimmed.replaceAll("(\\s|\t)+", " ").split("\\s");
				if (segments.length < 2) {
					continue;
				}
				final String host = segments[1];
				if (!mHosts.containsKey(host)) {
					mHosts.put(host, segments[0]);		
				}
			}
			return true;
		} catch (final IOException e) {
			return false;
		} finally {
			if (reader != null) {
				Utils.closeSilently(reader);
			}
		}
	}
}
