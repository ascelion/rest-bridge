
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Objects;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import ascelion.rest.bridge.client.RBUtils;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

abstract class MBRWBase<T> implements MessageBodyReader<T>, MessageBodyWriter<T>
{

	@Override
	public final boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAcceptedType( type, mt );
	}

	@Override
	public T readFrom( Class<T> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException
	{
		return is != null ? readFrom( type, mt, headers, is ) : null;
	}

	@Override
	public final boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAcceptedType( type, mt );
	}

	@Override
	public final void writeTo( T t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException
	{
		if( t != null ) {
			writeTo( t, type, mt, headers, os );
		}
	}

	abstract boolean isAcceptedType( Class<?> type, MediaType mt );

	abstract T readFrom( Class<T> type, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException;

	abstract void writeTo( T t, Class<?> type, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException;

	static final Charset updateMediaType( MultivaluedMap<String, Object> headers, MediaType mt, MediaType update )
	{
		if( mt == null ) {
			final String ct = Objects.toString( headers.getFirst( CONTENT_TYPE ), null );

			mt = ct != null ? MediaType.valueOf( ct ) : null;
		}

		Charset cs = RBUtils.charset( mt );

		if( mt == null || mt.isWildcardType() || mt.isWildcardSubtype() ) {
			mt = update;
			cs = RBUtils.charset( mt );

			if( RBUtils.isTextContent( mt ) ) {
				mt = mt.withCharset( cs.name() );
			}

			headers.putIfAbsent( CONTENT_TYPE, asList( mt.toString() ) );
		}

		return cs;
	}
}
