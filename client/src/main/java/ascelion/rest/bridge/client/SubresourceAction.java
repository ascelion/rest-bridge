
package ascelion.rest.bridge.client;

import java.util.concurrent.Callable;

class SubresourceAction
extends Action
{

	private final Class<?> resourceType;
	private final RestBridgeType rbt;

	SubresourceAction( int index, Class<?> resourceType, RestBridgeType rbt )
	{
		super( new ActionParam( index ) );

		this.resourceType = resourceType;
		this.rbt = rbt;
	}

	@Override
	Callable<?> execute( RestRequest req )
	{
		final RestClientIH inv = new RestClientIH( new RestBridgeType( this.resourceType, this.rbt, () -> req.getTarget() ) );

		return () -> inv.newProxy();
	}
}
