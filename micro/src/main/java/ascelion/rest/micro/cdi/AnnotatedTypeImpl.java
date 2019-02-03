
package ascelion.rest.micro.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;

import static java.util.Collections.singleton;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;

final class AnnotatedTypeImpl<X> implements AnnotatedType<X>
{

	private final AnnotatedType<X> delegate;
	private final Map<Class<? extends Annotation>, Set<Annotation>> annotations = new HashMap<>();

	AnnotatedTypeImpl( AnnotatedType<X> delegate )
	{
		this.delegate = delegate;

		this.delegate.getAnnotations().forEach( this::addAnnotation );
	}

	@Override
	public Set<Annotation> getAnnotations()
	{
		return unmodifiableSet( this.annotations.values().stream()
			.flatMap( Collection::stream )
			.collect( toSet() ) );
	}

	@Override
	public <T extends Annotation> T getAnnotation( Class<T> annotationType )
	{
		return (T) this.annotations.getOrDefault( annotationType, singleton( null ) )
			.iterator().next();
	}

	@Override
	public boolean isAnnotationPresent( Class<? extends Annotation> annotationType )
	{
		return this.annotations.containsKey( annotationType );
	}

	@Override
	public Class<X> getJavaClass()
	{
		return this.delegate.getJavaClass();
	}

	@Override
	public Type getBaseType()
	{
		return this.delegate.getBaseType();
	}

	@Override
	public Set<AnnotatedConstructor<X>> getConstructors()
	{
		return this.delegate.getConstructors();
	}

	@Override
	public Set<Type> getTypeClosure()
	{
		return this.delegate.getTypeClosure();
	}

	@Override
	public Set<AnnotatedMethod<? super X>> getMethods()
	{
		return this.delegate.getMethods();
	}

	@Override
	public Set<AnnotatedField<? super X>> getFields()
	{
		return this.delegate.getFields();
	}

	AnnotatedTypeImpl<X> addAnnotation( Annotation a )
	{
		this.annotations.compute( a.annotationType(), ( k, v ) -> {
			if( v == null ) {
				v = new HashSet<>();
			}

			v.add( a );

			return v;
		} );

		return this;
	}

}
