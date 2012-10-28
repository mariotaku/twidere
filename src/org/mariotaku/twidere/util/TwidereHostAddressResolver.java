package org.mariotaku.twidere.util;

import static android.text.TextUtils.isEmpty;

import java.io.IOException;
import java.net.InetAddress;
import java.util.LinkedHashMap;

import org.mariotaku.twidere.Constants;
import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.Section;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

import twitter4j.http.HostAddressResolver;
import android.content.Context;
import android.content.SharedPreferences;

public class TwidereHostAddressResolver implements Constants, HostAddressResolver {

	private final Resolver mResolver;
	private final SharedPreferences mHostMapping, mPreferences;
	private final LinkedHashMap<String, String> mHostCache = new LinkedHashMap<String, String>(512, 0.75f, false);
	private static final String DEFAULT_DNS_SERVER = "8.8.8.8";

	public TwidereHostAddressResolver(final Context context) throws IOException {
		this(context, false);
	}

	public TwidereHostAddressResolver(final Context context, final boolean local_only) throws IOException {
		mHostMapping = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		final String dns_address = mPreferences.getString(PREFERENCE_KEY_DNS_SERVER, DEFAULT_DNS_SERVER);
		mResolver = !local_only ? new SimpleResolver(!isEmpty(dns_address) ? dns_address : DEFAULT_DNS_SERVER) : null;
		if (mResolver != null) {
			mResolver.setTCP(true);
		}
	}

	@Override
	public String resolve(final String host) throws IOException {
		if (host == null) return null;
		// First, I'll try to load address cached.
		if (mHostCache.containsKey(host)) return mHostCache.get(host);
		// Then I'll try to load from custom host mapping.
		// Stupid way to find top domain, but really fast.
		final String[] host_segments = host.split("\\.");
		final int host_segments_length = host_segments.length;
		if (host_segments_length > 2) {
			final String top_domain = host_segments[host_segments_length - 2] + "."
					+ host_segments[host_segments_length - 1];
			if (mHostMapping.contains(top_domain)) {
				final String host_addr = mHostMapping.getString(top_domain, null);
				mHostCache.put(top_domain, host_addr);
				return host_addr;
			}
		} else {
			if (mHostMapping.contains(host)) {
				final String host_addr = mHostMapping.getString(host, null);
				mHostCache.put(host, host_addr);
				return host_addr;
			}
		}
		// Use TCP DNS Query if enabled.
		if (mResolver != null && mPreferences.getBoolean(PREFERENCE_KEY_TCP_DNS_QUERY, false)) {
			final Name name = new Name(host);
			final Record query = Record.newRecord(name, Type.A, DClass.IN);
			final Message response = mResolver.send(Message.newQuery(query));
			final Record[] records = response.getSectionArray(Section.ANSWER);
			if (records == null || records.length < 1) throw new IOException("Could not find " + host);
			String host_addr = null;
			// Test each IP address resolved.
			for (final Record record : records) {
				if (record instanceof ARecord) {
					final InetAddress ipv4_addr = ((ARecord) record).getAddress();
					if (ipv4_addr.isReachable(300)) {
						host_addr = ipv4_addr.getHostAddress();
						mHostCache.put(host, host_addr);
						break;
					}
				} else if (record instanceof AAAARecord) {
					final InetAddress ipv6_addr = ((AAAARecord) record).getAddress();
					if (ipv6_addr.isReachable(300)) {
						host_addr = ipv6_addr.getHostAddress();
						mHostCache.put(host, host_addr);
						break;
					}
				}
			}
			// No address is reachable, but I believe the IP is correct.
			if (host_addr == null) {
				final Record record = records[0];
				if (record instanceof ARecord) {
					final InetAddress ipv4_addr = ((ARecord) record).getAddress();
					host_addr = ipv4_addr.getHostAddress();
					mHostCache.put(host, host_addr);
				} else if (record instanceof AAAARecord) {
					final InetAddress ipv6_addr = ((AAAARecord) record).getAddress();
					host_addr = ipv6_addr.getHostAddress();
					mHostCache.put(host, host_addr);
				}
			}
			return host_addr;
		}
		return host;
	}

}
