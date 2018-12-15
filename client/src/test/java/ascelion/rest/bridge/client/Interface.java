
package ascelion.rest.bridge.client;

import java.time.LocalDate;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path( "interface" )
public interface Interface
{

	@GET
	@Path( "format" )
	String format( @QueryParam( "value" ) @DefaultValue( "1643-01-04" ) LocalDate date );

	@GET
	@Path( "parse" )
	LocalDate parse( @QueryParam( "value" ) @DefaultValue( "1643-01-04" ) String date );

	@GET
	String get();
}
