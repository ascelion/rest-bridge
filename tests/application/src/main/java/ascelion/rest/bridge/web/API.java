
package ascelion.rest.bridge.web;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Produces( MediaType.APPLICATION_JSON )
@Consumes( MediaType.APPLICATION_JSON )
public interface API<T>
{

	@POST
	T create( @Valid @NotNull T t );

	@DELETE
	void delete( @Valid @NotNull T t );

	@GET
	T get();

	@PUT
	T update( @Valid @NotNull T t );
}
