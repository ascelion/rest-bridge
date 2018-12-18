
package ascelion.rest.bridge.client;

import java.util.concurrent.Callable;

class SubresourceAction
extends Action
{

	private final Class<?> resourceType;
	private final ConvertersFactory cvsf;

	SubresourceAction( int index, Class<?> resourceType, ConvertersFactory cvsf )
	{
		super( new ActionParam( index ) );

		this.resourceType = resourceType;
		this.cvsf = cvsf;
	}

	@Override
	Callable<?> execute( RestRequest req )
	{
		final RestClientIH inv = new RestClientIH( this.resourceType, this.cvsf, () -> req.target );

		return () -> inv.newProxy();
	}
}
