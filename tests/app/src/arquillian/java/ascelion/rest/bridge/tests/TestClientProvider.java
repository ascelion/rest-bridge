
package ascelion.rest.bridge.tests;

import java.net.URI;
import java.util.ServiceLoader;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import ascelion.rest.bridge.etc.RestClientTrace;
import ascelion.rest.bridge.tests.providers.JerseyBridgeProvider;

import lombok.Getter;

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

		builder.register( JerseyTrace.class );
		builder.register( RestClientTrace.class );

		ServiceLoader.load( MessageBodyReader.class ).forEach( builder::register );
		ServiceLoader.load( MessageBodyWriter.class ).forEach( builder::register );
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
