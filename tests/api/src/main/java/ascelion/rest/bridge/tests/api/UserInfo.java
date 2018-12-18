
package ascelion.rest.bridge.tests.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

public interface UserInfo
{

	@GET
	@Path( "firstName" )
	String getFirstName();

	@GET
	@Path( "lastName" )
	String getLastName();

}
