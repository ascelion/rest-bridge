
package ascelion.rest.bridge.tests.app;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import ascelion.rest.bridge.tests.api.Hello;
import ascelion.rest.bridge.tests.api.UserBean;
import ascelion.rest.bridge.tests.api.UserInfo;
import ascelion.rest.bridge.tests.api.Users;

@ApplicationScoped
public class UsersImpl implements Users
{

	@Inject
	private Hello hello;

	@Override
	public UserInfo asInterface( String username )
	{
		return this.hello.authenticate( username, "password" );
	}

	@Override
	public UserBean asClass( String username )
	{
		return this.hello.authenticate( username, "password" );
	}
}
