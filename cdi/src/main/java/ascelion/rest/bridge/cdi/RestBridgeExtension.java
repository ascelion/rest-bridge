/**
 *
 */

package ascelion.rest.bridge.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessInjectionPoint;
import javax.ws.rs.Path;

import ascelion.rest.bridge.client.RestClient;

import static java.lang.String.format;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;
import org.apache.deltaspike.core.util.bean.BeanBuilder;
import org.apache.deltaspike.core.util.metadata.builder.ContextualLifecycle;

/**
 * @author pappy
 *
 */
public class RestBridgeExtension
implements Extension
{

	static class BeanLifecycle<T> implements ContextualLifecycle<T>
	{

		private final BeanManager bm;
		private final Class<T> ct;
		private RestClient client;

		BeanLifecycle( BeanManager bm, Class<T> ct )
		{
			this.bm = bm;
			this.ct = ct;
		}

		@Override
		public T create( Bean<T> bean, CreationalContext<T> ccx )
		{
			final T itf = createRestClient().getInterface( this.ct );

			ccx.push( itf );

			L.finest( format( "Created %s", itf ) );

			return itf;
		}

		@Override
		public void destroy( Bean<T> bean, T instance, CreationalContext<T> ccx )
		{
			RestClient.release( instance );

			L.finest( format( "Destroyed %s", instance ) );

			ccx.release();
		}

		private RestClient createRestClient()
		{
			if( this.client != null ) {
				return this.client;
			}

			final Set<Bean<?>> beans = this.bm.getBeans( RestClient.class );

			if( beans.isEmpty() ) {
				throw new UnsatisfiedResolutionException( format( "Cannot find bean of type %s", RestClient.class.getName() ) );
			}

			final Bean<RestClient> bean = (Bean<RestClient>) this.bm.resolve( beans );

			if( bean == null ) {
				throw new UnsatisfiedResolutionException( format( "Cannot resolve bean of type %s", RestClient.class.getName() ) );
			}

			final CreationalContext<RestClient> context = this.bm.createCreationalContext( bean );

			return this.client = (RestClient) this.bm.getReference( bean, RestClient.class, context );
		}
	}

	static private final Logger L = Logger.getLogger( RestBridgeExtension.class.getName() );

	static private final Set<Annotation> QUALIFIERS = Collections.unmodifiableSet( new HashSet<Annotation>()
	{

		{
			add( new AnyLiteral() );
			add( new DefaultLiteral() );
		}
	} );

	private final Collection<Class<?>> clients = new HashSet<>();

	void afterBeanDescovery( @Observes AfterBeanDiscovery event, BeanManager bm )
	{
		for( final Class<?> clientType : this.clients ) {
			event.addBean( createBean( bm, clientType ) );
		}
	}

	private <T> Bean<T> createBean( BeanManager bm, Class<T> clientType )
	{
		final ContextualLifecycle<T> lc = new BeanLifecycle<>( bm, clientType );

		return new BeanBuilder<T>( bm )
			.addQualifiers( QUALIFIERS )
			.beanClass( clientType )
			.addType( clientType )
			.beanLifecycle( lc )
			.passivationCapable( true )
			.create();
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
