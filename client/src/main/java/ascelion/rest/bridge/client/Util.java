
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;

import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.reflect.MethodUtils.getOverrideHierarchy;

import org.apache.commons.lang3.ClassUtils.Interfaces;

public final class Util
{

	static public ClassLoader threadClassLoader()
	{
		return AccessController.doPrivileged( (PrivilegedAction<ClassLoader>) () -> currentThread().getContextClassLoader() );
	}

	static public <T> Class<T> safeLoadClass( String name )
	{
		try {
			return (Class<T>) threadClassLoader().loadClass( name.trim() );
		}
		catch( final ClassNotFoundException e ) {
			return null;
		}
	}

	static public <T> Class<T> rtLoadClass( String name )
	{
		try {
			return (Class<T>) threadClassLoader().loadClass( name.trim() );
		}
		catch( final ClassNotFoundException e ) {
			throw new IllegalArgumentException( "Cannot load class " + name.trim() );
		}
	}

	static boolean isCDI()
	{
		try {
			javax.enterprise.inject.spi.CDI.current();

			return true;
		}
		catch( final NoClassDefFoundError e ) {
			return false;
		}
		catch( final IllegalStateException e ) {
			return false;
		}
	}

	static <T> T newInstance( Class<T> type )
	{
		try {
			return javax.enterprise.inject.spi.CDI.current().select( type ).get();
		}
		catch( final NoClassDefFoundError e ) {
			;
		}
		catch( final IllegalStateException e ) {
			;
		}
		catch( final RuntimeException e ) {
			;
		}

		try {
			return type.newInstance();
		}
		catch( InstantiationException | IllegalAccessException e ) {
			throw new RestClientException( "Cannot instantiate type " + type.getName() );
		}
	}

	static public <T> Collection<T> providers( Configuration cf, Class<T> type )
	{
		final Stream<T> si = cf
			.getInstances()
			.stream()
			.filter( type::isInstance )
			.map( type::cast );
		final Stream<T> sc = cf
			.getClasses()
			.stream()
			.filter( type::isAssignableFrom )
			.map( Util::newInstance )
			.map( type::cast );

		return Stream.concat( si, sc )
			.sorted( ( o1, o2 ) -> comparePriority( cf, o1.getClass(), o2.getClass(), type ) )
			.collect( toList() );
	}

	static private int comparePriority( Configuration cf, Class<?> c1, Class<?> c2, Class<?> type )
	{
		if( Proxy.isProxyClass( c1 ) ) {
			c1 = c1.getSuperclass();
		}
		if( Proxy.isProxyClass( c2 ) ) {
			c2 = c2.getSuperclass();
		}

		final int p1 = cf.getContracts( c1 ).getOrDefault( type, Priorities.USER );
		final int p2 = cf.getContracts( c2 ).getOrDefault( type, Priorities.USER );

		return Integer.compare( p1, p2 );
	}

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

	static <A extends Annotation> Optional<A> findAnnotation( Class<A> type, Class<?> cls )
	{
		if( cls == null || cls == Object.class ) {
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
