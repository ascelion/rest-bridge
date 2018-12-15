
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
	public void execute( RestRequest cx )
	{
		visitCollection( cx );
	}

	@Override
	void visitElement( RestRequest req, Object v )
	{
		req.header( this.annotation.value(), this.param.converter.apply( v ) );
	}
}
