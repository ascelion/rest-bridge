
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;

import static java.lang.String.format;
import static java.util.Arrays.asList;

import lombok.SneakyThrows;

abstract class INTParamBase<A extends Annotation> extends INTAnnotBase<A>
{

	final RestParam param;

	@SneakyThrows
	INTParamBase( A annotation, RestParam param )
	{
		super( annotation );

		this.param = param;
	}

	@Override
	protected void before( RestRequestContext rc )
	{
		final Object value = this.param.argument.apply( rc );
		final Collection<Object> values = new ArrayList<>();

		if( value instanceof Collection ) {
			values.addAll( (Collection<?>) value );
		}
		else if( value instanceof Object[] ) {
			values.addAll( asList( (Object[]) value ) );
		}
		else if( value != null ) {
			values.add( value );
		}

		if( values.isEmpty() ) {
			final Object dv = this.param.defValue.get();

			if( dv != null ) {
				values.add( dv );
			}
		}

		for( final Object v : values ) {
			if( v != null ) {
				visitAnnotationValue( rc, v );
			}
		}
	}

	void visitAnnotationValue( RestRequestContext rc, Object v )
	{
	}

	@Override
	public String about()
	{
		return format( "%s(%s)", getClass().getSimpleName(), aboutParam() );
	}

	abstract String aboutParam();

	@Override
	public int priority()
	{
		return PRIORITY_PARAMETERS;
	}
}
