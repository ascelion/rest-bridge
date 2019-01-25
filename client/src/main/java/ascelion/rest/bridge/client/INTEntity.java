
package ascelion.rest.bridge.client;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
final class INTEntity extends INTBase
{

	private final RestParam param;

	@Override
	void before( RestRequestContextImpl rc )
	{
		rc.entity( this.param.argument.apply( rc ) );
	}
}
