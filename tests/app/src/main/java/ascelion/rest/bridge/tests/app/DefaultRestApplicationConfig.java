
package ascelion.rest.bridge.tests.app;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import ascelion.rest.micro.tests.shared.LocalDateConverterProvider;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.glassfish.jersey.server.ServerProperties;

@ApplicationScoped
public class DefaultRestApplicationConfig implements RestApplicationConfig
{

	@Override
	public void configure( Set<Class<?>> classes, Set<Object> singletons, Map<String, Object> properties )
	{
		classes.add( BeanIMPL.class );
		classes.add( BeanResourceImpl.class );
		classes.add( HelloImpl.class );
		classes.add( MethodsImpl.class );
		classes.add( ValidatedImpl.class );
		classes.add( ConvertImpl.class );
		classes.add( UsersImpl.class );
		classes.add( AsyncBeanIMPL.class );

		classes.add( LocalDateConverterProvider.class );

		classes.add( JacksonResolver.class );

		classes.add( GenericExceptionMapper.class );

		final Logger logger = Logger.getLogger( "ascelion.bridge.tests.SERVER" );

		singletons.add( new LoggingFeature( logger, Level.INFO, Verbosity.PAYLOAD_TEXT, null ) );

		properties.put( ServerProperties.TRACING, "ON_DEMAND" );
		properties.put( ServerProperties.TRACING_THRESHOLD, "VERBOSE" );
		properties.put( CommonProperties.MOXY_JSON_FEATURE_DISABLE, true );
	}

}
