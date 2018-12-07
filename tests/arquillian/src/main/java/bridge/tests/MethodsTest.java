
package bridge.tests;

import ascelion.rest.bridge.web.Methods;

import bridge.tests.arquillian.IgnoreWithProvider;
import bridge.tests.providers.ResteasyBridgeProvider;
import bridge.tests.providers.ResteasyProxyProvider;
import org.junit.Test;

public class MethodsTest
extends AbstractTestCase<Methods>
{

	@Test
	public void delete()
	{
		this.client.delete();
	}

	@Test
	public void get()
	{
		this.client.get();
	}

	@Test
	@IgnoreWithProvider(
		reason = "unable to handle empty response for HEAD",
		value = {
			ResteasyBridgeProvider.class,
			ResteasyProxyProvider.class,
		} )
	public void head()
	{
		this.client.head();
	}

	@Test
	public void options()
	{
		this.client.options();
	}

	@Test
	public void post()
	{
		this.client.post();
	}

	@Test
	public void put()
	{
		this.client.put( "gigel" );
	}
}
