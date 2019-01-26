
package ascelion.rest.bridge.client;

import ascelion.rest.bridge.client.InterceptorChain.Context;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class INTSubResource implements InterceptorChain.Interceptor<RestRequestContext>
{

	private final RestClientData rcd;
	private final Class<?> resourceType;

	@Override
	public Object around( Context<RestRequestContext> context ) throws Exception
	{
		final RestClientData subrcd = new RestClientData( this.resourceType, this.rcd, () -> context.getData().getTarget() );
		final RestClientIH inv = new RestClientIH( subrcd );

		return inv.newProxy();
	}

}
