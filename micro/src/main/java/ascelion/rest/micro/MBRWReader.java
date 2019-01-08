
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import ascelion.rest.bridge.client.Util;

import org.apache.commons.io.IOUtils;

final class MBRWReader implements MessageBodyWriter<Reader>, MessageBodyReader<Reader>
{

	@Override
	public boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return type == Reader.class;
	}

	@Override
	public Reader readFrom( Class<Reader> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		return IOUtils.toBufferedReader( new InputStreamReader( is, Util.charset( mt ) ) );
	}

	@Override
	public boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return type == Reader.class;
	}

	@Override
	public void writeTo( Reader t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		IOUtils.copy( t, os, Util.charset( mt ) );
	}

}
