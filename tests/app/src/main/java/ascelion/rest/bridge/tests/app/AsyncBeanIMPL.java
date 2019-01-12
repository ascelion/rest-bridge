
package ascelion.rest.bridge.tests.app;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ascelion.rest.bridge.tests.api.BeanAPI;
import ascelion.rest.bridge.tests.api.BeanData;

@Path( "AsyncBeanAPI" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes( MediaType.APPLICATION_JSON )
public class AsyncBeanIMPL /* cannot do this: implements AsyncBeanAPI */
{

	@Inject
	private BeanAPI api;

	@POST
	public void create( @Suspended AsyncResponse rsp, @Valid @NotNull BeanData t )
	{
		rsp.resume( this.api.create( t ) );
	}

	@DELETE
	public void delete( @Suspended AsyncResponse rsp )
	{
		this.api.delete();

		rsp.resume( Response.ok() );
	}

	@GET
	public void get( @Suspended AsyncResponse rsp )
	{
		rsp.resume( this.api.get() );
	}

	@PUT
	public void update( @Suspended AsyncResponse rsp, @Valid @NotNull BeanData t )
	{
		rsp.resume( this.api.update( t ) );
	}
}
