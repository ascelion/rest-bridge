
package ascelion.rest.bridge.tests;

import java.net.URI;
import java.util.ServiceLoader;
import java.util.function.Supplier;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import ascelion.rest.bridge.tests.providers.JerseyBridgeProvider;
import ascelion.utils.jaxrs.RestClientTrace;

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

	private final Supplier<B> sup;
	private B builder;

	public TestClientProvider( Supplier<B> sup )
	{
		this.sup = sup;
	}

	public abstract <T> T createClient( URI target, Class<T> cls );

	public final void reset()
	{
		this.builder = this.sup.get();

		this.builder.register( JerseyTrace.class );
		this.builder.register( RestClientTrace.class );

		ServiceLoader.load( MessageBodyReader.class ).forEach( this.builder::register );
		ServiceLoader.load( MessageBodyWriter.class ).forEach( this.builder::register );
	}

	public final B getBuilder()
	{
		if( this.builder == null ) {
			reset();
		}

		return this.builder;
	}

	public boolean hasClientValidation()
	{
		return false;
	}

	protected void release( Object client )
	{
	}
}
