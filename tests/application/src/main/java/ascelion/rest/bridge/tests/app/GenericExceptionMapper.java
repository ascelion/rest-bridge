
package ascelion.rest.bridge.tests.app;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.server.ParamException;

@Provider
@Produces( MediaType.WILDCARD )
@ApplicationScoped
public class GenericExceptionMapper implements ExceptionMapper<Throwable>
{

	@Inject
	private ObjectMapper om;

	@Override
	public Response toResponse( Throwable t )
	{
		try {
			final Map<String, Object> map = buildMap( t );
			final String ent = this.om.writeValueAsString( map );

			Response.StatusType status = Response.Status.INTERNAL_SERVER_ERROR;

			if( t instanceof WebApplicationException ) {
				if( t instanceof ParamException ) {
					status = Response.Status.BAD_REQUEST;
				}
				else {
					status = ( (WebApplicationException) t ).getResponse().getStatusInfo();
				}
			}

			return Response.status( status )
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

	private Map<String, Object> buildMap( Throwable t )
	{
		final Map<String, Object> map = new LinkedHashMap<>();

		map.put( "message", t.getMessage() );
		map.put( "class", t.getClass().getName() );

		final Throwable cause = t.getCause();

		if( cause != null && cause != t ) {
			map.put( "cause", buildMap( cause ) );
		}

		return map;
	}

}
