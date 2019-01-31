
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

import ascelion.rest.bridge.client.Prioritised;

import lombok.RequiredArgsConstructor;

/**
 * It's unclear whether MBRWs should be ordered by priority (actually, none of the supported JAX-RS clients does it).
 */
@RequiredArgsConstructor
final class MBRWInterceptor implements ReaderInterceptor, WriterInterceptor
{

	private final RestBridgeConfiguration rbc;

	@Override
	public void aroundWriteTo( WriterInterceptorContext cx ) throws IOException, WebApplicationException
	{
		final Class<?> type = cx.getType();
		final Type genericType = cx.getGenericType();
		final Annotation[] annotations = cx.getAnnotations();
		final MediaType mediaType = cx.getMediaType();

		final MessageBodyWriter w = this.rbc.providers( MessageBodyWriter.class )
			.stream()
			.filter( p -> {
				return p.getInstance().isWriteable( type, genericType, annotations, mediaType );
			} )
			.sorted()
			.map( Prioritised::getInstance )
			.findFirst()
			.orElse( null );

		if( w == null ) {
			// no matching MBW, delegate to JAX-RS
			cx.proceed();
		}
		else {
			w.writeTo( cx.getEntity(), type, genericType, annotations, mediaType, cx.getHeaders(), cx.getOutputStream() );
		}
	}

	@Override
	public Object aroundReadFrom( ReaderInterceptorContext cx ) throws IOException, WebApplicationException
	{
		final InputStream is = cx.getInputStream();

		if( is == null ) {
			// let the JAX-RS client handle this case
			return cx.proceed();
		}

		final Class<?> type = cx.getType();
		final Type genericType = cx.getGenericType();
		final Annotation[] annotations = cx.getAnnotations();
		final MediaType mediaType = cx.getMediaType();

		final MessageBodyReader r = this.rbc.providers( MessageBodyReader.class )
			.stream()
			.filter( p -> {
				return p.getInstance().isReadable( type, genericType, annotations, mediaType );
			} )
			.sorted()
			.map( Prioritised::getInstance )
			.findFirst()
			.orElse( null );

		if( r == null ) {
			return cx.proceed();
		}
		else {
			return r.readFrom( type, genericType, annotations, mediaType, cx.getHeaders(), cx.getInputStream() );
		}
	}

}
