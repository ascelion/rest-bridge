
package ascelion.rest.bridge.tests.app;

import java.util.HashMap;

import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Provider
@Produces( MediaType.WILDCARD )
public class GenericExceptionMapper implements ExceptionMapper<Exception>
{

	@Inject
	private ObjectMapper om;

	@Override
	public Response toResponse( Exception exception )
	{
		try {
			final HashMap map = new HashMap<>();

			map.put( "class", exception.getClass() );
			map.put( "message", exception.getMessage() );

			final Throwable cause = exception.getCause();

			if( cause != null && cause != exception ) {
				map.put( "cause", cause.toString() );
			}

			final String ent = this.om.writeValueAsString( map );

			return Response.status( Response.Status.INTERNAL_SERVER_ERROR )
				.type( MediaType.APPLICATION_JSON_TYPE )
				.entity( ent )
				.build();
		}
		catch( final JsonProcessingException e ) {
			return Response.status( Response.Status.INTERNAL_SERVER_ERROR )
				.type( MediaType.TEXT_PLAIN )
				.entity( e.getMessage() )
				.build();
		}

	}

}
