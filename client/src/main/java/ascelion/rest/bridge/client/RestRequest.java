
package ascelion.rest.bridge.client;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor( access = AccessLevel.PACKAGE )
final class RestRequest<T> implements Callable<T>
{

	final RestRequestContextImpl rc;

	@Override
	public T call() throws Exception
	{
		if( this.rc.isAsync() ) {
			final Object ais = this.rc.rcd.aint.prepare();

			final CompletableFuture<T> fut = new CompletableFuture<>();

			this.rc.rcd.exec.execute( () -> invokeAsync( ais, fut ) );

			return (T) fut;
		}
		else {
			return invoke();
		}
	}

	private T invoke() throws Exception
	{
		RestClient.invokedMethod( this.rc.getJavaMethod() );

		this.rc.rcd.reqi.before( this.rc );

		final Invocation.Builder b = this.rc.getTarget().request();

		this.rc.getHeaders().forEach( ( k, v ) -> {
			// Jersey concatenates header values while Resteasy sends multiple headers
			// On the other hand, use of Wiremock in MP-TCK expects concatenated values
			// (or it is a wiremock issue)
			b.header( k, v.stream().collect( joining( "," ) ) );
		} );
		this.rc.getCookies().forEach( b::cookie );

		b.accept( this.rc.produces.toArray( new MediaType[0] ) );

		final Response rsp;

		try {
			final MediaType ct = this.rc.getContentType();

			if( this.rc.entity != null ) {
				final Entity<?> e = Entity.entity( this.rc.entity, ct );

				rsp = b.method( this.rc.getHttpMethod(), e );
			}
			else {
				if( ct != null ) {
					// Micro TCK uses a GET with a Content-Type, so let's keep it happy!
					b.header( HttpHeaders.CONTENT_TYPE, ct );
				}

				rsp = b.method( this.rc.getHttpMethod() );
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
			this.rc.rcd.reqi.after( this.rc );

			RestClient.invokedMethod( null );
		}

		final Throwable ex = this.rc.rcd.rsph.apply( rsp );

		if( ex != null ) {
			ex.fillInStackTrace();

			if( ex instanceof Error ) {
				throw(Error) ex;
			}

			throw(Exception) ex;
		}

		final Class<?> rawType = this.rc.getReturnType().getRawType();

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
				return rsp.readEntity( this.rc.getReturnType() );
			}
		}
		finally {
			rsp.close();
		}
	}

	private void invokeAsync( Object ais, CompletableFuture<T> fut )
	{
		this.rc.rcd.aint.before( ais );

		try {
			fut.complete( invoke() );
		}
		catch( final Exception e ) {
			fut.completeExceptionally( e );
		}
		finally {
			this.rc.rcd.aint.after( ais );
		}
	}
}
