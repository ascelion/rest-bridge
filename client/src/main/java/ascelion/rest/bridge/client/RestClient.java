
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import ascelion.rest.bridge.client.RestRequestInterceptor.Factory;

import static ascelion.rest.bridge.client.RBUtils.cleanRequestURI;
import static ascelion.rest.bridge.client.RestClientProperties.ASYNC_INTERCEPTOR;
import static ascelion.rest.bridge.client.RestClientProperties.NO_ASYNC_INTERCEPTOR;
import static ascelion.rest.bridge.client.RestClientProperties.NO_RESPONSE_HANDLER;
import static ascelion.rest.bridge.client.RestClientProperties.RESPONSE_HANDLER;
import static java.util.Collections.unmodifiableCollection;

import lombok.Getter;
import lombok.Setter;

public final class RestClient implements RestClientInfo, RestClientInternals
{

	static public RestClient newRestClient( Client client, URI target )
	{
		return new RestClient( client, target );
	}

	static public RestClient newRestClient( Client client, URI target, String base )
	{
		base = cleanRequestURI( base );

		if( base.length() > 0 ) {
			target = UriBuilder.fromUri( target ).path( base ).build();
		}

		return RestClient.newRestClient( client, target );
	}

	static private final ThreadLocal<Method> METHOD = new ThreadLocal<>();

	static public Method invokedMethod()
	{
		return METHOD.get();
	}

	static void invokedMethod( Method method )
	{
		if( method != null ) {
			METHOD.set( method );
		}
		else {
			METHOD.remove();
		}
	}

	private final Client client;
	@Getter
	@Setter
	private URI baseURI;

	private final Collection<RestRequestInterceptor.Factory> rriFactories = new ArrayList<>();
	@Setter
	@Getter
	private AsyncInterceptor<?> asyncInterceptor = NO_ASYNC_INTERCEPTOR;
	@Setter
	@Getter
	private Function<Response, Throwable> responseHandler = NO_RESPONSE_HANDLER;
	@Setter
	@Getter
	private Executor executor = Executors.newCachedThreadPool();
	private final ConvertersFactory convertersFactory;

	private RestClient( Client client, URI target )
	{
		this.client = client;
		this.baseURI = target;
		this.convertersFactory = new ConvertersFactoryImpl( client );

		final Object aint = client.getConfiguration().getProperty( ASYNC_INTERCEPTOR );
		final Object rsph = client.getConfiguration().getProperty( RESPONSE_HANDLER );

		if( aint != null ) {
			this.asyncInterceptor = (AsyncInterceptor<?>) aint;
		}
		if( rsph != null ) {
			this.responseHandler = (Function<Response, Throwable>) rsph;
		}

		this.rriFactories.add( new DefaultRRIFactory() );
	}

	@Override
	public Supplier<WebTarget> getTarget()
	{
		return () -> this.client.target( this.baseURI );
	}

	@Override
	public Configuration getConfiguration()
	{
		return this.client.getConfiguration();
	}

	@Override
	public ConvertersFactory getConvertersFactory()
	{
		return this.convertersFactory;
	}

	public void addRRIFactory( RestRequestInterceptor.Factory f )
	{
		this.rriFactories.add( f );
	}

	public <X> X getInterface( Class<X> type )
	{
		final RestServiceInfo rsi = new RestServiceInfo( this, type );
		final RestService rs = new RestService( rsi, this );

		return rs.newProxy();
	}

	@Override
	public Collection<Factory> rriFactories()
	{
		return unmodifiableCollection( this.rriFactories );
	}
}
