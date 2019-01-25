
package ascelion.rest.bridge.client;

import javax.ws.rs.HeaderParam;

final class INTHeaderParam extends INTParamBase<HeaderParam>
{

	INTHeaderParam( HeaderParam annotation, RestParam param )
	{
		super( annotation, param );
	}

	@Override
	void visitAnnotationValue( RestRequestContextImpl rc, Object v )
	{
		rc.header( this.annotation.value(), this.param.cvt.toString( v ) );
	}
}
