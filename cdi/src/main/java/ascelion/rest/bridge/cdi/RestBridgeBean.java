
package ascelion.rest.bridge.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

import ascelion.cdi1x.literals.AnyLiteral;
import ascelion.cdi1x.literals.DefaultLiteral;
import ascelion.rest.bridge.client.RestClient;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class RestBridgeBean<T> implements Bean<T>, PassivationCapable
{

	static private final Set<Annotation> QUALIFIERS = Collections.unmodifiableSet( new HashSet<Annotation>()
	{

		{
			add( new AnyLiteral() );
			add( new DefaultLiteral() );
		}
	} );

	private final BeanManager bm;
	private final Class<T> type;
	private volatile RestClient client;

	@Override
	public T create( CreationalContext<T> ccx )
	{
		return createRestClient().getInterface( this.type );
	}

	@Override
	public void destroy( T instance, CreationalContext<T> ccx )
	{
		ccx.release();
	}

	@Override
	public Set<Type> getTypes()
	{
		return singleton( this.type );
	}

	@Override
	public Set<Annotation> getQualifiers()
	{
		return QUALIFIERS;
	}

	@Override
	public Class<? extends Annotation> getScope()
	{
		return Dependent.class;
	}

	@Override
	public String getName()
	{
		return this.type.getName();
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes()
	{
		return emptySet();
	}

	@Override
	public boolean isAlternative()
	{
		return false;
	}

	@Override
	public String getId()
	{
		return this.type.getName();
	}

	@Override
	public Class<?> getBeanClass()
	{
		return this.type;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints()
	{
		return emptySet();
	}

	@Override
	public boolean isNullable()
	{
		return false;
	}

	private RestClient createRestClient()
	{
		if( this.client != null ) {
			return this.client;
		}

		synchronized( this ) {
			if( this.client != null ) {
				return this.client;
			}
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
