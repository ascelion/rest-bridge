
package ascelion.rest.bridge.client;

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;

class CookieParamAction
extends AnnotationAction<CookieParam>
{

	CookieParamAction( CookieParam a, ActionParam p )
	{
		super( p, a );
	}

	@Override
	public RestRequest<?> execute( final RestRequest<?> req )
	{
		visitCollection( req );

		return req;
	}

	@Override
	<T> void visitElement( RestRequest<?> req, T v )
	{
		if( v instanceof Cookie ) {
			final Cookie c = (Cookie) v;

			req.rc.cookie( new Cookie( this.annotation.value(), c.getValue(), c.getPath(), c.getDomain(), c.getVersion() ) );
		}
		else {
			req.rc.cookie( new Cookie( this.annotation.value(), this.param.converter.apply( v ) ) );
		}
	}
}
