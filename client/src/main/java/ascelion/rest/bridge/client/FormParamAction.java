
package ascelion.rest.bridge.client;

import javax.ws.rs.FormParam;

class FormParamAction
extends AnnotationAction<FormParam>
{

	FormParamAction( FormParam a, ActionParam p )
	{
		super( p, a );
	}

	@Override
	public void execute( RestRequest cx )
	{
		visitCollection( cx );
	}

	@Override
	void visitElement( RestRequest req, Object v )
	{
		req.form( this.annotation.value(), this.param.converter.apply( v ) );
	}

}
