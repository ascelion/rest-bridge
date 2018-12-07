
package ascelion.rest.bridge.web;

import java.beans.ConstructorProperties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor( onConstructor_ = {
	@ConstructorProperties( { "firstName", "lastName" } )
} )
public class UserInfo
{

	private final String firstName;
	private final String lastName;

	@GET
	@Path( "firstName" )
	public String getFirstName()
	{
		return this.firstName;
	}

	@GET
	@Path( "lastName" )
	public String getLastName()
	{
		return this.lastName;
	}
}
