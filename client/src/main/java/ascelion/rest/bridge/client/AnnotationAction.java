package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;

import ascelion.rest.bridge.client.Action.Priority;

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
}

