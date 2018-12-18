
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Priority;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;

import static org.apache.commons.lang3.reflect.MethodUtils.getOverrideHierarchy;

import org.apache.commons.lang3.ClassUtils.Interfaces;

final class Util
{

	static WebTarget addPathFromAnnotation( AnnotatedElement ae, WebTarget target )
	{
		final Path p = ae.getAnnotation( Path.class );

		if( p == null ) {
			return target;
		}

		final String v = p.value().trim();

		return v.isEmpty() || v.equals( "/" ) ? target : target.path( p.value() );
	}

	static String getHttpMethod( Method method )
	{
		return getOverrideHierarchy( method, Interfaces.INCLUDE ).stream()
			.map( Util::httpMethodOf )
			.filter( Objects::nonNull )
			.findFirst()
			.orElse( null );
	}

	private static String httpMethodOf( Method method )
	{
		String httpMethod = getHttpMethodName( method );

		for( final Annotation ann : method.getAnnotations() ) {
			final String m = getHttpMethodName( ann.annotationType() );

			if( m != null ) {
				if( httpMethod != null ) {
					throw new RestClientMethodException( "Too many HTTP methods", method );
				}

				httpMethod = m;
			}
		}
		return httpMethod;
	}

	private static String getHttpMethodName( AnnotatedElement element )
	{
		final HttpMethod a = element.getAnnotation( HttpMethod.class );

		return a != null ? a.value() : null;
	}

	static int byPriority( Object o1, Object o2 )
	{
		final Class c1 = o1 instanceof Class ? (Class) o1 : o1.getClass();
		final Class c2 = o2 instanceof Class ? (Class) o2 : o2.getClass();
		final int p1 = findAnnotation( Priority.class, c1 ).map( Priority::value ).orElse( 0 );
		final int p2 = findAnnotation( Priority.class, c2 ).map( Priority::value ).orElse( 0 );

		return Integer.compare( p1, p2 );
	}

	static <A extends Annotation> Optional<A> findAnnotation( Class<A> type, Class<?> cls )
	{
		if( type == null || (Class) type == Object.class ) {
			return Optional.empty();
		}

		final A a = cls.getAnnotation( type );

		if( a != null ) {
			return Optional.of( a );
		}

		return findAnnotation( type, cls.getSuperclass() );
	}

	static <A extends Annotation> Optional<A> findAnnotation( Class<A> type, Member memb, Class<?> cls )
	{
		if( type == null || (Class) type == Object.class ) {
			return Optional.empty();
		}

		final A a = ( (AnnotatedElement) memb ).getAnnotation( type );

		if( a != null ) {
			return Optional.of( a );
		}

		return Optional.ofNullable( cls.getAnnotation( type ) );
	}

	static Set<String> pathElements( String path )
	{
		final Set<String> elements = new LinkedHashSet<>();
		int index = path.indexOf( '{' );

		while( index >= 0 ) {
			final int nextIndex = path.indexOf( '}', index + 1 );

			if( nextIndex > 0 ) {
				elements.add( path.substring( index + 1, nextIndex ) );

				index = path.indexOf( '{', nextIndex + 1 );
			}
		}

		return elements;
	}

	private Util()
	{
	}
}
