
package ascelion.rest.micro.tests.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import com.fasterxml.jackson.databind.ObjectMapper;

final class WildcardBodyWriter<T> implements MessageBodyWriter<T>
{

	private final ObjectMapper om = new ObjectMapper();

	@Override
	public boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return mt == null || mt.isWildcardType() && mt.isWildcardSubtype();
	}

	@Override
	public void writeTo( T t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		if( t != null ) {
			final ByteArrayOutputStream buf = new ByteArrayOutputStream();

			this.om.writeValue( buf, t );

			headers.putSingle( HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON );
			os.write( buf.toByteArray() );
		}
	}

}
