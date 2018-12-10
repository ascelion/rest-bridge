
package ascelion.rest.bridge.tests.app;

import ascelion.rest.bridge.tests.api.Hello;
import ascelion.rest.bridge.tests.api.UserInfo;

public class HelloImpl
implements Hello
{

	@Override
	public UserInfo authenticate( String username, String password )
	{
		return new UserInfo( username + "(first)", username + ( "last" ) );
	}

	@Override
	public String sayByParam( String username )
	{
		return say( username );
	}

	@Override
	public String sayByPath( String username )
	{
		return say( username );
	}

	private String say( String username )
	{
		return String.format( "Hello, %s!", username );
	}
}
