
package ascelion.rest.bridge.client;

import java.util.function.Function;
import java.util.function.Supplier;

import javax.ws.rs.DefaultValue;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

final class RestParam
{

	final int index;
	final LazyParamConverter<Object> cvt;
	final Supplier<Object> defValue;
	final Function<RestRequestContext, Object> argument;
	final Class<?> type;

	RestParam( int index, Class<?> type, LazyParamConverter<?> cvt, DefaultValue def, Function<RestRequestContext, Object> argument )
	{
		this.index = index;
		this.cvt = (LazyParamConverter<Object>) cvt;

		final String dstr = ofNullable( def ).map( x -> trimToNull( x.value() ) ).orElse( null );

		if( dstr != null ) {
			if( cvt.isLazy() ) {
				this.defValue = () -> this.cvt.fromString( dstr );
			}
			else {
				final Object dobj = this.cvt.fromString( dstr );

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
