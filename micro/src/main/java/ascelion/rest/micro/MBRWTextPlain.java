
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Function;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import ascelion.rest.bridge.client.RBUtils;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import org.apache.commons.io.IOUtils;

class MBRWTextPlain implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{

	static private final Map<Class<?>, Function<String, ?>> READERS = new IdentityHashMap<>();

	static {
		READERS.put( boolean.class, Boolean::valueOf );
		READERS.put( Boolean.class, Boolean::valueOf );
		READERS.put( char.class, s -> s.charAt( 0 ) );
		READERS.put( Character.class, s -> s.charAt( 0 ) );
		READERS.put( int.class, Integer::valueOf );
		READERS.put( Integer.class, Integer::valueOf );
		READERS.put( long.class, Long::valueOf );
		READERS.put( Long.class, Long::valueOf );
		READERS.put( float.class, Float::valueOf );
		READERS.put( Float.class, Float::valueOf );
		READERS.put( double.class, Double::valueOf );
		READERS.put( Double.class, Double::valueOf );
		READERS.put( BigInteger.class, BigInteger::new );
		READERS.put( BigDecimal.class, BigDecimal::new );
	}

	@Override
	public boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return isAccepted( type, mt );
	}

	@Override
	public Object readFrom( Class<Object> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		final String value = trimToNull( IOUtils.toString( is, RBUtils.charset( mt ) ) );

		if( value == null ) {
			return null;
		}

		return READERS.getOrDefault( type, s -> fromString( type, s ) )
			.apply( value );
	}

	private Object fromString( Class<?> type, String value )
	{
		try {
			return type.getConstructor( String.class ).newInstance( value );
		}
		catch( final NoSuchMethodException e ) {
			;
		}
		catch( final InvocationTargetException e ) {
			throw new RuntimeException( format( "Cannot construct %s from string", type ), e.getCause() );
		}
		catch( final Exception e ) {
			throw new RuntimeException( format( "Cannot construct %s from string", type ), e );
		}

		try {
			return type.getMethod( "valueOf", String.class ).invoke( null, value );
		}
		catch( final NoSuchMethodException e ) {
			;
		}
		catch( final InvocationTargetException e ) {
			throw new RuntimeException( format( "Cannot construct %s from string", type ), e.getCause() );
		}
		catch( final Exception e ) {
			throw new RuntimeException( format( "Cannot construct %s from string", type ), e );
		}

		throw new RuntimeException( format( "Cannot construct %s from string", type ) );
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
			IOUtils.write( t.toString(), os, RBUtils.charset( mt ) );
		}
	}

	private boolean isAccepted( Class<?> type, MediaType mt )
	{
		return MediaType.TEXT_PLAIN_TYPE.equals( mt )
			&& ( Number.class.isAssignableFrom( type )
				|| Boolean.class == type || boolean.class == type
				|| Character.class == type || char.class == type
				|| Integer.class == type || int.class == type
				|| Long.class == type || long.class == type
				|| Float.class == type || float.class == type
				|| Double.class == type || double.class == type );
	}

}
