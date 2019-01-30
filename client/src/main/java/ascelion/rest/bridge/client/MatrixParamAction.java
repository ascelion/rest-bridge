
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
	public RestRequest<?> execute( RestRequest<?> req )
	{
		visitCollection( req );

		return req;
	}

	@Override
	<T> void visitElement( RestRequest<?> req, T v )
	{
		req.rc.matrix( this.annotation.value(), this.param.converter.apply( v ) );
	}
}
