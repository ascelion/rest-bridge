
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

class MBRWTextPlain implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{

	@Override
	public boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAccepted( type, mt );
	}

	@Override
	public void writeTo( Object t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream is ) throws IOException, WebApplicationException
	{
		throw new UnsupportedOperationException( "TODO" );
	}

	@Override
	public boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAccepted( type, mt );
	}

	@Override
	public Object readFrom( Class<Object> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream os ) throws IOException, WebApplicationException
	{
		throw new UnsupportedOperationException( "TODO" );
	}

	private boolean isAccepted( Class<?> type, MediaType mt )
	{
		return MediaType.TEXT_PLAIN_TYPE.equals( mt )
			&& ( Number.class.isAssignableFrom( type ) ||
				Character.class == type || char.class == type ||
				Long.class == type || long.class == type ||
				Integer.class == type || int.class == type ||
				Double.class == type || double.class == type ||
				Float.class == type || float.class == type ||
				Boolean.class == type || boolean.class == type );
	}

}
