
package ascelion.rest.micro.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.PassivationCapable;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.rest.client.spi.RestClientBuilderResolver;

class RestBridgeBean<T> implements Bean<T>, PassivationCapable
{

	static private final Set<Annotation> QUALIFIERS = Collections.unmodifiableSet( new HashSet<Annotation>()
	{

		{
			add( new Default.Literal() );
			add( new RestClient.RestClientLiteral() );
		}
	} );

	private final BeanManager bm;
	private final Class<T> type;
	private final Class<? extends Annotation> scope;

	RestBridgeBean( BeanManager bm, Class<T> type )
	{
		this.bm = bm;
		this.type = type;
		this.scope = lookupScope();
	}

	@Override
	public T create( CreationalContext<T> creationalContext )
	{
		final RestClientBuilder bld = RestClientBuilderResolver.instance().newBuilder();
		final String url = MP.getConfig( this.type, "url" ).orElseThrow( () -> new IllegalStateException( "Unable to determine base URI from configuration" ) );

		bld.baseUri( URI.create( url ) );

		return bld.build( this.type );
	}

	@Override
	public void destroy( T instance, CreationalContext<T> creationalContext )
	{
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
		return this.scope;
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

	private Class<? extends Annotation> lookupScope()
	{
		final Optional<Class<? extends Annotation>> c = MP.getConfig( this.type, "scope" )
			.map( this::loadClass );

		return c.orElse( inferScope() );
	}

	private <T> Class<T> loadClass( String name )
	{
		try {
			return (Class<T>) Thread.currentThread().getContextClassLoader().loadClass( name );
		}
		catch( final ClassNotFoundException e ) {
			throw new IllegalArgumentException( "Cannot load class " + name, e );
		}
	}

	private Class<? extends Annotation> inferScope()
	{
		final Set<Class<? extends Annotation>> scopes = Stream.of( this.type.getDeclaredAnnotations() )
			.map( Annotation::annotationType )
			.filter( t -> this.bm.isScope( t ) )
			.collect( toSet() );

		if( scopes.size() > 1 ) {
			throw new IllegalArgumentException( format( "The class %s has multiple scopes defined (%s) ", this.type.getName(), scopes ) );
		}

		return scopes.isEmpty() ? Dependent.class : scopes.iterator().next();
	}
}
