
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.ws.rs.DefaultValue;

class ActionParam
{

	final int index;
	final Class<?> type;
	private final Function<RestRequest<?>, Object> supplier;
	private final String defaultValue;
	final Function<Object, String> converter;

	ActionParam( int index )
	{
		this( index, null, new Annotation[0], o -> {
			throw new UnsupportedOperationException( "TODO (no supplier)" );
		} );
	}

	ActionParam( int index, Class<?> type, Annotation[] annotations, Function<RestRequest<?>, Object> supplier )
	{
		this( index, type, annotations, supplier, o -> {
			throw new UnsupportedOperationException( "TODO (no converter)" );
		} );
	}

	ActionParam( int index, Class<?> type, Annotation[] annotations, Function<RestRequest<?>, Object> supplier, Function<Object, String> converter )
	{
		this.index = index;
		this.type = type;
		this.supplier = supplier;
		this.converter = converter;

		this.defaultValue = Stream.of( annotations )
			.filter( a -> a.annotationType() == DefaultValue.class )
			.map( DefaultValue.class::cast )
			.map( DefaultValue::value )
			.findFirst().orElse( null );
	}

	Object currentValue( RestRequest<?> req )
	{
		final Object val = this.supplier.apply( req );

		return val != null ? val : this.defaultValue;
	}
}
