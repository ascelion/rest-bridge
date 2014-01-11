/**
 *
 */

package ascelion.rest.bridge.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.enterprise.util.AnnotationLiteral;

import ascelion.rest.bridge.client.RestClient;

import static java.lang.String.format;

/**
 * @author Pappy STĂNESCU
 *
 */
class RestBridgeBean<T>
implements Bean<T>, PassivationCapable
{

	static class DefaultLiteral
	extends AnnotationLiteral<Default>
	implements Default
	{
	}

	private final BeanManager bm;

	private final Class<T> clientType;

	private final Class<? extends Annotation> scope;

	private final String id = UUID.randomUUID().toString();

	RestBridgeBean( BeanManager bm, Class<T> clientType, Class<? extends Annotation> scope )
	{
		this.bm = bm;
		this.clientType = clientType;
		this.scope = scope;
	}

	@Override
	public T create( CreationalContext<T> creationalContext )
	{
		return createRestClient().getInterface( this.clientType );
	}

	@Override
	public void destroy( T instance, CreationalContext<T> creationalContext )
	{
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
		return Collections.<Annotation> singleton( new DefaultLiteral() );
	}

	@Override
	public Class<? extends Annotation> getScope()
	{
		return this.scope;
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

	private RestClient createRestClient()
	{
		final Set<Bean<?>> beans = this.bm.getBeans( RestClient.class );

		if( beans.isEmpty() ) {
			throw new UnsatisfiedResolutionException(
				format( "Cannot find bean of type %s", RestClient.class.getName() ) );
		}

		final Bean<RestClient> bean = (Bean<RestClient>) this.bm.resolve( beans );

		if( bean == null ) {
			throw new UnsatisfiedResolutionException( format( "Cannot resolve bean of type %s",
				RestClient.class.getName() ) );
		}

		final CreationalContext<RestClient> context = this.bm.createCreationalContext( bean );

		return (RestClient) this.bm.getReference( bean, RestClient.class, context );
	}

}
