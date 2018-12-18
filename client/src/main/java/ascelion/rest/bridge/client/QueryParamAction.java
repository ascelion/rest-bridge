
package ascelion.rest.bridge.client;

import javax.ws.rs.QueryParam;

class QueryParamAction
extends AnnotationAction<QueryParam>
{

	QueryParamAction( QueryParam a, ActionParam p )
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
		req.query( this.annotation.value(), this.param.converter.apply( v ) );
	}
}
