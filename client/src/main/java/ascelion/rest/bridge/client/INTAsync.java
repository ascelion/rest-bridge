
package ascelion.rest.bridge.client;

import ascelion.rest.bridge.client.InterceptorChain.Context;

final class INTAsync implements InterceptorChain.Interceptor<RestRequestContext>
{

	static final int PRIORITY = -3000;

	@Override
	public Object around( Context<RestRequestContext> context ) throws Exception
	{
		final RestRequestContext rc = context.getData();

		return null;
	}

	@Override
	public int priority()
	{
		return PRIORITY;
	}
}
