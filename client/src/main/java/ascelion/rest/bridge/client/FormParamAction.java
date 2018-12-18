
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
	public RestRequest execute( RestRequest req )
	{
		visitCollection( req );

		return req;
	}

	@Override
	void visitElement( RestRequest req, Object v )
	{
		req.form( this.annotation.value(), this.param.converter.apply( v ) );
	}

}
