
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

final class MBRWInputStream implements MessageBodyWriter<InputStream>, MessageBodyReader<InputStream>
{

	@Override
	public boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return type == InputStream.class;
	}

	@Override
	public InputStream readFrom( Class<InputStream> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		return IOUtils.toBufferedInputStream( is );
	}

	@Override
	public boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return type == InputStream.class;
	}

	@Override
	public void writeTo( InputStream t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		IOUtils.copy( t, os );
	}

}
