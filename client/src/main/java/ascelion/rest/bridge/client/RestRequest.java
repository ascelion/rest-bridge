
package ascelion.rest.bridge.client;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static java.util.Optional.ofNullable;

import lombok.Getter;

final class RestRequest implements Callable<Object>
{

	private final RestBridgeType rbt;
	final Object proxy;
	final Object[] arguments;
	private final String httpMethod;
	@Getter
	private WebTarget target;
	private final GenericType<?> returnType;

	private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
	private final Collection<Cookie> cookies = new ArrayList<>();
	private String[] accepts;
	private String contentType;
	private Object entity;

	RestRequest( RestBridgeType rbt, Object proxy, String httpMethod, WebTarget target, Type returnType, Object... arguments )
	{
		this.rbt = rbt;
		this.proxy = proxy;
		this.httpMethod = httpMethod;
		this.returnType = new GenericType<>( returnType );
		this.target = target;
		this.arguments = arguments == null ? new Object[0] : arguments;
	}

	void consumes( String[] value )
	{
		if( value.length > 0 ) {
			this.contentType = value[0];
		}
	}

	void cookie( Cookie c )
	{
		this.cookies.add( c );
	}

	void entity( Object entity )
	{
		if( this.entity != null ) {
			// TODO review this, could it be caught in the initialisation stage?
			throw new IllegalStateException( "The request entity has been already set" );
		}

		this.entity = entity;
	}

	void form( String name, String value )
	{
		Form form;

		try {
			if( this.entity == null ) {
				this.entity = form = new Form();
			}
			else {
				form = (Form) this.entity;
			}
		}
		catch( final ClassCastException e ) {
			// TODO review this, could it be caught in the initialisation stage?
			throw new IllegalStateException( "Trying to mix a non-form entity with a Form" );
		}

		form.param( name, value );
	}

	void header( String name, String value )
	{
		this.headers.add( name, value );
	}

	void matrix( String name, String value )
	{
		this.target = this.target.matrixParam( name, value );
	}

	void path( String name, String value )
	{
		this.target = this.target.resolveTemplate( name, value, true );
	}

	void produces( String[] value )
	{
		this.accepts = value;
	}

	void query( String name, String value )
	{
		this.target = this.target.queryParam( name, value );
	}

	@Override
	public Object call() throws Exception
	{
		final Invocation.Builder b = this.target.request();

		b.headers( this.headers );

		if( this.accepts != null ) {
			b.accept( this.accepts );
		}

		this.cookies.forEach( b::cookie );

		final Response rsp;

		if( this.entity != null ) {
			if( this.contentType == null ) {
				if( this.entity instanceof Form ) {
					this.contentType = MediaType.APPLICATION_FORM_URLENCODED;
				}
				else {
					this.contentType = defaultContentType();
				}
			}

			final Entity<?> e = Entity.entity( this.entity, this.contentType );

			rsp = b.method( this.httpMethod, e );
		}
		else {
			// to keep TCK happy
			if( this.contentType != null ) {
				b.header( "Content-Type", this.contentType );
			}

			rsp = b.method( this.httpMethod );
		}

		this.rbt.rsph.handleResponse( rsp );

		if( this.returnType.getRawType() == Response.class ) {
			return rsp;
		}

		try {
			return rsp.readEntity( this.returnType );
		}
		finally {
			rsp.close();
		}
	}

	private String defaultContentType()
	{
		return ofNullable( this.rbt.conf.getProperty( RestClientProperties.DEFAULT_CONTENT_TYPE ) )
			.map( Object::toString )
			.orElse( MediaType.TEXT_PLAIN );
	}
}
