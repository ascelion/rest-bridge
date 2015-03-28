
package ascelion.rest.bridge.client;

import java.lang.reflect.Proxy;
import java.net.URI;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;

public final class RestClient
{

	static public void release( Object itf )
	{
		if( itf != null ) {
			( (RestClientIH) Proxy.getInvocationHandler( itf ) ).close();
		}
	}

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

	final URI target;

	private RestCallback<ClientBuilder> onNewBuilder = b -> b;

	private RestCallback<Client> onNewClient = c -> c;

	RestCallback<Builder> onNewRequest = b -> b;

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
		if( base == null ) {
			this.target = target;
		}
		else {
			this.target = UriBuilder.fromUri( target ).path( base ).build();
		}
	}

	public <X> X getInterface( Class<X> cls )
	{
		final Client ct = createClient();
		ct.target( this.target );

		final RestClientIH ih = new RestClientIH( this, cls );

		return RestClientIH.newProxy( cls, ih );
	}

	public RestClient onNewBuilder( RestCallback<ClientBuilder> onNewBuilder )
	{
		this.onNewBuilder = onNewBuilder;

		return this;
	}

	public RestClient onNewClient( RestCallback<Client> onNewClient )
	{
		this.onNewClient = onNewClient;

		return this;
	}

	public RestClient onNewRequest( RestCallback<Invocation.Builder> onNewRequest )
	{
		this.onNewRequest = onNewRequest;

		return this;
	}

	Client createClient()
	{
		ClientBuilder newBuilder;

		try {
			newBuilder = this.onNewBuilder.apply( ClientBuilder.newBuilder() );
		}
		catch( final Throwable t ) {
			newBuilder = this.onNewBuilder.apply( null );
		}

		final ClientBuilder cb = this.onNewBuilder.apply( newBuilder );
		final Client ct = this.onNewClient.apply( cb.build() );

		return ct;
	}
}
