
package ascelion.rest.bridge.web;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path( Validated.PATH )
@Produces( MediaType.APPLICATION_JSON )
@Consumes( MediaType.APPLICATION_JSON )
public interface Validated
{

	String PATH = "validated";

	@POST
	@Path( "bean" )
	BeanValidData bean( BeanValidData value );

	@POST
	@Path( "beanNotNull" )
	BeanValidData beanNotNull( @NotNull BeanValidData value );

	@POST
	@Path( "beanValid" )
	BeanValidData beanValid( @Valid BeanValidData value );

	@POST
	@Path( "beanValidNotNull" )
	BeanValidData beanValidNotNull( @Valid @NotNull BeanValidData value );

	@POST
	@Path( "notNullFormParam" )
	@Produces( MediaType.APPLICATION_FORM_URLENCODED )
	@Consumes( MediaType.APPLICATION_FORM_URLENCODED )
	String notNullFormParam( @FormParam( "value" ) @NotNull String value );

	@GET
	@Path( "notNullHeaderParam" )
	String notNullHeaderParam( @HeaderParam( "value" ) @NotNull String value );

	@GET
	@Path( "notNullQueryParam" )
	String notNullQueryParam( @QueryParam( "value" ) @NotNull String value );

	@POST
	@Path( "notNullWithBean" )
	BeanValidData notNullWithBean( @FormParam( "value" ) @NotNull String value, @Valid @NotNull BeanValidData bean );
}