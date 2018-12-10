
package ascelion.rest.bridge.tests.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path( "methods" )
@Consumes( MediaType.WILDCARD )
@Produces( MediaType.TEXT_PLAIN )
public interface Methods
{

	@DELETE
	void delete();

	@GET
	String get();

	@HEAD
	String head();

	@OPTIONS
	String options();

	@POST
	void post();

	@PUT
	void put( String value );

}
