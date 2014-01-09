
package bridge.tests.providers;

import java.net.URI;

import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import bridge.tests.ClientProvider;

import ascelion.rest.bridge.web.RestApplication;

public class ResteasyProvider
implements ClientProvider
{

	@Override
	public <T> T createClient( URI target, Class<T> cls )
	{
		final ResteasyClient ct = onNewClient( new ResteasyClientBuilder().build() );
		final ResteasyWebTarget wt = ct.target( target ).path( RestApplication.BASE );

		return wt.proxy( cls );
	}
}
