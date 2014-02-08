
package bridge.tests;

import java.net.URI;
import java.util.logging.Logger;

import javax.ws.rs.client.Client;

public interface TestClientProvider
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

		try {
			client.register(
				cld.loadClass( "org.glassfish.jersey.filter.LoggingFilter" )
					.getConstructor( Logger.class, int.class )
					.newInstance( Logger.getLogger( "ascelion.bridge.REST" ), 32768 )
				);
		}
		catch( final Exception e ) {
		}

		return client;
	}
}
