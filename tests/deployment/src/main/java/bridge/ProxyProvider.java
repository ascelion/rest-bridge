
package bridge;

import java.net.URI;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;

import ascelion.rest.bridge.web.RestApplication;

public class ProxyProvider
implements ClientProvider
{

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		final WebTarget wt = ClientBuilder.newClient().target( target ).path( RestApplication.BASE );

		return WebResourceFactory.newResource( cls, wt );
	}
}
