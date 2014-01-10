package ascelion.rest.bridge.client;

import javax.ws.rs.FormParam;

class FormParamAction
extends AnnotationAction<FormParam>
{

	FormParamAction( FormParam annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
		RestMethod.collection( cx, v -> cx.form.param( this.annotation.value(), v.toString() ) );
	}

}

