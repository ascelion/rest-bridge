
package ascelion.rest.bridge.client;

import javax.ws.rs.HeaderParam;

class HeaderParamAction
extends AnnotationAction<HeaderParam>
{

	HeaderParamAction( HeaderParam a, ActionParam p )
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
		req.header( this.annotation.value(), this.param.converter.apply( v ) );
	}
}
