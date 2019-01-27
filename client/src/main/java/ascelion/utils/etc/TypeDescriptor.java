
package ascelion.utils.etc;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Arrays.stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.reflect.FieldUtils.getAllFieldsList;

import lombok.Getter;
import lombok.SneakyThrows;

public final class TypeDescriptor
{

	@Getter
	private final Class<?> type;
	private final Map<String, PropDescriptor<?>> props = new TreeMap<>();

	@SuppressWarnings( "rawtypes" )
	public TypeDescriptor( Object target )
	{
		this( requireNonNull( target, "Argument type cannot be null" ) instanceof Class ? (Class) target : target.getClass() );
	}

	@SneakyThrows
	public TypeDescriptor( Class<?> type )
	{
		this.type = type;

		stream( Introspector.getBeanInfo( this.type ).getPropertyDescriptors() )
			.map( PropDescriptor::new )
			.forEach( d -> this.props.put( d.getName(), d ) );

		getAllFieldsList( this.type ).stream()
			.filter( f -> {
				final PropDescriptor<?> p = this.props.get( f.getName() );

				if( p != null ) {
					p.addAnnotations( f.getAnnotations() );

					return false;
				}
				else {
					return true;
				}
			} )
			.map( PropDescriptor::new )
			.forEach( d -> this.props.put( d.getName(), d ) );
	}

	public Collection<PropDescriptor<?>> getProperties()
	{
		return this.props.values();
	}

	public Collection<PropDescriptor<?>> getProperties( Class<? extends Annotation> annotType )
	{
		return this.props.values().stream()
			.filter( p -> p.isAnnotationPresent( annotType ) )
			.collect( toList() );
	}

	public Collection<PropDescriptor<?>> getProperties( Class<? extends Annotation> annotType, Class<?> propType )
	{
		return this.props.values().stream()
			.filter( p -> p.isAnnotationPresent( annotType ) )
			.filter( p -> p.getType().isAssignableFrom( propType ) )
			.collect( toList() );
	}

	public void injectAnnotated( Class<? extends Annotation> annotType, Class<?> propType, Object target, Object value )
	{
		getProperties( annotType, propType )
			.forEach( p -> p.set( target, value ) );
	}
}
