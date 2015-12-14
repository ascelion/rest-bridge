/**
 *
 */

package ascelion.rest.bridge.cdi;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.ws.rs.Path;

/**
 * @author Pappy STĂNESCU
 *
 */
public class RestBridgeExtension
implements Extension
{

	static final Logger L = Logger.getLogger( RestBridgeExtension.class.getName() );

	private final Collection<Class<?>> clients = new HashSet<>();

	void afterBeanDescovery( @Observes AfterBeanDiscovery event, BeanManager bm )
	{
		for( final Class<?> clientType : this.clients ) {
			event.addBean( new RestBridgeBean( clientType, bm ) );
		}
	}

	void beforeShutdown( @Observes BeforeShutdown event )
	{
	}

	<T, X> void processInjectionPoint( @Observes ProcessInjectionPoint<T, X> event )
	{
		final InjectionPoint ijp = event.getInjectionPoint();
		final Type t = ijp.getType();

		if( t instanceof Class ) {
			final Class<?> classType = (Class<?>) t;

			if( classType.isInterface() && classType.isAnnotationPresent( Path.class ) ) {
				L.info( String.format( "Found %sL %s", ijp.getAnnotated(), classType.getName() ) );

				this.clients.add( classType );
			}
		}
	}

}
