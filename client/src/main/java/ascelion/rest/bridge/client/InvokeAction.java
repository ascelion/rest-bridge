
package ascelion.rest.bridge.client;

import java.lang.reflect.Type;
import java.net.URI;

import javax.ws.rs.RedirectionException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

class InvokeAction
extends Action
{

	static final int MAX_REDIRECTS = 4;

	final String httpMethod;

	final GenericType<?> returnType;

	InvokeAction( String httpMethod, Type exactReturnType )
	{
		super( new ActionParam( TAIL ) );

		this.httpMethod = httpMethod;
		this.returnType = new GenericType( exactReturnType );
	}

	@Override
	void execute( RestRequest cx )
	{
		try {
			final Invocation.Builder b = cx.target.request();

			b.headers( cx.headers );

			if( cx.accepts != null ) {
				b.accept( cx.accepts );
			}

			for( final Cookie c : cx.cookies ) {
				b.cookie( c );
			}

			if( cx.entity != null ) {
				if( cx.contentType == null ) {
					if( cx.entity instanceof Form ) {
						cx.contentType = MediaType.APPLICATION_FORM_URLENCODED;
					}
					else {
						cx.contentType = MediaType.APPLICATION_OCTET_STREAM;
					}
				}

				final Entity<?> e = Entity.entity( cx.entity, cx.contentType );

				cx.result = b.method( this.httpMethod, e, this.returnType );
			}
			else {
				cx.result = b.method( this.httpMethod, this.returnType );
			}
		}
		catch( final RedirectionException ex ) {
			if( cx.redirects++ == MAX_REDIRECTS ) {
				throw ex;
			}

			handleRedirection( cx, ex );
		}
	}

	private void handleRedirection( RestRequest cx, RedirectionException ex )
	{
		final URI location = ex.getLocation();
		final String path = cx.target.getUri().getPath();
		final String newPath = location.getPath();

		final int ix = newPath.indexOf( path );

		if( ix < 0 ) {
			throw ex;
		}

		cx.target = cx.client.target( ex.getLocation() );

		execute( cx );
	}
}
