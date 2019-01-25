
package ascelion.rest.bridge.client;

import ascelion.rest.bridge.client.InterceptorChain.Context;

final class INTAsync implements InterceptorChain.Interceptor<RestRequestContextImpl>
{

	static final int PRIORITY = -3000;

	@Override
	public Object around( Context<RestRequestContextImpl> context ) throws Exception
	{
		final RestRequestContextImpl rc = context.getData();

		return null;
	}

	@Override
	public int priority()
	{
		return PRIORITY;
	}
}
