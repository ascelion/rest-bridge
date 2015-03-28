
package ascelion.rest.bridge.client;

import javax.ws.rs.QueryParam;

class QueryParamAction
extends AnnotationAction<QueryParam>
{

	QueryParamAction( QueryParam annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( RestContext cx )
	{
		cx.target = cx.target.queryParam( this.annotation.value(), visitCollection( cx ).toArray() );
	}
}
