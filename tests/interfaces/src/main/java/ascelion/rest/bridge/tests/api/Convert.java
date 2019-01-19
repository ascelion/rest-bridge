
package ascelion.rest.bridge.tests.api;

import java.time.LocalDate;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path( "convert" )
@Produces( MediaType.TEXT_PLAIN )
public interface Convert
{

	@GET
	@Path( "formatDate" )
	String format( @QueryParam( "value" ) LocalDate date );

	@POST
	@Path( "formatDate" )
	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
	String formatPost( @FormParam( "value" ) LocalDate date );
}
