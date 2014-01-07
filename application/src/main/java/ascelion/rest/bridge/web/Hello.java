
package ascelion.rest.bridge.web;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

@Path( "hello" )
public interface Hello
{

	@GET
	@Path( "say" )
	String sayByParam( @QueryParam( "username" ) @DefaultValue( "guest" ) String username );

	@GET
	@Path( "{username}" )
	String sayByPath( @PathParam( "username" ) String username );
}
