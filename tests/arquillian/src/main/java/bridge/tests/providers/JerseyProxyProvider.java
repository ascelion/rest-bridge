
package bridge.tests.providers;

import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.filter.LoggingFilter;

import bridge.tests.TestClientProvider;

import ascelion.rest.bridge.web.RestApplication;

public class JerseyProxyProvider
implements TestClientProvider
{

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		final Client ct = onNewClient( ClientBuilder.newClient() );

		ct.register( new LoggingFilter( Logger.getLogger( "ascelion.REST" ), true ) );

		final WebTarget wt = ct.target( target ).path( RestApplication.BASE );

		return WebResourceFactory.newResource( cls, wt );
	}
}
