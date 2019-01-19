
package ascelion.rest.bridge.tests.api;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path( "users" )
public interface Users
{

	@Path( "{username}/interface" )
	UserInfo asInterface( @PathParam( "username" ) String username );

	@Path( "{username}/class" )
	UserBean asClass( @PathParam( "username" ) String username );
}
