
package ascelion.rest.bridge.client;

import javax.ws.rs.CookieParam;
import javax.ws.rs.core.Cookie;

class CookieParamAction
extends AnnotationAction<CookieParam>
{

	CookieParamAction( CookieParam annotation, int ix )
	{
		super( annotation, ix );
	}

	@Override
	public void execute( final RestContext cx )
	{
		visitCollection( cx );
	}

	@Override
	void visitElement( RestContext cx, Object v )
	{
		if( v instanceof Cookie ) {
			final Cookie c = (Cookie) v;

			cx.cookies.add( new Cookie( this.annotation.value(), c.getValue(), c.getPath(), c.getDomain(), c.getVersion() ) );
		}
		else {
			cx.cookies.add( new Cookie( this.annotation.value(), v.toString() ) );
		}
	}
}
