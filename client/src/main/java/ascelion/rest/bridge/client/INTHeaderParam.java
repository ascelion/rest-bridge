
package ascelion.rest.bridge.client;

import javax.ws.rs.HeaderParam;

final class INTHeaderParam extends INTParamBase<HeaderParam>
{

	INTHeaderParam( HeaderParam annotation, RestParam param )
	{
		super( annotation, param );
	}

	@Override
	protected void before( RestRequestContext rc )
	{
		rc.header( this.annotation.value(), null );

		super.before( rc );
	}

	@Override
	void visitAnnotationValue( RestRequestContext rc, Object v )
	{
		final String s = this.param.cvt.toString( v );

		if( s != null ) {
			rc.header( this.annotation.value(), s );
		}
	}

	@Override
	String aboutParam()
	{
		return this.annotation != null ? this.annotation.value() : null;
	}
}
