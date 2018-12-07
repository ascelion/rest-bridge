
package bridge.tests;

import java.net.URI;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.client.ClientBuilder;

import bridge.tests.providers.JerseyBridgeProvider;
import lombok.Getter;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;

public abstract class TestClientProvider<B extends ClientBuilder>
{

	private static TestClientProvider<?> instance;

	static public <B extends ClientBuilder> TestClientProvider<B> getInstance()
	{
		if( instance == null ) {
			try {
				instance = ServiceLoader.load( TestClientProvider.class ).iterator().next();
			}
			catch( final NoSuchElementException e ) {
				instance = new JerseyBridgeProvider();
			}
		}

		return (TestClientProvider<B>) instance;
	}

	public static void setInstance( TestClientProvider<?> instance )
	{
		TestClientProvider.instance = instance;
	}

	@Getter
	protected final B builder;

	public TestClientProvider( B builder )
	{
		this.builder = builder;

		try {
			builder.register( org.glassfish.jersey.jackson.JacksonFeature.class );
		}
		catch( final NoClassDefFoundError e ) {
		}

		try {
			final Logger logger = Logger.getLogger( "ascelion.bridge.REST" );

			builder.register( new org.glassfish.jersey.logging.LoggingFeature( logger, Level.INFO, Verbosity.PAYLOAD_TEXT, null ) );
		}
		catch( final NoClassDefFoundError e ) {
		}
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
