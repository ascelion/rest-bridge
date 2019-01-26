
package ascelion.rest.bridge.client;

import ascelion.utils.chain.InterceptorChainContext;

public abstract class RestRequestInterceptorBase implements RestRequestInterceptor
{

	static final int PRIORITY_HEAD = -2000;
	static final int PRIORITY_TAIL = +2000;

	@Override
	public final Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		final RestRequestContext rc = (RestRequestContext) context.getData();

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

	protected void before( RestRequestContext rc )
	{
	}

	protected void after( RestRequestContext rc, Object result, Exception exception )
	{
	}
}
