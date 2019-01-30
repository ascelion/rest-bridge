
package ascelion.rest.micro;

import java.util.List;
import java.util.function.Supplier;

import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;

import ascelion.rest.bridge.tests.api.SLF4JHandler;
import ascelion.utils.jaxrs.RestClientTrace;

import static ascelion.rest.micro.RestBridgeConfiguration.LOG;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.LoggerFactory;

@RunWith( Parameterized.class )
public class RestBridgeConfigurationSuite
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			new Object[] { "rest-bridge", LOG.getName(), "already", (Supplier) () -> new RestBridgeConfiguration() },
			// compare with Jersey's configuration
			new Object[] { "jersey-config", "org.glassfish.jersey.internal.Errors", "previous", (Supplier) () -> JerseyClientBuilder.createClient().getConfiguration() },
		};
	}

	@BeforeClass
	static public void setUpClass()
	{
		SLF4JHandler.install();

		( (Logger) LoggerFactory.getLogger( LOG.getName() ) ).setLevel( Level.ALL );
	}

	@Rule
	public CheckLogRule rule;

	private final Configurable<? extends Configuration> configurable;
	private final Configuration configuration;
	private final String cat;
	private final String lookup;

	public RestBridgeConfigurationSuite( String name, String cat, String lookup, Supplier<Configurable<? extends Configuration>> sup )
	{
		this.rule = new CheckLogRule( cat );
		this.configurable = sup.get();
		this.configuration = this.configurable.getConfiguration();
		this.cat = cat;
		this.lookup = lookup;
	}

	@Test
	public void registerTwice()
	{
		this.configurable.register( RestClientTrace.class );
		this.configurable.register( RestClientTrace.class );

		assertThat( this.configuration.isRegistered( RestClientTrace.class ), is( true ) );
		assertThat( this.configuration.isRegistered( new RestClientTrace() ), is( false ) );

		final List<ILoggingEvent> events = this.rule.getEvents( this.cat,
			e -> e.getMessage().contains( this.lookup ) );

		assertThat( events, hasSize( 1 ) );
	}

	@Test
	public void registerInstanceTwice()
	{
		final RestClientTrace i1 = new RestClientTrace();
		final RestClientTrace i2 = new RestClientTrace();

		this.configurable.register( i1 );
		this.configurable.register( i2 );

		assertThat( this.configuration.isRegistered( RestClientTrace.class ), is( true ) );
		assertThat( this.configuration.isRegistered( i1 ), is( true ) );
		assertThat( this.configuration.isRegistered( i2 ), is( false ) );

		final List<ILoggingEvent> events = this.rule.getEvents( this.cat,
			e -> e.getMessage().contains( this.lookup ) );

		assertThat( events, hasSize( 1 ) );
	}

	@Test
	public void registerNotSupportedANY()
	{
		this.configurable.register( this );

		assertThat( this.configuration.isRegistered( this ), is( false ) );
	}

	@Test
	public void registerNotSupportedJAXRS()
	{
		this.configurable.register( RestClientTrace.class );

		assertThat( this.configuration.isRegistered( getClass() ), is( false ) );
	}
}
