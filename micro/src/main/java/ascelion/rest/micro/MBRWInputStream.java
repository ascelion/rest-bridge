
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;

import org.apache.commons.io.IOUtils;

final class MBRWInputStream extends MBRWBase<InputStream>
{

	@Override
	boolean isAcceptedType( Class<?> type, MediaType mt )
	{
		return type == InputStream.class;
	}

	@Override
	InputStream readFrom( Class<InputStream> type, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		return IOUtils.toBufferedInputStream( is );
	}

	@Override
	void writeTo( InputStream t, Class<?> type, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		updateMediaType( headers, mt, APPLICATION_OCTET_STREAM_TYPE );

		IOUtils.copy( t, os );
	}

}
