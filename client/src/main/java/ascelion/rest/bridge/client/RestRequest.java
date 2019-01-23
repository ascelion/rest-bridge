
package ascelion.rest.bridge.client;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.util.Optional.ofNullable;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor( access = AccessLevel.PACKAGE )
final class RestRequest<T> implements Callable<T>
{

	private final RestClientData rcd;
	final Object proxy;
	private final GenericType<T> returnType;
	private final boolean async;
	private final String httpMethod;
	final RestRequestContextImpl rc;

	private String[] accepts;
	private String contentType;
	private Object entity;

	void consumes( String[] value )
	{
		if( value.length > 0 ) {
			this.contentType = value[0];
		}
	}

	void cookie( Cookie c )
	{
		this.rc.getCookies().add( c );
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

	void produces( String[] value )
	{
		this.accepts = value;
	}

	@Override
	public T call() throws Exception
	{
		if( this.contentType == null && this.entity != null ) {
			if( this.entity instanceof Form ) {
				this.contentType = MediaType.APPLICATION_FORM_URLENCODED;
			}
			else {
				this.contentType = defaultContentType();
			}
		}

		if( this.async ) {
			final Object ais = this.rcd.aint.prepare();

			final CompletableFuture<T> fut = new CompletableFuture<>();

			this.rcd.exec.execute( () -> invokeAsync( ais, fut ) );

			return (T) fut;
		}
		else {
			return invoke();
		}
	}

	private T invoke() throws Exception
	{
		RestClient.invokedMethod( this.rc.getJavaMethod() );

		final RestRequestContext newRc = this.rcd.reqi.before( this.rc );
		final Invocation.Builder b = newRc.getTarget().request();

		newRc.getHeaders().forEach( ( k, v ) -> v.forEach( x -> b.header( k, x ) ) );
		newRc.getCookies().forEach( b::cookie );

		if( this.accepts != null ) {
			b.accept( this.accepts );
		}
		else {
			b.accept( defaultContentType() );
		}

		final Response rsp;

		try {
			if( this.entity != null ) {
				final Entity<?> e = Entity.entity( this.entity, this.contentType );

				rsp = b.method( this.httpMethod, e );
			}
			else {
				if( this.contentType != null ) {
					// Micro TCK uses a GET with a Content-Type, so let's keep it happy!
					b.header( HttpHeaders.CONTENT_TYPE, this.contentType );
				}

				rsp = b.method( this.httpMethod );
			}
		}
		catch( final ProcessingException e ) {
			final Throwable c = e.getCause();

			if( c instanceof RuntimeException ) {
				throw(RuntimeException) c;
			}
			else {
				throw e;
			}
		}
		finally {
			this.rcd.reqi.after( newRc );

			RestClient.invokedMethod( null );
		}

		final Throwable ex = this.rcd.rsph.apply( rsp );

		if( ex != null ) {
			ex.fillInStackTrace();

			if( ex instanceof Error ) {
				throw(Error) ex;
			}

			throw(Exception) ex;
		}

		final Class<?> rawType = this.returnType.getRawType();

		if( rawType == Response.class ) {
			return (T) rsp;
		}

		try {
			if( rawType == void.class || rawType == Void.class ) {
				return null;
			}
			if( rsp.getStatus() == NO_CONTENT.getStatusCode() ) {
				return null;
			}
			else {
				return rsp.readEntity( this.returnType );
			}
		}
		finally {
			rsp.close();
		}
	}

	private void invokeAsync( Object ais, CompletableFuture<T> fut )
	{
		this.rcd.aint.before( ais );

		try {
			fut.complete( invoke() );
		}
		catch( final Exception e ) {
			fut.completeExceptionally( e );
		}
		finally {
			this.rcd.aint.after( ais );
		}
	}

	private String defaultContentType()
	{
		return ofNullable( this.rcd.conf.getProperty( RestClientProperties.DEFAULT_CONTENT_TYPE ) )
			.map( Object::toString )
			.orElse( MediaType.APPLICATION_OCTET_STREAM );
	}
}
