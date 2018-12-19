
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonWriter;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

@Produces( { "application/json", "application/*+json" } )
@Consumes( { "application/json", "application/*+json" } )
public class JsonPProvider implements MessageBodyReader<JsonStructure>, MessageBodyWriter<JsonStructure>
{

	@Override
	public boolean isWriteable( Class<?> type, Type gType, Annotation[] annotations, MediaType mType )
	{
		return isJsonP( type );
	}

	@Override
	public void writeTo( JsonStructure t, Class<?> type, Type gType, Annotation[] annotations, MediaType mType, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		try( JsonWriter w = Json.createWriter( os ) ) {
			w.write( t );
		}
	}

	@Override
	public boolean isReadable( Class<?> type, Type gType, Annotation[] annotations, MediaType mType )
	{
		return isJsonP( type );
	}

	@Override
	public JsonStructure readFrom( Class<JsonStructure> type, Type gType, Annotation[] annotations, MediaType mType, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		try( JsonReader r = Json.createReader( is ) ) {
			return r.read();
		}
	}

	private boolean isJsonP( Class<?> type )
	{
		return JsonStructure.class.isAssignableFrom( type )
			|| JsonObject.class.isAssignableFrom( type )
			|| JsonArray.class.isAssignableFrom( type );
	}

}
