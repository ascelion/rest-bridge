
package ascelion.rest.bridge.client;

import java.util.concurrent.CompletableFuture;

import ascelion.utils.chain.InterceptorChainContext;

final class INTAsync implements RestRequestInterceptor
{

	static final int PRIORITY = -3000;

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		final RestRequestContext rc = (RestRequestContext) context.getData();
		final Object ais = rc.rcd.aint.prepare();
		final CompletableFuture<Object> fut = new CompletableFuture<>();

		rc.rcd.exec.execute( () -> invokeAsync( context, rc, ais, fut ) );

		return fut;
	}

	@Override
	public int priority()
	{
		return PRIORITY;
	}

	private void invokeAsync( InterceptorChainContext<RestRequestContext> cx, RestRequestContext rc, Object ais, CompletableFuture<Object> fut )
	{
		rc.rcd.aint.before( ais );

		try {
			fut.complete( cx.proceed() );
		}
		catch( final Throwable t ) {
			fut.completeExceptionally( t );
		}
		finally {
			rc.rcd.aint.after( ais );
		}
	}
}
