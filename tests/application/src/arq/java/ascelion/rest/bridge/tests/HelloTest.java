
package ascelion.rest.bridge.tests;

import org.junit.Test;

import ascelion.rest.bridge.tests.api.Hello;
import ascelion.rest.bridge.tests.api.UserBean;

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

		final UserBean ui = this.client.authenticate( username, password );

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
