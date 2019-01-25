
package ascelion.rest.bridge.client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path( "{path1}" )
public interface InterfaceWithMissingPath
{

	@GET
	@Path( "{path2}/{path3}" )
	String get( @PathParam( "path2" ) String path3 );
}
