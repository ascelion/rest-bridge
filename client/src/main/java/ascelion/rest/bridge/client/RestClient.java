
package ascelion.rest.bridge.client;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.UriBuilder;

public final class RestClient
{

	final Client client;
	final URI target;

	public RestClient( Client client, URI target )
	{
		this( client, target, (String) null );
	}

	public RestClient( Client client, URI target, String base )
	{
		this.client = client;

		if( base == null ) {
			this.target = target;
		}
		else {
			this.target = UriBuilder.fromUri( target ).path( base ).build();
		}
	}

	public <X> X getInterface( Class<X> cls )
	{
		final WebTarget newTarget = Util.addPathFromAnnotation( cls, this.client.target( this.target ) );
		final RestClientIH ih = new RestClientIH( this.client, newTarget, cls );

		return ih.newProxy();
	}
}
