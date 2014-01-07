
package ascelion.rest.bridge.client;

import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

public class RestClient
{

	private final String base;

	public RestClient( Class<? extends Application> cls )
	{
		String path = null;

		for( Class c = cls; path == null && c != Application.class; c = cls.getSuperclass() ) {
			final ApplicationPath a = (ApplicationPath) c.getAnnotation( ApplicationPath.class );

			if( a != null ) {
				path = a.value();
			}
		}

		if( path == null ) {
			throw new IllegalArgumentException();
		}

		this.base = path;
	}

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
