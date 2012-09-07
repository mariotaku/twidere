package org.mariotaku.twidere.util;

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

import twitter4j.HostAddressResolver;
import android.content.Context;
import android.content.SharedPreferences;

public class TwidereHostAddressResolver implements Constants, HostAddressResolver {

	private final Resolver mResolver;
	private final SharedPreferences mPreferences;
	private final LinkedHashMap<String, String> mHostCache = new LinkedHashMap<String, String>(512, 0.75f, false);

	public TwidereHostAddressResolver(Context context) throws IOException {
		mPreferences = context.getSharedPreferences(HOST_MAPPING_PREFERENCES_NAME, Context.MODE_PRIVATE);
		mResolver = new SimpleResolver("8.8.8.8");
		mResolver.setTCP(true);
	}

	@Override
	public String resolve(String host) throws IOException {
		// First, I'll try to load address cached.
		if (mHostCache.containsKey(host)) return mHostCache.get(host);
		// Then I'll try to load from custom host mapping.
		if (mPreferences.contains(host)) {
			final String host_addr = mPreferences.getString(host, null);
			mHostCache.put(host, host_addr);
			return host_addr;
		}
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
			}
		}
		return host_addr;
	}

}
