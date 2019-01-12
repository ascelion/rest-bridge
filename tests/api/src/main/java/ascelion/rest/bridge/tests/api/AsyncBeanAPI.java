
package ascelion.rest.bridge.tests.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path( "AsyncBeanAPI" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes( MediaType.APPLICATION_JSON )
public interface AsyncBeanAPI
extends AsyncAPI<BeanData>
{
}
