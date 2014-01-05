/*
 * 				Twidere - Twitter client for Android
 * 
 *  Copyright (C) 2012-2014 Mariotaku Lee <mariotaku.lee@gmail.com>
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

package org.mariotaku.twidere.util.net;

import static android.text.TextUtils.isEmpty;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.mariotaku.twidere.Constants;
import org.mariotaku.twidere.util.HostsFileParser;
import org.mariotaku.twidere.util.Utils;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import twitter4j.http.HostAddressResolver;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedHashMap;

public class TwidereHostAddressResolver implements Constants, HostAddressResolver {

	private static final String RESOLVER_LOGTAG = "TwidereHostAddressResolver";

	private static final String DEFAULT_DNS_SERVER_ADDRESS = "8.8.8.8";

	private final SharedPreferences mHostMapping, mPreferences;
	private final HostsFileParser mHosts = new HostsFileParser();
	private final HostCache mHostCache = new HostCache(512);
	private final boolean mLocalMappingOnly;
	private final String mDnsAddress;

	private Resolver mDns;

	public TwidereHostAddressResolver(final Context context) {
		this(context, false);
	}

	public TwidereHostAddressResolver(final Context context, final boolean local_only) {
		mHostMapping = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String address = mPreferences.getString(KEY_DNS_SERVER, DEFAULT_DNS_SERVER_ADDRESS);
		mDnsAddress = isValidIpAddress(address) ? address : DEFAULT_DNS_SERVER_ADDRESS;
		mLocalMappingOnly = local_only;
	}

	@Override
	public String resolve(final String host) throws IOException {
		if (host == null || !mPreferences.getBoolean(KEY_IGNORE_SSL_ERROR, false)) return null;
		// First, I'll try to load address cached.
		if (mHostCache.containsKey(host)) {
			if (Utils.isDebugBuild()) {
				Log.d(RESOLVER_LOGTAG, "Got cached address " + mHostCache.get(host) + " for host " + host);
			}
			return mHostCache.get(host);
		}
		// Then I'll try to load from custom host mapping.
		// Stupid way to find top domain, but really fast.
		if (mHostMapping.contains(host)) {
			final String host_addr = mHostMapping.getString(host, null);
			mHostCache.put(host, host_addr);
			if (Utils.isDebugBuild()) {
				Log.d(RESOLVER_LOGTAG, "Got mapped address " + host_addr + " for host " + host);
			}
			return host_addr;
		}
		mHosts.init();
		if (mHosts.contains(host)) {
			final String host_addr = mHosts.getAddress(host);
			mHostCache.put(host, host_addr);
			if (Utils.isDebugBuild()) {
				Log.d(RESOLVER_LOGTAG, "Got mapped address " + host_addr + " for host " + host);
			}
			return host_addr;
		}
		final String[] host_segments = host.split("\\.");
		final int host_segments_length = host_segments.length;
		if (host_segments_length > 2) {
			final String top_domain = host_segments[host_segments_length - 2] + "."
					+ host_segments[host_segments_length - 1];
			if (mHostMapping.contains(top_domain)) {
				final String hostAddr = mHostMapping.getString(top_domain, null);
				mHostCache.put(host, hostAddr);
				if (Utils.isDebugBuild()) {
					Log.d(RESOLVER_LOGTAG, "Got mapped address (top domain) " + hostAddr + " for host " + host);
				}
				return hostAddr;
			}
		}
		initDns();
		// Use TCP DNS Query if enabled.
		if (mDns != null && mPreferences.getBoolean(KEY_TCP_DNS_QUERY, false)) {
			final Name name = new Name(host);
			final Record query = Record.newRecord(name, Type.A, DClass.IN);
			if (query == null) return host;
			final Message response;
			try {
				response = mDns.send(Message.newQuery(query));
			} catch (final IOException e) {
				return host;
			}
			if (response == null) return host;
			final Record[] records = response.getSectionArray(Section.ANSWER);
			if (records == null || records.length < 1) throw new IOException("Could not find " + host);
			String host_addr = null;
			// Test each IP address resolved.
			for (final Record record : records) {
				if (record instanceof ARecord) {
					final InetAddress ipv4_addr = ((ARecord) record).getAddress();
					if (ipv4_addr.isReachable(300)) {
						host_addr = ipv4_addr.getHostAddress();
					}
				} else if (record instanceof AAAARecord) {
					final InetAddress ipv6_addr = ((AAAARecord) record).getAddress();
					if (ipv6_addr.isReachable(300)) {
						host_addr = ipv6_addr.getHostAddress();
					}
				}
				if (mHostCache.put(host, host_addr) != null) {
					if (Utils.isDebugBuild()) {
						Log.d(RESOLVER_LOGTAG, "Resolved address " + host_addr + " for host " + host);
					}
					return host_addr;
				}
			}
			// No address is reachable, but I believe the IP is correct.
			final Record record = records[0];
			if (record instanceof ARecord) {
				final InetAddress ipv4_addr = ((ARecord) record).getAddress();
				host_addr = ipv4_addr.getHostAddress();
			} else if (record instanceof AAAARecord) {
				final InetAddress ipv6_addr = ((AAAARecord) record).getAddress();
				host_addr = ipv6_addr.getHostAddress();
			} else if (record instanceof CNAMERecord) return resolve(((CNAMERecord) record).getTarget().toString());
			mHostCache.put(host, host_addr);
			if (Utils.isDebugBuild()) {
				Log.d(RESOLVER_LOGTAG, "Resolved address " + host_addr + " for host " + host);
			}
			return host_addr;
		}
		if (Utils.isDebugBuild()) {
			Log.w(RESOLVER_LOGTAG, "Resolve address " + host + " failed, using original host");
		}
		return host;
	}

	void initDns() throws IOException {
		if (mDns != null) return;
		mDns = mLocalMappingOnly ? null : new SimpleResolver(mDnsAddress);
		if (mDns != null) {
			mDns.setTCP(true);
		}
	}

	static boolean isValidIpAddress(final String address) {
		return !isEmpty(address);
	}

	private static class HostCache extends LinkedHashMap<String, String> {

		private static final long serialVersionUID = -9216545511009449147L;

		HostCache(final int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		public String put(final String key, final String value) {
			if (value == null) return value;
			return super.put(key, value);
		}
	}
}
