
package ascelion.rest.bridge.client;

import ascelion.utils.chain.AroundInterceptor;

public interface RestRequestInterceptor extends AroundInterceptor<RestRequestContext>
{

	interface Factory
	{

		Iterable<RestRequestInterceptor> create( RestMethodInfo mi );
	}

	int PRIORITY_WIDTH = 1000;

	int PRIORITY_TAIL = -PRIORITY_WIDTH;
	int PRIORITY_INVOCATION = PRIORITY_TAIL - PRIORITY_WIDTH;
	int PRIORITY_PARAMETERS = PRIORITY_INVOCATION - PRIORITY_WIDTH;
	int PRIORITY_VALIDATION = PRIORITY_PARAMETERS - PRIORITY_WIDTH;
	int PRIORITY_ASYNC = PRIORITY_VALIDATION - PRIORITY_WIDTH;
	int PRIORITY_HEAD = PRIORITY_ASYNC - PRIORITY_WIDTH;

	@Override
	int priority();
}
