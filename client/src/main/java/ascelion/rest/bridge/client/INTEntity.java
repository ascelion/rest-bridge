
package ascelion.rest.bridge.client;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class INTEntity extends RestRequestInterceptorBase
{

	private final RestParam param;

	@Override
	protected void before( RestRequestContext rc )
	{
		rc.entity( this.param.argument.apply( rc ) );
	}
}
