
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.security.PrivilegedActionException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import javax.annotation.Priority;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

import static ascelion.utils.etc.Secured.runPrivileged;
import static ascelion.utils.etc.Secured.runPrivilegedWithException;
import static io.leangen.geantyref.GenericTypeReflector.getExactReturnType;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.ClassUtils.hierarchy;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;
import static org.apache.commons.lang3.reflect.MethodUtils.getOverrideHierarchy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ClassUtils.Interfaces;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
public final class RBUtils
{

	static public ClassLoader currentClassLoader()
	{
		return runPrivileged( () -> {
			return ofNullable( currentThread().getContextClassLoader() )
				.orElse( RBUtils.class.getClassLoader() );
		} );
	}

	static public <T> Class<T> safeLoadClass( String name )
	{
		try {
			return (Class<T>) runPrivilegedWithException( () -> currentClassLoader().loadClass( name.trim() ) );
		}
		catch( final PrivilegedActionException e ) {
			final Throwable c = e.getCause();

			if( c instanceof ClassNotFoundException ) {
				return null;
			}

			throw wrapException( c );
		}
	}

	static public <T> Class<T> rtLoadClass( String name )
	{
		try {
			return runPrivilegedWithException( () -> (Class<T>) currentClassLoader().loadClass( name.trim() ) );
		}
		catch( final PrivilegedActionException e ) {
			throw wrapException( e.getCause() );
		}
	}

	static public Charset charset( MediaType mt )
	{
		final String cs = ofNullable( mt )
			.map( MediaType::getParameters )
			.map( m -> m.get( "charset" ) )
			.orElse( "UTF-8" );

		return Charset.forName( cs );
	}

	static public int getPriority( Object t )
	{
		final Class<?> cls = t instanceof Class ? (Class<?>) t : t.getClass();

		return Optional.ofNullable( cls.getAnnotation( Priority.class ) )
			.map( Priority::value )
			.orElse( Priorities.USER );
	}

	static public int getPriority( Class<?> value, int priority )
	{
		return priority == -1 ? getPriority( value ) : priority;
	}

	public static boolean isCDI()
	{
		try {
			CDI.current().getBeanManager();

			return true;
		}
		catch( final NoClassDefFoundError e ) {
			return false;
		}
		catch( final IllegalStateException e ) {
			return false;
		}
	}

	static public <T> T newInstance( Class<T> type )
	{
		return newInstance( type, null );
	}

	static public <T> T newInstance( Class<T> type, Supplier<T> sup )
	{
		try {
			final BeanManager bm = CDI.current().getBeanManager();
			final Annotation[] quals = stream( type.getAnnotations() )
				.map( Annotation::annotationType )
				.filter( bm::isQualifier ).toArray( Annotation[]::new );

			final Set<Bean<?>> beans = bm.getBeans( type, quals );

			switch( beans.size() ) {
				case 0:
				break;

				case 1: {
					final Bean<?> bean = beans.iterator().next();
					final CreationalContext<?> cc = bm.createCreationalContext( bean );

					return (T) bm.getReference( bean, type, cc );
				}

				default:
					// TODO
					throw new AmbiguousResolutionException( "Ambigous: " + type.getName() );
			}
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
			return sup != null ? sup.get() : type.newInstance();
		}
		catch( InstantiationException | IllegalAccessException e ) {
			throw new RestClientException( "Cannot instantiate type " + type.getName() );
		}
	}

	static public boolean isTextContent( MediaType mt )
	{
		return ( mt != null ) &&
			( mt.equals( MediaType.APPLICATION_FORM_URLENCODED_TYPE )
				|| mt.getType().equals( "text" )
				|| mt.getSubtype().contains( "xml" )
				|| mt.getSubtype().contains( "json" ) );
	}

	static public <T> GenericType<T> genericType( Class<?> type, Method method )
	{
		final Type rt = getExactReturnType( method, type );
		final GenericType<?> gt = new GenericType<>( rt );

		if( gt.getRawType() == CompletionStage.class ) {
			return new GenericType<>( ( (ParameterizedType) gt.getType() ).getActualTypeArguments()[0] );
		}
		else {
			return new GenericType<>( rt );
		}
	}

