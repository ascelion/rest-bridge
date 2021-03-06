
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import ascelion.rest.bridge.client.RBUtils;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static javax.ws.rs.core.MediaType.WILDCARD;

import org.apache.commons.io.IOUtils;

@Consumes( { TEXT_PLAIN, WILDCARD } )
@Produces( { TEXT_PLAIN, WILDCARD } )
final class MBRWReader extends MBRWBase<Reader>
{

	@Override
	boolean isAcceptedType( Class<?> type, MediaType mt )
	{
		return type == Reader.class;
	}

	@Override
	Reader readFrom( Class<Reader> type, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		return IOUtils.toBufferedReader( new InputStreamReader( is, RBUtils.charset( mt ) ) );
	}

	@Override
	void writeTo( Reader t, Class<?> type, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		IOUtils.copy( t, os, updateMediaType( headers, mt, TEXT_PLAIN_TYPE ) );
	}

}
