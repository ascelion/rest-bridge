
package ascelion.rest.bridge.tests.api;

import java.beans.ConstructorProperties;

import lombok.AllArgsConstructor;

@AllArgsConstructor( onConstructor_ = {
	@ConstructorProperties( { "firstName", "lastName" } )
} )
public class UserBean implements UserInfo
{

	private String firstName;
	private String lastName;

	public UserBean()
	{
	}

	@Override
	public String getFirstName()
	{
		return this.firstName;
	}

	@Override
	public String getLastName()
	{
		return this.lastName;
	}
}
