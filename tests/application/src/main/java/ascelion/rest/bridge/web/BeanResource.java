
package ascelion.rest.bridge.web;

import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path( "beans" )
public interface BeanResource
{

	@GET
	@Path( "{path1}/{path2}" )
	public BeanInfo get( @BeanParam BeanInfo request );
}
