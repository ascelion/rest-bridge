
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

abstract class AnnotationAction<A extends Annotation>
extends Action
{

	final A annotation;

	AnnotationAction( A annotation, int ix )
	{
		super( ix );

		this.annotation = annotation;
	}

	AnnotationAction( A annotation, int ix, Priority px )
	{
		super( ix, px );

		this.annotation = annotation;
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();

		builder.append( getClass().getSimpleName() );
		builder.append( "[ix=" );
		builder.append( this.ix );
		builder.append( ", px=" );
		builder.append( this.px );
		builder.append( ", annotation=" );
		builder.append( this.annotation );
		builder.append( "]" );

		return builder.toString();
	}

	<T> Collection<T> visitCollection( RestContext cx )
	{
		final Collection<T> c;

		if( cx.parameterValue instanceof Collection ) {
			c = (Collection<T>) cx.parameterValue;
		}
		else if( cx.parameterValue instanceof Object[] ) {
			c = Arrays.asList( (T[]) cx.parameterValue );
		}
		else if( cx.parameterValue != null ) {
			c = Arrays.asList( (T) cx.parameterValue );
		}
		else {
			c = Collections.EMPTY_LIST;
		}

		for( final T t : c ) {
			visitElement( cx, t );
		}

		return c;
	}

	<T> void visitElement( RestContext cx, T t )
	{

	}
}
