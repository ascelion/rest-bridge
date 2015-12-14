
package ascelion.rest.bridge.cdi;

import static java.lang.String.format;

import static ascelion.rest.bridge.cdi.RestBridgeExtension.L;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.literal.DefaultLiteral;

import ascelion.rest.bridge.client.RestClient;

/**
 * @author pappy
 *
 */
class RestBridgeBean<T>
implements Bean<T>, PassivationCapable
{

	static final Set<Annotation> QUALIFIERS = Collections.unmodifiableSet( new HashSet<Annotation>()
	{

		{
			add( new AnyLiteral() );
			add( new DefaultLiteral() );
		}
	} );

	private final BeanManager bm;

	private final Class<T> clientType;

	private final String id = UUID.randomUUID().toString();

	private RestClient client;

	RestBridgeBean( Class<T> clientType, BeanManager bm )
	{
		this.clientType = clientType;
		this.bm = bm;
	}

	@Override
	public T create( CreationalContext<T> creationalContext )
	{
		final T itf = createRestClient().getInterface( this.clientType );

		creationalContext.push( itf );

		L.info( String.format( "Created %s", itf ) );

		return itf;
	}

	@Override
	public void destroy( T instance, CreationalContext<T> creationalContext )
	{
		RestClient.release( instance );

		L.info( String.format( "Destroyed %s", instance ) );

		creationalContext.release();
	}

	@Override
	public Class<?> getBeanClass()
	{
		return this.clientType;
	}

	@Override
	public String getId()
	{
		return this.id;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints()
	{
		return Collections.emptySet();
	}

	@Override
	public String getName()
	{
		return this.clientType.getName();
	}

	@Override
	public Set<Annotation> getQualifiers()
	{
		return QUALIFIERS;
	}

	@Override
	public Class<? extends Annotation> getScope()
	{
		return ApplicationScoped.class;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes()
	{
		return Collections.emptySet();
	}

	@Override
	public Set<Type> getTypes()
	{
		return Collections.<Type> singleton( this.clientType );
	}

	@Override
	public boolean isAlternative()
	{
		return false;
	}

	@Override
	public boolean isNullable()
	{
		return false;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		builder.append( "RestBridgeBean [clientType=" );
		builder.append( this.clientType.getName() );
		builder.append( "]" );
		return builder.toString();
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
