
package bridge.tests.providers;

import java.net.URI;

import ascelion.rest.bridge.web.API;

import bridge.tests.TestClientProvider;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

public class ResteasyProxyProvider
extends TestClientProvider<ResteasyClientBuilder>
{

	public ResteasyProxyProvider()
	{
		super( new ResteasyClientBuilder() );
	}

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		final ResteasyClient ct = getBuilder().build();
		final ResteasyWebTarget wt = ct.target( target ).path( API.BASE );

		return wt.proxy( cls );
	}
}
