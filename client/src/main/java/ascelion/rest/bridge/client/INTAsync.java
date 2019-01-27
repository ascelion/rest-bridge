
package ascelion.rest.bridge.client;

import java.util.concurrent.CompletableFuture;

import ascelion.utils.chain.InterceptorChainContext;

final class INTAsync implements RestRequestInterceptor
{

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		final RestRequestContext rc = context.getData();
		final Object ais = rc.getAsyncInterceptor().prepare();
		final CompletableFuture<Object> fut = new CompletableFuture<>();

		rc.getExecutor().execute( () -> invokeAsync( context, rc, ais, fut ) );

		return fut;
	}

	@Override
	public int priority()
	{
		return PRIORITY_ASYNC;
	}

	private void invokeAsync( InterceptorChainContext<RestRequestContext> cx, RestRequestContext rc, Object ais, CompletableFuture<Object> fut )
	{
		final AsyncInterceptor<Object> asyi = (AsyncInterceptor<Object>) rc.getAsyncInterceptor();

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
