
package ascelion.rest.micro;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;
import static javax.ws.rs.core.MediaType.WILDCARD;

import org.apache.commons.io.IOUtils;

@Consumes( { APPLICATION_OCTET_STREAM, WILDCARD } )
@Produces( { APPLICATION_OCTET_STREAM, WILDCARD } )
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
		final File file = File.createTempFile( "rest-bridge", "tmp" );

		file.deleteOnExit();

		try( OutputStream out = new BufferedOutputStream( new FileOutputStream( file ) ) ) {
			IOUtils.copy( is, out );
		}

		return file;
	}

	@Override
	void writeTo( File t, Class<?> type, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		try( InputStream in = new BufferedInputStream( new FileInputStream( t ) ) ) {
			IOUtils.copy( in, os );
		}
	}

}
