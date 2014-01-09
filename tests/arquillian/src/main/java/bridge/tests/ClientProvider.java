
package bridge.tests;

import java.net.URI;

import javax.ws.rs.client.Client;

public interface ClientProvider
{

	<T> T createClient( URI target, Class<T> cls );

	default <C extends Client> C onNewClient( C client )
	{
		final ClassLoader cld = Thread.currentThread().getContextClassLoader();

		try {
			client.register( cld.loadClass( "org.glassfish.jersey.jackson.JacksonFeature" ) );
		}
		catch( final ClassNotFoundException e ) {
		}

		return client;
	}
}
