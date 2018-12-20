
package ascelion.rest.micro;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.client.RestClientMethodException;
import ascelion.rest.bridge.client.Util;

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
		if( this.configuration.addRegistration( componentClass ) ) {
			this.configuration.addClass( componentClass );
		}

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, int priority )
	{
		if( this.configuration.addRegistration( componentClass, priority ) ) {
			this.configuration.addClass( componentClass );
		}

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, Class<?>... contracts )
	{
		if( this.configuration.addRegistration( componentClass, contracts ) ) {
			this.configuration.addClass( componentClass );
		}

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		if( this.configuration.addRegistration( componentClass, contracts ) ) {
			this.configuration.addClass( componentClass );
		}

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
		if( executor == null ) {
			throw new IllegalArgumentException( "Executor service cannot be null" );
		}

		this.executorService = executor;

		return this;
	}

	@Override
	public <T> T build( Class<T> type )
	{
		if( this.baseUrl == null ) {
			throw new IllegalStateException( "Base URL hasn't been set" );
		}

		ServiceLoader.load( RestClientListener.class )
			.forEach( l -> l.onNewClient( type, this ) );

		final ClientBuilder bld = ClientBuilder.newBuilder()
			.withConfig( this.configuration.forClient() );

		configureTimeouts( bld, type );
		configureExecutor( bld );

		final Client client = bld.build();

		configureProviders( client, type );

		RestClient rc;

		try {
			rc = new RestClient( client, this.baseUrl.toURI() );
		}
		catch( final URISyntaxException e ) {
			throw new RestClientDefinitionException( e );
		}

		rc.setResponseHandler( new MPResponseHandler( this.configuration ) );

		try {
			return rc.getInterface( type );
		}
		catch( final RestClientMethodException e ) {
			throw new RestClientDefinitionException( format( "%s in method %s.%s", e.getMessage(),
				e.getMethod().getDeclaringClass().getName(), e.getMethod().getName() ) );
		}
	}

	private <T> void configureProviders( Client clt, Class<T> type )
	{
		Stream.of( type.getAnnotationsByType( RegisterProvider.class ) )
			.forEach( a -> clt.register( a.value() ) );

		MP.getConfig( type, "providers" )
			.map( s -> Stream.of( s.split( "," ) ) )
			.orElse( Stream.empty() )
			.map( Util::safeLoadClass )
			.filter( Objects::nonNull )
			.forEach( clt::register );
	}

	private <T> void configureTimeouts( ClientBuilder bld, Class<T> type )
	{
		try {
			final long to = MP.getConfig( type, "connectTimeout" )
				.map( Long::parseLong )
				.orElse( this.connectTimeout );

			bld.connectTimeout( to, TimeUnit.MILLISECONDS );
		}
		catch( final NumberFormatException e ) {
			throw new IllegalStateException( format( "%s: unable to parse connectTimeout from configuration", type.getName() ), e );
		}

		try {
			final long to = MP.getConfig( type, "readTimeout" )
				.map( Long::parseLong )
				.orElse( this.readTimeout );

			bld.readTimeout( to, TimeUnit.MILLISECONDS );
		}
		catch( final NumberFormatException e ) {
			throw new IllegalStateException( format( "%s: unable to parse readTimeout from configuration", type.getName() ), e );
		}
	}

	private void configureExecutor( final ClientBuilder bld )
	{
		if( this.executorService != null ) {
			bld.executorService( this.executorService );
		}
	}
}
