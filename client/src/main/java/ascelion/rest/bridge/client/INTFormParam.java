
package ascelion.rest.bridge.client;

import javax.ws.rs.FormParam;

final class INTFormParam extends INTParamBase<FormParam>
{

	INTFormParam( FormParam annotation, RestParam param )
	{
		super( annotation, param );
	}

	@Override
	void visitAnnotationValue( RestRequestContextImpl rc, Object v )
	{
		rc.form( this.annotation.value(), this.param.cvt.toString( v ) );
	}
}
