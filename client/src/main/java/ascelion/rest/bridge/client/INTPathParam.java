
package ascelion.rest.bridge.client;

import javax.ws.rs.PathParam;

final class INTPathParam extends INTParamBase<PathParam>
{

	INTPathParam( PathParam annotation, RestParam param )
	{
		super( annotation, param );
	}

	@Override
	void visitAnnotationValue( RestRequestContext rc, Object v )
	{
		rc.path( this.annotation.value(), this.param.cvt.toString( v ) );
	}

	@Override
	String aboutParam()
	{
		return this.annotation != null ? this.annotation.value() : null;
	}
}
