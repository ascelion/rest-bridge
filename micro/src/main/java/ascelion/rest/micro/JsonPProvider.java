
package ascelion.rest.micro;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

@Produces( { "*/json", "*/*+json" } )
@Consumes( { "*/json", "*/*+json" } )
public class JsonPProvider extends MBRWBase<JsonStructure>
{

	@Override
	boolean isAcceptedType( Class<?> type, MediaType mType )
	{
		return JsonStructure.class.isAssignableFrom( type )
			|| JsonObject.class.isAssignableFrom( type )
			|| JsonArray.class.isAssignableFrom( type );
	}

	@Override
	void writeTo( JsonStructure t, Class<?> type, MediaType mType, MultivaluedMap<String, Object> headers, OutputStream os ) throws IOException, WebApplicationException
	{
		try( JsonWriter w = Json.createWriter( os ) ) {
			w.write( t );
		}
	}

	@Override
	JsonStructure readFrom( Class<JsonStructure> type, MediaType mType, MultivaluedMap<String, String> headers, InputStream is ) throws IOException, WebApplicationException
	{
		try( JsonReader r = Json.createReader( is ) ) {
			return r.read();
		}
	}

}
