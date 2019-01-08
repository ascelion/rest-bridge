
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;
import javax.json.bind.config.PropertyVisibilityStrategy;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.eclipse.yasson.YassonProperties;

@Produces( { "*/json", "*/*+json" } )
@Consumes( { "*/json", "*/*+json" } )
public class JsonBProvider implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{

	static private class AnyAccessStrategy implements PropertyVisibilityStrategy
	{

		@Override
		public boolean isVisible( Field field )
		{
			return true;
		}

		@Override
		public boolean isVisible( Method method )
		{
			return true;
		}

	}

	static private Jsonb jsonb( MediaType mt )
	{
		try {
			final JsonbConfig cf = new JsonbConfig();

			cf.setProperty( YassonProperties.ZERO_TIME_PARSE_DEFAULTING, true );
			cf.setProperty( YassonProperties.FAIL_ON_UNKNOWN_PROPERTIES, false );

			cf.withPropertyVisibilityStrategy( new AnyAccessStrategy() );

			if( mt != null ) {
				final String charset = mt.getParameters().get( "charset" );

				if( isNotBlank( charset ) ) {
					cf.withEncoding( charset );
				}
			}

			return JsonbBuilder.newBuilder()
				.withConfig( cf )
				.build();
		}
		catch( final JsonbException e ) {
			return null;
		}
	}

	private final boolean enabled;

	public JsonBProvider()
	{
		this.enabled = jsonb( null ) != null;
	}

	@Override
	public boolean isWriteable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return this.enabled && mt.getSubtype().endsWith( "json" );
	}

	@Override
	public void writeTo( Object t, Class<?> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		jsonb( mt ).toJson( t, os );
	}

	@Override
	public boolean isReadable( Class<?> type, Type gt, Annotation[] annotations, MediaType mt )
	{
		return this.enabled && mt.getSubtype().endsWith( "json" );
	}

	@Override
	public Object readFrom( Class<Object> type, Type gt, Annotation[] annotations, MediaType mt, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		return jsonb( mt ).fromJson( is, type );
	}

}
