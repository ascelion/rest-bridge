
package ascelion.rest.bridge.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path( "hello" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes( MediaType.APPLICATION_JSON )
public interface Hello
{

	@GET
	@Path( "authenticate" )
	UserInfo authenticate( @QueryParam( "username" ) String username, @HeaderParam( "password" ) String password );

	@GET
	String sayByParam( @QueryParam( "username" ) @DefaultValue( "guest" ) String username );

	@GET
	@Path( "{username}" )
	String sayByPath( @PathParam( "username" ) String username );
}
