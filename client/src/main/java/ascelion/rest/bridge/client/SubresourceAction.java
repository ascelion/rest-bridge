
package ascelion.rest.bridge.client;

import java.util.concurrent.Callable;

class SubresourceAction
extends Action
{

	private final Class<?> resourceType;
	private final RestClientData rcd;

	SubresourceAction( int index, Class<?> resourceType, RestClientData rcd )
	{
		super( new ActionParam( index ) );

		this.resourceType = resourceType;
		this.rcd = rcd;
	}

	@Override
	Callable<?> execute( RestRequest<?> req )
	{
		final RestClientIH inv = new RestClientIH( new RestClientData( this.resourceType, this.rcd, () -> req.getTarget() ) );

		return () -> inv.newProxy();
	}
}
