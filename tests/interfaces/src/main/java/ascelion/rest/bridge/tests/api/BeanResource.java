
package ascelion.rest.bridge.tests.api;

import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path( "beans" )
@Consumes( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
@Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON } )
public interface BeanResource
{

	@GET
	@Path( "{path1}/{path2}" )
	public BeanParamData get( @BeanParam BeanParamData request );
}
