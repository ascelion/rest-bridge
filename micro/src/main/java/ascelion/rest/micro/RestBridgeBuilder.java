
package ascelion.rest.micro;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Formatter;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
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
import ascelion.rest.micro.cdi.CDIRRIFactory;

import static ascelion.rest.bridge.client.RestClient.newRestClient;
import static ascelion.rest.micro.RestBridgeConfiguration.LOG;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.spi.RestClientListener;

final class RestBridgeBuilder implements RestClientBuilder
{

	static <T> void showConfig( Configuration cfg, String msg, Object... arguments )
	{
		LOG.debug( () -> {
			try( final Formatter fmt = new Formatter() ) {
				fmt.format( msg, arguments );

				final Set<Class<?>> classes = new TreeSet<>( ( c1, c2 ) -> c1.getName().compareTo( c2.getName() ) );

				cfg.getClasses().stream().forEach( classes::add );
				cfg.getInstances().stream().map( Object::getClass ).forEach( classes::add );

				classes.forEach( c -> {
					final String cts = cfg.getContracts( c )
						.entrySet().stream()
						.map( e -> format( "%s:%s", e.getKey().getSimpleName(), e.getValue() ) )
						.collect( joining( ", " ) );

					fmt.format( "\n    %s -> %s", c.getName(), cts );
				} );

				return fmt.toString();
			}
		} );
	}

	private final RestBridgeConfigRec configuration = new RestBridgeConfigRec();
	private URL baseUrl;
	private Long connectTimeout;
	private Long readTimeout;
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

		final RestBridgeConfiguration cfg = this.configuration.forClient( this );

		configureProviders( cfg, type );

		final ClientBuilder bld = RBUtils.newInstance( ClientBuilder.class, () -> {
			return ofNullable( defaultJAXRS( cfg ) )
				.orElse( ClientBuilder.newBuilder() );
		} ).withConfig( cfg.forJAXRS() );

		configureTimeouts( bld, type );

		final Client client = bld.build();
		RestClient rc;

		try {
			rc = newRestClient( client, this.baseUrl.toURI() );
		}
		catch( final URISyntaxException e ) {
			throw new RestClientDefinitionException( e );
		}

		if( this.executorService != null ) {
			rc.setExecutor( this.executorService );
		}

		rc.addRRIFactory( new RRIFactory() );
		if( RBUtils.isCDI() ) {
			rc.addRRIFactory( RBUtils.newInstance( CDIRRIFactory.class ) );
		}

		rc.setResponseHandler( new MPResponseHandler( cfg ) );
		rc.setAsyncInterceptor( new MPAsyncInterceptor( cfg ) );

		showConfig( client.getConfiguration(), "Created JAX-RS client for %s", type.getName() );
		showConfig( cfg, "Created MP client for %s", type.getName() );

		try {
			return rc.getInterface( type );
		}
		catch( final RestClientMethodException e ) {
			throw new RestClientDefinitionException( format( "%s in method %s.%s", e.getMessage(),
				e.getMethod().getDeclaringClass().getName(), e.getMethod().getName() ) );
		}
	}

	private ClientBuilder defaultJAXRS( final RestBridgeConfiguration cfg )
	{
		return (ClientBuilder) cfg.getProperty( ClientBuilder.JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY );
	}

	private <T> void configureProviders( Configurable<? extends Configuration> cfg, Class<T> type )
	{
		stream( type.getAnnotationsByType( RegisterProvider.class ) )
			.forEach( a -> cfg.register( a.value(), RBUtils.getPriority( a.value(), a.priority() ) ) );

		MP.getConfig( type, String.class, "providers" )
			.map( s -> stream( s.split( "," ) ) )
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

			final int priority = Integer.valueOf( MP.getConfig( Integer.class, name ).get() );

			cfg.register( prov, priority );
		}
	}

	private <T> void configureTimeouts( ClientBuilder bld, Class<T> type )
	{
		final Long cto = ofNullable( this.connectTimeout )
			.orElse( MP.getConfig( type, Long.class, "connectTimeout" ).orElse( null ) );

		if( cto != null ) {
			bld.connectTimeout( cto, TimeUnit.MILLISECONDS );
		}

		final Long rto = ofNullable( this.readTimeout )
			.orElse( MP.getConfig( type, Long.class, "readTimeout" ).orElse( null ) );

		if( rto != null ) {
			bld.readTimeout( rto, TimeUnit.MILLISECONDS );
		}
	}
}
