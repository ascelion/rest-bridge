
package bridge.tests.providers;

import java.net.URI;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.web.API;

import bridge.tests.TestClientProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;

public class JerseyBridgeProvider
extends TestClientProvider<JerseyClientBuilder>
{

	public JerseyBridgeProvider()
	{
		super( new JerseyClientBuilder() );
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

	@Override
	protected void release( Object client )
	{
		RestClient.release( client );
	}
}
