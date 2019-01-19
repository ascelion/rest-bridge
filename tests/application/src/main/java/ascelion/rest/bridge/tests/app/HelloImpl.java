
package ascelion.rest.bridge.tests.app;

import javax.enterprise.context.ApplicationScoped;

import ascelion.rest.bridge.tests.api.Hello;
import ascelion.rest.bridge.tests.api.UserBean;

@ApplicationScoped
public class HelloImpl
implements Hello
{

	@Override
	public UserBean authenticate( String username, String password )
	{
		return new UserBean( username + "(first)", username + ( "last" ) );
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
