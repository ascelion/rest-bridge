
package ascelion.rest.bridge.client;

import java.net.URI;
import java.util.function.UnaryOperator;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

public final class RestClient
{

	static private String getBase( Class<? extends Application> cls )
	{
		String path = null;

		for( Class c = cls; path == null && c != Application.class; c = cls.getSuperclass() ) {
			final ApplicationPath a = (ApplicationPath) c.getAnnotation( ApplicationPath.class );

			if( a != null ) {
				path = a.value();
			}
		}

		return path;
	}

	private final URI target;

	private final String base;

	private UnaryOperator<Client> onNewClient = t -> t;

	public RestClient( URI target )
	{
		this( target, (String) null );
	}

	public RestClient( URI target, Class<? extends Application> cls )
	{
		this( target, getBase( cls ) );
	}

	public RestClient( URI target, String base )
	{
		this.target = target;
		this.base = base;
	}

	public <X> X getInterface( Class<X> cls )
	{
		final Client ct = this.onNewClient.apply( ClientBuilder.newClient() );

		WebTarget wt = ct.target( this.target );

		if( this.base != null ) {
			wt = wt.path( this.base );
		}

		final RestClientIH ih = new RestClientIH( cls, wt );

		return RestClientIH.newProxy( cls, ih );
	}

	public RestClient onNewClient( UnaryOperator<Client> onNewClient )
	{
		this.onNewClient = onNewClient;

		return this;
	}
}
