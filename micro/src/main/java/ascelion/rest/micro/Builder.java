
package ascelion.rest.micro;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Configuration;

import ascelion.rest.bridge.client.RestClient;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.RestClientDefinitionException;

public class Builder implements RestClientBuilder
{

	private final Client client;
	private URL baseUrl;

	Builder( Client client )
	{
		this.client = client;
	}

	@Override
	public Configuration getConfiguration()
	{
		return this.client.getConfiguration();
	}

	@Override
	public RestClientBuilder property( String name, Object value )
	{
		this.client.property( name, value );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass )
	{
		this.client.register( componentClass );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, int priority )
	{
		this.client.register( componentClass, priority );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, Class<?>... contracts )
	{
		this.client.register( componentClass, contracts );

		return this;
	}

	@Override
	public RestClientBuilder register( Class<?> componentClass, Map<Class<?>, Integer> contracts )
	{
		this.client.register( componentClass, contracts );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component )
	{
		this.client.register( component );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, int priority )
	{
		this.client.register( component, priority );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, Class<?>... contracts )
	{
		this.client.register( component, contracts );

		return this;
	}

	@Override
	public RestClientBuilder register( Object component, Map<Class<?>, Integer> contracts )
	{
		this.client.register( component, contracts );

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
		return this;
	}

	@Override
	public RestClientBuilder readTimeout( long timeout, TimeUnit unit )
	{
		return this;
	}

	@Override
	public RestClientBuilder executorService( ExecutorService executor )
	{
		return this;
	}

	@Override
	public <T> T build( Class<T> clazz ) throws IllegalStateException, RestClientDefinitionException
	{
		try {
			return new RestClient( this.client, this.baseUrl.toURI() ).getInterface( clazz );
		}
		catch( final URISyntaxException e ) {
			throw new RestClientDefinitionException( e );
		}
	}
}
