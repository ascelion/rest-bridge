
package ascelion.rest.bridge.client;

import javax.ws.rs.Consumes;

class ConsumesAction
extends AnnotationAction<Consumes>
{

	ConsumesAction( Consumes a, int index )
	{
		super( new ActionParam( index ), a );
	}

	@Override
	public void execute( RestRequest req )
	{
		req.consumes( this.annotation.value() );
	}
}
