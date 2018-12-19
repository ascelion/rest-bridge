
package ascelion.rest.micro;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.client.RestClientMethodException;

import static java.lang.String.format;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

final class RestBridgeBuilder implements RestClientBuilder
{

	private final RestBridgeConfiguration configuration = new RestBridgeConfiguration();
	private URL baseUrl;
	private long connectTimeout = 0;
	private long readTimeout = 0;
	private ExecutorService executorService;

	@Override
	public Configuration getConfiguration()
	{
		return this.configuration;
	}

	@Override
	public RestClientBuilder property( String name, Object value )
	{
		this.configuration.property( name, value );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass )
	{
		this.configuration.addRegistration( componentClass );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, int priority )
	{
		this.configuration.addRegistration( componentClass, priority );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, Class<?>... contracts )
	{
		this.configuration.addRegistration( componentClass, contracts );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		this.configuration.addRegistration( componentClass, contracts );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component )
	{
		if( this.configuration.addRegistration( component.getClass() ) ) {
			this.configuration.addInstance( component );
		}

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, int priority )
	{
		if( this.configuration.addRegistration( component.getClass(), priority ) ) {
			this.configuration.addInstance( component );
		}

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, Class<?>... contracts )
	{
		if( this.configuration.addRegistration( component.getClass(), contracts ) ) {
			this.configuration.addInstance( component );
		}

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, Map<Class<?>, Integer> contracts )
	{
		if( this.configuration.addRegistration( component.getClass(), contracts ) ) {
			this.configuration.addInstance( component );
		}

		return this;
	}

	@Override
	public RestClientBuilder baseUrl( URL url )
	{
		this.baseUrl = url;

		return this;
	}

	@Override
	public RestClientBuilder connectTimeout( long timeout, TimeUnit unit )
	{
		this.connectTimeout = unit.toMillis( timeout );

		return this;
	}

	@Override
	public RestClientBuilder readTimeout( long timeout, TimeUnit unit )
	{
		this.readTimeout = unit.toMillis( timeout );

		return this;
	}

	@Override
	public RestClientBuilder executorService( ExecutorService executor )
	{
		this.executorService = executor;

		return this;
	}

	@Override
	public <T> T build( Class<T> clazz )
	{
		if( this.baseUrl == null ) {
			throw new IllegalStateException( "Base URL hasn't been set" );
		}

		ServiceLoader.load( RestClientListener.class )
			.forEach( l -> l.onNewClient( clazz, this ) );

		final ClientBuilder bld = ClientBuilder.newBuilder()
			.withConfig( this.configuration.copy() );

		try {
			final long to = MP.getConfig( clazz, "connectTimeout" )
				.map( Long::parseLong )
				.orElse( this.connectTimeout );

			bld.connectTimeout( to, TimeUnit.MILLISECONDS );
		}
		catch( final NumberFormatException e ) {
			throw new IllegalStateException( format( "%s: unable to parse connectTimeout from configuration", clazz.getName() ), e );
		}

		try {
			final long to = MP.getConfig( clazz, "readTimeout" )
				.map( Long::parseLong )
				.orElse( this.readTimeout );

			bld.readTimeout( to, TimeUnit.MILLISECONDS );
		}
		catch( final NumberFormatException e ) {
			throw new IllegalStateException( format( "%s: unable to parse readTimeout from configuration", clazz.getName() ), e );
		}

		if( this.executorService != null ) {
			bld.executorService( this.executorService );
		}

		final Client client = bld.build();

		Stream.of( clazz.getAnnotationsByType( RegisterProvider.class ) )
			.forEach( a -> client.register( a.value() ) );

		RestClient rc;

		try {
			rc = new RestClient( client, this.baseUrl.toURI() );
		}
		catch( final URISyntaxException e ) {
			throw new RestClientDefinitionException( e );
		}

		try {
			return rc.getInterface( clazz );
		}
		catch( final RestClientMethodException e ) {
			throw new RestClientDefinitionException( format( "%s in method %s.%s", e.getMessage(),
				e.getMethod().getDeclaringClass().getName(), e.getMethod().getName() ) );
		}
	}
}
