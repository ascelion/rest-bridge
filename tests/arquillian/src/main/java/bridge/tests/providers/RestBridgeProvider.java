
package bridge.tests.providers;

import java.net.URI;

import bridge.tests.TestClientProvider;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.web.RestApplication;

public class RestBridgeProvider
implements TestClientProvider
{

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		return new RestClient( target, RestApplication.class )
			.onNewClient( c -> onNewClient( c ) )
			.getInterface( cls );
	}
}
