
package ascelion.rest.bridge.client;

import javax.ws.rs.Produces;

class ProducesAction
extends AnnotationAction<Produces>
{

	ProducesAction( Produces a, int index )
	{
		super( new ActionParam( index ), a );
	}

	@Override
	public void execute( RestRequest cx )
	{
		cx.accepts = this.annotation.value();
	}

}
