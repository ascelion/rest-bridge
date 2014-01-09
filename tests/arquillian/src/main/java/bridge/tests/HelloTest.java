
package bridge.tests;

import org.junit.Test;

import ascelion.rest.bridge.web.Hello;
import ascelion.rest.bridge.web.UserInfo;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class HelloTest
extends AbstractTestCase<Hello>
{

	@Test
	public void authenticate()
	{
		final String username = "ghiţă";
		final String password = "surdu";

		final UserInfo ui = this.client.authenticate( username, password );

		assertThat( ui, is( notNullValue() ) );
	}

	@Test
	public void sayByParam()
	{
		final String username = "ghiţă";

		assertThat( this.client.sayByParam( username ), containsString( username ) );
	}

	@Test
	public void sayByPath()
	{
		final String username = "ghiţă";

		assertThat( this.client.sayByPath( username ), containsString( username ) );
	}
}
