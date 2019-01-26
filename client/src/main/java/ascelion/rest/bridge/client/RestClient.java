
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static ascelion.rest.bridge.client.RestClientProperties.ASYNC_INTERCEPTOR;
import static ascelion.rest.bridge.client.RestClientProperties.NO_ASYNC_INTERCEPTOR;
import static ascelion.rest.bridge.client.RestClientProperties.NO_REQUEST_INTERCEPTOR;
import static ascelion.rest.bridge.client.RestClientProperties.NO_RESPONSE_HANDLER;
import static ascelion.rest.bridge.client.RestClientProperties.REQUEST_INTERCEPTOR;
import static ascelion.rest.bridge.client.RestClientProperties.RESPONSE_HANDLER;

import lombok.Setter;

public final class RestClient
{

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
	@Setter
	private URI target;
	private final ConvertersFactory cvsf;
	@Setter
	private RequestInterceptor requestInterceptor = NO_REQUEST_INTERCEPTOR;
	@Setter
	private AsyncInterceptor<?> asyncInterceptor = NO_ASYNC_INTERCEPTOR;
	@Setter
	private Function<Response, Throwable> responseHandler = NO_RESPONSE_HANDLER;
	@Setter
	private Executor executor = Executors.newCachedThreadPool();

	public RestClient( Client client, URI target )
	{
		this( client, target, (String) null );
	}

	public RestClient( Client client, URI target, String base )
	{
		this.client = client;

		if( base == null || base.isEmpty() ) {
			this.target = target;
		}
		else {
			this.target = UriBuilder.fromUri( target ).path( base ).build();
		}

		this.cvsf = new ConvertersFactory( client );

		final Object reqi = client.getConfiguration().getProperty( REQUEST_INTERCEPTOR );
		final Object aint = client.getConfiguration().getProperty( ASYNC_INTERCEPTOR );
		final Object rsph = client.getConfiguration().getProperty( RESPONSE_HANDLER );

		if( reqi != null ) {
			this.requestInterceptor = (RequestInterceptor) reqi;
		}
		if( aint != null ) {
			this.asyncInterceptor = (AsyncInterceptor<?>) aint;
		}
		if( rsph != null ) {
			this.responseHandler = (Function<Response, Throwable>) rsph;
		}
	}

	public <X> X getInterface( Class<X> type )
	{
//		final Supplier<WebTarget> sup = () -> RBUtils.addPathFromAnnotation( type, this.client.target( this.target ) );
		final Supplier<WebTarget> sup = () -> this.client.target( this.target );
		final RestClientData rcd = new RestClientData(	type, this.client.getConfiguration(), this.cvsf,
														this.requestInterceptor, this.responseHandler, this.executor,
														(AsyncInterceptor<Object>) this.asyncInterceptor, sup );
		final RestClientIH ih = new RestClientIH( rcd );

		return ih.newProxy();
	}
}
