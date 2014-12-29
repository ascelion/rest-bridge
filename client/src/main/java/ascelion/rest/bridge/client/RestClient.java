
package ascelion.rest.bridge.client;

import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.function.UnaryOperator;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

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

	private final URI target;

	private final String base;

	private UnaryOperator<ClientBuilder> onNewBuilder = b -> b;

	private UnaryOperator<Client> onNewClient = c -> c;

	private UnaryOperator<WebTarget> onNewTarget = t -> t;

	private UnaryOperator<Builder> onBuildRequest = b -> b;

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
		final ClientBuilder cb = this.onNewBuilder.apply( ClientBuilder.newBuilder() );
		final Client ct = this.onNewClient.apply( cb.build() );
		WebTarget wt = this.onNewTarget.apply( ct.target( this.target ) );

		if( this.base != null ) {
			wt = wt.path( this.base );
		}

		final RestClientIH ih = new RestClientIH( cls, wt, this.onBuildRequest, ct );

		return RestClientIH.newProxy( cls, ih );
	}

	public RestClient onBuildRequest( UnaryOperator<Invocation.Builder> onBuildRequest )
	{
		this.onBuildRequest = onBuildRequest;

		return this;
	}

	public RestClient onNewBuilder( UnaryOperator<ClientBuilder> onNewBuilder )
	{
		this.onNewBuilder = onNewBuilder;

		return this;
	}

	public RestClient onNewClient( UnaryOperator<Client> onNewClient )
	{
		this.onNewClient = onNewClient;

		return this;
	}

	public RestClient onNewTarget( UnaryOperator<WebTarget> onNewTarget )
	{
		this.onNewTarget = onNewTarget;

		return this;
	}

}
