
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static java.lang.String.format;

abstract class AnnotationAction<A extends Annotation>
extends Action
{

	final A annotation;

	AnnotationAction( ActionParam param, A annotation )
	{
		super( param );

		this.annotation = annotation;
	}

	@Override
	String inspect()
	{
		return format( "%s, annotation = %s", super.inspect(), this.annotation );
	}

	final <T> Collection<T> visitCollection( RestRequest<?> req )
	{
		final Object value = this.param.currentValue( req );

		final Collection<T> c;

		if( value instanceof Collection ) {
			c = (Collection<T>) value;
		}
		else if( value instanceof Object[] ) {
			c = Arrays.asList( (T[]) value );
		}
		else if( value != null ) {
			c = Arrays.asList( (T) value );
		}
		else {
			c = Collections.EMPTY_LIST;
		}

		for( final T t : c ) {
			visitElement( req, t );
		}

		return c;
	}

	<T> void visitElement( RestRequest<?> req, T t )
	{
	}
}
