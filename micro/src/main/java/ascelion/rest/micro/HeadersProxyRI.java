
package ascelion.rest.micro;

import javax.ws.rs.core.HttpHeaders;

import ascelion.rest.bridge.client.RestRequestContext;
import ascelion.rest.bridge.client.RestRequestInterceptor;
import ascelion.utils.chain.InterceptorChainContext;

final class HeadersProxyRI implements RestRequestInterceptor
{

	private final ThreadLocalValue<HttpHeaders> headers = ThreadLocalProxy.create( HttpHeaders.class );

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		final boolean absent = this.headers.isAbsent();

		if( absent ) {
			this.headers.set( new HttpHeadersImpl( context.getData() ) );
		}

		try {
			return context.proceed();
		}
		finally {
			if( absent ) {
				this.headers.set( null );
			}
		}
	}

	@Override
	public int priority()
	{
		return PRIORITY_ASYNC + 1;
	}
}
