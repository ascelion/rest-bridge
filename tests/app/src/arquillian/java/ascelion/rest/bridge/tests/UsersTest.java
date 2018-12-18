
package ascelion.rest.bridge.tests;

import ascelion.rest.bridge.tests.api.UserInfo;
import ascelion.rest.bridge.tests.api.Users;
import ascelion.rest.bridge.tests.arquillian.IgnoreWithProvider;
import ascelion.rest.bridge.tests.providers.JerseyProxyProvider;
import ascelion.rest.bridge.tests.providers.ResteasyProxyProvider;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class UsersTest
extends AbstractTestCase<Users>
{

	@Test
	public void asInterface()
	{
		final UserInfo ui = this.client.asInterface( "username" );

		assertThat( ui, not( nullValue() ) );
		assertThat( ui.getFirstName(), not( nullValue() ) );
	}

	@Test
	@IgnoreWithProvider( reason = "not supported", value = {
		JerseyProxyProvider.class,
		ResteasyProxyProvider.class,
	} )
	public void asClass()
	{
		final UserInfo ui = this.client.asClass( "username" );

		assertThat( ui, not( nullValue() ) );
	}
}
