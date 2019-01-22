
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
import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.rest.bridge.client.RestClient;
import ascelion.rest.bridge.client.RestClientMethodException;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

final class RestBridgeBuilder implements RestClientBuilder
{

	private final RestBridgeConfiguration configuration = new RestBridgeConfiguration( this );
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
		this.configuration.register( componentClass );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, int priority )
	{
		this.configuration.register( componentClass, priority );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, Class<?>... contracts )
	{
		this.configuration.register( componentClass, contracts );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		this.configuration.register( componentClass, contracts );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component )
	{
		this.configuration.register( component );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, int priority )
	{
		this.configuration.register( component, priority );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, Class<?>... contracts )
	{
		this.configuration.register( component, contracts );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, Map<Class<?>, Integer> contracts )
	{
		this.configuration.register( component, contracts );

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

		final RestBridgeConfiguration cfg = this.configuration.forClient();

		configureProviders( cfg, type );

		final ClientBuilder bld = ofNullable( (ClientBuilder) cfg.getProperty( ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY ) )
			.orElse( ClientBuilder.newBuilder() ).withConfig( cfg );

		configureTimeouts( bld, type );

		final Client client = bld.build();

		RestClient rc;

		try {
			rc = new RestClient( client, this.baseUrl.toURI() );
		}
		catch( final URISyntaxException e ) {
			throw new RestClientDefinitionException( e );
		}

		if( this.executorService != null ) {
			rc.setExecutor( this.executorService );
		}

		rc.setRequestInterceptor( new MPRequestInterceptor() );
		rc.setResponseHandler( new MPResponseHandler( this.configuration ) );
		rc.setAsyncInterceptor( new MPAsyncInterceptor( cfg ) );

		try {
			return rc.getInterface( type );
		}
		catch( final RestClientMethodException e ) {
			throw new RestClientDefinitionException( format( "%s in method %s.%s", e.getMessage(),
				e.getMethod().getDeclaringClass().getName(), e.getMethod().getName() ) );
		}
	}

	private <T> void configureProviders( Configurable<? extends Configuration> cfg, Class<T> type )
	{
		Stream.of( type.getAnnotationsByType( RegisterProvider.class ) )
			.forEach( a -> cfg.register( a.value(), RBUtils.getPriority( a.value(), a.priority() ) ) );

		MP.getConfig( type, "providers" )
			.map( s -> Stream.of( s.split( "," ) ) )
			.orElse( Stream.empty() )
			.map( RBUtils::safeLoadClass )
			.filter( Objects::nonNull )
			.forEach( p -> cfg.register( p, RBUtils.getPriority( p ) ) );

		final String prefix = format( "%s/mp-rest/providers/", type.getName() );
		final Iterable<String> names = MP.getConfig().map( c -> c.getPropertyNames() ).orElse( emptyList() );

		for( final String name : names ) {
			if( !name.startsWith( prefix ) ) {
				continue;
			}

			final String[] vec = name.substring( prefix.length() ).split( "/" );

			if( vec.length != 2 ) {
				continue;
			}
			if( !vec[1].equals( "priority" ) ) {
				continue;
			}

			final Class<?> prov = RBUtils.safeLoadClass( vec[0] );

			if( prov == null ) {
				continue;
			}

			final int priority = Integer.valueOf( MP.getConfig( name ).get() );

			cfg.register( prov, priority );
		}
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
}
