
package ascelion.rest.bridge.tests.api;

import java.util.concurrent.CompletionStage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path( "async" )
@Produces( MediaType.APPLICATION_JSON )
@Consumes( MediaType.APPLICATION_JSON )
public interface AsyncAPI
{

	@POST
	@Valid
	@NotNull
	CompletionStage<BeanData> create( @Valid @NotNull BeanData t );

	@DELETE
	CompletionStage<Void> delete();

	@GET
	@Valid
	@NotNull
	CompletionStage<BeanData> get();

	@PUT
	@Valid
	@NotNull
	CompletionStage<BeanData> update( @Valid @NotNull BeanData t );
}
