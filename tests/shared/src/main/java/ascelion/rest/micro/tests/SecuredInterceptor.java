
package ascelion.rest.micro.tests;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.ClassUtils.hierarchy;
import static org.apache.commons.lang3.reflect.MethodUtils.getOverrideHierarchy;

import org.apache.commons.lang3.ClassUtils.Interfaces;

@Secured
@Interceptor
public class SecuredInterceptor
{

	static public List<String> invoked = new ArrayList<>();

	@AroundInvoke
	public Object check( InvocationContext ctx ) throws Exception
	{
		findAnnotations( Secured.class, ctx.getMethod(), ctx.getTarget().getClass() )
			.stream()
			.map( Secured::value )
			.forEach( invoked::add );

		return ctx.proceed();
	}

	static private <A extends Annotation> Set<A> findAnnotations( Class<A> type, Method method, Class<?> base )
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
}
