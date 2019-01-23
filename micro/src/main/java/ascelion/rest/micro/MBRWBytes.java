
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;

import org.apache.commons.io.IOUtils;

final class MBRWBytes extends MBRWBase<byte[]>
{

	@Override
	boolean isAcceptedType( Class<?> type, MediaType mt )
	{
		return type == byte[].class;
	}

	@Override
	byte[] readFrom( Class<byte[]> type, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException
	{
		return IOUtils.toByteArray( is );
	}

	@Override
	void writeTo( byte[] t, Class<?> type, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException
	{
		updateMediaType( headers, mt, APPLICATION_OCTET_STREAM_TYPE );

		IOUtils.write( t, os );
	}

}
