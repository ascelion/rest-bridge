
package ascelion.rest.bridge.tests.providers;

import java.net.URI;

import ascelion.rest.bridge.tests.TestClientProvider;
import ascelion.rest.bridge.tests.api.API;

import static ascelion.rest.bridge.client.RestClient.newRestClient;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;

public class ResteasyBridgeProvider
extends TestClientProvider<ResteasyClientBuilder>
{

	public ResteasyBridgeProvider()
	{
		super( ResteasyClientBuilder::new );
	}

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		return newRestClient( getBuilder().build(), target, API.BASE )
			.getInterface( cls );
	}

	@Override
	public boolean hasClientValidation()
	{
		return true;
	}
}
