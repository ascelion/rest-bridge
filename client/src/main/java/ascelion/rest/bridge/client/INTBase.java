
package ascelion.rest.bridge.client;

import ascelion.rest.bridge.client.InterceptorChain.Context;

abstract class INTBase implements InterceptorChain.Interceptor<RestRequestContext>
{

	static final int PRIORITY_HEAD = -2000;
	static final int PRIORITY_TAIL = +2000;

	@Override
	public final Object around( Context<RestRequestContext> context ) throws Exception
	{
		final RestRequestContextImpl rc = (RestRequestContextImpl) context.getData();

		before( rc );

		Object result = null;
		Exception exception = null;

		try {
			return result = context.proceed();
		}
		catch( final Exception e ) {
			throw exception = e;
		}
		finally {
			after( rc, result, exception );
		}
	}

	void before( RestRequestContextImpl rc )
	{
	}

	void after( RestRequestContextImpl rc, Object result, Exception exception )
	{
	}
}
