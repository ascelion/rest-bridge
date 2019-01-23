
package ascelion.rest.micro;

import java.util.List;
import java.util.function.Supplier;

import javax.ws.rs.core.Configurable;
import javax.ws.rs.core.Configuration;

import ascelion.rest.bridge.etc.RestClientTrace;
import ascelion.rest.bridge.tests.api.SLF4JHandler;

import static ascelion.rest.micro.RestBridgeConfiguration.LOG;
import static org.hamcrest.Matchers.containsString;
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
public class RestBridgeConfigurationTest
{

	@Parameterized.Parameters( name = "{0}" )
	static public Object data()
	{
		return new Object[] {
			new Object[] { "rest-bridge", true, (Supplier) () -> new RestBridgeConfiguration( new RestBridgeBuilder() ) },
			// compare with Jersey's configuration
			new Object[] { "jersey-config", false, (Supplier) () -> JerseyClientBuilder.createClient().getConfiguration() },
		};
	}

	@BeforeClass
	static public void setUpClass()
	{
		SLF4JHandler.install();

		( (Logger) LoggerFactory.getLogger( LOG.getName() ) ).setLevel( Level.ALL );
	}

	@Rule
	public final CheckLogRule rule = new CheckLogRule();

	private final Configurable<? extends Configuration> cf;
	private final boolean checkLog;

	public RestBridgeConfigurationTest( String name, boolean checkLog, Supplier<Configurable<? extends Configuration>> sup )
	{
		this.cf = sup.get();
		this.checkLog = checkLog;
	}

	@Test
	public void registerTwice()
	{
		this.cf.register( RestClientTrace.class );
		this.cf.register( RestClientTrace.class );

		if( this.checkLog ) {
			final List<ILoggingEvent> events = this.rule.getEvents( LOG.getName(), Level.WARN );

			assertThat( events, hasSize( 1 ) );

			final ILoggingEvent event = events.get( 0 );

			assertThat( event.getMessage(), containsString( "already" ) );
		}

		assertThat( this.cf.getConfiguration().isRegistered( RestClientTrace.class ), is( true ) );
		assertThat( this.cf.getConfiguration().isRegistered( new RestClientTrace() ), is( false ) );
	}

	@Test
	public void registerInstanceTwice()
	{
		final RestClientTrace i1 = new RestClientTrace();
		final RestClientTrace i2 = new RestClientTrace();

		this.cf.register( i1 );
		this.cf.register( i2 );

		if( this.checkLog ) {
			final List<ILoggingEvent> events = this.rule.getEvents( LOG.getName(), Level.WARN );

			assertThat( events, hasSize( 1 ) );

			final ILoggingEvent event = events.get( 0 );

			assertThat( event.getMessage(), containsString( "already" ) );
		}

		assertThat( this.cf.getConfiguration().isRegistered( RestClientTrace.class ), is( true ) );
		assertThat( this.cf.getConfiguration().isRegistered( i1 ), is( true ) );
		assertThat( this.cf.getConfiguration().isRegistered( i2 ), is( false ) );
	}

	@Test
	public void registerNone()
	{
		this.cf.register( getClass() );

		assertThat( this.cf.getConfiguration().isRegistered( getClass() ), is( false ) );
	}

	@Test
	public void registerUnkown()
	{
		this.cf.register( RestClientTrace.class );

		assertThat( this.cf.getConfiguration().isRegistered( getClass() ), is( false ) );
	}
}
