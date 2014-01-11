/**
 *
 */

package ascelion.rest.bridge.cdi;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.ws.rs.Path;

/**
 * @author Pappy STĂNESCU
 *
 */
@SuppressWarnings( "CdiManagedBeanInconsistencyInspection" )
public class RestBridgeExtension
implements Extension
{

	private static final Logger L = Logger.getLogger( RestBridgeExtension.class.getName() );

	private final Collection<Class<?>> services = new HashSet<>();

	public void afterBeanDescovery( BeanManager bm, @Observes AfterBeanDiscovery event )
	{
		for( final Class<?> cls : this.services ) {
			event.addBean( new RestBridgeBean<>( bm, cls, Dependent.class ) );
		}
	}

	public <T, X> void processInjectionPoint( BeanManager bm, @Observes ProcessInjectionPoint<T, X> event )
	{
		final InjectionPoint inj = event.getInjectionPoint();
		final Type t = inj.getType();

		if( t instanceof Class ) {
			final Class<?> classType = (Class) t;

			if( classType.isInterface() && classType.isAnnotationPresent( Path.class ) ) {
				L.info( String.format( "Found %s", inj.getAnnotated() ) );

				this.services.add( classType );
			}
		}
	}

}
