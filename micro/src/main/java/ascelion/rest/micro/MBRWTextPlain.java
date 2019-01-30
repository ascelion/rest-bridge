
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import ascelion.rest.bridge.client.RBUtils;
import ascelion.utils.etc.SimpleTypeBuilder;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static javax.ws.rs.core.MediaType.WILDCARD;

import org.apache.commons.io.IOUtils;

@Consumes( { TEXT_PLAIN, WILDCARD } )
@Produces( { TEXT_PLAIN, WILDCARD } )
final class MBRWTextPlain implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{

	static private final SimpleTypeBuilder STB = new SimpleTypeBuilder();

	@Override
	public boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAccepted( type, mt ) && ( STB.getBuilder( type ) != null );
	}

	@Override
	public Object readFrom( Class<Object> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		return STB.createFromPlainText( type, IOUtils.toString( is, RBUtils.charset( mt ) ) );
	}

	@Override
	public boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAccepted( type, mt );
	}

	@Override
	public void writeTo( Object t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		if( t != null ) {
			final Charset cs = RBUtils.charset( mt );

			if( mt == null || mt.isWildcardType() || mt.isWildcardSubtype() ) {
				mt = MediaType.TEXT_PLAIN_TYPE.withCharset( cs.name() );

				headers.putIfAbsent( HttpHeaders.CONTENT_TYPE, asList( mt.toString() ) );
			}

			IOUtils.write( t.toString(), os, cs );
		}
	}

	private boolean isAccepted( Class<?> type, MediaType mt )
	{
		return Number.class.isAssignableFrom( type )
			|| Boolean.class == type || boolean.class == type
			|| Character.class == type || char.class == type
			|| Integer.class == type || int.class == type
			|| Long.class == type || long.class == type
			|| Float.class == type || float.class == type
			|| Double.class == type || double.class == type;
	}

}
