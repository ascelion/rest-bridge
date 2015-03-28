
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.client.WebTarget;

final class Util
{

	static WebTarget addPathFromAnnotation( final AnnotatedElement ae, WebTarget target )
	{
		final Path p = ae.getAnnotation( Path.class );

		if( p != null ) {
			target = target.path( p.value() );
		}

		return target;
	}

	static Iterable<Field> getDeclaredFields( Class<?> cls )
	{
		final Collection<Field> fields = new ArrayList<Field>();

		addDeclaredFields( cls, fields );

		return fields;
	}

	static String getHttpMethod( Method method )
	{
		String httpMethod = getHttpMethodName( method );

		if( httpMethod != null ) {
			return httpMethod;
		}

		for( final Annotation ann : method.getAnnotations() ) {
			httpMethod = getHttpMethodName( ann.annotationType() );

			if( httpMethod != null ) {
				return httpMethod;
			}
		}

		return null;
	}

	private static void addDeclaredFields( Class<?> cls, Collection<Field> fields )
	{
		if( cls == Object.class ) {
			return;
		}

		addDeclaredFields( cls.getSuperclass(), fields );

		for( final Field field : cls.getDeclaredFields() ) {
			final int m = field.getModifiers();

			if( Modifier.isStatic( m ) ) {
				continue;
			}

			field.setAccessible( true );

			fields.add( field );
		}
	}

	private static String getHttpMethodName( AnnotatedElement element )
	{
		final HttpMethod annotation = element.getAnnotation( HttpMethod.class );

		if( annotation != null ) {
			return annotation.value();
		}

		return null;
	}

	private Util()
	{
	}
}
