
package ascelion.rest.bridge.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path( "interface" )
public interface Interface
{

	@GET
	String get();
}
