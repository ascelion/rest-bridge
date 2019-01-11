
package ascelion.rest.bridge.client;

import javax.ws.rs.Path;
import javax.ws.rs.client.Client;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
final class JerseyClientInitialisation
{

	@Path( "" )
	interface Init
	{

		@Path( "" )
		String options();
	}

	static void force( Client client )
	{
		try {
			RestClient.invokedMethod( Init.class.getMethod( "options" ) );

			// XXX how else to force Jersey client initialisation & feature processing?
			client.target( "" ).request().options();
		}
		catch( final Exception e ) {
		}
		finally {
			RestClient.invokedMethod( null );
		}
	}
}
