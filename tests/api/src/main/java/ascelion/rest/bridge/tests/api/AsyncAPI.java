
package ascelion.rest.bridge.tests.api;

import java.util.concurrent.CompletionStage;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

public interface AsyncAPI<T>
{

	@POST
	CompletionStage<T> create( @Valid @NotNull T t );

	@DELETE
	CompletionStage<Void> delete();

	@GET
	CompletionStage<T> get();

	@PUT
	CompletionStage<T> update( @Valid @NotNull T t );

}
