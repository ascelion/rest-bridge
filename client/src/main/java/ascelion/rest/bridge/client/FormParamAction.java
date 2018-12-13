
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
	public void execute( final RestContext cx )
	{
		visitCollection( cx );
	}

	@Override
	void visitElement( RestContext cx, Object v )
	{
		cx.form.param( this.annotation.value(), v.toString() );
	}

}
