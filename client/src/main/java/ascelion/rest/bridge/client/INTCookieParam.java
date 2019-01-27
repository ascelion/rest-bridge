
package ascelion.rest.bridge.client;

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;

class INTCookieParam extends INTParamBase<CookieParam>
{

	INTCookieParam( CookieParam annotation, RestParam param )
	{
		super( annotation, param );
	}

	@Override
	void visitAnnotationValue( RestRequestContext rc, Object value )
	{
		if( value instanceof Cookie ) {
			final Cookie c = (Cookie) value;

			rc.cookie( new Cookie( this.annotation.value(), c.getValue(), c.getPath(), c.getDomain(), c.getVersion() ) );
		}
		else {
			rc.cookie( new Cookie( this.annotation.value(), this.param.cvt.toString( value ) ) );
		}
	}

	@Override
	String aboutParam()
	{
		return this.annotation != null ? this.annotation.value() : null;
	}
}
