
package ascelion.rest.bridge.tests.providers;

import java.net.URI;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.tests.TestClientProvider;
import ascelion.rest.bridge.tests.api.API;

import org.glassfish.jersey.client.JerseyClientBuilder;

public class JerseyBridgeProvider
extends TestClientProvider<JerseyClientBuilder>
{

	public JerseyBridgeProvider()
	{
		super( JerseyClientBuilder::new );
	}

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		return new RestClient( getBuilder().build(), target, API.BASE )
			.getInterface( cls );
	}

	@Override
	public boolean hasClientValidation()
	{
		return true;
	}
}
