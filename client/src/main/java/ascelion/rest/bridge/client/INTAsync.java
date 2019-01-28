
package ascelion.rest.bridge.client;

import java.util.concurrent.CompletableFuture;

import ascelion.utils.chain.InterceptorChainContext;

final class INTAsync implements RestRequestInterceptor
{

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		final RestRequestContext rc = context.getData();
		final RestMethodInfo mi = rc.getMethodInfo();
		final Object ais = mi.getAsyncInterceptor().prepare();
		final CompletableFuture<Object> fut = new CompletableFuture<>();

		mi.getExecutor().execute( () -> invokeAsync( context, rc, ais, fut ) );

		return fut;
	}

	@Override
	public int priority()
	{
		return PRIORITY_ASYNC;
	}

	private void invokeAsync( InterceptorChainContext<RestRequestContext> cx, RestRequestContext rc, Object ais, CompletableFuture<Object> fut )
	{
		final AsyncInterceptor<Object> asyi = (AsyncInterceptor<Object>) rc.getMethodInfo().getAsyncInterceptor();

		asyi.before( ais );

		try {
			fut.complete( cx.proceed() );
		}
		catch( final Throwable t ) {
			fut.completeExceptionally( t );
		}
		finally {
			asyi.after( ais );
		}
	}
}
