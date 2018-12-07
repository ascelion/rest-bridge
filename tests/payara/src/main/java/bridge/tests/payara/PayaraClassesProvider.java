
package bridge.tests.payara;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import ascelion.rest.bridge.web.RestApplication;
import ascelion.rest.bridge.web.RestApplicationConfig;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;

@ApplicationScoped
public class PayaraClassesProvider implements RestApplicationConfig
{

	static private final Logger L = Logger.getLogger( RestApplication.class.getName() );

	@Override
	public void configure( Set<Class<?>> classes, Set<Object> singletons, Map<String, Object> properties )
	{
		classes.add( JacksonFeature.class );

		final LoggingFeature lf = new LoggingFeature( L, Level.INFO, Verbosity.PAYLOAD_TEXT, null );

		singletons.add( lf );
	}

}
