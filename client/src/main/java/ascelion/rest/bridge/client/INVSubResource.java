
package ascelion.rest.bridge.client;

import ascelion.utils.chain.InterceptorChainContext;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class INVSubResource implements RestRequestInterceptor
{

	private final RestClientInternals rci;
	private final Class<?> resourceType;

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		final RestRequestContext rc = context.getData();
		final RestClientInfo rci = new RestClientInfoImpl( rc, () -> rc.getReqTarget() );
		final RestServiceInfo rsi = new RestServiceInfo( rci, this.resourceType );
		final RestService inv = new RestService( rsi, this.rci );

		return inv.newProxy();
	}

	@Override
	public int priority()
	{
		return PRIORITY_INVOCATION;
	}

}
