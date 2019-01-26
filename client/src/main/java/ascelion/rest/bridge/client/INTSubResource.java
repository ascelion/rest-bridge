
package ascelion.rest.bridge.client;

import ascelion.utils.chain.InterceptorChainContext;
import ascelion.utils.chain.InterceptorChainWrapper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class INTSubResource implements InterceptorChainWrapper<RestRequestContext>
{

	private final RestClientData rcd;
	private final Class<?> resourceType;

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		final RestClientData subrcd = new RestClientData( this.resourceType, this.rcd, () -> context.getData().getTarget() );
		final RestClientIH inv = new RestClientIH( subrcd );

		return inv.newProxy();
	}

}
