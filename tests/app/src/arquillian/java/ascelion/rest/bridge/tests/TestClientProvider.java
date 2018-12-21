
package ascelion.rest.bridge.tests;

import java.net.URI;

import javax.ws.rs.client.ClientBuilder;

import ascelion.rest.bridge.tests.api.util.RestClientTrace;
import ascelion.rest.bridge.tests.providers.JerseyBridgeProvider;

import lombok.Getter;
import org.glassfish.jersey.jackson.JacksonFeature;

public abstract class TestClientProvider<B extends ClientBuilder>
{

	private static TestClientProvider<?> instance;

	static public <B extends ClientBuilder> TestClientProvider<B> getInstance()
	{
		if( instance == null ) {
			instance = new JerseyBridgeProvider();
		}

		return (TestClientProvider<B>) instance;
	}

	public static void setInstance( TestClientProvider<?> instance )
	{
		TestClientProvider.instance = instance;
	}

	@Getter
	private final B builder;

	public TestClientProvider( B builder )
	{
		this.builder = builder;

		builder.register( JacksonFeature.class );
		builder.register( JerseyTrace.class );
		builder.register( RestClientTrace.class );
	}

	public abstract <T> T createClient( URI target, Class<T> cls );

	public boolean hasClientValidation()
	{
		return false;
	}

	protected void release( Object client )
	{
	}
}
