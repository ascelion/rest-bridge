
package bridge.tests.providers;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;

import bridge.tests.TestClientProvider;

import ascelion.rest.bridge.web.RestApplication;

public class JerseyProxyProvider
implements TestClientProvider
{

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		final Client ct = onNewClient( ClientBuilder.newClient() );
		final WebTarget wt = ct.target( target ).path( RestApplication.BASE );

		return WebResourceFactory.newResource( cls, wt );
	}
}
