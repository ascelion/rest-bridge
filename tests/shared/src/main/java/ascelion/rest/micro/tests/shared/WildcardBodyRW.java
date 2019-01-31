
package ascelion.rest.micro.tests.shared;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

final class WildcardBodyRW implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{

	private final ObjectMapper om = new ObjectMapper();

	@Override
	public boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAcceptable( type, mt );
	}

	@Override
	public Object readFrom( Class<Object> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		final byte[] buf = IOUtils.toByteArray( is );

		if( buf.length == 0 ) {
			return null;
		}
		if( Map.class.isAssignableFrom( type ) ) {
			return this.om.readValue( buf, TreeMap.class );
		}
		else {
			return new String( buf, Charset.forName( "UTF-8" ) );
		}
	}

	@Override
	public boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAcceptable( type, mt );
	}

	@Override
	public void writeTo( Object t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		if( Map.class.isAssignableFrom( type ) ) {
			headers.putSingle( CONTENT_TYPE, APPLICATION_JSON );

			this.om.writeValue( os, t );
		}
		else {
			headers.putSingle( CONTENT_TYPE, TEXT_PLAIN_TYPE.withCharset( "UTF-8" ) );

			if( t != null ) {
				IOUtils.write( (String) t, os, Charset.forName( "UTF-8" ) );
			}
		}
	}

	private boolean isAcceptable( Class<?> type, MediaType mt )
	{
		if( type != String.class && !Map.class.isAssignableFrom( type ) ) {
			return false;
		}

		return mt == null || mt.isWildcardType() && mt.isWildcardSubtype();
	}

}
