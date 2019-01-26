
package ascelion.rest.bridge.client;

import javax.ws.rs.ext.ParamConverter;

public interface LazyParamConverter<T> extends ParamConverter<T>
{

	default boolean isLazy()
	{
		return getClass().isAnnotationPresent( Lazy.class );
	}
}
