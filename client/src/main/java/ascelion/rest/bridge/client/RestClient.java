
package ascelion.rest.bridge.client;

import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

public class RestClient
{

	private final String base;

	public RestClient( String base )
	{
		this.base = base;
	}

	public <X> X getInterface( Class<X> cls, URL target )
	{
		try {
			final WebTarget wt = ClientBuilder.newClient().target( target.toURI() ).path( this.base );
			final RestClientIH ih = new RestClientIH( cls, wt );

			return RestClientIH.newProxy( cls, ih );
		}
		catch( final URISyntaxException e ) {
			throw new WebApplicationException( e );
		}
	}
}
