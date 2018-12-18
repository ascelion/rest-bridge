
package ascelion.rest.bridge.client;

import javax.ws.rs.MatrixParam;

class MatrixParamAction
extends AnnotationAction<MatrixParam>
{

	MatrixParamAction( MatrixParam a, ActionParam p )
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
		req.matrix( this.annotation.value(), this.param.converter.apply( v ) );
	}
}
