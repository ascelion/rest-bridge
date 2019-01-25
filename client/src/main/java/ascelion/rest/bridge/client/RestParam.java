
package ascelion.rest.bridge.client;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.ext.ParamConverter;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

final class RestParam
{

	final int index;
	final ParamConverter<Object> cvt;
	final Supplier<Object> defValue;
	final Function<RestRequestContext, Object> argument;
	final Class<?> type;

	RestParam( int index, Class<?> type, ParamConverter<Object> cvt, DefaultValue def, Function<RestRequestContext, Object> argument )
	{
		this.index = index;
		this.cvt = cvt;

		final String dstr = ofNullable( def ).map( x -> trimToNull( x.value() ) ).orElse( null );

		if( dstr != null ) {
			final boolean lazy = RBUtils.findAnnotation( ParamConverter.Lazy.class, cvt.getClass() ).map( a -> true ).orElse( false );

			if( lazy ) {
				this.defValue = () -> cvt.fromString( dstr );
			}
			else {
				final Object dobj = cvt.fromString( dstr );

				this.defValue = () -> dobj;
			}
		}
		else {
			this.defValue = () -> null;
		}

		this.argument = argument;
		this.type = type;
	}

}
