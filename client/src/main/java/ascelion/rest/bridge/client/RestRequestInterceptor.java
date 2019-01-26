
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;

import ascelion.utils.chain.InterceptorChainWrapper;

public interface RestRequestInterceptor extends InterceptorChainWrapper<RestRequestContext>
{

	interface Factory
	{

		RestRequestInterceptor[] create( Class<?> type, Method method );
	}
}
