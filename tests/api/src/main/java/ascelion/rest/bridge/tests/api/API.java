
package ascelion.rest.bridge.tests.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

public interface API<T>
{

	String BASE = "rest";

	@POST
	@Valid
	@NotNull
	T create( @Valid @NotNull T t );

	@DELETE
	void delete();

	@GET
	@Valid
	@NotNull
	T get();

	@PUT
	@Valid
	@NotNull
	T update( @Valid @NotNull T t );
}
