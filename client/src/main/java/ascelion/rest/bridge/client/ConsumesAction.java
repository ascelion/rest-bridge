package ascelion.rest.bridge.client;

import javax.ws.rs.Consumes;

class ConsumesAction
extends AnnotationAction<Consumes>
{

	ConsumesAction( Consumes annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
		if( this.annotation.value().length > 0 ) {
			cx.contentType = this.annotation.value()[0];
		}
	}
}

