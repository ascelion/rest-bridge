package ascelion.rest.bridge.client;

import javax.ws.rs.HeaderParam;

class HeaderParamAction
extends AnnotationAction<HeaderParam>
{

	HeaderParamAction( HeaderParam annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
		RestMethod.collection( cx, v -> cx.headers.add( this.annotation.value(), v ) );
	}

}

