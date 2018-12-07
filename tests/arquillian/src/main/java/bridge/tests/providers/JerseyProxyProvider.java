
package bridge.tests.providers;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

import ascelion.rest.bridge.web.API;

import bridge.tests.TestClientProvider;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.glassfish.jersey.client.proxy.WebResourceFactory;

public class JerseyProxyProvider
extends TestClientProvider<JerseyClientBuilder>
{

	public JerseyProxyProvider()
	{
		super( new JerseyClientBuilder() );
	}

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		final Client ct = getBuilder().build();
		final WebTarget wt = ct.target( target ).path( API.BASE );

		return WebResourceFactory.newResource( cls, wt );
	}
}
