
package ascelion.rest.bridge.tests.app;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

import ascelion.rest.bridge.tests.api.Users;

@Path( "users/async" )
public class UsersAsyncImpl
{

	@Inject
	private Users users;
	@Inject
	private ExecutorService exec;

	@Path( "{username}/interface" )
	public void asInterface( @Suspended AsyncResponse rsp, @PathParam( "username" ) String username )
	{
		this.exec.submit( () -> rsp.resume( this.users.asInterface( username ) ) );
	}

	@Path( "{username}/class" )
	public void asClass( @Suspended AsyncResponse rsp, @PathParam( "username" ) String username )
	{
		this.exec.submit( () -> rsp.resume( this.users.asClass( username ) ) );
	}

}
