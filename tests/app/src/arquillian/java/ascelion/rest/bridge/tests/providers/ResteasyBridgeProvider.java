
package ascelion.rest.bridge.tests.providers;

import java.net.URI;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.tests.TestClientProvider;
import ascelion.rest.bridge.tests.api.API;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class ResteasyBridgeProvider
extends TestClientProvider<ResteasyClientBuilder>
{

	public ResteasyBridgeProvider()
	{
		super( new ResteasyClientBuilder() );
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
