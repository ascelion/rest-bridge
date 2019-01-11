
package ascelion.rest.bridge.client;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static java.util.Optional.ofNullable;

import io.leangen.geantyref.GenericTypeReflector;
import lombok.Getter;

final class RestRequest<T> implements Callable<T>
{

	private final RestBridgeType rbt;
	final Object proxy;
	private final Method javaMethod;
	final Object[] arguments;
	private final String httpMethod;
	@Getter
	private WebTarget target;

	private final GenericType<T> returnType;

	private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
	private final Collection<Cookie> cookies = new ArrayList<>();
	private String[] accepts;
	private String contentType;
	private Object entity;
	private boolean async;

	RestRequest( RestBridgeType rbt, Object proxy, Method javaMethod, String httpMethod, WebTarget target, Object... arguments )
	{
		this.rbt = rbt;
		this.proxy = proxy;
		this.javaMethod = javaMethod;
		this.httpMethod = httpMethod;

		final Type rt = GenericTypeReflector.getExactReturnType( this.javaMethod, rbt.type );
		GenericType<?> gt = new GenericType<>( rt );

		if( gt.getRawType() == CompletionStage.class ) {
			this.async = true;

			gt = new GenericType<>( ( (ParameterizedType) gt.getType() ).getActualTypeArguments()[0] );
		}
		else {
			this.async = false;

			gt = new GenericType<>( rt );
		}

		this.returnType = (GenericType<T>) gt;
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
	public T call() throws Exception
	{
		final Invocation.Builder b = this.target.request();

		b.headers( this.headers );

		if( this.accepts != null ) {
			b.accept( this.accepts );
		}

		this.cookies.forEach( b::cookie );

		if( this.contentType == null && this.entity != null ) {
			if( this.entity instanceof Form ) {
				this.contentType = MediaType.APPLICATION_FORM_URLENCODED;
			}
			else {
				this.contentType = defaultContentType();
			}
		}

		if( this.async ) {
			final Object ais = this.rbt.aint.prepare();

			final CompletableFuture<T> fut = new CompletableFuture<>();

			this.rbt.exec.execute( () -> invokeAsync( b, ais, fut ) );

			return (T) fut;
		}
		else {
			return invoke( b );
		}
	}

	private T invoke( Invocation.Builder b ) throws Exception
	{
		final Response rsp;

		RestClient.invokedMethod( this.javaMethod );

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
			RestClient.invokedMethod( null );
		}

		this.rbt.rsph.handleResponse( rsp );

		final Class<?> rawType = this.returnType.getRawType();

		if( rawType == Response.class ) {
			return (T) rsp;
		}

		try {
			if( rawType == void.class || rawType == Void.class ) {
				return null;
			}

			return rsp.readEntity( this.returnType );
		}
		finally {
			rsp.close();
		}
	}

	private void invokeAsync( Invocation.Builder b, Object ais, CompletableFuture<T> fut )
	{
		this.rbt.aint.before( ais );

		try {
			fut.complete( invoke( b ) );
		}
		catch( final Exception e ) {
			fut.completeExceptionally( e );
		}
		finally {
			this.rbt.aint.after( ais );
		}
	}

	private String defaultContentType()
	{
		return ofNullable( this.rbt.conf.getProperty( RestClientProperties.DEFAULT_CONTENT_TYPE ) )
			.map( Object::toString )
			.orElse( MediaType.TEXT_PLAIN );
	}
}
