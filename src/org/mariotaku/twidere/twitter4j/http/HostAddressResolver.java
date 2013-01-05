package org.mariotaku.twidere.twitter4j.http;

import java.io.IOException;

public interface HostAddressResolver {

	public String resolve(String host) throws IOException;

}
