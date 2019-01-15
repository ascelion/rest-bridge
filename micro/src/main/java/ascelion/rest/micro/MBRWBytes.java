
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import org.apache.commons.io.IOUtils;

final class MBRWBytes implements MessageBodyWriter<byte[]>, MessageBodyReader<byte[]>
{

	@Override
	public boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return type == byte[].class;
	}

	@Override
	public byte[] readFrom( Class<byte[]> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		return IOUtils.toByteArray( is );
	}

	@Override
	public boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return type == byte[].class;
	}

	@Override
	public void writeTo( byte[] t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		IOUtils.write( t, os );
	}

}