	static public String getExpression( String value )
	{
		return value.length() > 2 && value.startsWith( "{" ) && value.endsWith( "}" )
			? value.substring( 1, value.length() - 1 )
			: null;
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

	static public <A extends Annotation> Optional<A> findAnnotation( Class<A> type, Class<?> base )
	{
		if( base == null || base == Object.class ) {
			return Optional.empty();
		}

		final A a = base.getAnnotation( type );

		if( a != null ) {
			return Optional.of( a );
		}

		return findAnnotation( type, base.getSuperclass() );
	}

	static public <A extends Annotation> Optional<A> findAnnotation( Class<A> type, Member memb, Class<?> base )
	{
		if( type == null || (Class) type == Object.class ) {
			return Optional.empty();
		}

		if( memb instanceof AnnotatedElement ) {
			final Optional<A> o = ofNullable( ( (AnnotatedElement) memb ).getAnnotation( type ) );

			if( o.isPresent() ) {
				return o;
			}
		}

		return findAnnotation( type, base );
	}

	static public <A extends Annotation> Set<A> findAnnotations( Class<A> type, Method method, Class<?> base )
	{
		final Map<AnnotatedElement, Collection<A>> result = new LinkedHashMap<>();

		findAnnotations( type, method, base, result );

		return result.values().stream().flatMap( Collection::stream ).collect( toCollection( LinkedHashSet::new ) );
	}

	static private <A extends Annotation> void findAnnotations( Class<A> type, Method method, Class<?> base, Map<AnnotatedElement, Collection<A>> result )
	{
		getOverrideHierarchy( method, Interfaces.INCLUDE ).stream()
			.forEach( m -> {
				result.put( m, asList( m.getAnnotationsByType( type ) ) );

				stream( m.getAnnotations() )
					.map( Annotation::annotationType )
					.filter( t -> t != type )
					.forEach( t -> findAnnotations( type, t, result ) );
			} );

		findAnnotations( type, base, result );
	}

	static private <A extends Annotation> void findAnnotations( Class<A> type, Class<?> base, Map<AnnotatedElement, Collection<A>> result )
	{
		hierarchy( base, Interfaces.INCLUDE )
			.forEach( c -> {
				if( c != Object.class && !result.containsKey( c ) ) {
					result.put( c, asList( c.getAnnotationsByType( type ) ) );

					stream( c.getAnnotations() )
						.map( Annotation::annotationType )
						.filter( t -> t != type )
						.forEach( t -> findAnnotations( type, t, result ) );
				}
			} );
	}

	static Set<String> pathParameters( String path )
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

	static String getHttpMethod( Method method )
	{
		return getOverrideHierarchy( method, Interfaces.INCLUDE ).stream()
			.map( RBUtils::httpMethodOf )
			.filter( Objects::nonNull )
			.findFirst()
			.orElse( null );
	}

	public static int comparePriorities( Configuration cf, Class<?> type, Class<?> c1, Class<?> c2 )
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

	static public String getRequestURI( Path annotation )
	{
		if( annotation == null ) {
			return "";
		}

		return cleanRequestURI( annotation.value() );
	}

	static public String cleanRequestURI( String val )
	{
		val = trimToEmpty( val );

		if( val.isEmpty() ) {
			return val;
		}

		final StringBuilder sb = new StringBuilder( val );

		while( sb.length() > 0 && sb.charAt( 0 ) == '/' ) {
			sb.delete( 0, 1 );
		}

		int z;

		while( ( z = sb.length() ) > 0 && sb.charAt( z - 1 ) == '/' ) {
			sb.delete( z - 1, z );
		}

		final String path = sb.toString().replaceAll( "/{2,}", "/" );

		return path.isEmpty() ? "" : "/" + path;
	}

	private static String getHttpMethodName( AnnotatedElement element )
	{
		return ofNullable( element.getAnnotation( HttpMethod.class ) )
			.map( HttpMethod::value )
			.orElse( null );
	}

	static public RuntimeException wrapException( Throwable e )
	{
		return wrapException( e, null );
	}

	static public RuntimeException wrapException( Throwable e, String m )
	{
		if( e instanceof InvocationTargetException ) {
			return wrapException( e.getCause(), m );
		}
		if( e instanceof Error ) {
			throw(Error) e;
		}
		if( e instanceof RuntimeException ) {
			throw(RuntimeException) e;
		}

		return m != null ? new RuntimeException( m, e ) : new RuntimeException( e );
	}
}
