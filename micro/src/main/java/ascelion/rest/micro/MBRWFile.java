
package ascelion.rest.micro;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

final class MBRWFile extends MBRWBase<File>
{

	@Override
	boolean isAcceptedType( Class<?> type, MediaType mt )
	{
		return type == File.class;
	}

	@Override
	File readFrom( Class<File> type, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		throw new UnsupportedOperationException( "TODO" );
	}

	@Override
	void writeTo( File t, Class<?> type, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		throw new UnsupportedOperationException( "TODO" );
	}

}
