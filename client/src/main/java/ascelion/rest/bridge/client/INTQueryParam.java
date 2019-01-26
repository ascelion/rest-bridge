
package ascelion.rest.bridge.client;

import javax.ws.rs.QueryParam;

final class INTQueryParam extends INTParamBase<QueryParam>
{

	INTQueryParam( QueryParam annotation, RestParam param )
	{
		super( annotation, param );
	}

	@Override
	void visitAnnotationValue( RestRequestContext rc, Object v )
	{
		rc.query( this.annotation.value(), this.param.cvt.toString( v ) );
	}
}
