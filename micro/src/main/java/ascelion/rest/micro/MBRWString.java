
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import ascelion.rest.bridge.client.RBUtils;

import static javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;

import org.apache.commons.io.IOUtils;

final class MBRWString extends MBRWBase<String>
{

	@Override
	boolean isAcceptedType( Class<?> type, MediaType mt )
	{
		return type == String.class;
	}

	@Override
	String readFrom( Class<String> type, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException
	{
		return IOUtils.toString( is, RBUtils.charset( mt ) );
	}

	@Override
	void writeTo( String t, Class<?> type, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException
	{
		IOUtils.write( t, os, updateMediaType( headers, mt, TEXT_PLAIN_TYPE ) );
	}

}
