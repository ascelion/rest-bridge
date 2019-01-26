
package ascelion.rest.bridge.tests.api;

import java.util.concurrent.CompletionStage;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path( "users/async" )
public interface UsersAsync
{

	@Path( "{username}/interface" )
	CompletionStage<UserInfo> asInterface( @PathParam( "username" ) String username );

	@Path( "{username}/class" )
	CompletionStage<UserBean> asClass( @PathParam( "username" ) String username );
}
