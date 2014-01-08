
package ascelion.rest.bridge.web;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

@Path( "methods" )
public interface Methods
{

	@DELETE
	void delete();

	@GET
	void get();

	@HEAD
	void head();

	@OPTIONS
	void options();

	@POST
	void post();

	@PUT
	void put( String value );

}
