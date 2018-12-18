
package ascelion.rest.bridge.tests.api;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path( "hello" )
public interface Hello
{

	@GET
	@Path( "authenticate" )
	@Produces( MediaType.APPLICATION_JSON )
	UserBean authenticate( @QueryParam( "username" ) String username, @HeaderParam( "password" ) String password );

//	@Path( "{username}" )

	@GET
	@Produces( MediaType.TEXT_PLAIN )
	String sayByParam( @QueryParam( "username" ) @DefaultValue( "guest" ) String username );

	@GET
	@Path( "{username}" )
	@Produces( MediaType.TEXT_PLAIN )
	String sayByPath( @PathParam( "username" ) String username );
}
