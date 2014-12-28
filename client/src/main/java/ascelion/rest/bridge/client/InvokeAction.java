
package ascelion.rest.bridge.client;

import java.lang.reflect.Type;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;

class InvokeAction
extends Action
{

	final String httpMethod;

	final GenericType<?> returnType;

	InvokeAction( int ix, String httpMethod, Type exactReturnType )
	{
		super( ix );

		this.httpMethod = httpMethod;
		this.returnType = new GenericType( exactReturnType );
	}

	@Override
	void execute( RestContext cx )
	{
		final Invocation.Builder b = cx.target.request();

		b.headers( cx.headers );

		if( cx.accepts != null ) {
			b.accept( cx.accepts );
		}

		cx.cookies.forEach( c -> b.cookie( c ) );

		if( cx.entityPresent ) {
			if( cx.entity instanceof Form ) {
				( (Form) cx.entity ).asMap().putAll( cx.form.asMap() );
			}
			else if( cx.entity != null && !cx.form.asMap().isEmpty() ) {
				throw new UnsupportedOperationException( "Cannot send both entity and form parameters" );
			}

			if( cx.contentType == null ) {
				cx.contentType = MediaType.APPLICATION_OCTET_STREAM;
			}
		}
		else if( !cx.form.asMap().isEmpty() ) {
			cx.contentType = MediaType.APPLICATION_FORM_URLENCODED;
			cx.entity = cx.form;

			cx.entityPresent = true;
		}

		cx.onBuildRequest.apply( b );

		if( cx.entityPresent ) {
			cx.result = b.method( this.httpMethod, Entity.entity( cx.entity, cx.contentType ), this.returnType );
		}
		else {
			cx.result = b.method( this.httpMethod, this.returnType );
		}
	}
}
