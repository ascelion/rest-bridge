
package ascelion.rest.bridge.tests.app;

import java.util.concurrent.ExecutorService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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

import ascelion.rest.bridge.tests.api.API;
import ascelion.rest.bridge.tests.api.BeanData;

@RequestScoped
@Path( "async" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes( MediaType.APPLICATION_JSON )
public class AsyncSuspendedIMPL
{

	@Inject
	private API<BeanData> api;
	@Inject
	private ExecutorService exec;

	@POST
	public void create( @Suspended AsyncResponse rsp, BeanData t )
	{
		this.exec.submit( () -> rsp.resume( this.api.create( t ) ) );
	}

	@DELETE
	public void delete( @Suspended AsyncResponse rsp )
	{
		this.exec.submit( () -> {
			this.api.delete();
			rsp.resume( Response.ok() );
		} );
	}

	@GET
	public void get( @Suspended AsyncResponse rsp )
	{
		this.exec.submit( () -> rsp.resume( this.api.get() ) );
	}

	@PUT
	public void update( @Suspended AsyncResponse rsp, BeanData t )
	{
		this.exec.submit( () -> rsp.resume( this.api.update( t ) ) );
	}
}
