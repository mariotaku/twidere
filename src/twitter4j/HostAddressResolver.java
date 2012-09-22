package twitter4j;

import java.io.IOException;

public interface HostAddressResolver {

	public String resolve(String host) throws IOException;
}
