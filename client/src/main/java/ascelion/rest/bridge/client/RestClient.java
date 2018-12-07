
package ascelion.rest.bridge.client;

import java.lang.reflect.Proxy;
import java.net.URI;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
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

		for( Class<?> c = cls; path == null && c != Application.class; c = cls.getSuperclass() ) {
			final ApplicationPath a = c.getAnnotation( ApplicationPath.class );

			if( a != null ) {
				path = a.value();
			}
		}

		return path;
	}

	final URI target;

	private RestCallback<ClientBuilder> onNewBuilder = new RestCallbackWrapper<>( null );
	private RestCallback<Client> onNewClient = new RestCallbackWrapper<>( null );
	RestCallback<Invocation.Builder> onNewRequest = new RestCallbackWrapper<>( null );

	private Client client;

	public RestClient( URI target )
	{
		this( null, target, (String) null );
	}

	public RestClient( URI target, Class<? extends Application> cls )
	{
		this( null, target, getBase( cls ) );
	}

	public RestClient( Client client, URI target )
	{
		this( client, target, (String) null );
	}

	public RestClient( Client client, URI target, Class<? extends Application> cls )
	{
		this( client, target, getBase( cls ) );
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
		if( this.client == null ) {
			this.client = createClient();
		}

		this.client.target( this.target );

		final RestClientIH ih = new RestClientIH( this, this.client, cls );

		return RestClientIH.newProxy( cls, ih );
	}

	public RestClient onNewBuilder( RestCallback<ClientBuilder> onNewBuilder )
	{
		this.onNewBuilder = new RestCallbackWrapper<>( onNewBuilder );

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

	private Client createClient()
	{
		ClientBuilder newBuilder = null;
		RuntimeException exception = null;

		try {
			newBuilder = this.onNewBuilder.apply( ClientBuilder.newBuilder() );
		}
		catch( final RuntimeException t ) {
			exception = t;
			newBuilder = this.onNewBuilder.apply( null );
		}

		if( newBuilder == null ) {
			if( exception != null ) {
				throw exception;
			}
			else {
				throw new RuntimeException( "Cannot create ClientBuilder" );
			}
		}

		final ClientBuilder cb = this.onNewBuilder.apply( newBuilder );
		final Client ct = this.onNewClient.apply( cb.build() );

		return ct;
	}
}
