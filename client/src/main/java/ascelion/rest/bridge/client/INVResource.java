
package ascelion.rest.bridge.client;

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ascelion.utils.chain.InterceptorChainContext;

import static java.util.stream.Collectors.joining;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor( access = AccessLevel.PRIVATE )
final class INVResource implements RestRequestInterceptor
{

	static final RestRequestInterceptor INSTANCE = new INVResource();

	@Override
	public Object around( InterceptorChainContext<RestRequestContext> context ) throws Exception
	{
		return invoke( context.getData() );
	}

	@Override
	public int priority()
	{
		return PRIORITY_INVOCATION;
	}

	private Object invoke( RestRequestContext rc ) throws Exception
	{
		final Invocation.Builder ib = rc.getReqTarget().request();

		rc.getHeaders().forEach( ( k, v ) -> {
			// Jersey concatenates header values while Resteasy sends multiple headers
			// On the other hand, use of Wiremock in MP-TCK expects concatenated values
			// (or it is a wiremock issue)
			ib.header( k, v.stream().collect( joining( "," ) ) );
		} );
		rc.getCookies().forEach( ib::cookie );

		ib.accept( rc.produces.toArray( new MediaType[0] ) );

		final RestMethodInfo mi = rc.getMethodInfo();
		final Response rsp = getResponse( ib, rc, mi );
		final Throwable ex = mi.getResponseHandler().apply( rsp );

		if( ex != null ) {
			if( ex instanceof Error ) {
				throw(Error) ex;
			}

			throw(Exception) ex;
		}

		final Class<?> rawType = mi.getReturnType().getRawType();

		if( rawType == Response.class ) {
			return rsp;
		}

		try {
			if( rawType == void.class || rawType == Void.class ) {
				return null;
			}
			if( rsp.getStatus() == NO_CONTENT.getStatusCode() ) {
				return null;
			}
			if( rsp.hasEntity() ) {
				final Object ent = rsp.getEntity();

				if( mi.getReturnType().getRawType().isInstance( ent ) ) {
					return ent;
				}

				return rsp.readEntity( mi.getReturnType() );
			}
			else {
				return null;
			}
		}
		finally {
			if( rsp != null ) {
				rsp.close();
			}
		}
	}

	static private Response getResponse( Invocation.Builder ib, RestRequestContext rc, RestMethodInfo mi )
	{
		try {
			final MediaType mt = rc.getContentType();
			final Object ent = rc.entity();

			if( ent != null ) {
				final Entity<?> e = Entity.entity( ent, mt );

				return ib.method( mi.getHttpMethod(), e );
			}
			else {
				if( rc.hasBody() && mt != null ) {
					ib.header( CONTENT_TYPE, mt );
				}

				return ib.method( mi.getHttpMethod() );
			}
		}
		catch( final ProcessingException e ) {
			final Throwable c = e.getCause();

			if( c instanceof RuntimeException ) {
				throw(RuntimeException) c;
			}

			throw e;
		}
	}
}
