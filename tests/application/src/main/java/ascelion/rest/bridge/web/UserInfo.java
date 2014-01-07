
package ascelion.rest.bridge.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonDeserialize( as = UserInfoImpl.class )
@JsonSerialize( as = UserInfoImpl.class )
public interface UserInfo
{

	@GET
	@Path( "password" )
	String password();

	@GET
	@Path( "username" )
	String username();
}
