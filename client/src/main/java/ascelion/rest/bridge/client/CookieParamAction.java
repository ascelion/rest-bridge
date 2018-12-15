
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
	public void execute( final RestRequest cx )
	{
		visitCollection( cx );
	}

	@Override
	void visitElement( RestRequest req, Object v )
	{
		if( v instanceof Cookie ) {
			final Cookie c = (Cookie) v;

			req.cookies.add( new Cookie( this.annotation.value(), c.getValue(), c.getPath(), c.getDomain(), c.getVersion() ) );
		}
		else {
			req.cookies.add( new Cookie( this.annotation.value(), this.param.converter.apply( v ) ) );
		}
	}
}
