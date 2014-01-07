
package bridge;

import java.net.URI;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.web.RestApplication;

public class BridgeProvider
implements ClientProvider
{

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		return new RestClient( target, RestApplication.class ).getInterface( cls );
	}
}
