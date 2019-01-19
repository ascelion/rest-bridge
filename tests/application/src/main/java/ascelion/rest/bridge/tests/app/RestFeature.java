
package ascelion.rest.bridge.tests.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import ascelion.rest.micro.tests.shared.LocalDateConverterProvider;

import org.glassfish.jersey.CommonProperties;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.logging.LoggingFeature.Verbosity;
import org.glassfish.jersey.server.ServerProperties;

@Provider
public class RestFeature implements Feature
{

	@Override
	public boolean configure( FeatureContext context )
	{
		context.register( BeanIMPL.class );
		context.register( BeanResourceImpl.class );
		context.register( HelloImpl.class );
		context.register( MethodsImpl.class );
		context.register( ValidatedImpl.class );
		context.register( ConvertImpl.class );
		context.register( UsersImpl.class );

		context.register( AsyncSuspendedIMPL.class );
		//https://github.com/eclipse-ee4j/jersey/issues/3672
		//context.register( AsyncCompletionIMPL.class );

		context.register( LocalDateConverterProvider.class );

		context.register( JacksonFeature.class );
		context.register( JacksonResolver.class );

		context.register( GenericExceptionMapper.class );

		final Logger logger = Logger.getLogger( "ascelion.rest.bridge.tests.SERVER" );

		context.register( new LoggingFeature( logger, Level.FINEST, Verbosity.PAYLOAD_TEXT, null ) );

		context.property( ServerProperties.TRACING, "ON_DEMAND" );
		context.property( ServerProperties.TRACING_THRESHOLD, "VERBOSE" );
		context.property( CommonProperties.MOXY_JSON_FEATURE_DISABLE, true );

		return false;
	}

}
