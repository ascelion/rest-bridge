
package ascelion.rest.micro;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

import ascelion.rest.bridge.client.RestClient;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;

public class Builder implements RestClientBuilder
{

	private final RestClientConfiguration configuration = new RestClientConfiguration();
	private final ClientBuilder builder;
	private RestClient restClient;
	private URL baseUrl;

	Builder( ClientBuilder builder )
	{
		this.builder = builder;
	}

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
		this.configuration.addRegistration( component.getClass() );
		this.configuration.addInstance( component );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, int priority )
	{
		this.configuration.addRegistration( component.getClass(), priority );
		this.configuration.addInstance( component );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, Class<?>... contracts )
	{
		this.configuration.addRegistration( component.getClass(), contracts );
		this.configuration.addInstance( component );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, Map<Class<?>, Integer> contracts )
	{
		this.configuration.addRegistration( component.getClass(), contracts );
		this.configuration.addInstance( component );

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
		this.builder.connectTimeout( timeout, unit );

		return this;
	}

	@Override
	public RestClientBuilder readTimeout( long timeout, TimeUnit unit )
	{
		this.builder.readTimeout( timeout, unit );

		return this;
	}

	@Override
	public RestClientBuilder executorService( ExecutorService executor )
	{
		this.builder.executorService( executor );

		return this;
	}

	@Override
	public <T> T build( Class<T> clazz )
	{
		try {
			ensureClientBuilt();
		}
		catch( final URISyntaxException e ) {
			throw new RestClientDefinitionException( e );
		}

		return this.restClient.getInterface( clazz );
	}

	private void ensureClientBuilt() throws URISyntaxException
	{
		if( this.restClient == null ) {
			if( this.baseUrl == null ) {
				throw new IllegalStateException( "Base URL hasn't been set" );
			}

			final Client client = this.builder.withConfig( this.configuration ).build();

			this.restClient = new RestClient( client, this.baseUrl.toURI() );
		}
	}
}
