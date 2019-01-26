
package ascelion.rest.bridge.client;

import java.lang.annotation.Annotation;

public interface ConvertersFactory
{

	<T> LazyParamConverter<T> getConverter( Class<T> type, Annotation[] annotations );

}
