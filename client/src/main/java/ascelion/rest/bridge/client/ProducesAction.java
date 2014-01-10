package ascelion.rest.bridge.client;

import javax.ws.rs.Produces;

class ProducesAction
extends AnnotationAction<Produces>
{

	ProducesAction( Produces annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
		cx.accepts = this.annotation.value();
	}

}

