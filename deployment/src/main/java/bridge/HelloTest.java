
package bridge;

import org.junit.Test;

import ascelion.rest.bridge.web.Hello;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;

public class HelloTest
extends AbstractTestCase
{

	@Test
	public void sayByParam()
	{
		final Hello itf = this.client.getInterface( Hello.class, this.target );
		final String username = "ghiţă";

		assertThat( itf.sayByParam( username ), containsString( username ) );
	}

	@Test
	public void sayByPath()
	{
		final Hello itf = this.client.getInterface( Hello.class, this.target );
		final String username = "ghiţă";

		assertThat( itf.sayByPath( username ), containsString( username ) );
	}

}
