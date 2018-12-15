
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
	public void execute( RestRequest cx )
	{
		if( this.annotation.value().length > 0 ) {
			cx.contentType = this.annotation.value()[0];
		}
	}
}
